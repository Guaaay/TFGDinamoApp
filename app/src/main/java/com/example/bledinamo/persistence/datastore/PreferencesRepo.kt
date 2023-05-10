package com.example.bledinamo.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.entities.Profile
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton


val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepo @Inject constructor(
    var database: AppDatabase,
    @ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    // Define a key for your data
    private val currentProfileKey = stringPreferencesKey("currProfile")

    // Store data using the DataStore
    suspend fun updateCurrentProfile(profileName: String) {
        dataStore.edit { settings ->
            settings[currentProfileKey] = profileName
        }
    }

    // Read data from the DataStore
    suspend fun getCurrentProfile(): ProfileWithMeasurements? {
        val settings = dataStore.data.first()
        val profileName = settings[currentProfileKey] ?: ""
        val profileDao = database.profileDao()
        if(profileDao.getProfileWithGrips(profileName).isNotEmpty())
            return profileDao.getProfileWithGrips(profileName).first()
        else return null
    }
}