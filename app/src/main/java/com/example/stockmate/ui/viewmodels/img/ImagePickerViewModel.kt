package com.example.stockmate.ui.viewmodels.img

import androidx.lifecycle.ViewModel
import com.example.stockmate.data.util.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val imageStorage: ImageStorage
): ViewModel() {
    fun getImgAbsolutePathBasedOnDeviceStorage(currentPath: String): String {
        return imageStorage.getFile(currentPath).absolutePath
    }
}