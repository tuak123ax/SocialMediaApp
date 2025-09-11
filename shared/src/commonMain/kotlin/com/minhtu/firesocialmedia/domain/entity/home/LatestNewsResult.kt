package com.minhtu.firesocialmedia.domain.entity.home

import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

data class LatestNewsResult(
    val news: List<NewsInstance>?,
    val lastTimePostedValue: Double?,
    val lastKeyValue: String
)