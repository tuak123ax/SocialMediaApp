package com.minhtu.firesocialmedia.data.remote.auth.mapper.crypto

import com.minhtu.firesocialmedia.data.remote.auth.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials

fun CredentialsDTO.toDomain() : Credentials {
    return Credentials(email, password)
}