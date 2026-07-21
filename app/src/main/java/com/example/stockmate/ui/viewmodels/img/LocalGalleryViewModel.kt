package com.example.stockmate.ui.viewmodels.img

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.util.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalGalleryViewModel @Inject constructor(
    private val imageStorage: ImageStorage
): ViewModel() {
    fun saveImage(
        uri: Uri,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            onResult(imageStorage.saveImageToInternalStorage(uri))
        }
    }
}