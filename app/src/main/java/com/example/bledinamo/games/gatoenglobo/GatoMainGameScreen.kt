package com.example.bledinamo.games.gatoenglobo

import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.bledinamo.R
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.presentation.permissions.PermissionUtils
import com.example.bledinamo.presentation.permissions.SystemBroadcastReceiver
import com.example.bledinamo.ui.theme.fonts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.ceil


@Composable
fun GatoGameLoop(modifier: Modifier = Modifier,viewModel: GatoViewModel, navController: NavController){
    val gameState by viewModel.gameState.collectAsState()
    val gatoBitmap = ImageBitmap.imageResource(id = R.drawable.flycat_spsheet)
    val pawsBitmap = ImageBitmap.imageResource(id = R.drawable.allpaws)
    val bgBitmap = ImageBitmap.imageResource(id = R.drawable.bgressa_purp)
    val settingsBitmap = ImageBitmap.imageResource(id = R.drawable.settings)

    if(gameState.stage == GameState.GameStage.STOPPED){
        Canvas(modifier = modifier){
            gameState.bgState.drawBG(this,bgBitmap)
            var imWidth = (gatoBitmap.width/2)

            //Gato volando
            val check = gameState.tick % 8
            val frame = if (check < 4) 0 else 1

            drawImage(
                gatoBitmap,
                srcOffset = IntOffset((gatoBitmap.width/2)*frame, 0),
                srcSize = IntSize(gatoBitmap.width/2,gatoBitmap.height),
                dstSize = IntSize(imWidth,(gatoBitmap.height.toFloat()/1.1f).toInt()),
                dstOffset = IntOffset((this.size.width.toInt()/2)-imWidth/2,this.size.width.toInt()/4)
            )
        }
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
            Text(color = Color.hsv(356f,0.82f,0.71f),text = "GAME OVER", fontFamily = fonts,fontSize = 50.sp)
            Spacer(modifier = Modifier.size(20.dp))

            Text(color = Color.hsv(356f,0.82f,0.71f),text = "Puntos: " + gameState.score, fontFamily = fonts,fontSize = 34.sp)
            Spacer(modifier = Modifier.size(20.dp))
            viewModel.checkHighScore(gameState.score)
            if(viewModel.highScore){
                val infiniteTransition = rememberInfiniteTransition()
                val offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
                )

                Text(color = Color.hsv(offset,0.82f,0.71f),text = "¡NUEVO RECORD!", fontFamily = fonts,fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.size(30.dp))
            TextButton (onClick = {
                gameState.resetGame()
                viewModel.startGameLoop()

            }){
                Text(color = Color.DarkGray,text = "Volver a jugar", fontFamily = fonts,fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.size(10.dp))
            TextButton (onClick = {
                navController.popBackStack()
            }){
                Text(color = Color.DarkGray,text = "Salir", fontFamily = fonts,fontSize = 30.sp)
            }


        }
    }
    else if(gameState.stage == GameState.GameStage.RUNNING){
        Canvas(modifier = modifier){
            gameState.pawState.setCanvasSize(this.size,pawsBitmap)
            gameState.bgState.setCanvasSize(this.size,bgBitmap)
            val gato = gameState.gatoState
            gato.setCanvasHeight(this.size)

            gameState.bgState.drawBG(this,bgBitmap)
            var globoRect = gato.draw(this, gameState.tick,gatoBitmap)
            var pawRects = gameState.pawState.drawPaw(this,pawsBitmap)

            if(checkCollisions(pawRects,globoRect)){
                gameState.stage = GameState.GameStage.STOPPED
            }

        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally){
            Spacer(modifier = Modifier.size(40.dp))
            Text(color = Color.Black,text = gameState.score.toString(), fontFamily = fonts, fontSize = 45.sp)
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start){

            IconButton(onClick = {gameState.pause()}) {
                Icon(
                    settingsBitmap,
                    contentDescription = "Ajustes del juego",
                    modifier = Modifier.size(45.dp),
                    tint = Color.Black
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.End){
            Spacer(modifier = Modifier.size(10.dp))
            Text(color = Color.Black,text = "${"%.2f".format(gameState.load)} Kg", fontFamily = fonts,fontSize = 30.sp)
        }

    }
    else if(gameState.stage == GameState.GameStage.PAUSED){
        Canvas(modifier = modifier){
            gameState.bgState.drawBG(this,bgBitmap)
            gameState.pawState.drawPaw(this,pawsBitmap)
            /*var imWidth = (gatoBitmap.width/2)

            //Gato volando
            val check = gameState.tick % 8
            val frame = if (check < 4) 0 else 1

            drawImage(
                gatoBitmap,
                srcOffset = IntOffset((gatoBitmap.width/2)*frame, 0),
                srcSize = IntSize(gatoBitmap.width/2,gatoBitmap.height),
                dstSize = IntSize(imWidth,(gatoBitmap.height.toFloat()/1.1f).toInt()),
                dstOffset = IntOffset((this.size.width.toInt()/2)-imWidth/2,this.size.width.toInt()/4)
            )*/
        }
        Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
            Box(modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.hsv(288f, 0.16f, 0.3f, 0.5f)),)
            {

                Row(modifier=Modifier.fillMaxSize(),horizontalArrangement = Arrangement.End){
                    IconButton(modifier = Modifier.size(50.dp),onClick = {
                        gameState.resetGame()
                        viewModel.startGameLoop()
                    }) {
                        Icon(Icons.Default.Close,null,tint = Color.Black)
                    }

                }
                Column(modifier = Modifier.fillMaxSize(),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(color = Color.Black,text = "Ajustes", fontFamily = fonts,fontSize = 50.sp)
                    Spacer(modifier = Modifier.size(100.dp))
                    Text(color = Color.Black,text = "Velocidad del juego: ${(viewModel.speedSlider*100).toInt()}%", fontFamily = fonts,fontSize = 15.sp)
                    Slider(modifier = Modifier.padding(horizontal = 20.dp),value = viewModel.speedSlider, onValueChange = {
                        viewModel.speedSlider = it
                        gameState.pawState.changeSpeed(it)
                    })
                    Spacer(modifier = Modifier.size(20.dp))
                    Text(color = Color.Black,text = "Ancho de los agujeros: ${(viewModel.holeWidthSlider*100).toInt()}%", fontFamily = fonts,fontSize = 15.sp)
                    Slider(modifier = Modifier.padding(horizontal = 20.dp),value = viewModel.holeWidthSlider, onValueChange = {
                        viewModel.holeWidthSlider = it
                        gameState.pawState.changeHoleSize(it)
                    })
                    Spacer(modifier = Modifier.size(20.dp))
                    Text(color = Color.Black,text = "Fuerza necesaria: ${(viewModel.loadPercentSlider*100).toInt()}%", fontFamily = fonts,fontSize = 15.sp)
                    Slider(modifier=Modifier.padding(horizontal = 20.dp),value = viewModel.loadPercentSlider, onValueChange = {
                        viewModel.loadPercentSlider = it
                        gameState.gatoState.changeLoadPercent(it)
                    })
                }
            }
        }
    }
}

