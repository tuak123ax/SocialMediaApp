package com.minhtu.firesocialmedia.domain.entity.authentication

import kotlinx.serialization.Serializable

@Serializable
data class TwoFAResponse(
    val success: Boolean,
    val message: String = ""
)