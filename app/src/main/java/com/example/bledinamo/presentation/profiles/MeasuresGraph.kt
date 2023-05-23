package com.example.bledinamo.presentation.profiles

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bledinamo.R
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeasuresGraph(measureList: List<MaxGripMeasurement>) {
    //Graph
    //Log.d("GripGraph",viewModel.buffer.toString())

    if (measureList.size < 2) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No hay suficientes medidas para mostrar el gráfico", style = MaterialTheme.typography.subtitle1)
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val onBackground = MaterialTheme.colors.onBackground
        val onBackgroundGray = onBackground.copy(0.4f)
        var textColor = android.graphics.Color.parseColor("#FFFFFF")
        if(!isSystemInDarkTheme()){
            textColor = android.graphics.Color.parseColor("#000000")
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Canvas(
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .border(
                        BorderStroke(
                            1.dp, MaterialTheme.colors.onBackground
                        ),
                        RoundedCornerShape(10.dp)
                    ),
            )
            {
                val canvasWidth = size.width
                val widthOffset = 50f
                val canvasHeight = size.height - 50
                val xDiv = (canvasWidth - widthOffset) / measureList.size
                var maxVal = 0f
                measureList.forEach(){
                    maxVal = if (it.measurement > maxVal) it.measurement else maxVal
                }
                //El intervalo de lineas horizontales:
                val gaps = 20

                val paint = Paint().asFrameworkPaint().apply {
                    textSize = 20f
                    color = textColor
                }
                val numIntervals = ceil(maxVal / gaps).toInt()
                var j = numIntervals - 1
                //Pintamos los números del eje Y y las líneas horizontales
                for (i in 1..numIntervals) {
                    val yPos = ((i * canvasHeight / numIntervals) + 7)
                    drawIntoCanvas {
                        it.nativeCanvas.drawText((j * gaps).toString(), 10f, yPos, paint)
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
                measureList.forEachIndexed { index, measurement ->
                    val load = measurement.measurement
                    val xPos1 = xDiv * index
                    val xPos2 = xDiv * (index + 1)
                    //Pintamos la fecha de cada medida
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(formatDate(measureList[index].dateTaken) , xPos1+10, canvasHeight+30, paint)
                    }
                    //Líneas verticales
                    drawLine(
                        start = Offset(
                            x = xPos1 + widthOffset,
                            y = 0f
                        ),
                        end = Offset(
                            x = xPos1 + widthOffset,
                            y = canvasHeight
                        ),
                        color = onBackgroundGray,
                        strokeWidth = Stroke.HairlineWidth,
                    )

                    if (index < measureList.size - 1) {

                        drawLine(
                            start = Offset(
                                x = xPos1 + widthOffset,
                                y = calcY(maxVal, canvasHeight, load)
                            ),
                            end = Offset(
                                x = xPos2 + widthOffset,
                                y = calcY(maxVal, canvasHeight, measureList[index + 1].measurement)
                            ),
                            color = onBackground,
                            strokeWidth = Stroke.DefaultMiter,
                        )

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

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(date : LocalDateTime) : String{
    return "${date.dayOfMonth}/${date.monthValue}/${date.year.toString().subSequence(2,4)}"
}