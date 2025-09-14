package com.minhtu.firesocialmedia.domain.entity.home

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

data class LatestNewsResult(
    val news: List<NewsInstance>? = null,
    val lastTimePostedValue: Double? = null,
    val lastKeyValue: String? = null
)