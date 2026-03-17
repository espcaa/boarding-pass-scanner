package eu.espcaa.boardingpassscanner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "legs",
    foreignKeys = [
        ForeignKey(
            entity = BoardingPassEntity::class,
            parentColumns = ["id"],
            childColumns = ["boardingPassId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boardingPassId")]
)
data class LegEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boardingPassId: Long,
    val from: String,
    val to: String,
    val carrier: String,
    val flightNumber: String,
    val flightJulian: String,
    val seat: String,
    val sequenceNumber: String,
    val compartmentCode: String,
)