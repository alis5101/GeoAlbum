package com.geoalbum.dagger.modules

import androidx.room.Room
import com.geoalbum.GeoAlbumApplication
import com.geoalbum.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class GeoAlbumModule(private val application: GeoAlbumApplication) {

    @Provides
    @Singleton
    fun provideAppDatabase(): AppDatabase {
        return Room.databaseBuilder(
            context = application,
            klass = AppDatabase::class.java,
            name = "geo_album_database"
        ).build()
    }
}