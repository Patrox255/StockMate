package com.example.stockmate.data.remote.img

interface SearchableImage {
    val id: String
    val previewUrl: String
    val fullUrl: String
    val description: String
}

interface SearchableImageSearchResult<T : SearchableImage> {
    val total: Int
    val pageCount: Int
    val pageSize: Int
    val elements: List<T>
}