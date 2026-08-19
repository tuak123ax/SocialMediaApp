package com.minhtu.firesocialmedia.android.service.serviceimpl.remoteconfig

interface FetchResultCallback {
    fun fetchSuccess(minVersion : String)
    fun fetchFail()
}