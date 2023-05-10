package com.pnj.uts_tiketbus.data.tiket

import androidx.room.*

@Dao
interface Syawal_TiketDao {
    @Query("SELECT * FROM Syawal_Tiket WHERE nama_pembeli LIKE :namaPembeli")
    suspend fun searchTiket(namaPembeli: String) : List<Syawal_Tiket>

    @Insert
    suspend fun addTiket(tiket: Syawal_Tiket)

    @Update(entity = Syawal_Tiket::class)
    suspend fun updateTiket(tiket: Syawal_Tiket)

    @Delete
    suspend fun deleteTiket(tiket: Syawal_Tiket)

    @Query("SELECT * FROM Syawal_Tiket ORDER BY id DESC")
    suspend fun getAllTiket(): List<Syawal_Tiket>
}