package eu.espcaa.boardingpassscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airline_colors")
data class AirlineColorEntity(
    @PrimaryKey val carrier: String,
    val seedColor: Int
)
