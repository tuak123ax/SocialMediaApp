package com.minhtu.firesocialmedia.presentation.navigationscreen.friend

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FriendViewModel(
    private val saveFriendUseCase: SaveFriendUseCase,
    private val saveFriendRequestUseCase: SaveFriendRequestUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val friendRequestsList = MutableStateFlow<ArrayList<String>>(ArrayList())
    private val friendList = MutableStateFlow<ArrayList<String>>(ArrayList())
    val friendRequestsStatus = friendRequestsList.asStateFlow()
    val friendStatus = friendList.asStateFlow()
    fun updateFriendRequests(friendRequests : List<String>) {
        friendRequestsList.value.clear()
        friendRequestsList.value = ArrayList(friendRequests)
    }

    fun updateFriends(friends : List<String>) {
        friendList.value.clear()
        friendList.value = ArrayList(friends)
    }
    fun acceptFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                currentUser.friendRequests.remove(requester.uid)
                currentUser.friends.add(requester.uid)

                saveFriendRequestUseCase.invoke(
                    currentUser.uid,
                    currentUser.friendRequests
                )

                saveFriendUseCase.invoke(
                    currentUser.uid,
                    currentUser.friends
                )

                requester.friends.add(currentUser.uid)
                saveFriendUseCase.invoke(
                    requester.uid,
                    requester.friends
                )

                //Update value to notify UI
                friendRequestsList.value = ArrayList(currentUser.friendRequests)
                friendList.value = ArrayList(currentUser.friends)
            }
        }
    }
    fun rejectFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                currentUser.friendRequests.remove(requester.uid)

                saveFriendRequestUseCase.invoke(
                    currentUser.uid,
                    currentUser.friendRequests
                )

                //Update value to notify UI
                friendRequestsList.value = ArrayList(currentUser.friendRequests)
            }
        }
    }
}