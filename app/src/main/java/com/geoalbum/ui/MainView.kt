package com.geoalbum.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.geoalbum.R
import com.geoalbum.database.data.GeoAlbumEntry
import com.geoalbum.navigation.Screen
import com.geoalbum.ui.theme.GeoAlbumTheme
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun mainView(navController: NavController, albumList: List<GeoAlbumEntry>) {
    GeoAlbumTheme {
        Scaffold(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                )
                .then(Modifier.fillMaxSize()),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddEditItemViewScreen.route)
                    },
                    shape = CircleShape,
                    modifier = Modifier.offset(x = 16.dp, y = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, "Large floating action button")
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            if (albumList.isNullOrEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.empty_list)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(albumList.size) { entryId ->
                        itemView(navController, albumList[entryId])
                    }
                }
            }
        }
    }
}

@Composable
fun itemView(navController: NavController, entry: GeoAlbumEntry) {
    Card(
        modifier = Modifier.padding(bottom = 16.dp),
        onClick = {
            navController.navigate(Screen.DetailsViewScreen.withArgs(entry.id.toString()))
        }
    ) {
        AsyncImage(
            model = entry.photoUri,
            contentDescription = entry.description,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .width(200.dp)
                .height(200.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_placeholder)
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            text = entry.title
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = createDateFromTimestamp(entry.timestamp)
        )
    }
}

fun createDateFromTimestamp(timestamp: Long): String {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val netDate = Date(timestamp)
        return sdf.format(netDate)
    } catch (e: Exception) {
        return e.toString()
    }
}