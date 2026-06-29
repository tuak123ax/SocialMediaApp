package com.minhtu.firesocialmedia.domain.serviceimpl.notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class Client {

    companion object{
        private val retrofitCache = mutableMapOf<String, Retrofit>()
        fun getClient(url: String): Retrofit? {
            return retrofitCache.getOrPut(url) {
                Retrofit.Builder().baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }
}