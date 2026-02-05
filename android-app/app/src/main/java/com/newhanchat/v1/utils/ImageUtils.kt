package com.newhanchat.v1.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun compressImage(context: Context, imageUri: Uri): File? {
        return try {
            // 1. Calculate Scale Factor (Downscaling)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Target max dimension (e.g., 1024px width or height)
            val REQUIRED_SIZE = 1024
            var scale = 1
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                options.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }

            // 2. Load the Downscaled Bitmap
            val finalOptions = BitmapFactory.Options()
            finalOptions.inSampleSize = scale
            val inputStream2 = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, finalOptions)
            inputStream2?.close()

            if (bitmap == null) return null

            // 3. Compress to JPEG (Quality 70)
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)

            // Adjust quality (0-100). 70 is a good balance.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}