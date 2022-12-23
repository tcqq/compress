package com.example.compress

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val profileImageCrop = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val imagePath = result.getUriFilePath(this)!!
            binding.image.setImageURI(result.uriContent)

            // FIXME: compress image
/*            lifecycleScope.launch(Dispatchers.Main) {
                val compressedImagePath = Compress.with(this@MainActivity, imagePath)
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
            }*/
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
}