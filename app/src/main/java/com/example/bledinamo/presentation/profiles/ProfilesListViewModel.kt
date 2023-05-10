package com.example.bledinamo.presentation.profiles

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.datastore.PreferencesRepo
import com.example.bledinamo.persistence.entities.Profile
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

data class MainFormState(
    val currentName: String = "",
    val currentNameErrors: MutableList<String> = mutableListOf(),
    val currentAgeText :String = "",
    val currentAge: Int = -1,
    val currentAgeErrors: MutableList<String> = mutableListOf(),
    val male: Boolean = false,
    val female : Boolean = false,
    val currentDescription: String = "",

)
@HiltViewModel
class ProfilesListViewModel @Inject constructor(
    private val database: AppDatabase,
    private val prefRepo: PreferencesRepo,
): ViewModel() {


    var allProfiles by mutableStateOf<List<ProfileWithMeasurements>?>(null)
        private set

    var formResult by mutableStateOf<FormResultMessage>(FormResultMessage.Loading)

    var loadingProfiles by mutableStateOf(true)
        private set

    var validatingForm by mutableStateOf(false)
        private set


    private val _formState = MutableStateFlow(MainFormState())
    val formState: StateFlow<MainFormState> = _formState.asStateFlow()

    fun setCurrentProfile(profile: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                prefRepo.updateCurrentProfile(profile)
            }
        }
    }
    //Funciones para actualizar los valores del form
    fun updateName(name: String) {
        val errors = mutableListOf<String>()
        var update = true
        if (name.length < 3) {
            errors.add("El nombre del perfil debe tener al menos 3 caracteres")
        }
        if (name.length > 25) {
            update = false
        }
        if(update){
            _formState.value = _formState.value.copy(
                currentName = name,
                currentNameErrors = errors
            )
        }

    }

    fun updateSex(sex: String) {
        var _male: Boolean
        var _female: Boolean
        if (sex == "M"){
            _male = true
            _female = false
        }
        else{
            _female = true
            _male = false
        }
        _formState.value = _formState.value.copy(
            male =  _male,
            female = _female
        )
    }
    fun updateAge(age: String) {
        var _currentAge = 0
        var _currentAgeText = age
        val errors = mutableListOf<String>()
        try {
            _currentAge = age.toInt()
            if(_currentAge <0)
                errors.add("Introduce un número positivo")
            if(_currentAgeText.isEmpty()){
                errors.add("Introduce un número!")
            }
        }
        catch (e : NumberFormatException){
            errors.add("Introduce un número válido")
        }
        _formState.value = _formState.value.copy(
            currentAgeText = _currentAgeText,
            currentAge = _currentAge,
            currentAgeErrors = errors
        )
    }
    fun updateDesc(description: String) {
        _formState.value = _formState.value.copy(
            currentDescription = description,
        )
    }

    fun createProfile() {
        validatingForm = true


        allProfiles!!.forEach {
            if(_formState.value.currentName == it.profile.name) {
                formResult = FormResultMessage.ExistingName
                validatingForm = false
                return@createProfile
            }
        }
        Log.d("ProfilesViewModel","Validando")
        if(validateProfile()){
            Log.d("ProfilesViewModel","Validada")
            validatingForm = false
            val _sex : String
            if(_formState.value.male)
                _sex = "M"
            else
                _sex = "F"

            val newProfile = Profile(
                name = _formState.value.currentName,
                age = _formState.value.currentAge,
                sex = _sex,
                description = _formState.value.currentDescription
            )
            viewModelScope.launch {
                //Para pasar la operación de lectura de la BBDD a un hilo de E/S más apropiado que el hilo principal
                withContext(Dispatchers.IO) {
                    val profileDao = database.profileDao()
                    profileDao.createProfile(newProfile)
                }
            }
            Log.d("ProfilesViewModel","Success")
            formResult = FormResultMessage.Success
        }
        else{
            Log.d("ProfilesViewModel","Validating failed")
            validatingForm = false
            formResult = FormResultMessage.IncompleteFields
        }
    }
    private fun validateProfile() : Boolean{
        //Primero validamos que se hayan metido todos los elementos necesarios
        Log.d("ProfilesViewModel", "${_formState.value.currentNameErrors.isNotEmpty()}, " +
                "${_formState.value.currentAgeErrors.isNotEmpty()}, " +
                "${(!_formState.value.male && !_formState.value.female)}," )

        return !(_formState.value.currentNameErrors.isNotEmpty() || _formState.value.currentName.isEmpty()
                || _formState.value.currentAgeErrors.isNotEmpty() || _formState.value.currentAgeText.isEmpty()
                || (!_formState.value.male && !_formState.value.female))
    }


    fun getProfiles(){
        viewModelScope.launch {
            //Para pasar la operación de lectura de la BBDD a un hilo de E/S más apropiado que el hilo principal
            withContext(Dispatchers.IO) {
                val profileDao = database.profileDao()
                allProfiles = profileDao.getProfilesWithGripMeasurements()
            }

            if(allProfiles != null){
                loadingProfiles = false
            }
            Log.d("ProfilesViewModel",allProfiles.toString())
        }

    }


}