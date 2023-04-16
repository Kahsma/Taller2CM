package com.example.taller2cm

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2cm.databinding.ActivityMainBinding
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object {
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contactsLogic()
        binding.contacts.setOnClickListener {
            startActivity(Intent(baseContext,Contactsactivity::class.java))


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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your logic
                showToast("Contacts permission granted")
            } else {
                // Permission not granted, handle the error or inform the user
                showToast("Contacts permission denied")
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}