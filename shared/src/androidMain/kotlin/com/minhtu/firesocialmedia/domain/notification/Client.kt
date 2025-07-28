package com.minhtu.firesocialmedia.domain.notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class Client {

    companion object{
        private var retrofit: Retrofit? = null
        fun getClient(url: String): Retrofit? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder().baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }
}