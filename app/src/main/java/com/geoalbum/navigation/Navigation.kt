package com.geoalbum.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geoalbum.database.data.GeoAlbumEntry
import com.geoalbum.ui.AddItemView
import com.geoalbum.ui.DetailsView
import com.geoalbum.ui.mainView

@Composable
fun Navigation(albumList: List<GeoAlbumEntry>?) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.ListViewScreen.route) {
        composable(route = Screen.ListViewScreen.route) {
             mainView(navController, albumList ?: emptyList())
        }

        composable(
            route = Screen.DetailsViewScreen.route + "/{id}",
            arguments = listOf(navArgument("id"){
                type = NavType.StringType
            })
        ) { entry ->
            val id = entry.arguments?.getString("id")
            if(id != null) {
                DetailsView(id = id.toLong(), navController = navController)
            }
        }

        composable(
            route = Screen.AddEditItemViewScreen.route + "?id={id}",
            arguments = listOf(navArgument("id"){
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entry ->
            val id = entry.arguments?.getString("id")

            if(id != null) {
                AddItemView(navController, id.toLong())
            } else {
                AddItemView(navController)
            }
        }
    }
}