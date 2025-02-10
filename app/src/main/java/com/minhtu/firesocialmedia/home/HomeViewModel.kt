package com.minhtu.firesocialmedia.home

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
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel : ViewModel() {
    var listUsers: ArrayList<UserInstance>? = ArrayList()
    var listNews: ArrayList<NewsInstance>? = ArrayList()
    var currentUser : UserInstance? = null
    private val _allUsers : MutableLiveData<ArrayList<UserInstance>> = MutableLiveData()
    val allUsers = _allUsers
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        Log.e("HomeViewModel", "updateUsers")
    }

    fun findUserById(userId : String) : UserInstance?{
        for(user in listUsers!!){
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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val newsRandomId = generateRandomId()
                val newsInstance = NewsInstance(newsRandomId,user.uid, user.name,user.image,message,image)
                val storageReference = FirebaseStorage.getInstance().getReference()
                    .child("news").child(newsRandomId)
                val databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("news").child(newsRandomId)
                if(image.isNotEmpty()){
                    storageReference.putFile(Uri.parse(newsInstance.image)).addOnCompleteListener{ putFileTask ->
                        if(putFileTask.isSuccessful){
                            storageReference.downloadUrl.addOnSuccessListener { imageUrl ->
                                newsInstance.updateImage(imageUrl.toString())
                                databaseReference.setValue(newsInstance).addOnCompleteListener{addUserTask ->
                                    if(addUserTask.isSuccessful){
                                        _createPostStatus.postValue(true)
                                    } else {
                                        _createPostStatus.postValue(false)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    databaseReference.setValue(newsInstance).addOnCompleteListener{addNewsTask ->
                        if(addNewsTask.isSuccessful){
                            _createPostStatus.postValue(true)
                        } else {
                            _createPostStatus.postValue(false)
                        }
                    }
                }
            }
        }
    }
    private fun generateRandomId(): String {
        return UUID.randomUUID().toString()
    }

    fun resetPostStatus() {
        _createPostStatus = MutableLiveData<Boolean>()
        createPostStatus = _createPostStatus
        message = ""
        image = ""
    }
}