package com.example.compress

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.compress.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import org.apache.commons.io.FileUtils
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val profileImageCrop = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            //val imagePath = result.getUriFilePath(this)!!
            binding.image.setImageURI(result.uriContent)

            if (result.uriContent == null) {
                return@registerForActivityResult
                //show error invalid file
            }

            lifecycleScope.launch(Dispatchers.Main) {

                val imageFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createTempFileFromUri(name = "temp", uri = result.uriContent!!)
                } else {
                    File(result.getUriFilePath(this@MainActivity).toString())
                }

                if(imageFile == null){
                    return@launch
                    //show error
                }

                val compressedImagePath = Compress.with(this@MainActivity, imageFile)
                    .setQuality(80)
                    .concrete {
                        withMaxWidth(100f)
                        withMaxHeight(100f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)

                GlideApp.with(this@MainActivity)
                    .load(compressedImagePath)
                    .into(binding.image)
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

    private suspend fun createTempFileFromUri(
        uri: Uri,
        name: String,
    ): File? {
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