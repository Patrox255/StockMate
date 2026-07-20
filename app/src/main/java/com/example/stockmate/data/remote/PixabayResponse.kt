package com.example.stockmate.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>
)

@Serializable
data class PixabayImage(
    val id: Int,
    val pageURL: String,
    val tags: String,
    val previewURL: String,
    val largeImageURL: String,
)