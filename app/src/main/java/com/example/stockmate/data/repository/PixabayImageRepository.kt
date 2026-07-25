package com.example.stockmate.data.repository

import com.example.stockmate.data.exceptions.NoProductsFoundException
import com.example.stockmate.data.http.PixabayApi
import com.example.stockmate.data.remote.img.PixabayResponse

class PixabayImageRepository(
    private val api: PixabayApi
) {
    suspend fun search(
        query: String,
        page: Int,
        perPage: Int
    ): Result<PixabayResponse> {
        try {
            val response = api.searchImages(
                query = query,
                page = page,
                perPage = perPage
            )
            if (response.hits.isEmpty() && page == 1) {
                return Result.failure(NoProductsFoundException("No images found for query: $query"))
            } else {
                return Result.success(response)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}