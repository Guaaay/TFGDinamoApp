package com.example.bledinamo.games.gatoenglobo
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.*
import java.lang.Math.abs


class PawState {

    private var canvasSize : Size = Size(100f,100f)
    private var buildingSize : Size = Size(256f,canvasSize.height)
    private val buildings: MutableList<Paw> = emptyList<Paw>().toMutableList()
    private var initDone = false
    private var maxSpeed : Int = 40
    private var currentSpeed : Int = 10
    private var maxHoleSize : Float = canvasSize.height
    private var currentHoleSize : Float = canvasSize.height/4

    fun init(){

        if(canvasSize.width != 100f && !initDone ){
            buildings.clear()
            buildings.add(Paw(0,canvasSize,canvasSize.width,0f,0f,currentSpeed,canvasSize.height/4,buildingSize))
            buildings.forEach { it.reset(canvasSize.width) }
            maxHoleSize = canvasSize.height/1.3f
            initDone = true
        }
    }

    fun reset(){
        buildings.clear()
        buildings.add(Paw(0,canvasSize,canvasSize.width,0f,0f,currentSpeed,currentHoleSize,buildingSize))
        buildings.forEach { it.reset(canvasSize.width) }
        initDone = true
    }
    fun changeSpeed(percent: Float) {
        currentSpeed = (maxSpeed.toFloat() * percent).toInt()+1
        buildings.forEach { it.moveSpeed = (maxSpeed.toFloat() * percent).toInt()+1 }
    }

    fun changeHoleSize(percent: Float) {
        currentHoleSize = maxHoleSize * (percent+0.1f)
        buildings.forEach {
            it.holeSize = (maxHoleSize * (percent+0.1f))
            it.changeHoleSize()
        }
    }

    fun updatePos() : Boolean{
        var score = false
        buildings.forEach {
            if(buildings.size == 1 && it.isAtFrac(3)){
                buildings.add(Paw(0,canvasSize,canvasSize.width,0f,0f,it.moveSpeed,it.holeSize,buildingSize).reset(buildingSize.width))
            }
            else if(it.isAtFrac(3)){
                buildings.forEach{other ->
                    if(other != it){
                        other.reset(it.buildingSize.width)
                    }
                }
            }
            if(it.updatePos()){
                score = true

            }
        }
        return score
    }
    fun drawPaw(drawScope: DrawScope,
             myImageBitmap: ImageBitmap
    ): MutableList<Rect> {
        return drawScope.draw(myImageBitmap)
    }

    private fun DrawScope.draw(image : ImageBitmap) : MutableList<Rect> {

        val rects: MutableList<Rect> = emptyList<Rect>().toMutableList()
        buildings.forEach {
            val topleftUp =  IntOffset(it.xPos.toInt(),it.topYPos.toInt())
            val topleftDown = IntOffset(it.xPos.toInt(),it.botYPos.toInt())
            drawImage(
                image = image,
                srcOffset = IntOffset((image.width/3 * it.currentCat), 0),
                srcSize = IntSize(image.width/6, image.height),
                dstOffset = topleftDown,
                dstSize = IntSize(buildingSize.width.toInt(), buildingSize.height.toInt())
            )
            drawImage(
                image = image,
                srcOffset = IntOffset(((image.width/3 * it.currentCat) + image.width/6), 0),
                srcSize = IntSize(image.width/6, image.height),
                dstOffset = topleftUp,
                dstSize = IntSize(buildingSize.width.toInt(), buildingSize.height.toInt())
            )

            /*
            drawRect(
                color = Color.Blue,
                size = buildingSize,
                topLeft = topleftUp,
            )
            drawRect(
                color = Color.Blue,
                size = buildingSize,
                topLeft = topleftDown,
            )*/
            rects.add(Rect(topleftUp.toOffset(), buildingSize))
            rects.add(Rect(topleftDown.toOffset(), buildingSize))

        }
        return rects

    }
    fun setCanvasSize(size : Size, image: ImageBitmap){
        canvasSize= size
        val pawScaleFactor = canvasSize.height/image.height
        buildingSize = Size((image.width.toFloat()/6)*pawScaleFactor,canvasSize.height)
        if(buildings.isNotEmpty()){
            buildings.forEach {
                it.canvasSize = size
                it.buildingSize = buildingSize
            }
        }
    }


    data class Paw(
        var currentCat: Int,
        var canvasSize : Size,
        var xPos : Float,
        var topYPos : Float,
        var botYPos : Float,
        var moveSpeed : Int,
        var holeSize: Float,
        var buildingSize: Size,
    )
    {
        fun changeHoleSize(){
            botYPos = topYPos + canvasSize.height + holeSize
        }

        fun reset(pos: Float): Paw {
            currentCat = (0..2).random()
            xPos = (canvasSize.width + pos) - ((canvasSize.width+pos)%moveSpeed)
            topYPos = (-canvasSize.height.toInt()+30..-holeSize.toInt()-50).random().toFloat()
            botYPos = topYPos + canvasSize.height + holeSize
            return this
        }

        fun isAtFrac(frac : Int) : Boolean{
            return abs(xPos - canvasSize.width/frac) <= moveSpeed
        }
        fun updatePos() : Boolean{
            xPos -= moveSpeed
            val winDist = (canvasSize.width/4)
            if(xPos == winDist-(winDist%moveSpeed)){
                return true
            }
            return false
        }
    }
}