package eu.espcaa.boardingpassscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardingPassDao {

    @Insert
    suspend fun insertBoardingPass(boardingPass: BoardingPassEntity): Long

    @Insert
    suspend fun insertLegs(legs: List<LegEntity>)

    @Transaction
    suspend fun insertBoardingPassWithLegs(boardingPass: BoardingPassEntity, legs: List<LegEntity>) {
        val passId = insertBoardingPass(boardingPass)
        insertLegs(legs.map { it.copy(boardingPassId = passId) })
    }

    @Transaction
    @Query("SELECT * FROM boarding_passes ORDER BY scannedAt DESC")
    fun getAllBoardingPasses(): Flow<List<BoardingPassWithLegs>>

    @Transaction
    @Query("SELECT * FROM boarding_passes WHERE id = :id")
    suspend fun getBoardingPassById(id: Long): BoardingPassWithLegs?

    @Query("DELETE FROM boarding_passes WHERE id = :id")
    suspend fun deleteBoardingPass(id: Long)
}