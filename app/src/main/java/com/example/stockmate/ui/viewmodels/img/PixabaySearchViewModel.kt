package com.example.stockmate.ui.viewmodels.img

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.remote.PixabayImage
import com.example.stockmate.data.repository.PixabayImageRepository
import com.example.stockmate.data.util.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PixabaySearchViewModel @Inject constructor(
    private val repository: PixabayImageRepository,
    private val imageStorage: ImageStorage
) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<PixabayImage>>(emptyList())
    private val _imagesLoadingError = MutableStateFlow<String?>(null)
    private val _imagesLoading = MutableStateFlow(false)
    private val _isDownloading = MutableStateFlow(false)
    private val _downloadingImageError = MutableStateFlow<String?>(null)
    private val _selectedImage = MutableStateFlow<PixabayImage?>(null)

    val searchResults: StateFlow<List<PixabayImage>> = _searchResults
    val imagesLoadingError: StateFlow<String?> = _imagesLoadingError
    val imagesLoading: StateFlow<Boolean> = _imagesLoading
    val isDownloading: StateFlow<Boolean> = _isDownloading
    val downloadingImageError: StateFlow<String?> = _downloadingImageError
    val selectedImage: StateFlow<PixabayImage?> = _selectedImage

    fun searchImages(query: String) {
        viewModelScope.launch {
            _imagesLoading.value = true
            _imagesLoadingError.value = null
            _searchResults.value = emptyList()
            try {
                val response = repository.search(query)
                _searchResults.value = response.hits
            } catch(e: Exception) {
                // TODO: Handle error appropriately (e.g., show a message to the user)
                e.printStackTrace()
                _imagesLoadingError.value = "Failed to load images. Please check the provided query and network connection and try again."
            } finally {
                _imagesLoading.value = false
            }
        }
    }

    fun downloadSelectedImage(selectedImage: PixabayImage): String? {
        var localFileName: String? = null

        viewModelScope.launch {
            _isDownloading.value = true
            _downloadingImageError.value = null

            localFileName =
                imageStorage.downloadImageFromUrlToInternalStorage(selectedImage.largeImageURL)
            _isDownloading.value = false

            if (localFileName != null) {
                _selectedImage.value = selectedImage
            } else {
                _downloadingImageError.value =
                    "Failed to download the image. Please try again."
            }
        }
        return localFileName
    }

    fun updateSelectedImage(image: PixabayImage?) {
        _selectedImage.value = image
    }
}