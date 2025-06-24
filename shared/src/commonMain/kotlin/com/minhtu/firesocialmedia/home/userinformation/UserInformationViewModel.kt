package com.minhtu.firesocialmedia.home.userinformation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.PlatformContext
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.createMessageForServer
import com.minhtu.firesocialmedia.getCurrentTime
import com.minhtu.firesocialmedia.getRandomIdForNotification
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Relationship{
    FRIEND,
    FRIEND_REQUEST,
    WAITING_RESPONSE,
    NONE
}
class UserInformationViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    // StateFlow to update UI in Compose
    var coverPhoto by mutableStateOf(Constants.DEFAULT_AVATAR_URL)
    fun updateCover(input:String){
        coverPhoto = input
    }

    private var _addFriendStatus = MutableStateFlow<Relationship?>(null)
    var addFriendStatus = _addFriendStatus.asStateFlow()
    private var friendRequestList : ArrayList<String> = ArrayList()
    var currentRelationship : Relationship = Relationship.NONE
    private var updateFriendRequestJob : Job? = null
    fun clickAddFriendButton(friend : UserInstance?, currentUser : UserInstance?, platform : PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(friend != null && currentUser != null){
                    val tokenList = ArrayList<String>()
                    tokenList.add(friend.token)
                    updateFriendRequestJob?.cancel()
                    //Use background scope instead of viewModelScope here to prevent job cancellation
                    // when navigating to other screen.
                    val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    updateFriendRequestJob = backgroundScope.launch {
                        try{
                            when(currentRelationship) {
                                Relationship.FRIEND -> {
                                    friend.removeFriend(currentUser.uid)
                                    currentUser.removeFriend(friend.uid)
                                    removeFriend(friend, currentUser, platform)
                                    _addFriendStatus.value = Relationship.NONE
                                }
                                Relationship.FRIEND_REQUEST -> {
                                    removeFriendRequest(friend, currentUser, platform)
                                    friend.removeFriendRequest(currentUser.uid)
                                    _addFriendStatus.value = Relationship.NONE
                                }
                                Relationship.NONE -> {
                                    //Save friend request to db
                                    saveFriendRequest(friend, currentUser, platform)
                                    friend.addFriendRequest(currentUser.uid)

                                    val notiContent = "${currentUser.name} sent you a friend request!"
                                    val notification = NotificationInstance(getRandomIdForNotification(),
                                        notiContent,
                                        currentUser.image,
                                        currentUser.uid,
                                        getCurrentTime(),
                                        NotificationType.ADD_FRIEND,
                                        currentUser.uid)
                                    //Save notification to db
                                    Utils.saveNotification(notification, friend, platform)
                                    sendMessageToServer(createMessageForServer(notiContent, tokenList , currentUser))
                                    _addFriendStatus.value = Relationship.FRIEND_REQUEST
                                }
                                else -> {

                                }
                            }
                        } catch(e: Exception) {
                        }
                    }
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
        this.currentRelationship = relationship
        _addFriendStatus.value = relationship
    }

    private suspend fun saveFriendRequest(friend : UserInstance, currentUser: UserInstance, platform: PlatformContext) {
        try{
            friendRequestList.add(currentUser.uid)
            platform.database.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friendRequestList, Constants.FRIEND_REQUESTS_PATH)
            _addFriendStatus.value = Relationship.FRIEND_REQUEST
        } catch(e: Exception) {
        }
    }
    private suspend fun removeFriendRequest(friend : UserInstance, currentUser : UserInstance, platform: PlatformContext) {
        try{
            friendRequestList.remove(currentUser.uid)
            platform.database.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friendRequestList, Constants.FRIEND_REQUESTS_PATH)
            _addFriendStatus.value = Relationship.NONE
        } catch(e: Exception) {
        }
    }

    private suspend fun removeFriend(friend : UserInstance, currentUser : UserInstance, platform: PlatformContext) {
        try{
            platform.database.saveListToDatabase(currentUser.uid,
                Constants.USER_PATH, currentUser.friends, Constants.FRIENDS_PATH)
        } catch(e: Exception) {
        }
        try {
            platform.database.saveListToDatabase(friend.uid,
                Constants.USER_PATH, friend.friends, Constants.FRIENDS_PATH)
        } catch(e: Exception) {
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