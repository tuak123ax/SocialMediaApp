package com.minhtu.firesocialmedia.domain.entity.authentication

data class TwoFARequest(
    val apiKey: String,
    val action: String,
    val userId: String,
    val secret: String? = "",
    val otp: String? = "",
    val backupCode: String? = ""
)