package com.newhanchat.v1.data.model

data class PagedResponse<T>(
    val content: List<T>, // This is where the actual list of posts lives
    val pageable: Pageable? = null,
    val last: Boolean,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int, // Current page number
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

// Optional metadata class if Spring includes it
data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean
)