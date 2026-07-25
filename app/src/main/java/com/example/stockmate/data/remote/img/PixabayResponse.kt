package com.example.stockmate.data.remote.img

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

class PixabaySearchResultImgSearch(val raw: PixabayImage): SearchableImage {
    override val id: String
        get() = raw.id.toString()
    override val previewUrl: String
        get() = raw.previewURL
    override val fullUrl: String
        get() = raw.largeImageURL
    override val description: String
        get() = raw.tags
}

class PixabaySearchResponseImgSearch(
    val raw: PixabayResponse,
    val requestedPageSize: Int
): SearchableImageSearchResult<PixabaySearchResultImgSearch> {
    override val total: Int
        get() = raw.totalHits
    override val pageCount: Int
        get() = (total + pageSize - 1) / pageSize
    override val pageSize: Int
        get() = requestedPageSize
    override val elements: List<PixabaySearchResultImgSearch>
        get() = raw.hits.map { PixabaySearchResultImgSearch(it) }
}