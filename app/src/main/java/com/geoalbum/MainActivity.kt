package com.geoalbum

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebChromeClient.FileChooserParams
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.geoalbum.dagger.components.DaggerGeoAlbumComponent
import com.geoalbum.dagger.modules.GeoAlbumModule
import com.geoalbum.database.AppDatabase
import com.geoalbum.navigation.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.Permissions
import javax.inject.Inject

class MainActivity : ComponentActivity(), OnDataChangedTransmitter {

    var galleryChooseResult: ActivityResultLauncher<Intent>? = null
    var cameraChooseResult: ActivityResultLauncher<Intent>? = null

    @Inject
    lateinit var appDatabase: AppDatabase

    val onDataChangedListeners: MutableList<Listener> = mutableListOf()

    val requestCodeLocation = 123
    val requestCodeCamera = 124

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        galleryChooseResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uriList = FileChooserParams.parseResult(
                it.resultCode,
                it.data
            )

            val uri = uriList?.first()

            if(uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                onDataChangedListeners.forEach { listener -> listener.onImageUriDataChanged(uri) }
            }
        }

        cameraChooseResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val imageBitmap = it.data?.extras?.get("data") as Bitmap

            val tempUri: Uri = getImageUri(this, imageBitmap)

            onDataChangedListeners.forEach { listener -> listener.onImageUriDataChanged(tempUri) }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = appDatabase.geoAlbumDao().getAllEntries()
            response.collect {
                launch(Dispatchers.Main) {
                    setContent {
                        Navigation(it)
                    }
                }
            }
        }
    }

    private fun injectDependencies() {
        val component = DaggerGeoAlbumComponent
            .builder()
            .geoAlbumModule(GeoAlbumModule(application = application as GeoAlbumApplication))
            .build()
        component.inject(this)
    }

    fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCodeLocation
        )
    }

    fun isCameraPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                ),
                requestCodeCamera
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED).not()) {
            if(requestCode == requestCodeCamera) {
                Toast.makeText(this, R.string.camera_permissions_needed, Toast.LENGTH_LONG).show()
            }

            if(requestCode == requestCodeLocation) {
                Toast.makeText(this, R.string.location_permissions_needed, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun registerListener(listener: Listener) {
        onDataChangedListeners.add(listener)
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
}

interface OnDataChangedTransmitter {
    fun registerListener(listener: Listener)
}

interface Listener {
    fun onImageUriDataChanged(uri: Uri?)
}