package com.example.stockmate.data.http

import com.example.stockmate.data.remote.img.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

const val PIXABAY_DEFAULT_PAGE_SIZE = 20

interface PixabayApi {
    @GET("api/")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = PIXABAY_DEFAULT_PAGE_SIZE,
        @Query("image_type") imageType: String = "photo",
    ): PixabayResponse
}