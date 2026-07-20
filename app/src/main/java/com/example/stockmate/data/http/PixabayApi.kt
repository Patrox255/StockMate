package com.example.stockmate.data.http

import com.example.stockmate.data.remote.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {
    @GET("api/")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
    ): PixabayResponse
}