package com.mnaufalhamdani.takepickfile.utils

import android.net.Uri
import java.io.File

interface FileResult {
    fun onFileResult(uri: Uri)
    fun onResultCancel()
    fun onResulError(msg: String)
}