package com.minhtu.firesocialmedia.domain.repository

interface LocalRepository {
    suspend fun getFCMToken() : String
}