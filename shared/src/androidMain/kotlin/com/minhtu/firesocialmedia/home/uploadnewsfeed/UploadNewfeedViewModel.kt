package com.minhtu.firesocialmedia.home.uploadnewsfeed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    }

    private var _createPostStatus = MutableLiveData<Boolean>()
    var createPostStatus = _createPostStatus
    private var _updatePostStatus = MutableLiveData<Boolean>()
    var updatePostStatus = _updatePostStatus
    private var _postError = MutableLiveData<String>()
    var postError = _postError

    fun resetPostError(){
        _postError = MutableLiveData<String>()
        postError = _postError
    }

    private var _clickBackButton = MutableStateFlow(false)
    var clickBackButton = _clickBackButton.asStateFlow()
    fun onClickBackButton() {
        Log.e("clickBackButton", "onClickBackButton")
        _clickBackButton.value = true
    }
    fun resetBackValue() {
        Log.e("clickBackButton", "resetBackValue")
        _clickBackButton.value = false
    }

    fun createPost(user : UserInstance){
        viewModelScope.launch(Dispatchers.IO) {
            val newsRandomId = Utils.generateRandomId()
            if(message.isNotEmpty() || image.isNotEmpty()) {
                Log.e("createPost", "Success")
                //Save post to db
                val newsInstance = NewsInstance(newsRandomId,user.uid, user.name,user.image,message,image)
                newsInstance.timePosted = Utils.getCurrentTime()
                DatabaseHelper.saveInstanceToDatabase(newsRandomId,
                    Constants.NEWS_PATH,newsInstance,_createPostStatus)

                //Create noti object
                val notiContent = message
                val notification = NotificationInstance(Utils.getRandomIdForNotification(),
                    notiContent,currentUser!!.image,
                    currentUser!!.uid,
                    Utils.getCurrentTime(),
                    NotificationType.UPLOAD_NEW,
                    newsInstance.id)
                //Send Notification
                val friendTokens = getFriendTokens()
                if(notification.content.isNotEmpty()) {
                    Utils.sendMessageToServer(Utils.createMessageForServer(notification.content, friendTokens, currentUser!!))
                } else {
                    if(image.isNotEmpty()) {
                        val content = "Posted a picture!"
                        notification.updateContent(content)
                        Utils.sendMessageToServer(Utils.createMessageForServer(content, friendTokens, currentUser!!))
                    }
                }

                //Save notification to db
                Log.e("saveAndSendNotification", "saveNotification")
                for(friend in currentUser!!.friends) {
                    val friendsOfCurrentUser = Utils.findUserById(friend, listUsers)
                    Utils.saveNotification(notification, friendsOfCurrentUser!!)
                }
            } else {
                Log.e("createPost", "POST_NEWS_EMPTY_ERROR")
                _postError.postValue(Constants.POST_NEWS_EMPTY_ERROR)
            }
        }
    }

    fun resetPostStatus() {
        _createPostStatus = MutableLiveData<Boolean>()
        createPostStatus = _createPostStatus
        _updatePostStatus = MutableLiveData<Boolean>()
        updatePostStatus = _updatePostStatus
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

    fun updateNewInformation(new: NewsInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        backgroundScope.launch {
            if(message.isNotEmpty() || image.isNotEmpty()) {
                DatabaseHelper.updateNewsFromDatabase(Constants.NEWS_PATH, message, image ,new, _updatePostStatus)
            } else {
                Log.e("createPost", "UPDATE_NEWS_EMPTY_ERROR")
                _postError.postValue(Constants.POST_NEWS_EMPTY_ERROR)
            }
        }
    }
}