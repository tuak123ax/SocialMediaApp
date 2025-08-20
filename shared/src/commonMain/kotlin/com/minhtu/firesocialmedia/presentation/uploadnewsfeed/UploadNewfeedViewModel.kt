package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationType
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadNewfeedViewModel(
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

    fun createPost(user : UserInstance, platform: PlatformContext){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val newsRandomId = generateRandomId()
                if(message.isNotEmpty() || image.isNotEmpty() || video.isNotEmpty()) {
                    //Save post to db
                    val newsInstance = NewsInstance(newsRandomId,user.uid, user.name,user.image,message,image,video)
                    newsInstance.timePosted = getCurrentTime()
                    platform.database.saveInstanceToDatabase(newsRandomId,
                        Constants.NEWS_PATH,newsInstance,_createPostStatus)

                    //Create noti object
                    val notiContent = message
                    val notification = NotificationInstance(getRandomIdForNotification(),
                        notiContent,currentUser!!.image,
                        currentUser!!.uid,
                        getCurrentTime(),
                        NotificationType.UPLOAD_NEW,
                        newsInstance.id)
                    //Send Notification
                    val friendTokens = getFriendTokens(platform)
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
                        val friendsOfCurrentUser = findUserById(friend, platform)
                        Utils.saveNotification(notification, friendsOfCurrentUser!!, platform)
                    }
                } else {
                    _postError.value = Constants.POST_NEWS_EMPTY_ERROR
                }
            }
        }
    }

    fun resetPostStatus() {
        _createPostStatus.value = null
        _updatePostStatus.value = null
        message = ""
        image = ""
    }

    suspend fun getFriendTokens(platform: PlatformContext): ArrayList<String> {
        val friendTokens = ArrayList<String>()
        for(friend in currentUser!!.friends) {
            val user = findUserById(friend, platform)
            if(user != null) {
                friendTokens.add(user.token)
            }
        }
        return friendTokens
    }

    fun updateNewInformation(new: NewsInstance, platform: PlatformContext) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            if(message.isNotEmpty() || image.isNotEmpty() || video.isNotEmpty()) {
                platform.database.updateNewsFromDatabase(Constants.NEWS_PATH,message,image, video,new,_updatePostStatus)
            } else {
                _postError.value = Constants.POST_NEWS_EMPTY_ERROR
            }
        }
    }

    suspend fun findUserById(userId: String, platform: PlatformContext) : UserInstance? {
        return platform.database.getUser(userId)
    }
}