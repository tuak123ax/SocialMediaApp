package com.minhtu.firesocialmedia.domain.repository

interface ShowImageRepository {
    suspend fun downloadImage(image: String, fileName: String) : Boolean
}