package com.example.bledinamo.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.data.GripReceiveManager
import com.example.bledinamo.data.MyBuffer
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val database: AppDatabase
): ViewModel() {

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var load by mutableStateOf(0f)
        private set

    var buffer by mutableStateOf<MyBuffer<Float>>(MyBuffer(60))

    var maxLoad by mutableStateOf(0f)

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)



}