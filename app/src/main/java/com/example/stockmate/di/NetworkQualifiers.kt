package com.example.stockmate.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PixabayApiIdentifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PixabayClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenFoodFactsApiIdentifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenFoodFactsClient