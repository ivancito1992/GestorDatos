package com.gestordatos.BBDD

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IncidentEntity::class,IncidentBackupEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    //se tiene que crear una por cada DAO
    abstract fun incidentDao(): IncidentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "incident_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}