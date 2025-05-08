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
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
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
import kotlinx.coroutines.withContext

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

    private val _allUsers : MutableLiveData<ArrayList<UserInstance>> = MutableLiveData()
    val allUsers = _allUsers
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        likeCache = currentUser!!.likedPosts
        Log.e("HomeViewModel", "updateUsers")
    }

    private var _allNews  = mutableStateListOf<NewsInstance>()
    val allNews = _allNews
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.clear()
        _allNews.addAll(news)
        Log.e("HomeViewModel", "updateNews")
    }

    private val _allNotifications : MutableLiveData<ArrayList<NotificationInstance>> = MutableLiveData()
    val allNotifications = _allNotifications
    fun updateNotifications(notifications: ArrayList<NotificationInstance>) {
        _allNotifications.value = notifications
        Log.e("HomeViewModel", "updateNotifications")
    }

    var numberOfListNeedToLoad by mutableIntStateOf(0)
    fun decreaseNumberOfListNeedToLoad(input : Int) {
        numberOfListNeedToLoad -=  input
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
    }

    private fun saveAndSendNotification(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>) {
        Log.e("saveAndSendNotification", "saveAndSendNotification")
        val notiContent = "${currentUser.name} liked your post!"
        val notification = NotificationInstance(Utils.getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            Utils.getCurrentTime(),
            NotificationType.LIKE,
            selectedNew.id)
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

    fun deleteOrHideNew(action : String, new: NewsInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        backgroundScope.launch {
            if(action == "Delete") {
                DatabaseHelper.deleteNewsFromDatabase(Constants.NEWS_PATH, new)
            }
            listNews.remove(new)
            withContext(Dispatchers.Main) {
                updateNews(listNews)
            }
        }
    }
}