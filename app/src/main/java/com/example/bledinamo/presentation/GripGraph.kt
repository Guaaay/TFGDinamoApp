package com.example.bledinamo.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.data.MyBuffer
import com.example.bledinamo.presentation.permissions.PermissionUtils
import com.example.bledinamo.presentation.permissions.SystemBroadcastReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.ceil

@OptIn(ExperimentalPermissionsApi::class, ExperimentalTextApi::class)
//@PreviewParameter
//@Preview(showBackground = true)
@Composable
fun GripGraph(
    onBluetoothStateChanged: () -> Unit,
    viewModel: GripViewModel = hiltViewModel()
) {
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }
    val permissionState =
        rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)

    val lifecycleOwner = LocalLifecycleOwner.current

    val bleConnectionState = viewModel.connectionState

    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissionState.launchMultiplePermissionRequest()
                    Log.d("GripScreen", "Todos los permisos pedidos")
                    if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
                        Log.d("GripScreen", "Todos los permisos otorgados")
                        viewModel.reconnect()
                    }
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    if (bleConnectionState == ConnectionState.Connected) {
                        viewModel.disconnect()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            if (bleConnectionState == ConnectionState.Uninitialized) {
                viewModel.initializeConnection()
            }
        }
    }
    if (bleConnectionState == ConnectionState.Connected) {
        //Graph
        //Log.d("GripGraph","Updated value")
        var currLoad = viewModel.load
        var buffer = viewModel.buffer
        //Log.d("GripGraph",viewModel.buffer.toString())


        if (buffer.size < 2){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Aprieta el dinamómetro",style = MaterialTheme.typography.h6)
            }
            return
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally){
                Canvas(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(
                            BorderStroke(
                                1.dp, Color.Black
                            ),
                            RoundedCornerShape(10.dp)
                        ),
                )
                {
                    val canvasWidth = size.width
                    val widthOffset = 50f
                    val canvasHeight = size.height - 30
                    val xDiv = (canvasWidth-widthOffset) / buffer.size
                    val maxVal = buffer.max()
                    //El intervalo de lineas horizontales:
                    val gaps = 20
                    val paint = Paint().asFrameworkPaint().apply {
                        textSize = 20f
                    }
                    val numIntervals = ceil(maxVal / gaps).toInt()
                    var j = numIntervals-1
                    for (i in 1..numIntervals) {
                        val yPos = ((i * canvasHeight / numIntervals)+7)
                        drawIntoCanvas {
                            it.nativeCanvas.drawText((j*gaps).toString(),  10f, yPos, paint)
                        }
                        drawLine(
                            start = Offset(
                                x = widthOffset,
                                y = i * canvasHeight / numIntervals
                            ),
                            end = Offset(
                                x = canvasWidth,
                                y = i * canvasHeight / numIntervals
                            ),
                            color = Color.Gray,
                            strokeWidth = Stroke.HairlineWidth,
                        )
                        j -= 1
                    }
                    buffer.forEachIndexed { index, load ->
                        val xPos1 = xDiv * index
                        val xPos2 = xDiv * (index + 1)
                        if (index % 5 == 0 || index == buffer.size - 1) {
                            drawLine(
                                start = Offset(
                                    x = xPos1+widthOffset,
                                    y = 0f
                                ),
                                end = Offset(
                                    x = xPos1+widthOffset,
                                    y = size.height
                                ),
                                color = Color.Gray,
                                strokeWidth = Stroke.HairlineWidth,
                            )
                        }

                        if (index < buffer.size - 1) {
                            drawLine(
                                start = Offset(
                                    x = xPos1+widthOffset,
                                    y = calcY(maxVal, canvasHeight, load)
                                ),
                                end = Offset(
                                    x = xPos2+widthOffset,
                                    y = calcY(maxVal, canvasHeight, buffer[index + 1])
                                ),
                                color = Color.Black,
                                strokeWidth = Stroke.DefaultMiter,
                            )

                        }

                    }
                }
                Text(
                    text = "Máximo: ${"%.2f".format(viewModel.maxLoad)} Kg",
                    style = MaterialTheme.typography.h6
                )
                Button(
                    onClick = {
                        //TODO: En este caso terminará la medición
                        viewModel.resetValues()
                    }
                ) {
                    Text(text = "Reset")
                }
            }

        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .border(
                        BorderStroke(
                            5.dp, Color.Blue
                        ),
                        RoundedCornerShape(10.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bleConnectionState == ConnectionState.CurrentlyInitializing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        if (viewModel.initializingMessage != null) {
                            Text(
                                text = viewModel.initializingMessage!!
                            )
                        }
                    }
                } else if (!permissionState.allPermissionsGranted) {
                    Text(
                        text = "Ve a los ajustes de la app y otorga los permisos que faltan.",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = {
                            permissionState.launchMultiplePermissionRequest()
                            Log.d("GripScreen", "Launched permissions")
                            if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {

                                viewModel.reconnect()
                            }
                        }
                    ) {
                        Text(text = "Pedir permisos")
                    }
                } else if (viewModel.errorMessage != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.errorMessage!!
                        )
                        Button(
                            onClick = {
                                if (permissionState.allPermissionsGranted) {
                                    viewModel.initializeConnection()
                                }
                            }
                        ) {
                            Text(text = "Volver a intentar")
                        }
                    }
                } else if (bleConnectionState == ConnectionState.Disconnected) {
                    Button(onClick = {
                        viewModel.initializeConnection()
                    }) {
                        Text("Initialize again")
                    }
                }
            }

        }
    }

}


private fun calcY(maxVal: Float, cheight: Float, currVal: Float): Float {

    val prop = currVal / maxVal
    return cheight - prop * cheight + 30
}