package com.example.stockmate.di

import com.example.stockmate.data.http.OpenFoodFactsApi
import com.example.stockmate.data.http.interceptors.OpenFoodFactsInterceptor
import com.example.stockmate.data.repository.OpenFoodFactsRepository
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
object OpenFoodFactsModule {
    @Provides
    @Singleton
    fun provideInterceptor(): OpenFoodFactsInterceptor = OpenFoodFactsInterceptor()

    @Provides
    @Singleton
    @OpenFoodFactsClient
    fun provideOpenFoodFactsOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        openFoodFactsInterceptor: OpenFoodFactsInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(openFoodFactsInterceptor)
        .build()

    @Provides
    @Singleton
    @OpenFoodFactsApiIdentifier
    fun provideOpenFoodFactsRetrofit(
        @OpenFoodFactsClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit
        .Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType = "application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(
        @OpenFoodFactsApiIdentifier retrofit: Retrofit
    ): OpenFoodFactsApi = retrofit.create(OpenFoodFactsApi::class.java)

    @Provides
    @Singleton
    fun provideOpenFoodFactsRepository(
        openFoodFactsApi: OpenFoodFactsApi
    ): OpenFoodFactsRepository = OpenFoodFactsRepository(openFoodFactsApi)
}