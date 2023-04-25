package com.example.bledinamo.persistence.daos

import androidx.room.*
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import com.example.bledinamo.persistence.entities.Profile
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createProfile(vararg profile: Profile)

    @Update
    fun updateProfile(vararg profiles: Profile)

    @Delete
    fun deleteProfile(vararg profiles: Profile)

    @Transaction
    @Query("SELECT * FROM Profile")
    fun getProfileWithGripMeasurements(): List<ProfileWithMeasurements>
}