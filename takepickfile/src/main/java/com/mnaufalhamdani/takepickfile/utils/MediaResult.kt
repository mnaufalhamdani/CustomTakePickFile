package com.mnaufalhamdani.takepickfile.utils

import android.net.Uri

interface FileResult {
    fun onFileResult(uri: Uri)
    fun onResultCancel()
    fun onResulError(msg: String)
}