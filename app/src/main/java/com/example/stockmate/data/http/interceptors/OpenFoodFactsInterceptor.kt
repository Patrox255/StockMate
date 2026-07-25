package com.example.stockmate.data.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class OpenFoodFactsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", "StockMate - Android - Version 1.0 - https://github.com/Patrox255/StockMate")
            .build()

        return chain.proceed(request)
    }
}