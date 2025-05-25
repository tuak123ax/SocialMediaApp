package com.minhtu.firesocialmedia.home.comment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.createMessageForServer
import com.minhtu.firesocialmedia.generateRandomId
import com.minhtu.firesocialmedia.getCurrentTime
import com.minhtu.firesocialmedia.getRandomIdForNotification
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.NotificationType
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.sendMessageToServer
import com.minhtu.firesocialmedia.utils.Utils
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommentViewModel : ViewModel() {
    var listComments : ArrayList<CommentInstance> = ArrayList()
    private val _allComments : MutableStateFlow<ArrayList<CommentInstance>> = MutableStateFlow(ArrayList())
    val allComments = _allComments.asStateFlow()
    fun updateComments(comments: ArrayList<CommentInstance>) {
        _allComments.value.clear()
        _allComments.value = ArrayList(comments)
    }
    var message by mutableStateOf("")
    fun updateMessage(input : String){
        message = input
    }

    var image by mutableStateOf("")
    fun updateImage(input:String){
        image = input
    }

    private var _createCommentStatus : MutableStateFlow<Boolean?> = MutableStateFlow<Boolean?>(null)
    var createCommentStatus = _createCommentStatus.asStateFlow()
    fun sendComment(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>, platform: PlatformContext) {
        if(message.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try{
                    val commentRandomId = generateRandomId()
                    val commentInstance = CommentInstance(commentRandomId,currentUser.uid, currentUser.name,currentUser.image,message,image)
                    commentInstance.timePosted = getCurrentTime()
                    listComments.add(commentInstance)
                    updateComments(listComments)
                    platform.database.saveInstanceToDatabase(commentRandomId,
                        Constants.NEWS_PATH+"/"+selectedNew.id+"/"+ Constants.COMMENT_PATH,commentInstance,_createCommentStatus)

                    logMessage("updateCountValueInDatabase", listComments.size.toString())
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_COUNT_PATH, listComments.size)
                    updateMessage("")

                    //Save and send notification
                    saveAndSendNotification(currentUser, selectedNew, listUsers, platform)
                } catch(e: Exception) {
                }
            }
        }
    }

    fun resetCommentStatus() {
        _createCommentStatus.value = null
    }

    private suspend fun saveAndSendNotification(currentUser : UserInstance, selectedNew : NewsInstance, listUsers : ArrayList<UserInstance>, platform : PlatformContext) {
        val notiContent = "${currentUser.name} commented in your post!"
        val notification = NotificationInstance(getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.COMMENT,
            selectedNew.id)
        //Save notification to db
        val poster = Utils.findUserById(selectedNew.posterId, listUsers)
        Utils.saveNotification(notification, poster!!, platform)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser))
    }
}