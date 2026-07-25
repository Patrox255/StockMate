package com.example.stockmate.data.http

import com.example.stockmate.data.remote.img.OpenFoodFactsImgSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

const val OFF_DEFAULT_PAGE_SIZE = 20

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchProductsByName(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = OFF_DEFAULT_PAGE_SIZE,
        @Query("page") page: Int,
        @Query("fields") fields: String = "product_name,image_url,image_front_url"
    ): OpenFoodFactsImgSearchResponse
}