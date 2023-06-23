package com.example.bledinamo.games.gatoenglobo

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bledinamo.data.ConnectionState
import com.example.bledinamo.data.GripReceiveManager
import com.example.bledinamo.data.MyBuffer
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.datastore.PreferencesRepo
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import com.example.bledinamo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

data class GameState(
    var stage : GameStage = GameStage.START,
    var tick: Long = 0L,
    val gatoState: GatoState = GatoState(),
    val pawState: PawState = PawState(),
    val bgState: BGState = BGState(),
    var score: Int = 0,
    var load: Float = 0f,
){
    enum class GameStage{
        START,
        RUNNING,
        PAUSED,
        STOPPED
    }
    fun startRunning(){
        stage = GameStage.RUNNING
    }

    fun stop(){
        stage = GameStage.STOPPED
    }
    fun pause(){
        stage = GameStage.PAUSED
    }
    fun resetGame(){
        tick = 0L
        stage = GameStage.START
        score = 0
        pawState.reset()
    }
}

@HiltViewModel
class GatoViewModel @Inject constructor(
    private val gripReceiveManager: GripReceiveManager,
    private val prefRepo: PreferencesRepo,
    private val database: AppDatabase,
): ViewModel() {
    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var load by mutableStateOf(0f)
        private set

    var refLoad by mutableStateOf(0f)
        private set

    var buffer by mutableStateOf<MyBuffer<Float>>(MyBuffer(60))
        private set

    var maxLoad by mutableStateOf(0f)
        private set

    var hasCalibrated by mutableStateOf(false)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
        private set

    var currentProfile by mutableStateOf<ProfileWithMeasurements?>(null)
        private set

    var highScore by mutableStateOf(false)

    var speedSlider by mutableStateOf(0.25f)

    var holeWidthSlider by mutableStateOf(0.25f)

    var loadPercentSlider by mutableStateOf(0.8f)


    private val fps = 30L
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState


    fun startGameLoop(){
        highScore = false
        Log.d("GloboViewModel","iniciando")
        _gameState.value.startRunning()
        _gameState.value.gatoState.refLoad = refLoad
        if(_gameState.value.tick == 0L){
            viewModelScope.launch {

                while (_gameState.value.stage == GameState.GameStage.RUNNING) {
                    _gameState.value.pawState.init()
                    delay(1000L/fps)

                    // Make a copy of gameState to work with
                    val newGameState = _gameState.value.copy()

                    newGameState.gatoState.updatePos(load)
                    newGameState.bgState.updatePos()
                    if(newGameState.pawState.updatePos()){
                        newGameState.score += 1
                    }
                    newGameState.load = load

                    newGameState.tick = newGameState.tick + 1


                    _gameState.value = newGameState
                }
                while(_gameState.value.stage == GameState.GameStage.PAUSED){
                    //PAUSED GAME
                    delay(1000L/fps)
                    val newGameState = _gameState.value.copy()
                    newGameState.tick = newGameState.tick + 1
                    newGameState.pawState.updatePos()
                    _gameState.value = newGameState
                }
                while(_gameState.value.stage == GameState.GameStage.STOPPED){
                    _gameState.value.pawState.init()
                    delay(1000L/fps)
                    // Make a copy of gameState to work with
                    val newGameState = _gameState.value.copy()
                    newGameState.tick = newGameState.tick + 1
                    _gameState.value = newGameState
                }

            }
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMeasurement(){
        val measurement = MaxGripMeasurement(
            profileCreatorName = currentProfile!!.profile.name,
            measurement = maxLoad,
            dateTaken = LocalDateTime.now())

        viewModelScope.launch{
            withContext(Dispatchers.IO) {
                val profileDao = database.profileDao()
                profileDao.insertMeasurement(measurement)
            }
        }
    }
    fun checkHighScore(score:Int):Boolean{
        if(score>currentProfile!!.profile.gatoHighScore) {
            highScore = true
            viewModelScope.launch{
                withContext(Dispatchers.IO) {
                    currentProfile!!.profile.gatoHighScore = score
                    val profileDao = database.profileDao()
                    profileDao.updateProfile(currentProfile!!.profile)
                    currentProfile = prefRepo.getCurrentProfile()
                }
            }
            return true
        }
        return false
    }
    fun getCurrentProfile(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                currentProfile = prefRepo.getCurrentProfile()
            }
        }

    }

    private fun subscribeToChanges(){
        viewModelScope.launch {
            gripReceiveManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        load = result.data.load
                        buffer.add(load)
                        if(load > maxLoad)
                            maxLoad = load
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }

            }
        }
    }
    fun setCalibrated(){
        hasCalibrated = true
    }

    fun setReference(ref : Float){
        refLoad = maxLoad
    }

    fun disconnect(){
        gripReceiveManager.disconnect()
    }

    fun reconnect(){
        gripReceiveManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        gripReceiveManager.startReceiving()
    }


    fun resetValues(){
        buffer.removeAll(buffer)
        maxLoad = 0f
    }

    override fun onCleared() {
        super.onCleared()
        gripReceiveManager.closeConnection()
    }

}