package com.example.bledinamo.games.gatoenglobo

import androidx.compose.ui.graphics.ImageBitmap

enum class GloboAnim {
    CAYENDO,
    MEDIO,
    SUBIENDO,
}

data class GloboState(val status : GloboAnim) {
    private val imageWidth = 76
    private val imageHeight = 53
    private val velocityUnit = 3
    private var isGoingUp = false
    private var currentPosYOffset = 0

    fun updatePos(){

    }
}