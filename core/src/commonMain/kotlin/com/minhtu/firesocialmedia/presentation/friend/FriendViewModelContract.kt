package com.minhtu.firesocialmedia.presentation.friend

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for friend actions used by multiple feature modules.
 */
interface FriendViewModelContract {
    val friendRequestsStatus: StateFlow<List<String>>
    val friendStatus: StateFlow<List<String>>

    fun updateFriendRequests(friendRequests: List<String>)
    fun updateFriends(friends: List<String>)
    fun acceptFriendRequest(requester: UserInstance, currentUser: UserInstance)
    fun rejectFriendRequest(requester: UserInstance, currentUser: UserInstance)
}
