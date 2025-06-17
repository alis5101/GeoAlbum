package com.geoalbum.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.geoalbum.MainActivity
import com.geoalbum.R
import com.geoalbum.database.data.GeoAlbumEntry
import com.geoalbum.navigation.Screen
import com.geoalbum.ui.theme.GeoAlbumTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DetailsView(id: Long, navController: NavController) {

    val context = LocalContext.current
    var entry by remember { mutableStateOf<GeoAlbumEntry?>(null) }
    val shouldShowDeleteDialog = remember { mutableStateOf(false) }

    if(shouldShowDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDeleteDialog.value = false
            },
            title = { Text(text = stringResource(R.string.delete_alert_title)) },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            (context as MainActivity).appDatabase.geoAlbumDao().delete(entry!!)
                            launch(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                        shouldShowDeleteDialog.value = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_alert_delete_button),
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        shouldShowDeleteDialog.value = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_alert_cancel_button),
                        color = Color.White
                    )
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        entry = (context as MainActivity).appDatabase.geoAlbumDao().getEntryById(id)
    }

    GeoAlbumTheme {
        Scaffold(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 16.dp
            )
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = entry?.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_placeholder)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    text = entry?.title ?: ""
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = entry?.description ?: ""
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.latitude_title, entry?.latitude.toString())
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.longitude_title, entry?.longitude.toString())
                )

                Button(
                    onClick = {
                        navController.navigate(Screen.AddEditItemViewScreen.route + "?id=${(entry?.id.toString())}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.edit_button)
                    )
                }

                Button(
                    onClick = {
                        shouldShowDeleteDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = stringResource(R.string.delete_button)
                    )
                }
            }
        }
    }
}