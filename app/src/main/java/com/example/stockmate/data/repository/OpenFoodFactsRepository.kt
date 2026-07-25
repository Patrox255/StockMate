package com.example.stockmate.data.repository

import com.example.stockmate.data.exceptions.NoProductsFoundException
import com.example.stockmate.data.http.OpenFoodFactsApi
import com.example.stockmate.data.remote.img.OpenFoodFactsImgSearchResponse
import javax.inject.Inject

class OpenFoodFactsRepository @Inject constructor(
    private val api: OpenFoodFactsApi
) {
    suspend fun searchProductsByName(
        query: String,
        page: Int,
        pageSize: Int
    ): Result<OpenFoodFactsImgSearchResponse> {
        return try {
            var response = api.searchProductsByName(query, page = page, pageSize = pageSize)

            val productsWithImages = response.products.filter { product ->
                !product.getBestImageUrl().isNullOrEmpty()
            }
            val filteredResponse = response.copy(products = productsWithImages)

            if (productsWithImages.isEmpty() && page == 1) {
                Result.failure(NoProductsFoundException(query))
            } else {
                Result.success(filteredResponse)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}