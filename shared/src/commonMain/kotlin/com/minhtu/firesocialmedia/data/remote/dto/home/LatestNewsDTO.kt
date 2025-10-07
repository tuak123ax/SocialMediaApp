package com.minhtu.firesocialmedia.data.remote.dto.home

import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import kotlinx.serialization.Serializable

@Serializable
data class LatestNewsDTO(
    val news: List<NewsDTO>? = null,
    val lastTimePostedValue: Double? = null,
    val lastKeyValue: String? = null
)