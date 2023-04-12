package com.example.bledinamo.data

import com.example.bledinamo.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface GripReceiveManager {

    val data: MutableSharedFlow<Resource<GripResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}