package com.minhtu.firesocialmedia.data.remote.dto.authentication

import kotlinx.serialization.Serializable

@Serializable
data class TwoFARequestDTO(
    val apiKey: String,
    val action: String,
    val userId: String,
    val secret: String? = "",
    val otp: String? = "",
    val backupCode: String? = ""
)

