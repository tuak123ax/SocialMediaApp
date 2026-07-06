package com.minhtu.firesocialmedia.presentation.friend

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.presentation.friend.FriendViewModelContract
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendViewModel(
    private val saveFriendUseCase: SaveFriendUseCase,
    private val saveFriendRequestUseCase: SaveFriendRequestUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), FriendViewModelContract {
    private val friendRequestsList = MutableStateFlow<ArrayList<String>>(ArrayList())
    private val friendList = MutableStateFlow<ArrayList<String>>(ArrayList())

    override val friendRequestsStatus = friendRequestsList.asStateFlow()
    override val friendStatus = friendList.asStateFlow()

    override fun updateFriendRequests(friendRequests: List<String>) {
        friendRequestsList.value = ArrayList(friendRequests)
    }

    override fun updateFriends(friends: List<String>) {
        friendList.value = ArrayList(friends)
    }

    override fun acceptFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        currentUser.friendRequests.remove(requester.uid)
        if (!currentUser.friends.contains(requester.uid)) {
            currentUser.friends.add(requester.uid)
        }
        if (!requester.friends.contains(currentUser.uid)) {
            requester.friends.add(currentUser.uid)
        }

        friendRequestsList.value = ArrayList(currentUser.friendRequests)
        friendList.value = ArrayList(currentUser.friends)

        viewModelScope.launch(ioDispatcher) {
            saveFriendRequestUseCase.invoke(currentUser.uid, currentUser.friendRequests)
            saveFriendUseCase.invoke(currentUser.uid, currentUser.friends)
            saveFriendUseCase.invoke(requester.uid, requester.friends)
        }
    }

    override fun rejectFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        currentUser.friendRequests.remove(requester.uid)
        friendRequestsList.value = ArrayList(currentUser.friendRequests)

        viewModelScope.launch(ioDispatcher) {
            saveFriendRequestUseCase.invoke(currentUser.uid, currentUser.friendRequests)
        }
    }
}
