package eu.espcaa.boardingpassscanner.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BoardingPassEntity::class, LegEntity::class, AirlineColorEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardingPassDao(): BoardingPassDao
    abstract fun airlineColorDao(): AirlineColorDao
}