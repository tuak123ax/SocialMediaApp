package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadFile(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Header("Authorization") auth: String,
        @Header("x-upsert") xUpsert: String = "true",
        @Body body: RequestBody
    ): Response<Unit>

    @DELETE("storage/v1/object/{bucket}/{path}")
    suspend fun deleteFile(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Header("Authorization") auth: String
    ): Response<Unit>
}