package com.example.stockmate.data.http.interceptors

import com.example.stockmate.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class PixabayInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain.request()
            .url
            .newBuilder()
            .addQueryParameter("key", BuildConfig.PIXABAY_API_KEY)
            .build()

        val request = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}