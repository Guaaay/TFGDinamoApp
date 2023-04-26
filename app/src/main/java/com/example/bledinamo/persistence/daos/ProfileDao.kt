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

    @Query("SELECT * FROM Profile WHERE name LIKE :name")
    fun getProfile(name: String): List<Profile>
    @Query("SELECT * FROM Profile WHERE name LIKE :name")
    fun getProfileWithGrips(name: String): List<ProfileWithMeasurements>
    @Transaction
    @Query("SELECT * FROM Profile")
    fun getProfilesWithGripMeasurements(): List<ProfileWithMeasurements>
}