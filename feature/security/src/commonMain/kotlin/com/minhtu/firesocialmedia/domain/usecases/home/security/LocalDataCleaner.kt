package com.minhtu.firesocialmedia.domain.usecases.home.security

fun interface LocalDataCleaner {
    suspend fun clear()
}
