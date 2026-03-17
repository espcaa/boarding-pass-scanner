package eu.espcaa.boardingpassscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boarding_passes")
data class BoardingPassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passengerName: String,
    val pnrCode: String,
    val numberOfLegs: Int,
    val isEticket: Boolean,
    val year: Int,
    val rawBarcode: String,
    val scannedAt: Long = System.currentTimeMillis()
)