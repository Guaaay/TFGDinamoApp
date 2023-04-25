package com.example.bledinamo.persistence.entities

import androidx.room.*

@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val profileId: Int = 0,
    val name: String,
    val age:Int,
    val sex:String,

)

@Entity
data class ProfileWithMeasurements(
    @Embedded val profile: Profile,
    @Relation(
        parentColumn = "profileId",
        entityColumn = "profileCreatorId"
    )
    val measurements: List<MaxGripMeasurement>
)