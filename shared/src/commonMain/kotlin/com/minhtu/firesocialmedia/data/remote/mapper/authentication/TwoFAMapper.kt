package com.minhtu.firesocialmedia.data.remote.mapper.authentication
import com.minhtu.firesocialmedia.data.remote.dto.authentication.TwoFARequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.authentication.TwoFAResponseDTO
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
fun TwoFARequest.toDTO(): TwoFARequestDTO = TwoFARequestDTO(
    apiKey = apiKey,
    action = action,
    userId = userId,
    secret = secret,
    otp = otp,
    backupCode = backupCode
)
fun TwoFAResponseDTO.toDomain(): TwoFAResponse = TwoFAResponse(
    success = success,
    message = message
)
