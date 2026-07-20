package com.example.stockmate.data.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID
import javax.inject.Inject

class ImageStorage (
    private val context: Context
) {
    fun getFile(fileName: String): File = File(context.filesDir, fileName)
    fun getUri(fileName: String): Uri = getFile(fileName).toUri()
    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return null

            val mimeType = resolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val fileName = createImgFileName(extension)
            val file = getFile(fileName)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun downloadImageFromUrlToInternalStorage(imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()

                val extension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)?.takeIf { it.isNotEmpty() } ?: "jpg"
                val fileName = createImgFileName(extension)
                val file = getFile(fileName)
                val outputStream = FileOutputStream(file)

                inputStream.copyTo(outputStream)

                inputStream.close()
                outputStream.close()

                fileName
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun createImgFileName(extension: String): String {
        val fileName = "product_${UUID.randomUUID()}.${extension}"
        return fileName
    }
}

