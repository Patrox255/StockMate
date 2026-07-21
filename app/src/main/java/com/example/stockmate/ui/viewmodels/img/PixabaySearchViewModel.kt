package com.example.stockmate.ui.viewmodels.img

import com.example.stockmate.data.http.PIXABAY_DEFAULT_PAGE_SIZE
import com.example.stockmate.data.remote.img.PixabaySearchResponseImgSearch
import com.example.stockmate.data.remote.img.PixabaySearchResultImgSearch
import com.example.stockmate.data.repository.PixabayImageRepository
import com.example.stockmate.data.util.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PixabaySearchViewModel @Inject constructor(
    private val repository: PixabayImageRepository,
    private val imageStorage: ImageStorage
) :  BaseImageSearchViewModel<PixabaySearchResultImgSearch, PixabaySearchResponseImgSearch>(
    imageStorage,
    defaultPageSize = PIXABAY_DEFAULT_PAGE_SIZE,
    availablePageSizes = listOf(10, 20, 50, 100)
) {
    override suspend fun performSearch(query: String, page: Int, pageSize: Int): PixabaySearchResponseImgSearch {
        return repository.search(query, page, pageSize).fold(
            onSuccess = { PixabaySearchResponseImgSearch(it, pageSize) },
            onFailure = { throw it }
        )
    }
}