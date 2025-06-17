package com.geoalbum.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.geoalbum.database.data.GeoAlbumEntry

@Dao
interface GeoAlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GeoAlbumEntry): Long // Zwraca ID wstawionego rekordu

    @Update
    suspend fun update(entry: GeoAlbumEntry)

    @Delete
    suspend fun delete(entry: GeoAlbumEntry)

    @Query("SELECT * FROM geo_album_entries ORDER BY timestamp DESC")
    fun getAllEntries(): kotlinx.coroutines.flow.Flow<List<GeoAlbumEntry>> // Użyj Flow dla reaktywnych aktualizacji

    @Query("SELECT * FROM geo_album_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): GeoAlbumEntry?
}