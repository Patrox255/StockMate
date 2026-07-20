package com.example.stockmate.data.repository

import com.example.stockmate.data.http.PixabayApi
import com.example.stockmate.data.remote.PixabayImage
import com.example.stockmate.data.remote.PixabayResponse

class PixabayImageRepository(
    private val api: PixabayApi
) {
    suspend fun search(query: String): PixabayResponse {
        return api.searchImages(
            query = query
        )
    }
}