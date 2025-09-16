package com.minhtu.firesocialmedia.data.mapper.home

import com.minhtu.firesocialmedia.data.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.mapper.news.toDomain
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult

fun LatestNewsDTO.toDomain() : LatestNewsResult?{
    val listNews = news?.map { it.toDomain() }
    return if(news != null && lastTimePostedValue != null && lastKeyValue != null) {
        LatestNewsResult(listNews, lastTimePostedValue, lastKeyValue)
    } else{
        null
    }
}