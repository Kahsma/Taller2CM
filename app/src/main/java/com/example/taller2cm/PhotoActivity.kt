package com.example.taller2cm

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.taller2cm.databinding.ActivityPhotoBinding
import java.io.File

class PhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var uriCamera: Uri

    //request gallery
    private val galleryrequest = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback { result: Uri? -> loadImage(result) })
    private val cameraRequest = registerForActivityResult(ActivityResultContracts.TakePicture(), ActivityResultCallback { loadImage(uriCamera) })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intialzeFile()
        binding.galleryB.setOnClickListener {
            galleryrequest.launch("image/*")
        }
        binding.cameraB.setOnClickListener {
            cameraRequest.launch(uriCamera)
        }



    }

    private fun intialzeFile() {
        val file = File(filesDir,"picFromCamera")
        uriCamera = FileProvider.getUriForFile(this, applicationContext.packageName+".fileprovider",file)
    }

    fun loadImage(result: Uri?){
        val imageStream = contentResolver.openInputStream(result!!)
        val image = BitmapFactory.decodeStream(imageStream)
        binding.image.setImageBitmap(image)

    }
}