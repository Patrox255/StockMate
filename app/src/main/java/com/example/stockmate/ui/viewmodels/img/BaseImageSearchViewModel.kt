package com.example.stockmate.ui.viewmodels.img

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.exceptions.NoProductsFoundException
import com.example.stockmate.data.remote.img.SearchableImage
import com.example.stockmate.data.remote.img.SearchableImageSearchResult
import com.example.stockmate.data.util.img.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseImageSearchViewModel<T : SearchableImage, Y : SearchableImageSearchResult<T>>(
    private val imageStorage: ImageStorage,
    defaultPageSize: Int,
    val availablePageSizes: List<Int> = listOf(10, 20, 50)
) : ViewModel() {

    private var currentQuery: String = ""
    private var currentPage: Int = 1

    private val _pageSize = MutableStateFlow(defaultPageSize)
    val pageSize: StateFlow<Int> = _pageSize

    private val _results = MutableStateFlow<List<T>>(emptyList())
    val results: StateFlow<List<T>> = _results
    private val _hasMorePages = MutableStateFlow(false)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages
    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage

    private val _imagesLoadingError = MutableStateFlow<String?>(null)
    val imagesLoadingError: StateFlow<String?> = _imagesLoadingError

    private val _imagesLoading = MutableStateFlow(false)
    val imagesLoading: StateFlow<Boolean> = _imagesLoading

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadingImageError = MutableStateFlow<String?>(null)
    val downloadingImageError: StateFlow<String?> = _downloadingImageError

    private val _selectedImage = MutableStateFlow<T?>(null)
    val selectedImage: StateFlow<T?> = _selectedImage

    protected abstract suspend fun performSearch(query: String, page: Int, pageSize: Int): Y

    fun searchImages(query: String) {
        if (query.isBlank())
        {
            _imagesLoadingError.value = "Search query cannot be empty."
            return
        }
        currentQuery = query
        currentPage = 1

        viewModelScope.launch {
            _imagesLoading.value = true
            _imagesLoadingError.value = null
            _results.value = emptyList()
            _hasMorePages.value = false
            try {
                applyResponse(performSearch(query, currentPage, _pageSize.value), append = false)
            } catch (e: NoProductsFoundException) {
                _imagesLoadingError.value = e.message
            } catch (e: Exception) {
                e.printStackTrace()
                _imagesLoadingError.value =
                    "Failed to load images. Please check the provided query and network connection and try again."
            } finally {
                _imagesLoading.value = false
            }
        }
    }

    fun updatePageSize(newSize: Int) {
        if (newSize == _pageSize.value) return
        _pageSize.value = newSize
        if (currentQuery.isNotBlank())
            searchImages(currentQuery)
    }

    fun loadNextPage() {
        if (_isLoadingNextPage.value || _imagesLoading.value || !_hasMorePages.value || currentQuery.isBlank()) return

        viewModelScope.launch {
            _isLoadingNextPage.value = true
            val nextPage = currentPage + 1
            try {
                val response = performSearch(currentQuery, nextPage, _pageSize.value)
                currentPage = nextPage
                applyResponse(response, append = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _imagesLoadingError.value =
                    "Failed to load more images."
            } finally {
                _isLoadingNextPage.value = false
            }
        }
    }

    private fun applyResponse(response: Y, append: Boolean) {
        _results.value = if (append) {
            _results.value + response.elements
        } else {
            response.elements
        }
        _hasMorePages.value = response.elements.isNotEmpty() && currentPage < response.pageCount
    }

    fun downloadSelectedImage(image: T, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadingImageError.value = null

            val localFileName = imageStorage.downloadImageFromUrlToInternalStorage(image.fullUrl)
            _isDownloading.value = false

            if (localFileName != null) {
                _selectedImage.value = image
                onSuccess(localFileName)
            } else {
                _downloadingImageError.value = "Failed to download the image. Please try again."
            }
        }
    }

    fun updateSelectedImage(image: T?) {
        _selectedImage.value = image
    }
}