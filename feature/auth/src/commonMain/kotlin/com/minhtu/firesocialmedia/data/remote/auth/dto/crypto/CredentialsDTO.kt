package com.minhtu.firesocialmedia.data.remote.auth.dto.crypto

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsDTO(val email: String, val password: String)
