package com.example.stockmate.ui.viewmodels.img

import com.example.stockmate.data.http.OFF_DEFAULT_PAGE_SIZE
import com.example.stockmate.data.remote.img.OpenFoodFactsImgSearchResponseImgSearch
import com.example.stockmate.data.remote.img.OpenFoodFactsImgSearchResultImgSearch
import com.example.stockmate.data.repository.OpenFoodFactsRepository
import com.example.stockmate.data.util.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OpenFoodFactsViewModel @Inject constructor(
    private val repository: OpenFoodFactsRepository,
    private val imageStorage: ImageStorage
): BaseImageSearchViewModel<OpenFoodFactsImgSearchResultImgSearch, OpenFoodFactsImgSearchResponseImgSearch>(
    imageStorage,
    defaultPageSize = OFF_DEFAULT_PAGE_SIZE,
    availablePageSizes = listOf(10, 20, 50, 100)
) {
    override suspend fun performSearch(query: String, page: Int, pageSize: Int): OpenFoodFactsImgSearchResponseImgSearch {
        return repository.searchProductsByName(query, page, pageSize).fold(
            onSuccess = { OpenFoodFactsImgSearchResponseImgSearch(it, pageSize) },
            onFailure = { throw it }
        )
    }
}