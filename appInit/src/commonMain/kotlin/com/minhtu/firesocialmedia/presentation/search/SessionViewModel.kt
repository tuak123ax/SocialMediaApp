package com.minhtu.firesocialmedia.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance as HomeNewsInstance
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val searchUserByNameUseCase: SearchUserByNameUseCase,
    private val newsInteractor: NewsInteractor
) : ViewModel() {
    var currentUser: UserInstance? = null
        private set

    var loadedUsersCache: HashMap<String, UserInstance?> = HashMap()
    private val _loadedUserState = MutableStateFlow<Map<String, UserInstance?>>(emptyMap())
    val loadedUserState: StateFlow<Map<String, UserInstance?>> = _loadedUserState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = getCurrentUserUidUseCase.invoke() ?: return@launch
            currentUser = getUserUseCase.invoke(uid, true)
        }
    }

    suspend fun findUserById(userId: String): UserInstance? {
        loadedUsersCache[userId]?.let { return it }
        val user = getUserUseCase.invoke(userId, false)
        loadedUsersCache[userId] = user
        _loadedUserState.value = HashMap(loadedUsersCache)
        return user
    }

    fun ensureUserLoaded(userId: String) {
        if (loadedUsersCache.containsKey(userId)) return
        viewModelScope.launch {
            findUserById(userId)
        }
    }

    suspend fun searchUserByName(name: String): List<UserInstance> {
        return searchUserByNameUseCase.invoke(name)
    }

    // News search (Posts tab). Firebase Realtime DB has no full-text search, so this pages
    // through the same NewsInteractor.pageLatest cursor Home uses (10 raw posts at a time),
    // filtering each page client-side. This state is deliberately independent from
    // HomeViewModel's own listNews/lastTimePosted/lastKey/hasMoreData - searching here must not
    // depend on (or disturb) however far the user has scrolled Home's own feed.
    var newsSearchResults by mutableStateOf<List<HomeNewsInstance>>(emptyList())
        private set
    var isLoadingMoreNews by mutableStateOf(false)
        private set
    var hasMoreNews by mutableStateOf(true)
        private set
    private var newsSearchQuery: String = ""
    private var newsLastTimePosted: Double? = null
    private var newsLastKey: String? = null
    // Bumped on every new query so a fetch still in flight for a stale query can detect it's
    // been superseded and avoid clobbering newer results.
    private var searchGeneration = 0

    private val NEWS_SEARCH_PAGE_SIZE = 10
    // Safety cap on raw pages fetched per call, in case a query matches nothing for many pages
    // in a row - avoids looping through the entire dataset in one call.
    private val MAX_PAGES_PER_LOAD = 20

    fun searchNews(query: String) {
        if (query == newsSearchQuery) return
        newsSearchQuery = query
        newsLastTimePosted = null
        newsLastKey = null
        hasMoreNews = true
        newsSearchResults = emptyList()
        searchGeneration++
        isLoadingMoreNews = false
        if (query.isNotEmpty()) {
            fetchMoreMatches(query, searchGeneration)
        }
    }

    fun loadMoreMatchingNews() {
        if (isLoadingMoreNews || !hasMoreNews || newsSearchQuery.isEmpty()) return
        fetchMoreMatches(newsSearchQuery, searchGeneration)
    }

    private fun fetchMoreMatches(query: String, generation: Int) {
        isLoadingMoreNews = true
        viewModelScope.launch {
            try {
                val existingIds = newsSearchResults.mapTo(HashSet()) { it.id }
                val matches = ArrayList<HomeNewsInstance>()
                var pagesFetched = 0
                while (matches.size < NEWS_SEARCH_PAGE_SIZE &&
                    hasMoreNews &&
                    pagesFetched < MAX_PAGES_PER_LOAD &&
                    generation == searchGeneration
                ) {
                    val page = newsInteractor.pageLatest(NEWS_SEARCH_PAGE_SIZE, newsLastTimePosted, newsLastKey)
                    pagesFetched++
                    if (page == null) {
                        // Same caveat as HomeViewModel.getLatestNews(): HomeMapper.toDomain()
                        // collapses a legitimate final page into a null result (its cursor
                        // fields come back null), so treat null as "no more data", not a failure.
                        hasMoreNews = false
                        break
                    }
                    page.news?.forEach { new ->
                        if (new.message.contains(query, ignoreCase = true) && existingIds.add(new.id)) {
                            matches.add(new)
                        }
                    }
                    if (page.lastTimePostedValue == null) {
                        hasMoreNews = false
                    } else {
                        newsLastTimePosted = page.lastTimePostedValue
                        newsLastKey = page.lastKeyValue
                    }
                }
                if (generation == searchGeneration && matches.isNotEmpty()) {
                    newsSearchResults = newsSearchResults + matches
                }
            } finally {
                if (generation == searchGeneration) {
                    isLoadingMoreNews = false
                }
            }
        }
    }
}
