package com.minhtu.firesocialmedia.presentation.exploregroup

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExploreGroupViewModel(
    private val fetchRecommendGroupsUseCase : FetchRecommendGroupsUseCase,
    private val fetchFeatureGroupsUseCase : FetchFeatureGroupsUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val recommendPageSize = 10
    private val featurePageSize = 10

    private var currentRecommendLimit = recommendPageSize
    private var currentFeatureLimit = featurePageSize

    private var isLoadingRecommend = false
    private var isLoadingFeature = false

    private var endReachedRecommend = false
    private var endReachedFeature = false

    private val _fetchRecommendGroups = MutableStateFlow<List<GroupInstance>>(emptyList())
    var fetchRecommendGroups = _fetchRecommendGroups.asStateFlow()
    fun loadInitialRecommendGroups(currentUser : UserInstance, query : String) {
        currentRecommendLimit = recommendPageSize
        endReachedRecommend = false
        _fetchRecommendGroups.value = emptyList()
        loadMoreRecommendGroups(currentUser, query)
    }
    fun loadMoreRecommendGroups(currentUser : UserInstance, query : String) {
        if (isLoadingRecommend || endReachedRecommend) return
        isLoadingRecommend = true
        viewModelScope.launch(ioDispatcher) {
            val result = fetchRecommendGroupsUseCase.invoke(currentRecommendLimit)
            val filtered = result
                .filter { currentUser.uid !in it.members }
                .filter { query.isBlank() || it.name.contains(query, true) }
            _fetchRecommendGroups.value = filtered
            endReachedRecommend = result.size < currentRecommendLimit
            if (!endReachedRecommend) currentRecommendLimit += recommendPageSize
            isLoadingRecommend = false
        }
    }

    fun resetFetchRecommendGroups() {
        _fetchRecommendGroups.value = emptyList()
    }

    private val _fetchFeatureGroups = MutableStateFlow<List<GroupInstance>>(emptyList())
    var fetchFeatureGroups = _fetchFeatureGroups.asStateFlow()
    fun loadInitialFeatureGroups(currentUser : UserInstance, query : String) {
        currentFeatureLimit = featurePageSize
        endReachedFeature = false
        _fetchFeatureGroups.value = emptyList()
        loadMoreFeatureGroups(currentUser, query)
    }
    fun loadMoreFeatureGroups(currentUser : UserInstance, query : String) {
        if (isLoadingFeature || endReachedFeature) return
        isLoadingFeature = true
        viewModelScope.launch(ioDispatcher) {
            val result = fetchFeatureGroupsUseCase.invoke(currentFeatureLimit)
            //Only show the groups that current user is not the member
            val filtered = result
                .filter { currentUser.uid !in it.members }
                .filter { query.isBlank() || it.name.contains(query, true) }
                .shuffled()
            _fetchFeatureGroups.value = filtered
            endReachedFeature = result.size < currentFeatureLimit
            if (!endReachedFeature) currentFeatureLimit += featurePageSize
            isLoadingFeature = false
        }
    }

    fun resetFetchFeatureGroups() {
        _fetchFeatureGroups.value = emptyList()
    }
}