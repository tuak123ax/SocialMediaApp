package com.minhtu.firesocialmedia.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
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

class HomeViewModel : ViewModel() {
    val listState = LazyListState()
    var listUsers: ArrayList<UserInstance> = ArrayList()
    var listNews: ArrayList<NewsInstance> = ArrayList()
    var listNotificationOfCurrentUser = mutableStateListOf<NotificationInstance>()
    var currentUser : UserInstance? = null
    var currentUserState by mutableStateOf(currentUser)
    fun updateCurrentUser(user: UserInstance, context: Context) {
        currentUser = user
        currentUserState = currentUser
        Log.e("HomeViewModel", "update current user")
        updateFCMTokenForCurrentUser(context)
    }

    fun removeNotificationInList(notification: NotificationInstance) {
        listNotificationOfCurrentUser.remove(notification)
    }
    private fun updateFCMTokenForCurrentUser(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val secureSharedPreferences = CryptoHelper.getEncryptedSharedPreferences(context)
            val currentFCMToken = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
            if(!currentFCMToken.isNullOrEmpty()) {
                if(currentUser!!.token != currentFCMToken) {
                    currentUser!!.token = currentFCMToken
                    DatabaseHelper.saveStringToDatabase(currentUser!!.uid,Constants.USER_PATH, currentFCMToken, Constants.TOKEN_PATH)
                }
            }
        }
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

