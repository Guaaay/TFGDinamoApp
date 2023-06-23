package com.example.bledinamo.persistence.daos

import androidx.room.*
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import com.example.bledinamo.persistence.entities.Profile
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements

@Dao
interface ProfileDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun createProfile(vararg profile: Profile)

    @Update
    fun updateProfile(vararg profiles: Profile)


    @Delete
    fun deleteProfiles(vararg profile: Profile)

    @Transaction
    @Insert
    fun insertMeasurement(vararg measurement: MaxGripMeasurement)

    @Transaction
    @Query("DELETE FROM MaxGripMeasurement WHERE profileCreatorName LIKE :name")
    fun deleteMaxGripMeasurementsForProfile(name: String)


    @Transaction
    @Query("SELECT * FROM Profile WHERE name LIKE :name")
    fun getProfile(name: String): List<Profile>
    @Transaction
    @Query("SELECT * FROM Profile WHERE name LIKE :name")
    fun getProfileWithGrips(name: String): List<ProfileWithMeasurements>
    @Transaction
    @Query("SELECT * FROM Profile")
    fun getProfilesWithGripMeasurements(): List<ProfileWithMeasurements>
}