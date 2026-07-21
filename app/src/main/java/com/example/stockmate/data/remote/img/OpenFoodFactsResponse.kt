package com.example.stockmate.data.remote.img

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsImgSearchResponse(
    val count: Int = 0,
    val page: Int = 1,
    @SerialName("page_count")
    val pageCount: Int = 0,
    @SerialName("page_size")
    val pageSize: Int = 0,
    val products: List<OffProduct> = emptyList()
)

@Serializable
data class OffProduct(
    val id: String = "",
    @SerialName("product_name")
    val productName: String = "Unknown Product",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("image_front_url")
    val imageFrontUrl: String? = null
) {
    fun getBestImageUrl(): String? {
        return imageUrl ?: imageFrontUrl
    }
}

class OpenFoodFactsImgSearchResultImgSearch(val raw: OffProduct): SearchableImage {
    override val id: String
        get() = raw.id
    override val previewUrl: String
        get() = raw.getBestImageUrl() ?: ""
    override val fullUrl: String
        get() = raw.getBestImageUrl() ?: ""
    override val description: String
        get() = raw.productName
}

class OpenFoodFactsImgSearchResponseImgSearch(
    val raw: OpenFoodFactsImgSearchResponse,
    val requestedPageSize: Int
): SearchableImageSearchResult<OpenFoodFactsImgSearchResultImgSearch> {
    override val total: Int
        get() = raw.count
    override val pageCount: Int
        get() = raw.pageCount.takeIf {it > 0}
            ?: ((total + pageSize - 1) / pageSize)
    override val pageSize: Int
        get() = requestedPageSize
    override val elements: List<OpenFoodFactsImgSearchResultImgSearch>
        get() = raw.products.map { OpenFoodFactsImgSearchResultImgSearch(it) }
}