    private val _allUsers : MutableLiveData<ArrayList<UserInstance>> = MutableLiveData()
    val allUsers = _allUsers
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        likeCache = currentUser!!.likedPosts
        Log.e("HomeViewModel", "updateUsers")
    }

    private  val _allNews : MutableLiveData<ArrayList<NewsInstance>> = MutableLiveData()
    val allNews  = _allNews
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.value  = news
        Log.e("HomeViewModel", "updateNews")
    }

    private val _allNotifications : MutableLiveData<ArrayList<NotificationInstance>> = MutableLiveData()
    val allNotifications = _allNotifications
    fun updateNotifications(notifications: ArrayList<NotificationInstance>) {
        _allNotifications.value = notifications
        Log.e("HomeViewModel", "updateNotifications")
    }

    var message by mutableStateOf("")
    fun updateMessage(input : String){
        message = input
    }

    var image by mutableStateOf("")
    fun updateImage(input:String){
        image = input
    }

    var numberOfListNeedToLoad by mutableIntStateOf(0)
    fun decreaseNumberOfListNeedToLoad(input : Int) {
        numberOfListNeedToLoad -=  input
    }

    private var _createPostStatus = MutableLiveData<Boolean>()
    var createPostStatus = _createPostStatus
    private var _postError = MutableLiveData<String>()
    var postError = _postError

    fun resetPostError(){
        _postError = MutableLiveData<String>()
        postError = _postError
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
                    notiContent,currentUser!!.image, currentUser!!.uid, Utils.getCurrentTime(),NotificationType.LIKE)
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
        message = ""
        image = ""
    }

    fun clearAccountInStorage(context : Context){
        CryptoHelper.clearAccount(context)
    }

    //-----------------------------Like and comment function-----------------------------//
    // StateFlow to update UI in Compose
    private var _likedPosts = MutableStateFlow<HashMap<String,Boolean>>(HashMap())
    val likedPosts = _likedPosts.asStateFlow()
    private var likeCache : HashMap<String,Boolean> = HashMap()
    private var unlikeCache : ArrayList<String> = ArrayList()
    private var updateLikeJob : Job? = null
    private var _likeCountList = MutableStateFlow<HashMap<String,Int>>(HashMap())
    var likeCountList = _likeCountList
    fun addLikeCountData(newsId : String, likeCount : Int) {
        _likeCountList.value[newsId] = likeCount
    }
    fun clickLikeButton(news : NewsInstance) {
        val isLiked = likeCache[news.id] ?: false
        if (isLiked) {
            likeCache.remove(news.id) // Unlike
            unlikeCache.add(news.id)
            if(_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! - 1
            }
        } else {
            likeCache[news.id] = true // Like
            unlikeCache.remove(news.id)
            if(_likeCountList.value[news.id] != null) {
                _likeCountList.value[news.id] = _likeCountList.value[news.id]!! + 1
            } else {
                _likeCountList.value[news.id] = 1
            }
        }

        _likedPosts.value = HashMap(likeCache)
        val tempLikeCountList = HashMap(_likeCountList.value)

        updateLikeJob?.cancel()
        //Use background scope instead of viewModelScope here to prevent job cancellation
        // when navigating to other screen.
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        updateLikeJob = backgroundScope.launch {
            Log.d("Task", "Starting task in backgroundScope")
            Log.d("tempLikeCountList", "tempLikeCountList size: "+ tempLikeCountList.size )
            for(likedNew in tempLikeCountList.keys) {
                Log.d("likedNew", "likedNew: $likedNew ${tempLikeCountList[likedNew]}")
            }
            sendLikeUpdatesToFirebase(tempLikeCountList)
        }
    }

    private fun sendLikeUpdatesToFirebase(likeCountList : HashMap<String,Int>) {
        currentUser!!.likedPosts = likeCache
        DatabaseHelper.saveValueToDatabase(currentUser!!.uid,
            Constants.USER_PATH, likeCache, Constants.LIKED_POSTS_PATH)
        Log.e("sendLikeUpdatesToFirebase", "size: "+ likeCache.keys.size)
        for(likedNew in likeCache.keys) {
            Log.e("likedNew", "likedNew: $likedNew")
            if(likeCountList[likedNew] != null) {
                DatabaseHelper.updateCountValueInDatabase(likedNew,
                    Constants.NEWS_PATH,
                    Constants.LIKED_COUNT_PATH, likeCountList[likedNew]!!)
                try{
                    val new = Utils.findNewById(likedNew, listNews)
                    //Save and send notification
                    if(new != null) {
                        Log.e("sendNoti", "sendNoti")
                        saveAndSendNotification(currentUser!!, new, listUsers)
                    }
                } catch(e: Exception) {
                    Log.e("ClickLikeButton", "Error save and send notification: ${e.message}")
                }
            }
        }
        for(unlikedNew in unlikeCache) {
            if(likeCountList[unlikedNew] != null) {
                DatabaseHelper.updateCountValueInDatabase(unlikedNew,
                    Constants.NEWS_PATH,
                    Constants.LIKED_COUNT_PATH, likeCountList[unlikedNew]!!)
            }
        }
    }

    fun updateLikeStatus(){
        _likedPosts.value = HashMap(likeCache)
    }

    //-----------------------------Comment-----------------------------//
    private var _commentCountList = MutableStateFlow<HashMap<String,Int>>(HashMap())
    var commentCountList = _commentCountList
    fun addCommentCountData(newsId : String, commentCount : Int) {
        _commentCountList.value[newsId] = commentCount
    }
    private var _commentStatus : MutableLiveData<NewsInstance> = MutableLiveData()
    var commentStatus = _commentStatus.asFlow()
    fun clickCommentButton(newsInstance: NewsInstance) {
        Log.e("clickCommentButton", "clickCommentButton")
        _commentStatus.value = newsInstance
    }
    fun resetCommentStatus() {
        _commentStatus = MutableLiveData<NewsInstance>()
        commentStatus = _commentStatus.asFlow()
        message = ""
        image = ""
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

    private fun saveAndSendNotification(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>) {
        Log.e("saveAndSendNotification", "saveAndSendNotification")
        val notiContent = "${currentUser.name} liked your post!"
        val notification = NotificationInstance(Utils.getRandomIdForNotification(),
            notiContent,currentUser.image, currentUser.uid, Utils.getCurrentTime(),NotificationType.LIKE)
        //Save notification to db
        val poster = Utils.findUserById(selectedNew.posterId, listUsers)
        Log.e("saveAndSendNotification", "saveNotification")
        Utils.saveNotification(notification, poster!!)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        Log.e("saveAndSendNotification", "sendMessageToServer: "+ tokenList.size)
        Utils.sendMessageToServer(Utils.createMessageForServer(notiContent, tokenList, currentUser))
    }

    fun deleteNotification(notification: NotificationInstance) {
        currentUser!!.notifications.remove(notification)
        Utils.deleteNotification(notification, currentUser!!)
    }
}