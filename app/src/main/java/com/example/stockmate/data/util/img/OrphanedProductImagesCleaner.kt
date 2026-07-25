package com.example.stockmate.data.util.img

import com.example.stockmate.data.dao.ProductDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class OrphanedProductImagesCleaner @Inject constructor(
    private val productDao: ProductDao,
    private val imageStorage: ImageStorage
){
    suspend fun cleanUp() {
        withContext(Dispatchers.IO) {
            try {
                val dbImages: Set<String> = productDao.getAllUsedImagePaths().toSet()
                val diskImages: List<String> = imageStorage.getAllSavedProductImageNames()

                var deletedCount = 0
                diskImages.forEach { imageName ->
                    if (!dbImages.contains(imageName)) {
                        imageStorage.deleteImage(imageName)
                        deletedCount++
                    }
                }

                if (deletedCount > 0) {
                    println("OrphanedProductImagesCleaner: Deleted $deletedCount orphaned product images.")
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}