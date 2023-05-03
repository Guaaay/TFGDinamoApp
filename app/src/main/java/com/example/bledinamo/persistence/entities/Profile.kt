package com.example.bledinamo.persistence.entities

import androidx.room.*
import java.time.LocalDateTime

@Entity
data class Profile(
    @PrimaryKey
    val name: String,
    val age:Int,
    val sex:String,
    val description:String,

)

@Entity
data class MaxGripMeasurement (
    @PrimaryKey(autoGenerate = true)
    val measurementId: Int,
    val profileCreatorName: String,
    val measurement: Float,
    val dateTaken: LocalDateTime,
)

data class ProfileWithMeasurements(
    @Embedded
    val profile: Profile,
    @Relation(
        parentColumn = "name",
        entityColumn = "profileCreatorName"
    )
    val measurements: List<MaxGripMeasurement>
)