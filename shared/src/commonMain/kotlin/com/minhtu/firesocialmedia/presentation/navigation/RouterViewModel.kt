package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.presentation.home.Home
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.security.twoFA.VerifyOTP
import com.minhtu.firesocialmedia.presentation.signin.SignIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RouterViewModel(
    private val checkLocalAccountUseCase : CheckLocalAccountUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val get2FAVerifiedStatusUseCase: Get2FAVerifiedStatusUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var currentUser = mutableStateOf(UserInstance())
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination
    init {
        viewModelScope.launch(ioDispatcher) {
            _startDestination.value = resolveStartDestination()
        }
    }
    private suspend fun resolveStartDestination(): String {
        // 1. Check local account
        val account = checkLocalAccountUseCase()
            ?: return SignIn.getScreenName()

        // 2. If already verified 2FA → go Home immediately
        if (get2FAVerifiedStatusUseCase()) {
            return Home.getScreenName()
        }

        // 3. Get user
        val userId = getCurrentUserUidUseCase()
            ?: return SignIn.getScreenName()

        val user = getUserUseCase(userId, true)
            ?: return SignIn.getScreenName()

        currentUser.value = user

        // 4. Decide navigation
        return if (user.twoFAEnabled) {
            VerifyOTP.getScreenName()
        } else {
            Home.getScreenName()
        }
    }
}