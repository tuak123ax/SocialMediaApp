package com.minhtu.firesocialmedia.group.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.presentation.selectgroup.SelectGroup
import com.minhtu.firesocialmedia.presentation.selectgroup.SelectGroupViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test

class SelectGroupScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val vm = SelectGroupViewModel(GetAllGroupsUseCase(object : BaseFakeGroupRepository() {}))
        val searchVm = SearchViewModel()
        val user = UserInstance(uid = "u1").apply {
            groups["g1"] = GroupInstance(id = "g1", name = "G1")
            groups["g2"] = GroupInstance(id = "g2", name = "G2")
        }

        composeRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                SelectGroup.SelectGroupScreen(
                    currentUser = user,
                    selectGroupViewModel = vm,
                    searchViewModel = searchVm,
                    localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                    onNavigateBack = {},
                    onNavigateToCreateGroup = {},
                    onNavigateToSelectedGroup = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


