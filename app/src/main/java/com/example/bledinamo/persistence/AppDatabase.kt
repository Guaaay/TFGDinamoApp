package com.example.bledinamo.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bledinamo.persistence.daos.MaxGripMeasurementDao
import com.example.bledinamo.persistence.daos.ProfileDao
import com.example.bledinamo.persistence.entities.MaxGripMeasurement
import com.example.bledinamo.persistence.entities.Profile

@Database(entities = [MaxGripMeasurement::class, Profile::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun maxGripMeasurementDao(): MaxGripMeasurementDao

}