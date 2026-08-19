package com.minhtu.firesocialmedia.presentation.profile

import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase
) : ViewModel() {
    private val _currentUserState = MutableStateFlow<UserInstance?>(null)
    val currentUserState: StateFlow<UserInstance?> = _currentUserState.asStateFlow()

    // Kept for callers that only need a synchronous snapshot (e.g. click handlers
    // fired after the screen has already observed a non-null currentUserState).
    val currentUser: UserInstance?
        get() = _currentUserState.value

    var loadedUsersCache: HashMap<String, UserInstance?> = HashMap()
    private val _loadedUserState = MutableStateFlow<Map<String, UserInstance?>>(emptyMap())
    val loadedUserState: StateFlow<Map<String, UserInstance?>> = _loadedUserState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = getCurrentUserUidUseCase.invoke() ?: return@launch
            _currentUserState.value = getUserUseCase.invoke(uid, true)
        }
    }

    suspend fun findUserById(userId: String): UserInstance? {
        loadedUsersCache[userId]?.let { return it }
        val user = getUserUseCase.invoke(userId, false)
        loadedUsersCache[userId] = user
        _loadedUserState.value = HashMap(loadedUsersCache)
        return user
    }

    fun ensureUserLoaded(userId: String) {
        if (loadedUsersCache.containsKey(userId)) return
        viewModelScope.launch {
            findUserById(userId)
        }
    }
}
