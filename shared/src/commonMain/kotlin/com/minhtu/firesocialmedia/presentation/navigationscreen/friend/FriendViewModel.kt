package com.minhtu.firesocialmedia.presentation.navigationscreen.friend

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.SaveListToDatabaseUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FriendViewModel(
    private val saveListToDatabaseUseCase: SaveListToDatabaseUseCase,
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

                saveListToDatabaseUseCase.invoke(
                    currentUser.uid,
                    Constants.USER_PATH,
                    currentUser.friendRequests,
                    Constants.FRIEND_REQUESTS_PATH
                )
                saveListToDatabaseUseCase.invoke(
                    currentUser.uid,
                    Constants.USER_PATH,
                    currentUser.friends,
                    Constants.FRIENDS_PATH
                )
                requester.friends.add(currentUser.uid)
                saveListToDatabaseUseCase.invoke(
                    requester.uid,
                    Constants.USER_PATH,
                    requester.friends,
                    Constants.FRIENDS_PATH
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

                saveListToDatabaseUseCase.invoke(
                    currentUser.uid,
                    Constants.USER_PATH,
                    currentUser.friendRequests,
                    Constants.FRIEND_REQUESTS_PATH
                )

                //Update value to notify UI
                friendRequestsList.value = ArrayList(currentUser.friendRequests)
            }
        }
    }
}