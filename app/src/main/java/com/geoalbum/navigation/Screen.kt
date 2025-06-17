package com.geoalbum.navigation

sealed class Screen(val route: String) {
    object ListViewScreen: Screen("list_view_screen")
    object DetailsViewScreen: Screen("details_view_screen")
    object AddEditItemViewScreen: Screen("add_edit_item_view_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}