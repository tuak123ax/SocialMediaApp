package com.minhtu.firesocialmedia.core.domain.repository

interface ShowImageRepository {
    suspend fun downloadImage(image: String, fileName: String) : Boolean
}