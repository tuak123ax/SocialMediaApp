package com.minhtu.firesocialmedia.core.domain.usecases.settings

class BuildOtpAuthUrlUseCase() {
    operator fun invoke(appName: String,
                                user: String,
                                secret: String?) : String {
        return "otpauth://totp/$appName:$user?secret=$secret&issuer=$appName"
    }
}