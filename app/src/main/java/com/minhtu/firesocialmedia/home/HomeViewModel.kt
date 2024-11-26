package com.minhtu.firesocialmedia.home

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import java.util.UUID

class HomeViewModel : ViewModel() {
    private val _allUsers : MutableLiveData<ArrayList<UserInstance>> = MutableLiveData()
    val allUsers = _allUsers
    fun updateUsers(users: ArrayList<UserInstance>) {
        _allUsers.value = users
        Log.e("HomeViewModel", "updateUsers")
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

    private val _createPostStatus = MutableLiveData<Boolean>()
    val createPostStatus = _createPostStatus

    fun createPost(poster: String, avatar: String){
        val newsRandomId = generateRandomId()
        val newsInstance = NewsInstance(newsRandomId,poster,avatar,message,image)
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
    private fun generateRandomId(): String {
        return UUID.randomUUID().toString()
    }
}