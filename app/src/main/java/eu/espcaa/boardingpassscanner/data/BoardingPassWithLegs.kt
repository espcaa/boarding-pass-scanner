package eu.espcaa.boardingpassscanner.data

import androidx.room.Embedded
import androidx.room.Relation

data class BoardingPassWithLegs(
    @Embedded val boardingPass: BoardingPassEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "boardingPassId"
    )
    val legs: List<LegEntity>
)