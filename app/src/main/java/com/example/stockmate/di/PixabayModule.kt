package com.example.stockmate.di

import com.example.stockmate.data.http.PixabayApi
import com.example.stockmate.data.http.interceptors.PixabayInterceptor
import com.example.stockmate.data.repository.PixabayImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PixabayModule {
    @Provides
    @Singleton
    fun providePixabayInterceptor(): PixabayInterceptor = PixabayInterceptor()

    @Provides
    @Singleton
    @PixabayClient
    fun provideOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        pixabayInterceptor: PixabayInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(pixabayInterceptor)
            .build()

    @Provides
    @Singleton
    @PixabayApiIdentifier
    fun providePixabayRetrofit(
        @PixabayClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://pixabay.com/")
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    contentType = "application/json".toMediaType()
                )
            )
            .build()

    @Provides
    @Singleton
    fun providePixabayApi(
        @PixabayApiIdentifier retrofit: Retrofit
    ): PixabayApi =
        retrofit.create(PixabayApi::class.java)

    @Provides
    @Singleton
    fun providePixabayImageRepository(
        pixabayApi: PixabayApi
    ) = PixabayImageRepository(
        api = pixabayApi
    )
}