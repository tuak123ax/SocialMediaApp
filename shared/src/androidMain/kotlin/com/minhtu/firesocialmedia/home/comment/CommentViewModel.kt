package com.minhtu.firesocialmedia.home.comment

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    var listComments : ArrayList<CommentInstance> = ArrayList()
    private val _allComments : MutableStateFlow<ArrayList<CommentInstance>> = MutableStateFlow(ArrayList())
    val allComments = _allComments.asStateFlow()
    fun updateComments(comments: ArrayList<CommentInstance>) {
        _allComments.value = ArrayList(comments)
        Log.e("CommentViewModel", "updateComments")
    }
    var message by mutableStateOf("")
    fun updateMessage(input : String){
        message = input
    }

    var image by mutableStateOf("")
    fun updateImage(input:String){
        image = input
    }

    private var _createCommentStatus = MutableLiveData<Boolean>()
    var createCommentStatus = _createCommentStatus
    fun sendComment(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>) {
        if(message.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try{
                    val commentRandomId = Utils.generateRandomId()
                    val commentInstance = CommentInstance(commentRandomId,currentUser.uid, currentUser.name,currentUser.image,message,image)
                    commentInstance.timePosted = Utils.getCurrentTime()
                    listComments.add(commentInstance)
                    updateComments(listComments)
                    Log.d("CommentViewModel", "Comment added: $commentInstance")
                    DatabaseHelper.saveInstanceToDatabase(commentRandomId,
                        Constants.NEWS_PATH+"/"+selectedNew.id+"/"+ Constants.COMMENT_PATH,commentInstance,_createCommentStatus)
                    DatabaseHelper.updateCountValueInDatabase(selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_COUNT_PATH, listComments.size)
                    updateMessage("")

                    //Save and send notification
                    saveAndSendNotification(currentUser, selectedNew, listUsers)
                } catch(e: Exception) {
                    Log.e("SendComment", "Error saving comment: ${e.message}")
                }
            }
        }
    }

    private fun saveAndSendNotification(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>) {
        val notiContent = "${currentUser.name} commented in your post!"
        val notification = NotificationInstance(Utils.getRandomIdForNotification(),
            notiContent,currentUser.image, currentUser.uid, Utils.getCurrentTime(), NotificationType.COMMENT)
        //Save notification to db
        val poster = Utils.findUserById(selectedNew.posterId, listUsers)
        Utils.saveNotification(notification, poster!!)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        Utils.sendMessageToServer(Utils.createMessageForServer(notiContent, tokenList, currentUser))
    }
}