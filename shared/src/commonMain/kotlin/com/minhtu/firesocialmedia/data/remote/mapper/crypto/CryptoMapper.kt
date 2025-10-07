package com.minhtu.firesocialmedia.data.remote.mapper.crypto

import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.domain.entity.crypto.Credentials

fun CredentialsDTO.toDomain() : Credentials {
    return Credentials(email, password)
}