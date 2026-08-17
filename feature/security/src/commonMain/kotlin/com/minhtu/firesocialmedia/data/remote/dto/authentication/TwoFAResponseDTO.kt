package com.minhtu.firesocialmedia.data.remote.dto.authentication

import kotlinx.serialization.Serializable

@Serializable
data class TwoFAResponseDTO(
    val success: Boolean,
    val message: String = ""
)
