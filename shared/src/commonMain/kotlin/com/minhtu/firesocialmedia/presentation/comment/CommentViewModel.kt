package com.minhtu.firesocialmedia.presentation.comment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.SaveValueToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.UpdateCountValueInDatabase
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.platform.showToast
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentViewModel(
    private val commentInteractor: CommentInteractor,
    private val getUserUseCase : GetUserUseCase,
    private val saveValueToDatabaseUseCase: SaveValueToDatabaseUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val updateCountValueInDatabase: UpdateCountValueInDatabase,
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
    fun sendComment(currentUser : UserInstance,
                    selectedNew : NewsInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(_commentBeReplied.value == null) {
                    if(message.isNotEmpty()) {
                        try{
                            val commentRandomId = generateRandomId()
                            val commentInstance = CommentInstance(commentRandomId,currentUser.uid, currentUser.name,currentUser.image,message,image)
                            commentInstance.timePosted = getCurrentTime()
                            listComments.add(commentInstance)
                            updateComments(listComments)
                            _createCommentStatus.value = commentInteractor.saveComment(
                                commentRandomId,
                                Constants.NEWS_PATH+"/"+selectedNew.id+"/"+ Constants.COMMENT_PATH,
                                commentInstance
                            )

                            updateCountValueInDatabase.invoke(
                                selectedNew.id,
                                Constants.NEWS_PATH,
                                Constants.COMMENT_COUNT_PATH,
                                listComments.size
                            )
                            updateMessage("")

                            //Save and send notification
                            saveAndSendNotification(currentUser, selectedNew)
                        } catch(e: Exception) {
                        }
                    }
                } else {
                    //Handle reply comment
                    if(message.isNotEmpty()) {
                        onReplyComment(_commentBeReplied.value!!, currentUser, selectedNew)
                    }
                    updateCommentBeReplied(null)
                }
            }
        }
    }

    fun resetCommentStatus() {
        _createCommentStatus.value = null
    }

    private suspend fun saveAndSendNotification(currentUser : UserInstance, selectedNew : NewsInstance) {
        val notiContent = "${currentUser.name} commented in your post!"
        val notification = NotificationInstance(getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.COMMENT,
            selectedNew.id)
        //Save notification to db
        val poster = getUserUseCase.invoke(selectedNew.posterId)
        saveNotification(notification, poster!!, saveNotificationToDatabaseUseCase)
        //Send notification to poster
        val tokenList = ArrayList<String>()
        tokenList.add(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser, "BASIC"))
    }

    suspend fun saveNotification(
        notification: NotificationInstance,
        friend : UserInstance,
        saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase) {
        //Save notification to friend's notification list
        try{
            friend.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(
                friend.uid,
                Constants.USER_PATH,
                friend.notifications)
        } catch(e: Exception) {
        }
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
    fun onReplyComment(currentComment: CommentInstance, currentUser : UserInstance, selectedNew : NewsInstance) {
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
                    _createCommentStatus.value = commentInteractor.saveComment(
                        commentRandomId,
                        Constants.NEWS_PATH+"/"+selectedNew.id+"/"+ Constants.COMMENT_PATH+"/"+currentComment.id+"/"+ Constants.LIST_REPLIES_PATH,
                        commentInstance
                    )

                    updateCountValueInDatabase.invoke(
                        selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" + currentComment.id +"/"
                                +Constants.COMMENT_COUNT_PATH,
                        currentComment.listReplies.size
                    )
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
    fun onLikeComment(selectedNew : NewsInstance, currentUser : UserInstance, comment : CommentInstance) {
        logMessage("onLikeComment", { comment.id })
        val isLiked = likeCache[comment.id] == 1
        if (isLiked) {
            // Unlike
            likeCache.remove(comment.id)
            unlikeCache.add(comment.id)
            if(_likeCountList.value[comment.id] != null) {
                _likeCountList.value[comment.id] = _likeCountList.value[comment.id]!! - 1
            }
        } else {
            // Like
            likeCache[comment.id] = 1
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
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), selectedNew, currentUser)
        }
    }

    val sendLikeDataStatus = mutableStateOf(false)
    private suspend fun sendLikeUpdatesToFirebase(likeCountList : HashMap<String,Int>, selectedNew : NewsInstance, currentUser : UserInstance) {
        currentUser.likedComments = likeCache
        val result = saveValueToDatabaseUseCase.invoke(
            currentUser.uid,
            Constants.USER_PATH,
            likeCache,
            Constants.LIKED_COMMENT_PATH
        )
        sendLikeDataStatus.value = result
        val listCommentId = listComments.map { it.id}
        for(likedComment in likeCache.keys) {
            if(likeCountList[likedComment] != null) {
                if(listCommentId.contains(likedComment)) {
                    updateCountValueInDatabase.invoke(
                        selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" +
                                likedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[likedComment]!!
                    )
                }
                if(mapSubComments.keys.contains(likedComment)) {
                    updateCountValueInDatabase.invoke(
                        selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" +
                                findParentCommentId(likedComment) + "/" +
                                Constants.LIST_REPLIES_PATH + "/" +
                                likedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[likedComment]!!
                    )
                }
            }
        }
        for(unlikedComment in unlikeCache) {
            if(likeCountList[unlikedComment] != null) {
                if(listCommentId.contains(unlikedComment)) {
                    updateCountValueInDatabase.invoke(
                        selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" +
                                unlikedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[unlikedComment]!!
                    )
                }
                if(mapSubComments.keys.contains(unlikedComment)) {
                    updateCountValueInDatabase.invoke(
                        selectedNew.id,
                        Constants.NEWS_PATH,
                        Constants.COMMENT_PATH + "/" +
                                findParentCommentId(unlikedComment) + "/" +
                                Constants.LIST_REPLIES_PATH + "/" +
                                unlikedComment + "/" +
                                Constants.LIKED_COUNT_PATH, likeCountList[unlikedComment]!!
                    )
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

    fun onDeleteComment(selectedNew : NewsInstance, comment : CommentInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            val listCommentId = listComments.map { it.id}
            if(listCommentId.contains(comment.id)){
                commentInteractor.deleteComment(
                    Constants.NEWS_PATH + "/" +
                            selectedNew.id + "/" +
                            Constants.COMMENT_PATH,
                    comment
                )
            }
            if(mapSubComments.keys.contains(comment.id)) {
                commentInteractor.deleteComment(
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

    suspend fun findUserById(userId: String) : UserInstance?{
        return getUserUseCase.invoke(userId)
    }

    suspend fun getAllCommentsOfNew(newsId : String) {
        val result = commentInteractor.getAllComments(
            Constants.COMMENT_PATH,
            newsId
        )
        if(result == null) {
            showToast("Cannot get all comments of this new. Try again!")
        } else {
            listComments.clear()
            listComments.addAll(result)
            updateComments(listComments)

            listComments.forEach { comment ->
                addLikeCountData(comment.id, comment.likeCount)

                comment.listReplies.forEach { (replyId, reply) ->
                    mapSubComments[replyId] = reply
                    addLikeCountData(replyId, reply.likeCount)
                }
            }
        }
    }

    fun clearCommentList() {
        _allComments.value.clear()
    }
}