package com.geoalbum.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geoalbum.database.data.GeoAlbumEntry

@Database(entities = [GeoAlbumEntry::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun geoAlbumDao(): GeoAlbumDao
}