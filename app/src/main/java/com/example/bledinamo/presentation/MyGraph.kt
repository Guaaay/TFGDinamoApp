package com.example.bledinamo.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bledinamo.data.MyBuffer

@Composable
fun MyGraph(
    onBluetoothStateChanged:() -> Unit,
    buffer: MyBuffer<Float>,
    modifier : Modifier,
) {
    Canvas(modifier = modifier) {
        // Total number of transactions.
        val totalRecords = buffer.size

        // Maximum distance between dots (transactions)
        val lineDistance = size.width / (totalRecords + 1)

        // Canvas height
        val cHeight = size.height

        // Add some kind of a "Padding" for the initial point where the line starts.
        var currentLineDistance = 0F + lineDistance

        buffer.forEachIndexed { index, _ ->
            if (totalRecords >= index + 2) {
                drawLine(
                    start = Offset(
                        x = currentLineDistance,
                        y = calculateYCoordinate(
                            higherTransactionRateValue = buffer.max(),
                            currentTransactionRate = 10f,
                            canvasHeight = cHeight
                        )
                    ),
                    end = Offset(
                        x = currentLineDistance + lineDistance,
                        y = calculateYCoordinate(
                            higherTransactionRateValue = buffer.max(),
                            currentTransactionRate = 10f,
                            canvasHeight = cHeight
                        )
                    ),
                    color = Color(40, 193, 218),
                    strokeWidth = Stroke.DefaultMiter
                )
            }
            currentLineDistance += lineDistance
        }
    }

}
private fun calculateYCoordinate(
    higherTransactionRateValue: Float,
    currentTransactionRate: Float,
    canvasHeight: Float
): Float {
    val maxAndCurrentValueDifference = (higherTransactionRateValue - currentTransactionRate)
    val relativePercentageOfScreen = (canvasHeight / higherTransactionRateValue)
    return maxAndCurrentValueDifference * relativePercentageOfScreen
}