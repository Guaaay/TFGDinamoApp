package com.example.bledinamo.games.gatoenglobo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset


class GatoState() {
    var refLoad = 0f
    private var loadMultiplier = 10f
    private var yPos = 0f
    private var canvasSize = Size(0f,0f)
    private var loadPercent = 0.8f


    fun draw(drawScope: DrawScope,
             tick: Long,
             myImageBitmap: ImageBitmap
    ): Rect {
        return drawScope.draw(myImageBitmap,tick)
    }

    private fun DrawScope.draw(image : ImageBitmap, tick: Long) : Rect {
        val imHeight = canvasSize.height.toInt()/17
        val scaleFactor = (imHeight.toFloat()/image.height.toFloat())
        val imWidth = ((image.width/2)*scaleFactor).toInt()
        //Log.d("GLOBO:", "ScaleFac: $scaleFactor Width: ${imWidth} Height; $imHeight")
        val imOffset = IntOffset(canvasSize.width.toInt()/4,yPos.toInt()-300)

        val check = tick % 8
        val frame = if (check < 4) 0 else 1

        drawImage(
            image,
            srcOffset = IntOffset((image.width/2 * frame), 0),
            srcSize = IntSize(image.width/2, image.height),
            dstSize = IntSize(imWidth,imHeight),
            dstOffset = imOffset
        )
        return Rect(imOffset.toOffset(),Size(imWidth.toFloat(),imHeight.toFloat()))
    }

    fun changeLoadPercent(newPercent:Float){
        loadPercent = newPercent
    }

    fun setCanvasHeight(size : Size){
        canvasSize = size
        loadMultiplier = canvasSize.height/(refLoad*loadPercent)
    }

    fun updatePos(load : Float){
        yPos = canvasSize.height - load*loadMultiplier

        if(yPos < 300f){
            yPos = 300f
        }
    }
}