package eu.espcaa.boardingpassscanner.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BoardingPassEntity::class, LegEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardingPassDao(): BoardingPassDao
}