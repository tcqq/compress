package com.example.compress

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.compress.databinding.ActivityMainBinding
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val profileImageCrop = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            if (result.uriContent == null) {
                Toast.makeText(this@MainActivity, "Invalid file", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    val imageFile = createTempFileFromUri(uri = result.uriContent!!, name = "temp")
                        ?: return@launch
                    val compressedImage = Compressor.compress(this@MainActivity, imageFile)

                    GlideApp.with(this@MainActivity)
                        .load(compressedImage)
                        .into(binding.image)
                }
            }
        } else {
            Log.e(TAG, result.error?.message!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.image.setOnClickListener {
            profileImageCrop.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = cropImageOptions().copy(
                        aspectRatioX = 6,
                        aspectRatioY = 2,
                        fixAspectRatio = true,
                    )
                )
            )
        }
    }

    private fun cropImageOptions() = CropImageOptions(
        imageSourceIncludeCamera = true,
        imageSourceIncludeGallery = true,
        backgroundColor = Color.TRANSPARENT,
        allowRotation = true,
        cropMenuCropButtonIcon = R.drawable.baseline_check_24,
        activityTitle = "Adjust image",
        outputCompressFormat = Bitmap.CompressFormat.PNG
    )

    private suspend fun createTempFileFromUri(uri: Uri, name: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val ext = try {
                    uri.toString().substring(uri.toString().lastIndexOf("."))
                } catch (e: Exception) {
                    ".png"
                }
                val stream = contentResolver.openInputStream(uri)
                val file = File.createTempFile(name, ext, cacheDir)
                FileUtils.copyInputStreamToFile(
                    stream,
                    file
                )
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}