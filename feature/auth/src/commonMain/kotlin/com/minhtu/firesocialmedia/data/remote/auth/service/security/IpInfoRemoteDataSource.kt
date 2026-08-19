package com.minhtu.firesocialmedia.data.remote.auth.service.security

import com.minhtu.firesocialmedia.data.remote.auth.dto.settings.auth.IpInfoResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class IpInfoRemoteDataSource(
    private val client: HttpClient,
    private val token: String
) {
    suspend fun getApproximateLocation(): IpInfoResponseDTO {
        return client.get("https://ipinfo.io/json") {
            parameter("token", token)
        }.body()
    }
}
