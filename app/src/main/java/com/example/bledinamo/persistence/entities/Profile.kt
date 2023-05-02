package com.example.bledinamo.persistence.entities

import androidx.room.*

@Entity
data class Profile(
    @PrimaryKey
    val name: String,
    val age:Int,
    val sex:String,
    val description:String,

)

@Entity
data class ProfileWithMeasurements(
    @Embedded val profile: Profile,
    @Relation(
        parentColumn = "name",
        entityColumn = "profileCreatorName"
    )
    val measurements: List<MaxGripMeasurement>
)