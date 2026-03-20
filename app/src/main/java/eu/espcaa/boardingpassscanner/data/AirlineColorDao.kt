package eu.espcaa.boardingpassscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AirlineColorDao {

    @Query("SELECT * FROM airline_colors")
    suspend fun getAll(): List<AirlineColorEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AirlineColorEntity)
}
