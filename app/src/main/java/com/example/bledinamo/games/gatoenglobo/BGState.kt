package com.example.bledinamo.games.gatoenglobo

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.*


class BGState {

    private var canvasSize : Size = Size(100f,100f)
    private var xPos : Float = 0f
    private var speed: Float = 5f
    private var imHeight: Float = 100f
    private var imWidth: Float = 100f


    fun updatePos() {
        xPos -= speed
        if(xPos < -imWidth){
            xPos = 0f
        }
    }
    fun drawBG(drawScope: DrawScope,
             myImageBitmap: ImageBitmap
    ) {
        drawScope.draw(myImageBitmap)
    }

    private fun DrawScope.draw(image : ImageBitmap){

        drawImage(
            image,
            dstSize = IntSize(imWidth.toInt(),imHeight.toInt()),
            dstOffset = IntOffset(xPos.toInt(),0)
        )
        drawImage(
            image,
            dstSize = IntSize(imWidth.toInt(),imHeight.toInt()),
            dstOffset = IntOffset(xPos.toInt()+imWidth.toInt(),0)
        )

    }
    fun setCanvasSize(size : Size, image: ImageBitmap){
        canvasSize= size
        val bgScaleFactor:Float= canvasSize.height/image.height
        imHeight = canvasSize.height
        imWidth = image.width.toFloat()*bgScaleFactor

    }
}