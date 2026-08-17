package com.minhtu.firesocialmedia.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase
) : ViewModel() {
    var currentUser: UserInstance? = null
        private set

    var loadedUsersCache: HashMap<String, UserInstance?> = HashMap()
    private val _loadedUserState = MutableStateFlow<Map<String, UserInstance?>>(emptyMap())
    val loadedUserState: StateFlow<Map<String, UserInstance?>> = _loadedUserState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = getCurrentUserUidUseCase.invoke() ?: return@launch
            currentUser = getUserUseCase.invoke(uid, true)
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
