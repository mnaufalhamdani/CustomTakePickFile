@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.takepickfile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.android.gms.location.LocationRequest
import com.mnaufalhamdani.takepickfile.R
import com.mnaufalhamdani.takepickfile.TakePickFile
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_CAMERA_ONLY
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_FRONT_CAMERA_ONLY
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_IS_FACE_DETECTION
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_IS_WATERMARK
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_LATITUDE
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_LENS_CAMERA
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_LINE_OF_ID
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_LONGITUDE
import com.mnaufalhamdani.takepickfile.TakePickFile.Companion.EXTRA_TYPE_MEDIA
import com.mnaufalhamdani.takepickfile.core.FaceContourDetectionProcessor
import com.mnaufalhamdani.takepickfile.core.LocationLiveData
import com.mnaufalhamdani.takepickfile.databinding.FragmentFileBinding
import com.mnaufalhamdani.takepickfile.utils.FileResult
import com.mnaufalhamdani.takepickfile.utils.convertPathToBitmap
import com.mnaufalhamdani.takepickfile.utils.drawMultilineTextToBitmap
import com.mnaufalhamdani.takepickfile.utils.getAddressFromGPS
import com.mnaufalhamdani.takepickfile.utils.saveBitmap
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FileFragment : BaseFragment<FragmentFileBinding>(R.layout.fragment_file) {
    companion object {
        private const val TAG = "CameraXFragment"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        private const val REQUEST_CODE_GALLERY = 100
    }

    private var lensCamera: Int = 1
    private val typeMedia by lazy { arguments?.getInt(EXTRA_TYPE_MEDIA) ?: 0 }
    private val showLineOfId by lazy { arguments?.getBoolean(EXTRA_LINE_OF_ID) ?: false }
    private val cameraOnly by lazy { arguments?.getBoolean(EXTRA_CAMERA_ONLY) ?: false }
    private val frontCameraOnly by lazy { arguments?.getBoolean(EXTRA_FRONT_CAMERA_ONLY) ?: false }
    private val isFaceDetection by lazy { arguments?.getBoolean(EXTRA_IS_FACE_DETECTION) ?: false }
    private val isWaterMark by lazy { arguments?.getBoolean(EXTRA_IS_WATERMARK) ?: false }
    private val latitude by lazy { arguments?.getDouble(EXTRA_LATITUDE) ?: 0.0 }
    private val longitude by lazy { arguments?.getDouble(EXTRA_LONGITUDE) ?: 0.0 }
    private var mLatitude = 0.0
    private var mLongitude = 0.0

    lateinit var onResult: FileResult
    private lateinit var locationGPS: LocationLiveData

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var recording: Recording? = null
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var orientationEventListener: OrientationEventListener? = null
    private var aspectRatio = AspectRatio.RATIO_16_9
    private val mMedia = MediaPlayer()

    override fun onBackPressed() {
        onResult.onResultCancel()
    }

    override val binding: FragmentFileBinding by lazy {
        FragmentFileBinding.inflate(
            layoutInflater
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onResult = binding.root.context as FileResult
        lensCamera = arguments?.getInt(EXTRA_LENS_CAMERA) ?: 1
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (showLineOfId){
            if (isFaceDetection){
                binding.lineOfId.visibility = View.GONE
            }else{
                binding.lineOfId.visibility = View.VISIBLE
            }
        }

        if (cameraOnly){
            binding.btnGallery.visibility = View.INVISIBLE
        }

        if (frontCameraOnly){
            binding.btnSwitchCamera.visibility = View.INVISIBLE
        }

        binding.btnSwitchCamera.setOnClickListener {
            lensCamera = if (lensCamera == CameraSelector.LENS_FACING_FRONT) {
                binding.btnSwitchCamera.animate().setDuration(200).rotation(0f)
                CameraSelector.LENS_FACING_BACK
            } else {
                camera.cameraControl.enableTorch(false)
                binding.btnFlashCamera.setImageResource(R.drawable.ic_flash_off)

                binding.btnSwitchCamera.animate().setDuration(200).rotation(180f)
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }

        binding.btnFlashCamera.setOnClickListener {
            setFlashIcon(camera)
        }

        binding.btnGallery.setOnClickListener {
            var typeMyme = "image/*"
            if (typeMedia == TakePickFile.TypeMedia.PHOTO.value) {
                typeMyme = "image/*"
            }else if (typeMedia == TakePickFile.TypeMedia.VIDEO.value) {
                typeMyme = "video/*"
            }

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = typeMyme
            startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_CODE_GALLERY)
        }

        onRunPermission(
            listenerGranted = {
                if (typeMedia == TakePickFile.TypeMedia.PHOTO.value) {
                    if (isWaterMark){
                        locationGPS = LocationLiveData(binding.root.context, pPriority = LocationRequest.PRIORITY_HIGH_ACCURACY)
                        mLatitude = latitude
                        mLongitude = longitude

                        binding.tvWatermark.text = setLocation(mLatitude, mLongitude)
                    }
                    binding.btnTakePicture.visibility = View.VISIBLE
                    binding.btnTakeVideo.visibility = View.GONE
                    binding.btnTakePicture.setOnClickListener {
                        takePhoto()
                    }
                }else if (typeMedia == TakePickFile.TypeMedia.VIDEO.value) {
                    binding.btnTakeVideo.visibility = View.VISIBLE
                    binding.btnTakePicture.visibility = View.GONE
                    binding.btnTakeVideo.setOnClickListener {
                        takeVideo()
                    }
                }

                startCamera()
                observeVM()
                              },
            listenerDeny = { Toast.makeText(binding.root.context, it, Toast.LENGTH_SHORT).show() }
        )
    }

    private fun observeVM() {
        if (isFaceDetection && typeMedia == TakePickFile.TypeMedia.PHOTO.value){
            binding.btnGallery.visibility = View.INVISIBLE
            binding.graphicOverlay.isFaceDetected.observe(viewLifecycleOwner) {
                if (it) {
                    binding.btnTakePicture.visibility = View.VISIBLE
                } else {
                    binding.btnTakePicture.visibility = View.INVISIBLE
                }
            }
        }else if (isFaceDetection && typeMedia == TakePickFile.TypeMedia.VIDEO.value){
            Toast.makeText(binding.root.context, "Face Detection Not Available", Toast.LENGTH_LONG).show()
        }

        if (isWaterMark && typeMedia == TakePickFile.TypeMedia.PHOTO.value){
            locationGPS.observe(viewLifecycleOwner){
                it?.let {
                    mLatitude = it.latitude
                    mLongitude = it.longitude
                    binding.tvWatermark.text = setLocation(mLatitude, mLongitude)
                }
            }
        }else if (isFaceDetection && typeMedia == TakePickFile.TypeMedia.VIDEO.value){
            Toast.makeText(binding.root.context, "Location Not Available", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Recycle")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    var cursor: Cursor? = null
                    val path = try {
                        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Video.Media.DATA)
                        cursor = binding.root.context.contentResolver.query(it, projection, null, null, null)
                        val columnIndexImage = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        val columnIndexVideo = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        cursor?.moveToFirst()
                        cursor?.getString(columnIndexImage ?: columnIndexVideo ?: 0)
                    }finally {
                        cursor?.close()
                    }

                    path?.let { newPath ->
                        val uri = Uri.fromFile(File(newPath))
                        onResult.onFileResult(uri)
                    }
                }
            }
        }
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        binding.graphicOverlay.cameraSelector = if (lensCamera == 1) {
            CameraSelector.DEFAULT_BACK_CAMERA
        }else{
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        return FaceContourDetectionProcessor(binding.graphicOverlay)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(binding.root.context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(binding.root.context))
    }

    @SuppressLint("WrongConstant")
    private fun bindCameraUserCases() {
        val rotation = binding.viewFinder.display.rotation

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            )
            .setAspectRatio(aspectRatio)
            .build()

        videoCapture = VideoCapture.withOutput(recorder).apply {
            targetRotation = rotation
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensCamera)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, selectAnalyzer())
            }

        orientationEventListener = object : OrientationEventListener(binding.root.context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val myRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture.targetRotation = myRotation
                videoCapture.targetRotation = myRotation
            }
        }
        orientationEventListener?.enable()

        try {
            cameraProvider.unbindAll()

            camera = if (isFaceDetection && typeMedia == TakePickFile.TypeMedia.PHOTO.value){
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,videoCapture, imageAnalyzer
                )
            }else{
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,videoCapture
                )
            }
            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpZoomTapToFocus(){
        binding.viewFinder.setOnTouchListener { _, event ->
            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                binding.viewFinder.width.toFloat(), binding.viewFinder.height.toFloat()
            )
            val autoFocusPoint = factory.createPoint(event.x, event.y)
            try {
                camera.cameraControl.startFocusAndMetering(
                    FocusMeteringAction.Builder(
                        autoFocusPoint,
                        FocusMeteringAction.FLAG_AF
                    ).apply {
                        disableAutoCancel()
                    }.build()
                )
            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }
            true
        }
    }

    private fun setFlashIcon(camera: Camera) {
        if (lensCamera == CameraSelector.LENS_FACING_BACK) {
            if (camera.cameraInfo.hasFlashUnit()) {
                if (camera.cameraInfo.torchState.value == 0) {
                    camera.cameraControl.enableTorch(true)
                    binding.btnFlashCamera.setImageResource(R.drawable.ic_flash_on)
                } else {
                    camera.cameraControl.enableTorch(false)
                    binding.btnFlashCamera.setImageResource(R.drawable.ic_flash_off)
                }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Flash is Not Available",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnFlashCamera.isEnabled = false
            }
        }
    }

    private fun takePhoto() {
        binding.btnTakePicture.isEnabled = false

        if (isFaceDetection && typeMedia == TakePickFile.TypeMedia.PHOTO.value){
            if (binding.graphicOverlay.isFaceDetected.value == false){
                binding.btnTakePicture.isEnabled = true
                return
            }
        }

        val imageFolder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), getString(R.string.app_name)
        )
        if (!imageFolder.exists()) {
            imageFolder.mkdir()
        }

        val fileName = "IMG_" + SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Images")
            }
        }

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (lensCamera == CameraSelector.LENS_FACING_FRONT)
        }
        val outputOption =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                OutputFileOptions.Builder(
                    binding.root.context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).setMetadata(metadata).build()
            }else{
                val imageFile = File(imageFolder, fileName)
                OutputFileOptions.Builder(imageFile)
                    .setMetadata(metadata).build()
            }

        playCapture(binding.root.context)

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(binding.root.context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    binding.btnTakePicture.isEnabled = true

                    outputFileResults.savedUri?.let {saved ->
                        if (isWaterMark){
                            val converBitmap = convertPathToBitmap(saved, binding.root.context)
                            if (converBitmap == null) {
                                onResult.onResulError("Failed decode file")
                                return@let
                            }
                            val bitmap = drawMultilineTextToBitmap(
                                binding.root.context,
                                converBitmap,
                                setLocation(mLatitude, mLongitude).toString(),
                                12
                            )
                            saveBitmap(saved.toFile().absolutePath, bitmap) {
                                if (!it){
                                    Toast.makeText(binding.root.context, "Photo save failed", Toast.LENGTH_SHORT).show()
                                    return@saveBitmap
                                }
                                onResult.onFileResult(saved)
                            }
                        }else{
                            onResult.onFileResult(saved)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.btnTakePicture.isEnabled = true
                    onResult.onResulError(exception.message.toString())
                }

            }
        )
    }

    private fun playCapture(context: Context) {
        mMedia.reset()
        val file = context.assets.openFd("sound_capture.mp3")

        mMedia.setDataSource(file.fileDescriptor, file.startOffset, file.length)
        mMedia.setVolume(0.1F, 0.1F)
        mMedia.prepare()
        mMedia.start()
    }

    private fun takeVideo(){

        binding.btnTakeVideo.isEnabled = false

        binding.btnFlashCamera.visibility = View.GONE
        binding.btnSwitchCamera.visibility = View.GONE
        binding.btnGallery.visibility = View.GONE

        val curRecording = recording
        if (curRecording != null){
            curRecording.stop()
            stopRecording()
            recording = null
            return
        }
        startRecording()
        val videoFolder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
            ), getString(R.string.app_name)
        )
        if (!videoFolder.exists()) {
            videoFolder.mkdir()
        }

        val fileName = "VID_" + SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis()) + ".mp4"

