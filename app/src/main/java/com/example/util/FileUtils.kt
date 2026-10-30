package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedDocumentItem(
    val id: String,
    val title: String,
    val examCategory: String,
    val type: String, // "Photo", "Signature", "Thumb"
    val sizeKb: Float,
    val width: Int,
    val height: Int,
    val dateAdded: String,
    val uriString: String
)

object FileUtils {
    private const val TAG = "FileUtils"

    /**
     * Loads a Bitmap from an Android content Uri and rotates it according to EXIF orientation.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Read EXIF orientation
            val exifStream = context.contentResolver.openInputStream(uri)
            val orientation = if (exifStream != null) {
                val exif = ExifInterface(exifStream)
                val ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exifStream.close()
                ori
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URI: $uri", e)
            null
        }
    }

    /**
     * Saves processed bytes to device storage.
     * Uses MediaStore on Android 10+ (API 29+) which requires zero runtime permissions.
     */
    fun saveImageToStorage(
        context: Context,
        bytes: ByteArray,
        displayName: String,
        isPng: Boolean = false
    ): Uri? {
        val extension = if (isPng) "png" else "jpg"
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val fileName = "${displayName}_${System.currentTimeMillis()}.$extension"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ImageCompressCloud")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(bytes)
                        outputStream.flush()
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    uri
                } else null
            } else {
                // Fallback for Android 9 and below: save to Pictures directory
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(picturesDir, "ImageCompressCloud").apply { mkdirs() }
                val file = File(folder, fileName)
                FileOutputStream(file).use { out ->
                    out.write(bytes)
                    out.flush()
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving to MediaStore", e)
            // Internal cache fallback
            try {
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(bytes)
                    out.flush()
                }
                Uri.fromFile(file)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed cache fallback", ex)
                null
            }
        }
    }

    /**
     * Shares the image Uri using Android Share Sheet.
     */
    fun shareImage(context: Context, uri: Uri, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = context.contentResolver.getType(uri) ?: "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Exam File"))
    }

    fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}
