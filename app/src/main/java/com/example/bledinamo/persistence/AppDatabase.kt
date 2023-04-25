package com.example.bledinamo.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bledinamo.persistence.daos.MaxGripMeasurementDao
import com.example.bledinamo.persistence.entities.MaxGripMeasurement

@Database(entities = [MaxGripMeasurement::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun maxGripMeasurementDao(): MaxGripMeasurementDao
}