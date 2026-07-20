package com.example.stockmate.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PixabayApiIdentifier

@Qualifier
@Retention
annotation class PixabayClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenFoodFactsApiIdentifier