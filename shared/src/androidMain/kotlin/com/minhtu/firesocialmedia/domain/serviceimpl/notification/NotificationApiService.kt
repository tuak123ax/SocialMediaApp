package com.minhtu.firesocialmedia.domain.serviceimpl.notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import java.util.HashMap

interface NotificationApiService {
    @POST("fcm/send")
    suspend fun sendNotification(
        @HeaderMap headers : HashMap<String, String>,
        @Body messageBody : String
    ): Call<String>

    @POST("AKfycbw4JXnBNCl-hoHi2l0_l-Ugp-9icTBWPJVR5PyKqe5o7-JJ-p26yFVpBO8kUZhxtUSzWA/exec")
    fun sendToAppScript(
        @Body request: String
    ): Call<Void>
}