package com.minhtu.firesocialmedia.data.remote.mapper.home

import com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDomain
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult

fun LatestNewsDTO.toDomain() : LatestNewsResult?{
    val listNews = news?.map { it.toDomain() }
    return if(news != null && lastTimePostedValue != null && lastKeyValue != null) {
        LatestNewsResult(listNews, lastTimePostedValue, lastKeyValue)
    } else{
        null
    }
}