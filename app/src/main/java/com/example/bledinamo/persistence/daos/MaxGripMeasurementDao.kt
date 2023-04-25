package com.example.bledinamo.persistence.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bledinamo.persistence.entities.MaxGripMeasurement

@Dao
interface MaxGripMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaxGripMeasurement(vararg measurement: MaxGripMeasurement)


}