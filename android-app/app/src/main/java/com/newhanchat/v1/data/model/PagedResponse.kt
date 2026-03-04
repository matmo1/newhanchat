package com.newhanchat.v1.data.model

data class PagedResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean
)