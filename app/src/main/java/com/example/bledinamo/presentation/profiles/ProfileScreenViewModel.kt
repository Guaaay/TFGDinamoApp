package com.example.bledinamo.presentation.profiles

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bledinamo.persistence.AppDatabase
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


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val database: AppDatabase
): ViewModel() {


    var profileResult by mutableStateOf<ProfileWithMeasurements?>(null)
        private set

    var loadingProfile by mutableStateOf(true)
        private set

    var showDialog by mutableStateOf(false)
        private set

    var deleted by mutableStateOf(false)
        private set





    fun showDialog(){
        showDialog = true;
    }
    fun hideDialog(){
        showDialog = false;
    }



    fun deleteProfile(profile: Profile){
        viewModelScope.launch {
            //Para pasar la operación de lectura de la BBDD a un hilo de E/S más apropiado que el hilo principal
            withContext(Dispatchers.IO) {
                val profileDao = database.profileDao()
                profileDao.deleteProfiles(profile)
                profileDao.deleteMaxGripMeasurementsForProfile(profile.name)
                deleted = true
            }
        }
    }
    fun setDeleted(){
        deleted = false
    }
    fun getProfile(profileName : String){

        viewModelScope.launch {
            //Para pasar la operación de lectura de la BBDD a un hilo de E/S más apropiado que el hilo principal
            withContext(Dispatchers.IO) {
                val profileDao = database.profileDao()
                profileResult = profileDao.getProfileWithGrips(profileName).first()
            }

            if(profileResult != null){
                loadingProfile = false
            }
        }
    }

}