//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
//            put(MediaStore.Images.Media.MIME_TYPE,"video/mp4")
//        }

//        val outputOption = FileOutputOptions
//            .Builder(binding.root.context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()

        val videoFile = File(videoFolder, fileName)
        val fileOutputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture.output
            .prepareRecording(binding.root.context, fileOutputOptions)
            .apply {
                if (ActivityCompat.checkSelfPermission(
                        binding.root.context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(binding.root.context)){ recordEvent->
                when(recordEvent){
                    is VideoRecordEvent.Start -> {
                        binding.btnTakeVideo.setImageResource(R.drawable.ic_xml_record_stop)
                        binding.btnTakeVideo.isEnabled = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()){
                            onResult.onFileResult(recordEvent.outputResults.outputUri)
                        }else{
                            recording?.close()
                            recording = null
                            onResult.onResulError(recordEvent.error.toString())
                        }
                        binding.btnTakeVideo.setImageResource(R.drawable.ic_xml_record_start)
                        binding.btnTakeVideo.isEnabled = true

                        binding.btnFlashCamera.visibility = View.VISIBLE
                        binding.btnSwitchCamera.visibility = View.VISIBLE
                        binding.btnGallery.visibility = View.VISIBLE
                    }
                }
            }

    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        if (recording != null){
            recording?.stop()
            takeVideo()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimer = object : Runnable{
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime() - binding.recodingTimer.base
            val timeString = currentTime.toFormattedTime()
            binding.recodingTimer.text = timeString
            handler.postDelayed(this,1000)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun Long.toFormattedTime():String{
        val seconds = ((this / 1000) % 60).toInt()
        val minutes = ((this / (1000 * 60)) % 60).toInt()
        val hours = ((this / (1000 * 60 * 60)) % 24).toInt()

        return if (hours >0){
            String.format("%02d:%02d:%02d",hours,minutes,seconds)
        }else{
            String.format("%02d:%02d",minutes,seconds)
        }
    }

    private fun startRecording(){
        binding.recodingTimer.visibility = View.VISIBLE
        binding.recodingTimer.base = SystemClock.elapsedRealtime()
        binding.recodingTimer.start()
        handler.post(updateTimer)
    }

    private fun stopRecording(){
        binding.recodingTimer.visibility = View.GONE
        binding.recodingTimer.stop()
        handler.removeCallbacks(updateTimer)
    }

    private fun setLocation(latitude: Double, longitude: Double): String? {
        val address = getAddressFromGPS(binding.root.context, latitude, longitude)
        if (address != null) {
            var text = ""
            text += address.address
            text += "\n"
            text += "Lat : ${address.latitude}, Lon : ${address.longitude}"
            text += "\n"
            text += address.timeStamp
            return text
        }
        return null
    }
}
