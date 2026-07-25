package com.example.stockmate.data.util

import com.example.stockmate.data.util.img.ImageStorage
import javax.inject.Inject

class FormImageTracker @Inject constructor(
    private val imageStorage: ImageStorage,
) {
    // --- These variables are intended to help with image management when choosing an image in the form.
    // They are used to track the initial image path, whether the form data has been saved,
    // and which images need to be deleted when the form data is saved, as every image upon selection is
    // automatically saved on the device.
    private var initialImagePath: String? = null
    private var currentImagePath: String? = null
    private var isSaved = false
    private val imagesToDelete = mutableListOf<String>()
    /// ---

    fun init(originalPath: String?) {
        initialImagePath = originalPath
        currentImagePath = originalPath
    }

    fun onImageChanged(newPath: String?) {
        val previousPath = currentImagePath
        if (previousPath != null && previousPath != initialImagePath) {
            imagesToDelete.add(previousPath)
        }
        currentImagePath = newPath
    }

    fun markAsSaved() {
        isSaved = true
    }

    // It only deletes either the current image or the initial image when they are different from
    // the original image that was existent when the form was opened. This is because in edit
    // mode such image has already been saved in the device therefore it should not be deleted.
    // In add mode on the other hand the initial image is always null, so it will always delete
    // the current image if it is not null and desired.
    fun cleanUp() {
        imagesToDelete.forEach { imageStorage.deleteImage(it) }
        imagesToDelete.clear()

        if (isSaved) {
            if (initialImagePath != null && initialImagePath != currentImagePath) {
                imageStorage.deleteImage(initialImagePath!!)
            }
        } else {
            if (currentImagePath != null && currentImagePath != initialImagePath) {
                imageStorage.deleteImage(currentImagePath!!)
            }
        }
    }
}