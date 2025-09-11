package com.minhtu.firesocialmedia.data.dto.home

import com.minhtu.firesocialmedia.data.dto.news.NewsDTO
import kotlinx.serialization.Serializable

@Serializable
data class LatestNewsDTO(
    val news: List<NewsDTO>? = null,
    val lastTimePostedValue: Double? = null,
    val lastKeyValue: String? = null
)