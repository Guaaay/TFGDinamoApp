package com.example.bledinamo.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.data.GripReceiveManager
import com.example.bledinamo.data.MyBuffer
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.datastore.PreferencesRepo
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import com.example.bledinamo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

import javax.inject.Inject

@HiltViewModel
class GripViewModel @Inject constructor(
    private val gripReceiveManager: GripReceiveManager,
    private val prefRepo: PreferencesRepo,
    private val database: AppDatabase,
): ViewModel() {

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var load by mutableStateOf(0f)
        private set

    var buffer by mutableStateOf<MyBuffer<Float>>(MyBuffer(60))
        private set

    var maxLoad by mutableStateOf(0f)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
        private set

    var currentProfile by mutableStateOf<ProfileWithMeasurements?>(null)
        private set


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMeasurement(){
        val measurement = MaxGripMeasurement(
            profileCreatorName = currentProfile!!.profile.name,
            measurement = maxLoad,
            dateTaken = LocalDateTime.now())

        viewModelScope.launch{
            withContext(Dispatchers.IO) {
                val profileDao = database.profileDao()
                profileDao.insertMeasurement(measurement)
            }
        }
    }
    fun getCurrentProfile(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                currentProfile = prefRepo.getCurrentProfile()
            }
        }

    }
    private fun subscribeToChanges(){
        viewModelScope.launch {
            gripReceiveManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        load = result.data.load
                        buffer.add(load)
                        if(load > maxLoad)
                            maxLoad = load
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }

            }
        }
    }

    fun disconnect(){
        gripReceiveManager.disconnect()
    }

    fun reconnect(){
        gripReceiveManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        gripReceiveManager.startReceiving()
    }


    fun resetValues(){
        buffer.removeAll(buffer)
        maxLoad = 0f
    }

    override fun onCleared() {
        super.onCleared()
        gripReceiveManager.closeConnection()
    }

}