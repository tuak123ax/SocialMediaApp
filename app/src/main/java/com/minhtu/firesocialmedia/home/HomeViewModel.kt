package com.minhtu.firesocialmedia.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel : ViewModel() {
    var listUsers: ArrayList<UserInstance> = ArrayList()
    var listNews: ArrayList<NewsInstance> = ArrayList()
    lateinit var currentUser : UserInstance
    private val _allUsers : MutableLiveData<ArrayList<UserInstance>> = MutableLiveData()
    val allUsers = _allUsers
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        likeCache = currentUser.likedPosts
        Log.e("HomeViewModel", "updateUsers")
    }

    fun findUserById(userId : String) : UserInstance?{
        for(user in listUsers){
            if(user.uid == userId) {
                return user
            }
        }
        return null
    }

    private  val _allNews : MutableLiveData<ArrayList<NewsInstance>> = MutableLiveData()
    val allNews  = _allNews
    fun updateNews(news: ArrayList<NewsInstance>) {
        _allNews.value  = news
        Log.e("HomeViewModel", "updateNews")
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

    fun createPost(user : UserInstance){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val newsRandomId = Utils.generateRandomId()
                val newsInstance = NewsInstance(newsRandomId,user.uid, user.name,user.image,message,image)
                DatabaseHelper.saveInstanceToDatabase(newsRandomId,Constants.NEWS_PATH,newsInstance,_createPostStatus)
            } catch(e: Exception) {
                Log.e("CreatePost", "Error saving post: ${e.message}")
                _createPostStatus.postValue(false)
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
    var likeCache : HashMap<String,Boolean> = HashMap()
    private var updateLikeJob : Job? = null
    fun clickLikeButton(news : NewsInstance) {
        val isLiked = likeCache[news.id] ?: false
        if (isLiked) {
            likeCache.remove(news.id) // Unlike
        } else {
            likeCache[news.id] = true // Like
        }

        _likedPosts.value = HashMap(likeCache)

        updateLikeJob?.cancel()
        updateLikeJob = viewModelScope.launch(Dispatchers.IO) {
            try{
                delay(3000)
                sendLikeUpdatesToFirebase()
            } catch(e: Exception) {
                Log.e("ClickLikeButton", "Error updating like status: ${e.message}")
            }
        }
    }

    private fun sendLikeUpdatesToFirebase() {
        currentUser.likedPosts = likeCache
        DatabaseHelper.saveValueToDatabase(currentUser.uid,Constants.USER_PATH, likeCache, Constants.LIKED_POSTS_PATH)
    }

    fun updateLikeStatus(){
        _likedPosts.value = likeCache
    }

    private var _commentStatus : MutableLiveData<NewsInstance> = MutableLiveData()
    var commentStatus = _commentStatus
    fun clickCommentButton(newsInstance: NewsInstance) {
        _commentStatus.value = newsInstance
    }
    fun resetCommentStatus() {
        _commentStatus = MutableLiveData<NewsInstance>()
        commentStatus = _commentStatus
        message = ""
        image = ""
    }
}