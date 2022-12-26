package com.example.compress

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * @author Perry Lance
 * @since 2022-12-23 Created
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val profileImageCrop = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val imageFile = createTempFileFromUri(uri) ?: return@launch
                    val compressedImage = Compressor.compress(this@MainActivity, imageFile)

                    val beforeSize = roundOffDecimal(imageFile.sizeInMb)
                    val afterSize = roundOffDecimal(compressedImage.sizeInMb)

                    binding.fileSize.text = "Before size: $beforeSize MB\nAfter size: $afterSize MB"

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

    private fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}