package com.minhtu.firesocialmedia.ios.service.serviceimpl.auth

import com.minhtu.firesocialmedia.constants.security.Constants
import com.minhtu.firesocialmedia.data.remote.dto.authentication.TwoFAResponseDTO
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDTO
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDomain
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.ios.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.logMessage
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class IosTwoFactorAuthService : TwoFactorAuthService {

    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    override suspend fun generateSecretFor2FA(): String {
        val length = 16
        val result = StringBuilder(length)
        repeat(length) {
            result.append(BASE32_CHARS[(0 until BASE32_CHARS.length).random()])
        }
        return result.toString()
    }

    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "enable",
                    userId = userId,
                    secret = secret,
                    otp = otpToVerify
                )
            )
        } catch (e: Exception) {
            logMessage("enableOTP", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyOTP(userId: String, otpToVerify: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify",
                    userId = userId,
                    otp = otpToVerify
                )
            )
        } catch (e: Exception) {
            logMessage("verifyOTP", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun disable2FA(userId: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "disable",
                    userId = userId
                )
            )
        } catch (e: Exception) {
            logMessage("disable2FA", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    override suspend fun verifyBackupCode(userId: String, backupCode: String): TwoFAResponse {
        return try {
            send2FARequest(
                TwoFARequest(
                    apiKey = AppConfig.twoFAApiKey,
                    action = "verify_backup",
                    userId = userId,
                    backupCode = backupCode
                )
            )
        } catch (e: Exception) {
            logMessage("verifyBackupCode", { "Exception happened: ${e.message}" })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    private suspend fun send2FARequest(request: TwoFARequest): TwoFAResponse {
        val response = KtorProvider.client.post(Constants.APP_SCRIPT_URL + Constants.APP_SCRIPT_2FA_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request.toDTO())
        }
        return if (response.status.isSuccess()) {
            response.body<TwoFAResponseDTO>().toDomain()
        } else {
            TwoFAResponse(false, "Request failed: ${response.status}")
        }
    }
}
