package com.mnaufalhamdani.takepickfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.mnaufalhamdani.takepickfile.databinding.ActivityFileBinding
import com.mnaufalhamdani.takepickfile.utils.FileResult
import java.io.File

class FileActivity : AppCompatActivity(), FileResult {
    private lateinit var binding: ActivityFileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val host = supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        val navController = host.navController
        navController.setGraph(R.navigation.nav_graph, intent.extras)
    }

    override fun onFileResult(uri: Uri) {
        val intent = Intent()
        intent.data = uri
        intent.putExtra(TakePickFile.EXTRA_FILE_PATH, uri.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResultCancel() {
        setResult(Activity.RESULT_CANCELED, getCancelledIntent())
        finish()
    }

    override fun onResulError(msg: String) {
        val intent = Intent()
        intent.putExtra(TakePickFile.EXTRA_ERROR, msg)
        setResult(TakePickFile.RESULT_ERROR, intent)
        finish()
    }

    private fun getCancelledIntent(): Intent {
        val intent = Intent()
        val message = "Unknown Error"
        intent.putExtra(TakePickFile.EXTRA_ERROR, message)
        return intent
    }
}