package com.mnaufalhamdani.customtakepickfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.mnaufalhamdani.customtakepickfile.databinding.ActivityMainBinding
import com.mnaufalhamdani.takepickfile.TakePickFile

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCapture.setOnClickListener {
            TakePickFile.with(this)
                .defaultCamera(TakePickFile.LensCamera.LENS_BACK_CAMERA)//default is LENS_FRONT_CAMERA
                .typeMedia(TakePickFile.TypeMedia.PHOTO)//default is PHOTO
                .setLineOfId(false)//default is false
                .start(0)
        }

        binding.btnCaptureId.setOnClickListener {
            TakePickFile.with(this)
                .defaultCamera(TakePickFile.LensCamera.LENS_BACK_CAMERA)//default is LENS_FRONT_CAMERA
                .typeMedia(TakePickFile.TypeMedia.PHOTO)//default is PHOTO
                .setLineOfId(true)//default is false
                .cameraOnly(true)//default is false
                .start(0)
        }

        binding.btnRecord.setOnClickListener {
            TakePickFile.with(this)
                .defaultCamera(TakePickFile.LensCamera.LENS_BACK_CAMERA)//default is LENS_FRONT_CAMERA
                .typeMedia(TakePickFile.TypeMedia.VIDEO)//default is PHOTO
                .setLineOfId(false)//default is false
                .start(0)
        }

        binding.btnCaptureFace.setOnClickListener {
            TakePickFile.with(this)
                .defaultCamera(TakePickFile.LensCamera.LENS_FRONT_CAMERA)//default is LENS_FRONT_CAMERA
                .typeMedia(TakePickFile.TypeMedia.PHOTO)//default is PHOTO
                .setLineOfId(false)//default is false
                .isFaceDetection(true)//default is false
                .isWaterMark(true)//default is false
                .start(0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    it.data?.let { uri ->
                        processFile(uri)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processFile(uri: Uri) {
        Log.d("processImage2:", uri.toFile().absolutePath)
        val path = uri.toFile().absolutePath
        binding.tvPath.text = path
    }
}