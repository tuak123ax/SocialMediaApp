package com.minhtu.firesocialmedia.home.userinformation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

enum class Relationship{
    FRIEND,
    FRIEND_REQUEST,
    WAITING_RESPONSE,
    NONE
}
class UserInformationViewModel : ViewModel() {
    // StateFlow to update UI in Compose
    private var _addFriendStatus = MutableStateFlow<Relationship?>(null)
    var addFriendStatus = _addFriendStatus
    private var friendRequestList : ArrayList<String> = ArrayList()
    var currentRelationship : Relationship = Relationship.NONE
    private var updateFriendRequestJob : Job? = null
    fun clickAddFriendButton(friend : UserInstance?, currentUser : UserInstance?) {
        Log.e("UserInformationViewModel", "clickAddFriendButton")
        if(friend != null && currentUser != null){
            val tokenList = ArrayList<String>()
            tokenList.add(friend.token)
            updateFriendRequestJob?.cancel()
            //Use background scope instead of viewModelScope here to prevent job cancellation
            // when navigating to other screen.
            val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            updateFriendRequestJob = backgroundScope.launch {
                try{
                    Log.d("Task", "Starting task in backgroundScope")
                    when(currentRelationship) {
                        Relationship.FRIEND -> {
                            friend.removeFriend(currentUser.uid)
                            currentUser.removeFriend(friend.uid)
                            removeFriend(friend, currentUser)
                            _addFriendStatus.value = Relationship.NONE
                        }
                        Relationship.FRIEND_REQUEST -> {
                            removeFriendRequest(friend, currentUser)
                            friend.removeFriendRequest(currentUser.uid)
                            _addFriendStatus.value = Relationship.NONE
                        }
                        Relationship.NONE -> {
                            //Save friend request to db
                            saveFriendRequest(friend, currentUser)
                            friend.addFriendRequest(currentUser.uid)

                            val notiContent = "${currentUser.name} sent you a friend request!"
                            val notification = NotificationInstance(Utils.getRandomIdForNotification(),
                                notiContent,currentUser.image, currentUser.uid, Utils.getCurrentTime(),NotificationType.ADD_FRIEND)
                            //Save notification to db
                            Utils.saveNotification(notification, friend)
                            Utils.sendMessageToServer(Utils.createMessageForServer(notiContent, tokenList , currentUser))
                            _addFriendStatus.value = Relationship.FRIEND_REQUEST
                        }
                        else -> {

                        }
                    }
                } catch(e: Exception) {
                    Log.e("ClickLikeButton", "Error updating like status: ${e.message}")
                }
            }
        }
    }

    fun checkRelationship(friend: UserInstance, currentUser: UserInstance): Relationship {
        return if(currentUser.friends.contains(friend.uid)){
            Relationship.FRIEND
        } else if(currentUser.friendRequests.contains(friend.uid)){
            Relationship.WAITING_RESPONSE
        } else { if(friend.friendRequests.contains(currentUser.uid)){
            Relationship.FRIEND_REQUEST
            } else {
                Relationship.NONE
            }
        }
    }

    fun updateRelationship(relationship: Relationship) {
        Log.e("UserInformationViewModel", "updateRelationship: "+ convertRelationshipToString(relationship))
        this.currentRelationship = relationship
        _addFriendStatus.value = relationship
    }

    private suspend fun saveFriendRequest(friend : UserInstance, currentUser: UserInstance) {
        try{
            friendRequestList.add(currentUser.uid)
            DatabaseHelper.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friendRequestList, Constants.FRIEND_REQUESTS_PATH)
            _addFriendStatus.value = Relationship.FRIEND_REQUEST
        } catch(e: Exception) {
            Log.e("saveFriendRequest", "Error save friend request: ${e.message}")
        }
    }
    private suspend fun removeFriendRequest(friend : UserInstance, currentUser : UserInstance) {
        try{
            friendRequestList.remove(currentUser.uid)
            DatabaseHelper.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friendRequestList, Constants.FRIEND_REQUESTS_PATH)
            _addFriendStatus.value = Relationship.NONE
        } catch(e: Exception) {
            Log.e("removeFriendRequest", "Error remove friend request: ${e.message}")
        }
    }

    private suspend fun removeFriend(friend : UserInstance, currentUser : UserInstance) {
        try{
            Log.e("removeFriend", "Remove friend for current user")
            Log.e("removeFriend", "Friends: ${currentUser.friends.size}")
            DatabaseHelper.saveListToDatabase(currentUser.uid,
                Constants.USER_PATH, currentUser.friends, Constants.FRIENDS_PATH)
        } catch(e: Exception) {
            Log.e("removeFriend", "Error remove friend of current user: ${e.message}")
        }
        try {
            Log.e("removeFriend", "Remove friend for other user")
            Log.e("removeFriend", "Friends: ${currentUser.friends.size}")
            DatabaseHelper.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friend.friends, Constants.FRIENDS_PATH)
        } catch(e: Exception) {
            Log.e("removeFriend", "Error remove friend of friend: ${e.message}")
        }
        _addFriendStatus.value = Relationship.NONE
    }

    private fun convertRelationshipToString(relationship: Relationship) : String {
        return when(relationship) {
            Relationship.FRIEND -> "Friend"
            Relationship.FRIEND_REQUEST -> "Friend Request"
            Relationship.NONE -> "None"
            Relationship.WAITING_RESPONSE -> "Waiting Response"
        }
    }
}