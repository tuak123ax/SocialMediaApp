package com.minhtu.firesocialmedia.presentation.userinformation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val saveFriendUseCase: SaveFriendUseCase,
    private val saveFriendRequestUseCase: SaveFriendRequestUseCase,
    private val saveNotificationToDatabaseUseCase : SaveNotificationToDatabaseUseCase,
    private val checkCalleeAvailableUseCase: CheckCalleeAvailableUseCase,
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
    fun clickAddFriendButton(friend : UserInstance?, currentUser : UserInstance?) {
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
                                    val notification = NotificationInstance(getRandomIdForNotification(),
                                        notiContent,
                                        currentUser.image,
                                        currentUser.uid,
                                        getCurrentTime(),
                                        NotificationType.ADD_FRIEND,
                                        currentUser.uid)
                                    //Save notification to db
                                    Utils.saveNotification(notification, friend, saveNotificationToDatabaseUseCase)
                                    sendMessageToServer(createMessageForServer(notiContent, tokenList , currentUser, "BASIC"))
                                    _addFriendStatus.value = Relationship.FRIEND_REQUEST
                                }
                                else -> {

                                }
                            }
                        } catch(_: Exception) {
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

    private suspend fun saveFriendRequest(friend : UserInstance, currentUser: UserInstance) {
        try{
            friendRequestList.add(currentUser.uid)

            saveFriendRequestUseCase.invoke(
                friend.uid,
                friendRequestList
            )
            _addFriendStatus.value = Relationship.FRIEND_REQUEST
        } catch(_: Exception) {
        }
    }
    private suspend fun removeFriendRequest(friend : UserInstance, currentUser : UserInstance) {
        try{
            friendRequestList.remove(currentUser.uid)
            saveFriendRequestUseCase.invoke(
                friend.uid,
                friendRequestList
            )
            _addFriendStatus.value = Relationship.NONE
        } catch(_: Exception) {
        }
    }

    private suspend fun removeFriend(friend : UserInstance, currentUser : UserInstance) {
        try{
            saveFriendUseCase.invoke(
                currentUser.uid,
                currentUser.friends
            )
        } catch(_: Exception) {
        }
        try {
            saveFriendUseCase.invoke(
                friend.uid,
                friend.friends
            )
        } catch(_: Exception) {
        }
        _addFriendStatus.value = Relationship.NONE
    }

    private val _calleeCurrentState = MutableStateFlow<Boolean?>(null)
    var calleeCurrentState = _calleeCurrentState.asStateFlow()
    fun checkCalleeAvailable(callee : UserInstance){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val result = checkCalleeAvailableUseCase.invoke(
                    callee.uid
                )
                _calleeCurrentState.value = result
            }
        }
    }

    fun resetCalleeState() {
        _calleeCurrentState.value = null
    }
}