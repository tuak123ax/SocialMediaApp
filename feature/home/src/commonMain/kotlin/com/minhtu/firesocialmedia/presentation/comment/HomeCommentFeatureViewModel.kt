package com.minhtu.firesocialmedia.presentation.comment

import androidx.compose.runtime.mutableStateOf
import com.minhtu.firesocialmedia.data.remote.service.clipboard.home.ClipboardService
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationType
import com.minhtu.firesocialmedia.home.entity.notification.toSharedNotification
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.HomeCommentInteractor
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.home.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.home.platform.sendMessageToServer
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeCommentFeatureViewModel(
    private val commentInteractor: HomeCommentInteractor,
    private val getUserUseCase: GetUserUseCase,
    private val saveLikedCommentsUseCase: HomeSaveLikedCommentsUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val updateCommentCountForNewUseCase: HomeUpdateCommentCountForNewUseCase,
    private val updateReplyCountForCommentUseCase: HomeUpdateReplyCountForCommentUseCase,
    private val updateLikeCountForCommentUseCase: HomeUpdateLikeCountForCommentUseCase,
    private val updateLikeCountForSubCommentUseCase: HomeUpdateLikeCountForSubCommentUseCase,
    private val clipboardService: ClipboardService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    var listComments: ArrayList<CommentInstance> = ArrayList()
    var mapSubComments: HashMap<String, CommentInstance> = HashMap()

    private val _allComments: MutableStateFlow<ArrayList<CommentInstance>> = MutableStateFlow(ArrayList())
    val allComments = _allComments.asStateFlow()

    private fun updateComments(comments: ArrayList<CommentInstance>) {
        _allComments.value.clear()
        _allComments.value = ArrayList(comments)
    }

    private val _message = MutableStateFlow("")
    val messageFlow = _message.asStateFlow()
    var message: String
        get() = _message.value
        set(value) {
            _message.value = value
        }

    fun updateMessage(input: String) {
        _message.value = input
    }

    var image: String = ""

    fun updateImage(input: String) {
        image = input
    }

    private val _createCommentStatus: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val createCommentStatus = _createCommentStatus.asStateFlow()

    fun sendComment(currentUser: UserInstance, selectedNewId: String, selectedNewPosterId: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (_commentBeReplied.value == null) {
                    if (message.isNotBlank()) {
                        try {
                            val commentRandomId = generateRandomId()
                            val commentInstance = CommentInstance(commentRandomId, currentUser.uid, currentUser.name, currentUser.image, message, image)
                            commentInstance.timePosted = getCurrentTime()
                            listComments.add(commentInstance)
                            updateComments(listComments)
                            _createCommentStatus.value = commentInteractor.saveComment(
                                selectedNewId,
                                commentRandomId,
                                commentInstance
                            )

                            updateCommentCountForNewUseCase.invoke(
                                selectedNewId,
                                listComments.size
                            )
                            updateMessage("")

                            saveAndSendNotification(currentUser, selectedNewId, selectedNewPosterId)
                        } catch (e: Exception) {
                            logMessage("sendComment") { "Error when sendComment: ${e.message}" }
                        }
                    }
                } else {
                    if (message.isNotEmpty()) {
                        onReplyComment(_commentBeReplied.value!!, currentUser, selectedNewId)
                    }
                    updateCommentBeReplied(null)
                }
            }
        }
    }

    fun resetCommentStatus() {
        _createCommentStatus.value = null
    }

    private suspend fun saveAndSendNotification(currentUser: UserInstance, selectedNewId: String, selectedNewPosterId: String) {
        val notiContent = "${currentUser.name} commented in your post!"
        val notification = NotificationInstance(
            getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.COMMENT,
            selectedNewId
        )
        val poster = getUserUseCase.invoke(selectedNewPosterId, false)
        saveNotification(notification, poster ?: return)
        val tokenList = arrayListOf(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser.token, currentUser.uid, currentUser.image, currentUser.email, currentUser.name, "BASIC"))
    }

    private suspend fun saveNotification(notification: NotificationInstance, friend: UserInstance) {
        try {
            friend.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(friend.uid, ArrayList(friend.notifications.map { it.toSharedNotification() }))
        } catch (_: Exception) {
        }
    }

    fun copyToClipboard(text: String, platform: PlatformContext) {
        viewModelScope.launch(ioDispatcher) {
            clipboardService.copy(text)
        }
    }

    private val _commentBeReplied = MutableStateFlow<CommentInstance?>(null)
    val commentBeReplied = _commentBeReplied.asStateFlow()

    fun updateCommentBeReplied(value: CommentInstance?) {
        _commentBeReplied.value = value
    }

    private fun onReplyComment(currentComment: CommentInstance, currentUser: UserInstance, selectedNewId: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    val commentRandomId = generateRandomId()
                    val commentInstance = CommentInstance(commentRandomId, currentUser.uid, currentUser.name, currentUser.image, message, image)
                    commentInstance.timePosted = getCurrentTime()

                    listComments.remove(currentComment)
                    currentComment.listReplies[commentInstance.id] = commentInstance
                    listComments.add(currentComment)
                    updateComments(listComments)
                    _createCommentStatus.value = commentInteractor.saveSubComment(
                        commentRandomId,
                        selectedNewId,
                        currentComment.id,
                        commentInstance
                    )

                    updateReplyCountForCommentUseCase.invoke(
                        selectedNewId,
                        currentComment.id,
                        currentComment.listReplies.size
                    )
                    updateMessage("")
                    updateCommentBeReplied(null)
                } catch (_: Exception) {
                }
            }
        }
    }

    private val _likedComments = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val likedComments = _likedComments.asStateFlow()
    private var likeCache: HashMap<String, Int> = HashMap()
    private var unlikeCache: ArrayList<String> = ArrayList()
    private var updateLikeJob: Job? = null
    private val _likeCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val likeCountList = _likeCountList.asStateFlow()

    private fun addLikeCountData(commentId: String, likeCount: Int) {
        _likeCountList.value[commentId] = likeCount
    }

    fun onLikeComment(selectedNewId: String, currentUser: UserInstance, comment: CommentInstance) {
        val isLiked = likeCache[comment.id] == 1
        if (isLiked) {
            likeCache.remove(comment.id)
            unlikeCache.add(comment.id)
            if (_likeCountList.value[comment.id] != null) {
                _likeCountList.value[comment.id] = _likeCountList.value[comment.id]!! - 1
            }
        } else {
            likeCache[comment.id] = 1
            unlikeCache.remove(comment.id)
            if (_likeCountList.value[comment.id] != null) {
                _likeCountList.value[comment.id] = _likeCountList.value[comment.id]!! + 1
            } else {
                _likeCountList.value[comment.id] = 1
            }
        }

        _likedComments.value = HashMap(likeCache)

        updateLikeJob?.cancel()
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        updateLikeJob = backgroundScope.launch {
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), selectedNewId, currentUser)
        }
    }

    val sendLikeDataStatus = mutableStateOf(false)

    private suspend fun sendLikeUpdatesToFirebase(
        likeCountList: HashMap<String, Int>,
        selectedNewId: String,
        currentUser: UserInstance
    ) {
        currentUser.likedComments = likeCache
        saveLikedCommentsUseCase.invoke(currentUser.uid, likeCache)
        val result = saveLikedCommentsUseCase.invoke(currentUser.uid, likeCache)
        sendLikeDataStatus.value = result
        val listCommentId = listComments.map { it.id }
        for (likedComment in likeCache.keys) {
            if (likeCountList[likedComment] != null) {
                if (listCommentId.contains(likedComment)) {
                    updateLikeCountForCommentUseCase.invoke(
                        selectedNewId,
                        likedComment,
                        likeCountList[likedComment]!!
                    )
                }
                if (mapSubComments.keys.contains(likedComment)) {
                    updateLikeCountForSubCommentUseCase.invoke(
                        selectedNewId,
                        likedComment,
                        findParentCommentId(likedComment),
                        likeCountList[likedComment]!!
                    )
                }
            }
        }
        for (unlikedComment in unlikeCache) {
            if (likeCountList[unlikedComment] != null) {
                if (listCommentId.contains(unlikedComment)) {
                    updateLikeCountForCommentUseCase.invoke(
                        selectedNewId,
                        unlikedComment,
                        likeCountList[unlikedComment]!!
                    )
                }
                if (mapSubComments.keys.contains(unlikedComment)) {
                    updateLikeCountForSubCommentUseCase.invoke(
                        selectedNewId,
                        unlikedComment,
                        findParentCommentId(unlikedComment),
                        likeCountList[unlikedComment]!!
                    )
                }
            }
        }
    }

    private fun findParentCommentId(childCommentId: String): String {
        return listComments.firstOrNull { it.listReplies.containsKey(childCommentId) }?.id ?: ""
    }

    fun updateLikeStatus() {
        _likedComments.value = HashMap(likeCache)
    }

    fun updateLikeCommentOfCurrentUser(currentUser: UserInstance) {
        likeCache = currentUser.likedComments
    }

    fun onDeleteComment(selectedNewId: String, comment: CommentInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            val listCommentId = listComments.map { it.id }
            if (listCommentId.contains(comment.id)) {
                commentInteractor.deleteComment(selectedNewId, comment)
            }
            if (mapSubComments.keys.contains(comment.id)) {
                commentInteractor.deleteSubComment(selectedNewId, findParentCommentId(comment.id), comment)
            }
        }
    }

    suspend fun findUserById(userId: String): UserInstance? {
        return getUserUseCase.invoke(userId, false)
    }

    fun getAllCommentsOfNew(newsId: String) {
        viewModelScope.launch(ioDispatcher) {
            val result = commentInteractor.getAllComments(newsId)
            if (result == null) {
                showToast("Cannot get all comments of this new. Try again!")
            } else {
                listComments.clear()
                mapSubComments.clear()
                _likeCountList.value.clear()

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
    }

    fun clearCommentList() {
        _allComments.value.clear()
    }
}
