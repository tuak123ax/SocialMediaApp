package com.minhtu.firesocialmedia.domain.serviceimpl.remoteconfig

interface FetchResultCallback {
    fun fetchSuccess(minVersion : String)
    fun fetchFail()
}