package com.minhtu.firesocialmedia.presentation.userinformation

import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileFriendViewModel(
    private val saveFriendUseCase: ProfileSaveFriendUseCase,
    private val saveFriendRequestUseCase: ProfileSaveFriendRequestUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val friendRequestsList = MutableStateFlow<ArrayList<String>>(ArrayList())
    private val friendList = MutableStateFlow<ArrayList<String>>(ArrayList())

    val friendRequestsStatus = friendRequestsList.asStateFlow()
    val friendStatus = friendList.asStateFlow()

    fun updateFriendRequests(friendRequests: List<String>) {
        friendRequestsList.value = ArrayList(friendRequests)
    }

    fun updateFriends(friends: List<String>) {
        friendList.value = ArrayList(friends)
    }

    fun acceptFriendRequest(requester: UserInstance, currentUser: UserInstance) {
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

    fun rejectFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        currentUser.friendRequests.remove(requester.uid)
        friendRequestsList.value = ArrayList(currentUser.friendRequests)

        viewModelScope.launch(ioDispatcher) {
            saveFriendRequestUseCase.invoke(currentUser.uid, currentUser.friendRequests)
        }
    }
}
