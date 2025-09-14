package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadNewfeedViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val saveNewToDatabase : SaveNewToDatabaseUseCase,
    private val updateNewsFromDatabaseUseCase: UpdateNewsFromDatabaseUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var currentUser : UserInstance? = null
    fun updateCurrentUser(user: UserInstance) {
        currentUser = user
    }
    var message by mutableStateOf("")
    fun updateMessage(input : String){
        message = input
    }

    var image by mutableStateOf("")
    fun updateImage(input:String){
        image = input
        video = ""
    }

    var video by mutableStateOf("")
    fun updateVideo(input : String) {
        video = input
        image = ""
    }

    private var _createPostStatus = MutableStateFlow<Boolean?>(null)
    var createPostStatus = _createPostStatus.asStateFlow()
    private var _updatePostStatus = MutableStateFlow<Boolean?>(null)
    var updatePostStatus = _updatePostStatus.asStateFlow()
    private var _postError = MutableStateFlow<String?>(null)
    var postError = _postError.asStateFlow()

    fun resetPostError(){
        _postError.value = null
    }

    private var _clickBackButton = MutableStateFlow(false)
    var clickBackButton = _clickBackButton.asStateFlow()
    fun onClickBackButton() {
        _clickBackButton.value = true
    }
    fun resetBackValue() {
        _clickBackButton.value = false
    }

    fun createPost(user : UserInstance){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val newsRandomId = generateRandomId()
                if(message.isNotEmpty() || image.isNotEmpty() || video.isNotEmpty()) {
                    //Save post to db
                    val newsInstance = NewsInstance(newsRandomId,user.uid, user.name,user.image,message,image,video)
                    newsInstance.timePosted = getCurrentTime()
                    _createPostStatus.value = saveNewToDatabase.invoke(
                        newsRandomId,
                        newsInstance
                    )

                    //Create noti object
                    val notiContent = message
                    val notification = NotificationInstance(getRandomIdForNotification(),
                        notiContent,currentUser!!.image,
                        currentUser!!.uid,
                        getCurrentTime(),
                        NotificationType.UPLOAD_NEW,
                        newsInstance.id)
                    //Send Notification
                    val friendTokens = getFriendTokens()
                    if(friendTokens.isNotEmpty()){
                        if(notification.content.isNotEmpty()) {
                            sendMessageToServer(createMessageForServer(notification.content, friendTokens, currentUser!!, "BASIC"))
                        } else {
                            if(image.isNotEmpty()) {
                                val content = "Posted a picture!"
                                notification.updateContent(content)
                                sendMessageToServer(createMessageForServer(content, friendTokens, currentUser!!, "BASIC"))
                            } else {
                                if(video.isNotEmpty()) {
                                    val content = "Posted a video!"
                                    notification.updateContent(content)
                                    sendMessageToServer(createMessageForServer(content, friendTokens, currentUser!!, "BASIC"))
                                }
                            }
                        }
                    }

                    //Save notification to db
                    for(friend in currentUser!!.friends) {
                        val friendsOfCurrentUser = findUserById(friend)
                        saveNotification(
                            notification,
                            friendsOfCurrentUser!!,
                            saveNotificationToDatabaseUseCase)
                    }
                } else {
                    _postError.value = Constants.POST_NEWS_EMPTY_ERROR
                }
            }
        }
    }

    suspend fun saveNotification(
        notification: NotificationInstance,
        friend : UserInstance,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase) {
        //Save notification to friend's notification list
        try{
            friend.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(
                friend.uid,
                friend.notifications)
        } catch(_: Exception) {
        }
    }

    fun resetPostStatus() {
        _createPostStatus.value = null
        _updatePostStatus.value = null
        message = ""
        image = ""
    }

    suspend fun getFriendTokens(): ArrayList<String> {
        val friendTokens = ArrayList<String>()
        for(friend in currentUser!!.friends) {
            val user = findUserById(friend)
            if(user != null) {
                friendTokens.add(user.token)
            }
        }
        return friendTokens
    }

    fun updateNewInformation(new: NewsInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            if(message.isNotEmpty() || image.isNotEmpty() || video.isNotEmpty()) {
                _updatePostStatus.value = updateNewsFromDatabaseUseCase.invoke(
                    message,
                    image,
                    video,
                    new
                )
            } else {
                _postError.value = Constants.POST_NEWS_EMPTY_ERROR
            }
        }
    }

    suspend fun findUserById(userId: String) : UserInstance? {
        return getUserUseCase.invoke(userId)
    }
}