package com.geoalbum.dagger.components

import com.geoalbum.MainActivity
import com.geoalbum.dagger.modules.GeoAlbumModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (GeoAlbumModule::class),
    ]
)
interface GeoAlbumComponent {
    fun inject(activity: MainActivity)
}