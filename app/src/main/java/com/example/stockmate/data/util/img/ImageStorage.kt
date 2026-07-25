package com.example.stockmate.data.util.img

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

private const val PRODUCT_IMG_PREFIX = "product_"

class ImageStorage (
    private val context: Context
) {
    private companion object {
        const val DEFAULT_MAX_WIDTH = 1024
        const val DEFAULT_MAX_HEIGHT = 1024
        const val JPEG_QUALITY = 80
    }

    fun getFile(fileName: String): File = File(context.filesDir, fileName)
    fun getUri(fileName: String): Uri = getFile(fileName).toUri()
    suspend fun saveImageToInternalStorage(
        uri: Uri,
        reqWidth: Int = DEFAULT_MAX_WIDTH,
        reqHeight: Int = DEFAULT_MAX_HEIGHT
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = decodeScaledBitmap(uri, reqWidth, reqHeight) ?: return@withContext null
                saveCompressedBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    suspend fun downloadImageFromUrlToInternalStorage(
        imageUrl: String,
        reqWidth: Int = DEFAULT_MAX_WIDTH,
        reqHeight: Int = DEFAULT_MAX_HEIGHT
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = URL(imageUrl)
                    .openStream()
                    .use {it.readBytes()}

                val bitmap = decodeScaledBitmap(bytes, reqWidth, reqHeight) ?: return@withContext null
                saveCompressedBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun decodeScaledBitmap(
        imageBytes: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

        options.inSampleSize =
            calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false

        return BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size,
            options
        )
    }

    private fun decodeScaledBitmap(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val resolver = context.contentResolver

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize =
            calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false

        return resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun saveCompressedBitmap(bitmap: Bitmap): String {
        val fileName = createImgFileName("jpg")
        val file = getFile(fileName)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        }

        return fileName
    }

    fun deleteImage(fileName: String) {
        try {
            val file = getFile(fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createImgFileName(extension: String): String {
        val fileName = "${PRODUCT_IMG_PREFIX}${UUID.randomUUID()}.${extension}"
        return fileName
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun getAllSavedProductImageNames(): List<String> {
        return context.filesDir.listFiles { file ->
            file.isFile && file.name.startsWith(PRODUCT_IMG_PREFIX)
        }?.map { it.name } ?: emptyList()
    }

}

