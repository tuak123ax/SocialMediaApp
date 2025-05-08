package com.minhtu.firesocialmedia.home.navigationscreen.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FriendViewModel : ViewModel() {
    private val friendRequestsList = MutableStateFlow<ArrayList<String>>(ArrayList())
    private val friendList = MutableStateFlow<ArrayList<String>>(ArrayList())
    val friendRequestsStatus = friendRequestsList
    val friendStatus = friendList
    fun updateFriendRequests(friendRequests : List<String>) {
        friendRequestsList.value.clear()
        friendRequestsList.value = ArrayList(friendRequests)
    }

    fun updateFriends(friends : List<String>) {
        friendList.value.clear()
        friendList.value = ArrayList(friends)
    }
    fun acceptFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        viewModelScope.launch(Dispatchers.IO) {
            currentUser.friendRequests.remove(requester.uid)
            currentUser.friends.add(requester.uid)
            DatabaseHelper.saveListToDatabase(currentUser.uid,
                Constants.USER_PATH,
                currentUser.friendRequests,
                Constants.FRIEND_REQUESTS_PATH)
            DatabaseHelper.saveListToDatabase(currentUser.uid,
                Constants.USER_PATH,
                currentUser.friends,
                Constants.FRIENDS_PATH)
            requester.friends.add(currentUser.uid)
            DatabaseHelper.saveListToDatabase(requester.uid,
                Constants.USER_PATH,
                requester.friends,
                Constants.FRIENDS_PATH)

            //Update value to notify UI
            friendRequestsList.value = ArrayList(currentUser.friendRequests)
            friendList.value = ArrayList(currentUser.friends)
        }
    }
    fun rejectFriendRequest(requester: UserInstance, currentUser: UserInstance) {
        viewModelScope.launch(Dispatchers.IO) {
            currentUser.friendRequests.remove(requester.uid)
            DatabaseHelper.saveListToDatabase(currentUser.uid,
                Constants.USER_PATH,
                currentUser.friendRequests,
                Constants.FRIEND_REQUESTS_PATH)

            //Update value to notify UI
            friendRequestsList.value = ArrayList(currentUser.friendRequests)
        }
    }
}