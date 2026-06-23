package com.minhtu.firesocialmedia.feature.comment.presentation.comment

import androidx.compose.runtime.mutableStateOf
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.core.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.platform.createMessageForServer
import com.minhtu.firesocialmedia.platform.generateRandomId
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.getRandomIdForNotification
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.sendMessageToServer
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModelContract
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

class CommentFeatureViewModel(
    private val commentInteractor: CommentInteractor,
    private val getUserUseCase: GetUserUseCase,
    private val saveLikedCommentsUseCase: SaveLikedCommentsUseCase,
    private val saveNotificationToDatabaseUseCase: SaveNotificationToDatabaseUseCase,
    private val updateCommentCountForNewUseCase: UpdateCommentCountForNewUseCase,
    private val updateReplyCountForCommentUseCase: UpdateReplyCountForCommentUseCase,
    private val updateLikeCountForCommentUseCase: UpdateLikeCountForCommentUseCase,
    private val updateLikeCountForSubCommentUseCase: UpdateLikeCountForSubCommentUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), CommentViewModelContract {

    var listComments: ArrayList<CommentInstance> = ArrayList()
    var mapSubComments: HashMap<String, CommentInstance> = HashMap()

    private val _allComments: MutableStateFlow<ArrayList<CommentInstance>> = MutableStateFlow(ArrayList())
    override val allComments = _allComments.asStateFlow()

    private fun updateComments(comments: ArrayList<CommentInstance>) {
        _allComments.value.clear()
        _allComments.value = ArrayList(comments)
    }

    private val _message = MutableStateFlow("")
    override val messageFlow = _message.asStateFlow()
    override var message: String
        get() = _message.value
        set(value) {
            _message.value = value
        }

    override fun updateMessage(input: String) {
        _message.value = input
    }

    var image: String = ""

    fun updateImage(input: String) {
        image = input
    }

    private val _createCommentStatus: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val createCommentStatus = _createCommentStatus.asStateFlow()

    override fun sendComment(currentUser: UserInstance, selectedNew: NewsInstance) {
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
                                selectedNew.id,
                                commentRandomId,
                                commentInstance
                            )

                            updateCommentCountForNewUseCase.invoke(
                                selectedNew.id,
                                listComments.size
                            )
                            updateMessage("")

                            saveAndSendNotification(currentUser, selectedNew)
                        } catch (e: Exception) {
                            logMessage("sendComment") { "Error when sendComment: ${e.message}" }
                        }
                    }
                } else {
                    if (message.isNotEmpty()) {
                        onReplyComment(_commentBeReplied.value!!, currentUser, selectedNew)
                    }
                    updateCommentBeReplied(null)
                }
            }
        }
    }

    override fun resetCommentStatus() {
        _createCommentStatus.value = null
    }

    private suspend fun saveAndSendNotification(currentUser: UserInstance, selectedNew: NewsInstance) {
        val notiContent = "${currentUser.name} commented in your post!"
        val notification = NotificationInstance(
            getRandomIdForNotification(),
            notiContent,
            currentUser.image,
            currentUser.uid,
            getCurrentTime(),
            NotificationType.COMMENT,
            selectedNew.id
        )
        val poster = getUserUseCase.invoke(selectedNew.posterId, false)
        saveNotification(notification, poster ?: return)
        val tokenList = arrayListOf(poster.token)
        sendMessageToServer(createMessageForServer(notiContent, tokenList, currentUser, "BASIC"))
    }

    private suspend fun saveNotification(notification: NotificationInstance, friend: UserInstance) {
        try {
            friend.addNotification(notification)
            saveNotificationToDatabaseUseCase.invoke(friend.uid, friend.notifications)
        } catch (_: Exception) {
        }
    }

    override fun copyToClipboard(text: String, platform: PlatformContext) {
        viewModelScope.launch(ioDispatcher) {
            platform.clipboard.copy(text)
        }
    }

    private val _commentBeReplied = MutableStateFlow<CommentInstance?>(null)
    override val commentBeReplied = _commentBeReplied.asStateFlow()

    override fun updateCommentBeReplied(value: CommentInstance?) {
        _commentBeReplied.value = value
    }

    private fun onReplyComment(currentComment: CommentInstance, currentUser: UserInstance, selectedNew: NewsInstance) {
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
                        selectedNew.id,
                        currentComment.id,
                        commentInstance
                    )

                    updateReplyCountForCommentUseCase.invoke(
                        selectedNew.id,
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
    override val likedComments = _likedComments.asStateFlow()
    private var likeCache: HashMap<String, Int> = HashMap()
    private var unlikeCache: ArrayList<String> = ArrayList()
    private var updateLikeJob: Job? = null
    private val _likeCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    override val likeCountList = _likeCountList.asStateFlow()

    private fun addLikeCountData(commentId: String, likeCount: Int) {
        _likeCountList.value[commentId] = likeCount
    }

    override fun onLikeComment(selectedNew: NewsInstance, currentUser: UserInstance, comment: CommentInstance) {
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
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), selectedNew, currentUser)
        }
    }

    val sendLikeDataStatus = mutableStateOf(false)

    private suspend fun sendLikeUpdatesToFirebase(
        likeCountList: HashMap<String, Int>,
        selectedNew: NewsInstance,
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
                        selectedNew.id,
                        likedComment,
                        likeCountList[likedComment]!!
                    )
                }
                if (mapSubComments.keys.contains(likedComment)) {
                    updateLikeCountForSubCommentUseCase.invoke(
                        selectedNew.id,
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
                        selectedNew.id,
                        unlikedComment,
                        likeCountList[unlikedComment]!!
                    )
                }
                if (mapSubComments.keys.contains(unlikedComment)) {
                    updateLikeCountForSubCommentUseCase.invoke(
                        selectedNew.id,
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

    override fun updateLikeStatus() {
        _likedComments.value = HashMap(likeCache)
    }

    override fun updateLikeCommentOfCurrentUser(currentUser: UserInstance) {
        likeCache = currentUser.likedComments
    }

    override fun onDeleteComment(selectedNew: NewsInstance, comment: CommentInstance) {
        val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        backgroundScope.launch {
            val listCommentId = listComments.map { it.id }
            if (listCommentId.contains(comment.id)) {
                commentInteractor.deleteComment(selectedNew.id, comment)
            }
            if (mapSubComments.keys.contains(comment.id)) {
                commentInteractor.deleteSubComment(selectedNew.id, findParentCommentId(comment.id), comment)
            }
        }
    }

    override suspend fun findUserById(userId: String): UserInstance? {
        return getUserUseCase.invoke(userId, false)
    }

    override fun getAllCommentsOfNew(newsId: String) {
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

    override fun clearCommentList() {
        _allComments.value.clear()
    }
}
