package com.example.bledinamo

import android.util.Log
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bledinamo.persistence.AppDatabase
import com.example.bledinamo.persistence.entities.Profile

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, "test-database"
        ).build()
        val profileDao = db.profileDao()
        var newProfile = Profile(name = "juanjo", age = 45, sex = "M")
        profileDao.createProfile(newProfile)
        newProfile = Profile(name = "martina", age = 24, sex = "F")
        profileDao.createProfile(newProfile)
        val perfiles = profileDao.getProfilesWithGripMeasurements()
        Log.d("TEST:" ,perfiles.toString())
        assertEquals(perfiles.first().profile.name,"juanjo")
    }

}