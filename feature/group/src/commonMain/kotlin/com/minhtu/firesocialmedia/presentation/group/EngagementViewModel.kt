package com.minhtu.firesocialmedia.presentation.group

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.news.group.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.UpdateLikeCountForNewUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EngagementViewModel(
    private val updateLikeCountForNewUseCase: UpdateLikeCountForNewUseCase,
    private val saveLikedPostUseCase: SaveLikedPostUseCase,
    private val saveLikeNotificationUseCase: SaveLikeNotificationUseCase,
    private val getNewByIdUseCase: GetNewByIdUseCase,
    private val deleteNewsUseCase: DeleteNewsUseCase,
    private val deletePollUseCase: DeletePollUseCase
) : ViewModel() {

    private var likeCache: HashMap<String, Int> = HashMap()
    private var unlikeCache: ArrayList<String> = ArrayList()
    private var updateLikeJob: Job? = null

    private val _likedPosts = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val likedPosts: StateFlow<HashMap<String, Int>> = _likedPosts.asStateFlow()

    private val _likeCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val likeCountList: MutableStateFlow<HashMap<String, Int>> = _likeCountList

    private val _commentCountList = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val commentCountList: MutableStateFlow<HashMap<String, Int>> = _commentCountList

    private val _commentStatus = MutableStateFlow<NewsInstance?>(null)
    val commentStatus: StateFlow<NewsInstance?> = _commentStatus.asStateFlow()

    private val _sharedNewsById = MutableStateFlow<Map<String, NewsInstance?>>(emptyMap())
    val sharedNewsById: StateFlow<Map<String, NewsInstance?>> = _sharedNewsById.asStateFlow()

    val isLoadingMore = mutableStateOf(false)

    fun seedLikedPosts(currentUser: UserInstance?) {
        likeCache = HashMap(currentUser?.likedPosts ?: HashMap())
        _likedPosts.value = HashMap(likeCache)
    }

    fun addLikeCountData(newsId: String, likeCount: Int) {
        _likeCountList.value[newsId] = likeCount
    }

    fun addCommentCountData(newsId: String, commentCount: Int) {
        _commentCountList.value[newsId] = commentCount
    }

    fun clickLikeButton(newsId: String, posterId: String, currentUser: UserInstance?) {
        val isLiked = likeCache[newsId] == 1
        if (isLiked) {
            likeCache.remove(newsId)
            unlikeCache.add(newsId)
            _likeCountList.value[newsId]?.let { _likeCountList.value[newsId] = it - 1 }
        } else {
            likeCache[newsId] = 1
            unlikeCache.remove(newsId)
            _likeCountList.value[newsId] = (_likeCountList.value[newsId] ?: 0) + 1
        }
        _likedPosts.value = HashMap(likeCache)

        updateLikeJob?.cancel()
        val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        updateLikeJob = backgroundScope.launch {
            sendLikeUpdatesToFirebase(HashMap(_likeCountList.value), currentUser, newsId, posterId)
        }
    }

    private suspend fun sendLikeUpdatesToFirebase(
        likeCountListSnapshot: HashMap<String, Int>,
        currentUser: UserInstance?,
        clickedNewsId: String,
        posterId: String
    ) {
        if (currentUser == null) return
        currentUser.likedPosts = likeCache
        saveLikedPostUseCase.invoke(currentUser.uid, likeCache)
        try {
            if (likeCache.containsKey(clickedNewsId)) {
                likeCountListSnapshot[clickedNewsId]?.let { count ->
                    updateLikeCountForNewUseCase.invoke(clickedNewsId, count)
                    saveLikeNotificationUseCase.invoke(currentUser, posterId, clickedNewsId)
                }
            }
            for (unlikedNewsId in unlikeCache) {
                likeCountListSnapshot[unlikedNewsId]?.let { count ->
                    updateLikeCountForNewUseCase.invoke(unlikedNewsId, count)
                }
            }
        } catch (e: Exception) {
            // swallow, matching HomeViewModel's existing best-effort background sync behavior
        }
    }

    fun updateLikeStatus() {
        _likedPosts.value = HashMap(likeCache)
    }

    fun clickCommentButton(newsInstance: NewsInstance) {
        _commentStatus.value = newsInstance
    }

    fun resetCommentStatus() {
        _commentStatus.value = null
    }

    suspend fun ensureSharedNew(sharedNewId: String) {
        if (sharedNewId.isBlank()) return
        if (_sharedNewsById.value.containsKey(sharedNewId)) return
        val new = getNewByIdUseCase.invoke(sharedNewId)
        _sharedNewsById.value = _sharedNewsById.value + (sharedNewId to new)
    }

    suspend fun deleteNews(news: NewsInstance) {
        deleteNewsUseCase.invoke(news)
    }

    suspend fun deletePoll(newsId: String, pollId: String, groupId: String): Boolean {
        return deletePollUseCase.invoke(newsId, pollId, groupId)
    }
}
