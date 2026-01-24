package com.minhtu.firesocialmedia.group.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroup
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroupViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test

private class RepoForExplore : BaseFakeGroupRepository() {
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> =
        (1..5).map { GroupInstance(id = "g$it", name = "Group $it") }
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> =
        (1..3).map { GroupInstance(id = "f$it", name = "Feat $it") }
}

class ExploreGroupScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val repo = RepoForExplore()
        val vm = ExploreGroupViewModel(
            FetchRecommendGroupsUseCase(repo),
            FetchFeatureGroupsUseCase(repo)
        )
        val searchVm = SearchViewModel()
        val user = UserInstance(uid = "u1")

        composeRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                ExploreGroup.ExploreGroupScreen(
                    currentUser = user,
                    localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                    exploreGroupViewModel = vm,
                    searchViewModel = searchVm,
                    onNavigateBack = {},
                    onNavigateToGroupDetails = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


