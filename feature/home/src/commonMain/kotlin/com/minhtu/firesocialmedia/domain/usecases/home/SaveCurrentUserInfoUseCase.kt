package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.home.entity.user.toDto
import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService

class SaveCurrentUserInfoUseCase(
    private val cryptoService: HomeCryptoService
) {
    suspend operator fun invoke(user : UserInstance) {
        cryptoService.saveCurrentUserInfo(user.toDto())
    }
}
