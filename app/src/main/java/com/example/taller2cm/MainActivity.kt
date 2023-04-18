package com.example.taller2cm

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2cm.databinding.ActivityMainBinding
import android.Manifest
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object {
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 100
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_STORAGE_PERMISSION = 2
        private const val REQUEST_LOCATION_PERMISSION = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contacts.setOnClickListener {
            contactsLogic()

        }
        binding.gallery.setOnClickListener {
            galleryLogic()
        }

        binding.map.setOnClickListener {
            mapLogic()
        }

    }

    private fun galleryLogic(){
        if(ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
        )== PackageManager.PERMISSION_GRANTED
        ){
            // Permission already granted, proceed with your logic
            showToast("Camera permission already granted")
            startActivity(Intent(baseContext,PhotoActivity::class.java))

        }else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            showCameraRationaleDialog()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }



    private fun contactsLogic(){
        if (ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed with your logic
            showToast("Contacts permission already granted")
            startActivity(Intent(baseContext,Contactsactivity::class.java))

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            // Permission denied, show rationale message and request permission again
            showRationaleDialog()
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun mapLogic(){
        if (ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )== PackageManager.PERMISSION_GRANTED
        ){
            // Permission already granted, proceed with your logic
            showToast("location permission already granted")
            if(isLocationEnabled()){
                startActivity(Intent(baseContext,MapActivity::class.java))

            }else{
                showLocationEnableDialog()
            }


        }else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            showLocationRatinaleDialog()
        }else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location service not enabled")
            .setMessage("This app needs access to your location to work properly.")
            .setPositiveButton("Enable") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                // Check if camera permission has been granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic

                } else {
                    showCameraRationaleDialog()
                    // Permission not granted, handle the error or inform the user
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                // Check if storage permissions have been granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted, proceed with your logic
                } else {
                    // Permission not granted, handle the error or inform the user
                }
            }
            CONTACTS_PERMISSION_REQUEST_CODE -> {
                // Check if contacts permission has been granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic
                } else {
                    // Permission not granted, show rationale dialog or inform the user
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                        // Show rationale dialog
                        showRationaleDialog()
                    } else {
                        // Permission permanently denied, handle the error or inform the user
                    }
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                // Check if contacts permission has been granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic
                } else {
                    // Permission not granted, show rationale dialog or inform the user
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show rationale dialog
                        showLocationRatinaleDialog()
                    } else {
                        // Permission permanently denied, handle the error or inform the user
                    }
                }
            }

        }
    }

    private fun showRationaleDialog() {
        Log.d("asd", "Showing rationale dialog")
        AlertDialog.Builder(this)
            .setTitle("Contacts permission needed")
            .setMessage("This app needs access to your contacts to work properly.")
            .setPositiveButton("OK") { _, _ ->
                Log.d("MainActivity", "Requesting permission again")
                // Request permission again
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CONTACTS_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Log.d("MainActivity", "Rationale dialog cancelled")
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun showCameraRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission  Needed")
            .setMessage("This app needs access to your camera to work properly.")
            .setPositiveButton("OK"){_,_->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
            .create()
            .show()




    }

    private  fun showLocationRatinaleDialog(){

        AlertDialog.Builder(this)
            .setTitle("Location Permission  Needed")
            .setMessage("This app needs access to your Location to work properly.")
            .setPositiveButton("OK"){_,_->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
            .create()
            .show()

    }






    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}