package com.example.bledinamo.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class MaxGripMeasurement (
    @PrimaryKey val id: Int,
    val measurement: Float,
    val dateTaken: LocalDateTime,
)