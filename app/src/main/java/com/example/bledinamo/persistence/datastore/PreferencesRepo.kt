package com.example.bledinamo.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.entities.Profile
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import kotlinx.coroutines.flow.first
import javax.inject.Inject


val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesRepo(@Inject private val database: AppDatabase,
                      private val context: Context) {
    private val dataStore = context.dataStore

    // Define a key for your data
    private val currentProfileKey = stringPreferencesKey("counter")

    // Store data using the DataStore
    suspend fun updateCurrentProfile(profile: Profile) {
        val profileName = profile.name
        dataStore.edit { settings ->
            settings[currentProfileKey] = profileName
        }
    }

    // Read data from the DataStore
    suspend fun getCurrentProfile(): ProfileWithMeasurements {
        val settings = dataStore.data.first()
        val profileName = settings[currentProfileKey] ?: ""
        val profileDao = database.profileDao()
        return profileDao.getProfileWithGrips(profileName).first()
    }
}