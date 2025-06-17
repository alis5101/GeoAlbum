package com.geoalbum.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.geoalbum.Listener
import com.geoalbum.MainActivity
import com.geoalbum.OnDataChangedTransmitter
import com.geoalbum.R
import com.geoalbum.database.data.GeoAlbumEntry
import com.geoalbum.ui.theme.GeoAlbumTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddItemView(navController: NavController, itemId: Long? = null) {
    var title by remember { mutableStateOf(TextFieldValue(text = "")) }
    var description by remember { mutableStateOf(TextFieldValue(text = "")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isCancelButtonEnabled by remember { mutableStateOf(true) }
    var isSaveButtonEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (itemId != null) {
        LaunchedEffect(Unit) {
            val entry = (context as MainActivity).appDatabase.geoAlbumDao().getEntryById(itemId)

            title = TextFieldValue(entry?.title ?: "")
            description = TextFieldValue(entry?.description ?: "")
            imageUri = entry?.photoUri?.let { it.toUri() }
        }
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
                    .verticalScroll(rememberScrollState())
            ) {
                TextField(
                    placeholder = { Text(text = stringResource(R.string.title_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    value = title, onValueChange = { title = it })

                TextField(
                    placeholder = { Text(text = stringResource(R.string.description_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    value = description,
                    onValueChange = { description = it })

                if (context is OnDataChangedTransmitter) {
                    (context as OnDataChangedTransmitter).registerListener(object : Listener {
                        override fun onImageUriDataChanged(uri: Uri?) {
                            imageUri = uri
                        }
                    })
                }

                if (imageUri == null) {
                    Button(
                        onClick = {
                            selectImageInAlbum(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.choose_image)
                        )
                    }
                    Button(
                        onClick = {
                            takePhoto(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.take_a_photo)
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp, bottom = 16.dp)
                            .width(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop,
                    )
                }

                ShowLocationContainer(
                    context,
                    latitude,
                    { newValue -> latitude = newValue },
                    longitude,
                    { newValue -> longitude = newValue })

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val item = GeoAlbumEntry(
                                id = itemId,
                                title = title.text,
                                description = description.text,
                                photoUri = imageUri.toString(),
                                latitude = latitude!!,
                                longitude = longitude!!,
                                timestamp = System.currentTimeMillis()
                            )

                            if (itemId != null) {
                                (context as MainActivity).appDatabase.geoAlbumDao().update(item)
                            } else {
                                (context as MainActivity).appDatabase.geoAlbumDao().insert(item)
                            }

                            launch(Dispatchers.Main) {
                                isSaveButtonEnabled = false
                                navController.popBackStack()
                            }

                        }
                    },
                    enabled = computeSaveButtonEnableState(
                        forceButtonEnabled = !isSaveButtonEnabled,
                        title = title.text,
                        description = description.text,
                        imageUri = imageUri,
                        latitude = latitude,
                        longitude = longitude,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save_button)
                    )
                }
                Button(
                    onClick = {
                        isCancelButtonEnabled = false
                        navController.popBackStack()
                    },
                    enabled = isCancelButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.cancel_button)
                    )
                }
            }
        }
    }
}

fun computeSaveButtonEnableState(
    forceButtonEnabled: Boolean,
    title: String?,
    description: String?,
    imageUri: Uri?,
    latitude: Double?,
    longitude: Double?,
): Boolean {
    if (forceButtonEnabled) {
        return true
    }

    return (title.isNullOrEmpty() || description.isNullOrEmpty() || imageUri == null || imageUri.toString()
        .isEmpty() || latitude == null || longitude == null).not()
}

fun selectImageInAlbum(context: Context) {
    val openGallery = Intent(Intent.ACTION_OPEN_DOCUMENT)
    openGallery.type = "image/*"
    (context as MainActivity).galleryChooseResult?.launch(openGallery)
}

fun takePhoto(context: Context) {
    val isCameraPermissionGranted = (context as MainActivity).isCameraPermissionGranted()

    if (isCameraPermissionGranted) {
        val intent1 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        (context as MainActivity).cameraChooseResult?.launch(intent1)
    }
}

@Composable
fun ShowLocationContainer(
    context: Context,
    latitude: Double?,
    onLatitudeChange: (Double) -> Unit,
    longitude: Double?,
    onLongitudeChange: (Double) -> Unit
) {
    var isLocationPermissionGranted = false
    if (context is MainActivity) {
        isLocationPermissionGranted = context.isLocationPermissionGranted()
    }

    if (!isLocationPermissionGranted) {
        ReloadLocation(context) {
            it.first?.let { onLatitudeChange.invoke(it) }
            it.second?.let { onLongitudeChange.invoke(it) }
        }

        return
    }

    val locationPair = getLocation(context)

    onLatitudeChange.invoke(locationPair.first!!)
    onLongitudeChange.invoke(locationPair.second!!)

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        textAlign = TextAlign.Center,
        text = stringResource(R.string.latitude_title, latitude.toString())
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        textAlign = TextAlign.Center,
        text = stringResource(R.string.longitude_title, longitude.toString())
    )
}

@Composable
fun ReloadLocation(context: Context, onDataChanged: (Pair<Double?, Double?>) -> Unit) {
    Button(
        onClick = {
            if (!(context as MainActivity).isLocationPermissionGranted()) {
                (context as MainActivity).requestLocationPermission()
            } else {
                val locationPair = getLocation(context)
                if (locationPair.first != null || locationPair.second != null) {
                    onDataChanged(locationPair)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.reload_location_button)
        )
    }
}

@SuppressLint("MissingPermission")
fun getLocation(context: Context): Pair<Double?, Double?> {
    if (!(context as MainActivity).isLocationPermissionGranted()) {
        return Pair(null, null)
    }

    var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        5000,
        0F,
        gpsLocationListener
    )

    val lastKnownLocationByGps =
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

    val latitude = lastKnownLocationByGps?.latitude
    val longitude = lastKnownLocationByGps?.longitude

    return Pair(latitude, longitude)
}