package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object SupabaseClient {

    const val BASE_URL = "https://pcklhkkafpomfvhboini.supabase.co/"

    val api: SupabaseStorageApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(SupabaseStorageApi::class.java)
    }
}