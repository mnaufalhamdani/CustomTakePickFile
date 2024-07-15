package com.mnaufalhamdani.takepickfile.utils

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
    arrayListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
} else {
    arrayListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
}

// TO PREVENT DOUBLE CLICK
private var mLastClickTime: Long = 0
fun singleClick(): Boolean {
    // mis-clicking prevention, using threshold of 1000 ms
    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
        return false
    }
    mLastClickTime = SystemClock.elapsedRealtime()
    return true
}

fun getAddressFromGPS(context: Context, latitude: Double, longitude: Double, ): AddressDomain? {
    val fileNameFormat = "dd-MM-yyyy HH:mm:ss"
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if (!addresses.isNullOrEmpty()) {
            return AddressDomain(
                address = addresses[0].getAddressLine(0),
                latitude = latitude.toString(),
                longitude = longitude.toString(),
                timeStamp = SimpleDateFormat(
                    fileNameFormat,
                    Locale.getDefault()
                ).format(System.currentTimeMillis())
            )
        }else return null
    }catch (e: Exception){
        Log.e("getAddressFromGPS", e.message.toString())
        return AddressDomain(
            address = "Alamat tidak diketahui, mohon muat ulang dan pastikan koneksi internet Anda stabil.",
            latitude = "-",
            longitude = "-",
            timeStamp = SimpleDateFormat(
                fileNameFormat,
                Locale.US
            ).format(System.currentTimeMillis())
        )
    }
}

fun drawMultilineTextToBitmap(context: Context, resId: Bitmap, waterMark: String?, textSize: Int?): Bitmap {
    //set TextSize
    var mSize = textSize
    if (mSize == null) mSize = 12

    // prepare canvas
    val resources = context.resources
    val scale = resources.displayMetrics.density
    var bitmap = resId
    var bitmapConfig = bitmap.config
    // set default bitmap config if none
    if (bitmapConfig == null) {
        bitmapConfig = Bitmap.Config.ARGB_8888
    }

    // resource bitmaps are imutable,
    // so we need to convert it to mutable one
    bitmap = bitmap.copy(bitmapConfig, true)
    val canvas = Canvas(bitmap)

    // new antialiased Paint
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    // text color - #3D3D3D
    paint.color = Color.WHITE
    // text size in pixels
    paint.textSize = mSize * scale * 2

    // set text width to canvas width minus 16dp padding
    val textWidth = canvas.width - (16 * scale).toInt()
    // init StaticLayout for text
    val textLayout = StaticLayout(
        waterMark, paint, textWidth, Layout.Alignment.ALIGN_NORMAL,
        1.0f, 2.0f, false
    )

    // get height of multiline text
    val textHeight = textLayout.height
    // get position of text's top left corner
    val x = (bitmap.width - textWidth) / 2
    val y = (bitmap.height - textHeight) * 98 / 100

    // draw text to the Canvas center
    canvas.save()
    canvas.translate(x.toFloat(), y.toFloat())
    textLayout.draw(canvas)
    canvas.restore()
    return bitmap
}

fun convertPathToBitmap(imagePath: Uri, context: Context): Bitmap? {
    try {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imagePath)
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context.contentResolver, imagePath)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }
    }catch (e: Exception){
        Log.d("convertPathToBitmap", e.message.toString())
        return null
    }
}

fun saveBitmap(path: String, bitmap: Bitmap, listener: (Boolean) -> Unit) {
    try {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }

        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        out.flush()
        out.close()
    } catch (e: IOException) {
        listener(false)
        e.printStackTrace()
    } catch (e: OutOfMemoryError) {
        listener(false)
        e.printStackTrace()
    } catch (e: java.lang.Exception) {
        listener(false)
        e.printStackTrace()
    } finally {
        listener(true)
    }
}