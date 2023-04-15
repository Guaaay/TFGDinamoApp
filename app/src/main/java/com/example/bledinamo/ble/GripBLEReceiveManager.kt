package com.example.bledinamo.ble

import CCCD_DESCRIPTOR_UUID
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.KeyEvent.DispatcherState
import androidx.annotation.RequiresApi
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.data.GripReceiveManager
import com.example.bledinamo.data.GripResult
import com.example.bledinamo.util.Resource
import isIndicatable
import isNotifiable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import printGattTable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
class GripBLEReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context,
): GripReceiveManager {

    private val DEVICE_NAME = "dinamo"
    private val GRIP_SERVICE_UUID="0000a200-0000-1000-8000-00805f9b34fb"
    private val GRIP_CHARACTERISTIC_UUID="0000a201-0000-1000-8000-00805f9b34fb"

    override val data: MutableSharedFlow<Resource<GripResult>> = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private var coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType : Int, result: ScanResult){
            if(result.device.name == DEVICE_NAME){
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Conectando al dispositivo"))
                }
                if(isScanning){
                    result.device.connectGatt(context,false,gattCallback)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private val MAXIMUM_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Descubriendo servicios"))
                    }
                    gatt.discoverServices()
                    this@GripBLEReceiveManager.gatt = gatt
                }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = GripResult(load = 0f, ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            }else{
                gatt.close()
                currentConnectionAttempt += 1
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Intentando conectar $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"))
                }
                if(currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPTS){
                    startReceiving()
                }else{
                    coroutineScope.launch {
                        data.emit(Resource.Error("No se pudo conectar al dispositivo BLE"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Ajustando espacio MTU..."))
                }
                requestMtu(517)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristic(GRIP_SERVICE_UUID,GRIP_CHARACTERISTIC_UUID)
            if(characteristic == null){
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "No se pudo encontrar el servicio del dinamómetro"))
                }
                return
            }
            enableNotification(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            when(characteristic.uuid){
                UUID.fromString(GRIP_CHARACTERISTIC_UUID) -> {
                    val grip : GripResult= GripResult(
                        load = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).float,
                        connectionState = ConnectionState.Connected)
                    Log.d("GripBLEReceiveManager",grip.load.toString())
                    coroutineScope.launch{
                        data.emit(
                            Resource.Success(data = grip)
                        )
                    }
                }
                else -> Unit
            }
        }
    }
    private fun enableNotification(characteristic: BluetoothGattCharacteristic){
        val ccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return

        }

        characteristic.getDescriptor(ccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                Log.d("BLEReceiveManager","Notificación de características falló")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }


    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload : ByteArray){
        gatt?.let{ gatt ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor,payload)
            }

            else @Suppress("DEPRECATION"){
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            }
        }?:  error("Not connected to a BLE device")
    }
    private fun findCharacteristic(serviceUUID: String, characteristicsUUID: String) : BluetoothGattCharacteristic?{
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }


    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Escaneando dispositivos BLE"))
        }
        isScanning = true
        bleScanner.startScan(null,scanSettings,scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharacteristic(GRIP_SERVICE_UUID, GRIP_CHARACTERISTIC_UUID)
        if(characteristic != null){
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
    }
    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUUID = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUUID)?.let {cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic,false) == false){
                Log.d("GripReceiveManager","No se pudo desconectar la característica")
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

}