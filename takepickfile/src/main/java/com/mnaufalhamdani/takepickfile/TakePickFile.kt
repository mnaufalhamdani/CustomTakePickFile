@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.takepickfile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mnaufalhamdani.takepickfile.utils.singleClick

open class TakePickFile {
    companion object {
        // Default Request Code to Pick Image
        const val RESULT_ERROR = 404

        internal const val EXTRA_FILE_PATH = "extra.file_path"
        const val EXTRA_ERROR = "extra.error"

        internal const val EXTRA_TYPE_MEDIA = "EXTRA_TYPE_MEDIA"
        internal const val EXTRA_LENS_CAMERA = "EXTRA_LENS_CAMERA"
        internal const val EXTRA_LINE_OF_ID = "EXTRA_LINE_OF_ID"
        internal const val EXTRA_CAMERA_ONLY = "EXTRA_CAMERA_ONLY"
        internal const val EXTRA_FRONT_CAMERA_ONLY = "EXTRA_FRONT_CAMERA_ONLY"
        internal const val EXTRA_MAX_DURATION = "EXTRA_MAX_DURATION"

        //use for Face Camera
        internal const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        internal const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
        internal const val EXTRA_IS_FACE_DETECTION = "EXTRA_IS_FACE_DETECTION"
        internal const val EXTRA_IS_WATERMARK = "EXTRA_IS_WATERMARK"

        /**
         * Use this to use CaptureCameraX in Activity Class
         *
         * @param activity Activity Instance
         */
        @JvmStatic
        fun with(activity: Activity): Builder {
            return Builder(activity)
        }

        /**
         * Use this to use CaptureCameraX in Fragment Class
         *
         * @param fragment Fragment Instance
         */
        @JvmStatic
        fun with(fragment: Fragment): Builder {
            return Builder(fragment)
        }
    }

    enum class TypeMedia(val value: Int) {
        PHOTO(0), VIDEO(1)
    }

    enum class LensCamera(val value: Int) {
        LENS_FRONT_CAMERA(0), LENS_BACK_CAMERA(1)
    }

    class Builder(private val activity: Activity) {

        private var fragment: Fragment? = null
        private var typeMedia: TypeMedia = TypeMedia.PHOTO
        private var lensFacing: LensCamera = LensCamera.LENS_BACK_CAMERA
        private var showLineOfId: Boolean = false
        private var cameraOnly: Boolean = false
        private var frontCameraOnly: Boolean = false
        private var maxDuration: Long = 0
        private var latitude: Double = 0.0
        private var longitude: Double = 0.0
        private var isFaceDetection: Boolean = false
        private var isWaterMark: Boolean = false

        /**
         * Call this while picking image for fragment.
         */
        constructor(fragment: Fragment) : this(fragment.requireActivity()) {
            this.fragment = fragment
        }

        fun defaultCamera(lensCamera: LensCamera): Builder {
            this.lensFacing = lensCamera
            return this
        }

        fun typeMedia(typeMedia: TypeMedia): Builder {
            this.typeMedia = typeMedia
            return this
        }

        fun setLineOfId(showLineOfId: Boolean): Builder {
            this.showLineOfId = showLineOfId
            return this
        }

        fun cameraOnly(cameraOnly: Boolean): Builder {
            this.cameraOnly = cameraOnly
            return this
        }

        fun frontCameraOnly(frontCameraOnly: Boolean): Builder {
            this.frontCameraOnly = frontCameraOnly
            if (frontCameraOnly)
                this.lensFacing = LensCamera.LENS_FRONT_CAMERA
            return this
        }

        fun setMaxDuration(maxDuration: Long): Builder {
            this.maxDuration = maxDuration
            return this
        }

        fun coordinat(latitude: Double, longitude: Double): Builder {
            this.latitude = latitude
            this.longitude = longitude
            return this
        }

        fun isFaceDetection(isFaceDetection: Boolean): Builder {
            this.isFaceDetection = isFaceDetection
            return this
        }

        fun isWaterMark(isWaterMark: Boolean): Builder {
            this.isWaterMark = isWaterMark
            return this
        }

        fun start(reqCode: Int) {
            if (!singleClick()) return
            startActivity(reqCode)
        }

        private fun getBundle(): Bundle {
            return Bundle().apply {
                putInt(EXTRA_TYPE_MEDIA, typeMedia.value)
                putInt(EXTRA_LENS_CAMERA, lensFacing.value)
                putBoolean(EXTRA_LINE_OF_ID, showLineOfId)
                putBoolean(EXTRA_CAMERA_ONLY, cameraOnly)
                putBoolean(EXTRA_FRONT_CAMERA_ONLY, frontCameraOnly)
                putLong(EXTRA_MAX_DURATION, maxDuration)
                putBoolean(EXTRA_IS_FACE_DETECTION, isFaceDetection)
                putBoolean(EXTRA_IS_WATERMARK, isWaterMark)
                putDouble(EXTRA_LATITUDE, latitude)
                putDouble(EXTRA_LONGITUDE, longitude)
            }
        }

        /**
         * Start CaptureCameraX with given Argument
         */
        private fun startActivity(reqCode: Int) {
            val intent = Intent(activity, FileActivity::class.java)
            intent.putExtras(getBundle())
            if (fragment != null) {
                fragment?.startActivityForResult(intent, reqCode)
            } else {
                activity.startActivityForResult(intent, reqCode)
            }
        }
    }
}