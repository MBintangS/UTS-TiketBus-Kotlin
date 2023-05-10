package com.example.uts_tiketbus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pnj.uts_tiketbus.data.tiket.Syawal_Tiket
import com.pnj.uts_tiketbus.data.tiket.Syawal_TiketDao


@Database(entities = [Syawal_Tiket::class], version = 1)
abstract class Syawal_TiketDB : RoomDatabase() {
    abstract fun getSyawal_TiketDao(): Syawal_TiketDao

    companion object{
        @Volatile
        private var instance: Syawal_TiketDB? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            Syawal_TiketDB::class.java,
            "resto-db"
        ).build()
    }
}


