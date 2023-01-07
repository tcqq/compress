package com.example.compress

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.math.log2
import kotlin.math.pow

/**
 * @author Nilesh Rathod
 * @since 2022-11-05 Created
 */
suspend fun Context.createTempFileFromUri(uri: Uri): File? {
    return withContext(Dispatchers.IO) {
        try {
            val stream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, uri.getName(this@createTempFileFromUri))
            FileUtils.copyInputStreamToFile(stream, file)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun Uri.getName(context: Context): String {
    val returnCursor = context.contentResolver.query(this, null, null, null, null)!!
    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val fileName = returnCursor.getString(nameIndex)
    returnCursor.close()
    return fileName
}

val File.formatSize: String
    get() = length().formatAsFileSize

val Int.formatAsFileSize: String
    get() = toLong().formatAsFileSize

val Long.formatAsFileSize: String
    get() = log2(if (this != 0L) toDouble() else 1.0).toInt().div(10).let {
        val precision = when (it) {
            0 -> 0; 1 -> 1; else -> 2
        }
        val prefix = arrayOf("", "K", "M", "G", "T", "P", "E", "Z", "Y")
        String.format("%.${precision}f ${prefix[it]}B", toDouble() / 2.0.pow(it * 10.0))
    }