package com.minhtu.firesocialmedia.home.uploadnewsfeed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.PlatformContext
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.createMessageForServer
import com.minhtu.firesocialmedia.generateRandomId
import com.minhtu.firesocialmedia.getCurrentTime
import com.minhtu.firesocialmedia.getRandomIdForNotification
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadNewfeedViewModel : ViewModel() {
    var listUsers: ArrayList<UserInstance> = ArrayList()
    var currentUser : UserInstance? = null
    fun updateCurrentUser(user: UserInstance) {
        currentUser = user
    }
    fun updateListUsers(users: ArrayList<UserInstance>) {
        listUsers = users
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
        viewModelScope.launch(Dispatchers.IO) {
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
                val friendTokens = getFriendTokens()
                if(friendTokens.isNotEmpty()){
                    if(notification.content.isNotEmpty()) {
                        sendMessageToServer(createMessageForServer(notification.content, friendTokens, currentUser!!))
                    } else {
                        if(image.isNotEmpty()) {
                            val content = "Posted a picture!"
                            notification.updateContent(content)
                            sendMessageToServer(createMessageForServer(content, friendTokens, currentUser!!))
                        } else {
                            if(video.isNotEmpty()) {
                                val content = "Posted a video!"
                                notification.updateContent(content)
                                sendMessageToServer(createMessageForServer(content, friendTokens, currentUser!!))
                            }
                        }
                    }
                }

                //Save notification to db
                for(friend in currentUser!!.friends) {
                    val friendsOfCurrentUser = Utils.findUserById(friend, listUsers)
                    Utils.saveNotification(notification, friendsOfCurrentUser!!, platform)
                }
            } else {
                _postError.value = Constants.POST_NEWS_EMPTY_ERROR
            }
        }
    }

    fun resetPostStatus() {
        _createPostStatus.value = null
        _updatePostStatus.value = null
        message = ""
        image = ""
    }

    fun getFriendTokens(): ArrayList<String> {
        val friendTokens = ArrayList<String>()
        for(friend in currentUser!!.friends) {
            val user = Utils.findUserById(friend, listUsers)
            if(user != null) {
                friendTokens.add(user.token)
            }
        }
        return friendTokens
    }

    fun updateNewInformation(new: NewsInstance, platform: PlatformContext) {
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        backgroundScope.launch {
            if(message.isNotEmpty() || image.isNotEmpty() || video.isNotEmpty()) {
                platform.database.updateNewsFromDatabase(Constants.NEWS_PATH,message,image, video,new,_updatePostStatus)
            } else {
                _postError.value = Constants.POST_NEWS_EMPTY_ERROR
            }
        }
    }
}