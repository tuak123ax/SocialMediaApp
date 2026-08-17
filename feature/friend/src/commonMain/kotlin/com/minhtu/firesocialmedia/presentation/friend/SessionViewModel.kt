package com.minhtu.firesocialmedia.presentation.friend

import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetUserUseCase
import com.minhtu.firesocialmedia.friend.entity.user.UserInstance
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
