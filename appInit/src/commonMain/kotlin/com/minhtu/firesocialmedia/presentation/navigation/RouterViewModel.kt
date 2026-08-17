package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.constants.AuthRouteNames
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetUserUseCase
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
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
            ?: return AuthRouteNames.SignIn.SCREEN_NAME

        // 2. If already verified 2FA → go Home immediately
        if (get2FAVerifiedStatusUseCase()) {
            return HomeNavGraph.HOME_SCREEN_NAME
        }

        // 3. Get user
        val userId = getCurrentUserUidUseCase()
            ?: return AuthRouteNames.SignIn.SCREEN_NAME

        val user = getUserUseCase(userId, true)
            ?: return AuthRouteNames.SignIn.SCREEN_NAME

        currentUser.value = user

        // 4. Decide navigation
        return if (user.twoFAEnabled) {
            "VerifyOTPScreen"
        } else {
            HomeNavGraph.HOME_SCREEN_NAME
        }
    }
}