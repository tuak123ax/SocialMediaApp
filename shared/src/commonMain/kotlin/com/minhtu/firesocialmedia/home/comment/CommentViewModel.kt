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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var listComments : ArrayList<CommentInstance> = ArrayList()
    var mapSubComments : HashMap<String, CommentInstance> = HashMap()
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
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(_commentBeReplied.value == null) {
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
                } else {
                    //Handle reply comment
                    if(message.isNotEmpty()) {
                        onReplyComment(_commentBeReplied.value!!, currentUser, selectedNew, platform)
                    }
                    updateCommentBeReplied(null)
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

    fun copyToClipboard(text : String,platform: PlatformContext) {
        viewModelScope.launch(ioDispatcher) {
            platform.clipboard.copy(text)
        }
    }

    //Comment or reply comment
    private val _commentBeReplied = MutableStateFlow<CommentInstance?>(null)
    val commentBeReplied = _commentBeReplied.asStateFlow()

    fun updateCommentBeReplied(value : CommentInstance?) {
        _commentBeReplied.value = value
    }
    fun onReplyComment(currentComment: CommentInstance, currentUser : UserInstance, selectedNew : NewsInstance, platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try{
                    val commentRandomId = generateRandomId()
                    val commentInstance = CommentInstance(commentRandomId,currentUser.uid, currentUser.name,currentUser.image,message,image)
                    commentInstance.timePosted = getCurrentTime()

                    listComments.remove(currentComment)
                    currentComment.listReplies.put(commentInstance.id, commentInstance)
                    listComments.add(currentComment)
                    updateComments(listComments)
                    platform.database.saveInstanceToDatabase(commentRandomId,
                        Constants.NEWS_PATH+"/"+selectedNew.id+"/"+ Constants.COMMENT_PATH+"/"+currentComment.id+"/"+ Constants.LIST_REPLIES_PATH,
                        commentInstance,_createCommentStatus)

                    logMessage("updateCountValueInDatabase", listComments.size.toString())
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" + currentComment.id +"/"
                                +Constants.COMMENT_COUNT_PATH,
                        currentComment.listReplies.size)
                    updateMessage("")
                    updateCommentBeReplied(null)

                    //Save and send notification
//                saveAndSendNotification(currentUser, selectedNew, listUsers, platform)
                } catch(e: Exception) {
                }
            }
        }
    }

    //-----------------Like comment-----------------//
    private var _likedComments = MutableStateFlow<HashMap<String,Int>>(HashMap())
    val likedComments = _likedComments.asStateFlow()
    private var likeCache : HashMap<String,Int> = HashMap()
    private var unlikeCache : ArrayList<String> = ArrayList()
    private var updateLikeJob : Job? = null
    private var _likeCountList = MutableStateFlow<HashMap<String,Int>>(HashMap())
    var likeCountList = _likeCountList.asStateFlow()
    fun addLikeCountData(commentId : String, likeCount : Int) {
        _likeCountList.value[commentId] = likeCount
    }
    fun onLikeComment(selectedNew : NewsInstance, currentUser : UserInstance, comment : CommentInstance, platform : PlatformContext) {
        logMessage("onLikeComment", comment.id)
        val isLiked = likeCache[comment.id] == 1
        if (isLiked) {
            likeCache.remove(comment.id) // Unlike
            unlikeCache.add(comment.id)
            if(_likeCountList.value[comment.id] != null) {
                _likeCountList.value[comment.id] = _likeCountList.value[comment.id]!! - 1
            }
        } else {
            likeCache[comment.id] = 1 // Like
            unlikeCache.remove(comment.id)
            if(_likeCountList.value[comment.id] != null) {
                _likeCountList.value[comment.id] = _likeCountList.value[comment.id]!! + 1
            } else {
                _likeCountList.value[comment.id] = 1
            }
        }

        _likedComments.value = HashMap(likeCache)

        updateLikeJob?.cancel()
        //Use background scope instead of viewModelScope here to prevent job cancellation
        // when navigating to other screen.
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        updateLikeJob = backgroundScope.launch {
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), selectedNew, currentUser, platform)
        }
    }

    private suspend fun sendLikeUpdatesToFirebase(likeCountList : HashMap<String,Int>, selectedNew : NewsInstance, currentUser : UserInstance, platform: PlatformContext) {
        currentUser.likedComments = likeCache
        platform.database.saveValueToDatabase(currentUser.uid,
            Constants.USER_PATH, likeCache, Constants.LIKED_COMMENT_PATH)
        val listCommentId = listComments.map { it.id}
        for(likedComment in likeCache.keys) {
            if(likeCountList[likedComment] != null) {
                if(listCommentId.contains(likedComment)) {
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                                Constants.NEWS_PATH,
                                Constants.COMMENT_PATH + "/" +
                                likedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[likedComment]!!)
                }
                if(mapSubComments.keys.contains(likedComment)) {
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                                Constants.NEWS_PATH,
                                Constants.COMMENT_PATH + "/" +
                                findParentCommentId(likedComment) + "/" +
                                Constants.LIST_REPLIES_PATH + "/" +
                                likedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[likedComment]!!)
                }
            }
        }
        for(unlikedComment in unlikeCache) {
            if(likeCountList[unlikedComment] != null) {
                if(listCommentId.contains(unlikedComment)) {
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                                        Constants.NEWS_PATH,
                                        Constants.COMMENT_PATH + "/" +
                                        unlikedComment + "/" +
                                        Constants.LIKED_COUNT_PATH, likeCountList[unlikedComment]!!)
                }
                if(mapSubComments.keys.contains(unlikedComment)) {
                    platform.database.updateCountValueInDatabase(selectedNew.id,
                                Constants.NEWS_PATH,
                                Constants.COMMENT_PATH + "/" +
                                findParentCommentId(unlikedComment) + "/" +
                                Constants.LIST_REPLIES_PATH + "/" +
                                unlikedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[unlikedComment]!!)
                }
            }
        }
    }

    private fun findParentCommentId(childCommentId : String) : String{
        return listComments.firstOrNull { it.listReplies.containsKey(childCommentId)}?.id ?: ""
    }

    fun updateLikeStatus(){
        _likedComments.value = HashMap(likeCache)
    }

    fun updateLikeCommentOfCurrentUser(currentUser: UserInstance) {
        likeCache = currentUser.likedComments
    }

    fun onDeleteComment(selectedNew : NewsInstance, comment : CommentInstance, platform : PlatformContext) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            val listCommentId = listComments.map { it.id}
            if(listCommentId.contains(comment.id)){
                platform.database.deleteCommentFromDatabase(
                            Constants.NEWS_PATH + "/" +
                            selectedNew.id + "/" +
                            Constants.COMMENT_PATH,
                            comment
                )
            }
            if(mapSubComments.keys.contains(comment.id)) {
                platform.database.deleteCommentFromDatabase(
                    Constants.NEWS_PATH + "/" +
                            selectedNew.id + "/" +
                            Constants.COMMENT_PATH + "/" +
                            findParentCommentId(comment.id) + "/" +
                            Constants.LIST_REPLIES_PATH,
                    comment
                )
            }
        }
    }
}