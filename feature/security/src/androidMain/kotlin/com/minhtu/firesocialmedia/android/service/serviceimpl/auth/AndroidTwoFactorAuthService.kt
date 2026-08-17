package com.minhtu.firesocialmedia.android.service.serviceimpl.auth

import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.Client
import com.minhtu.firesocialmedia.constants.security.Constants
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDTO
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDomain
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.logMessage
import java.security.SecureRandom

class AndroidTwoFactorAuthService : TwoFactorAuthService {

    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val secureRandom = SecureRandom()

    override suspend fun generateSecretFor2FA(): String {
        val length = 16
        val result = StringBuilder(length)
        repeat(length) {
            val index = secureRandom.nextInt(BASE32_CHARS.length)
            result.append(BASE32_CHARS[index])
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
            logMessage("enableOTP", { "Exception happened: " + e.message })
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
            logMessage("verifyOTP", { "Exception happened: " + e.message })
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
            logMessage("disable2FA", { "Exception happened: " + e.message })
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
            logMessage("verifyBackupCode", { "Exception happened: " + e.message })
            TwoFAResponse(false, "Exception happened!")
        }
    }

    private fun send2FARequest(request: TwoFARequest): TwoFAResponse {
        val response = Client.getClient(Constants.APP_SCRIPT_URL)
            ?.create(AuthenticationApiService::class.java)!!
            .sendVerifyRequestToAppScript(request.toDTO())
            .execute()

        return if (response.isSuccessful) {
            val twoFAResponseDTO = response.body()
            logMessage("sendVerifyOTPRequest") {
                "Success: ${response.code()} | success=${twoFAResponseDTO?.success} | message=${twoFAResponseDTO?.message}"
            }
            twoFAResponseDTO?.toDomain() ?: TwoFAResponse(false, "Error happened. Please try again!")
        } else {
            val errorBody = response.errorBody()?.string()
            logMessage("sendVerifyOTPRequest") {
                """
                Request failed:
                - Code: ${response.code()}
                - Message: ${response.message()}
                - Error Body: $errorBody
                """.trimIndent()
            }
            TwoFAResponse(false, response.code().toString())
        }
    }
}