fun checkCollisions(buildRects: MutableList<Rect>, globoRect: Rect): Boolean {
    buildRects.forEach{
        if(it.overlaps(globoRect))
            return true
    }
    return false
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GatoMainGameScreen(
    navController : NavController,
    onBluetoothStateChanged: () -> Unit,
    viewModel: GatoViewModel = hiltViewModel()
)
{
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
                    //Obtenemos el perfil actual
                    viewModel.getCurrentProfile()
                    permissionState.launchMultiplePermissionRequest()
                    if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
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
        //Gestionamos el juego principal
        if(!viewModel.hasCalibrated){
            Graph(viewModel,navController)
        }
        else{
            //LLamar el bucle del juego
            viewModel.startGameLoop()
            GatoGameLoop(modifier = Modifier.fillMaxSize(),viewModel,navController)
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
                            5.dp, MaterialTheme.colors.secondary
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable fun Graph(viewModel:GatoViewModel, navController: NavController){
    var currLoad = viewModel.load
    var buffer = viewModel.buffer
    if(viewModel.currentProfile == null) {

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.padding(20.dp))
            CircularProgressIndicator()
            Text(
                text = "Cargando perfil..."
            )

        }
    }
    else{
        Spacer(modifier = Modifier.padding(5.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .wrapContentSize(),
            elevation = 4.dp

        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
                Text(
                    text = "Midiendo para perfil",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewModel.currentProfile!!.profile.name,
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onSurface,
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
            }
        }
    }
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
        val onBackground = MaterialTheme.colors.onBackground
        val onBackgroundGray = onBackground.copy(0.4f)

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
                val gaps = 5
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
                        color = onBackgroundGray,
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
                            color = onBackgroundGray,
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
                            color = onBackground,
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
                    viewModel.saveMeasurement()
                    viewModel.setReference(viewModel.maxLoad)
                    viewModel.setCalibrated()
                }
            ) {
                Text(text = "Comenzar el juego")
            }
            Spacer(modifier = Modifier.padding(40.dp))
            Text(
                text = "¡Aprieta todo lo fuerte que puedas!",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center
            )
        }

    }
}
private fun calcY(maxVal: Float, cheight: Float, currVal: Float): Float {

    val prop = currVal / maxVal
    return cheight - prop * cheight + 30
}