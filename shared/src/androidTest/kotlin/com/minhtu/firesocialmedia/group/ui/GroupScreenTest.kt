package com.minhtu.firesocialmedia.group.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.Group
import org.junit.Rule
import org.junit.Test

class GroupScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_main_group_buttons() {
        val user = UserInstance(uid = "u1")
        composeRule.setContent {
            Group.GroupScreen(
                currentUser = user,
                onNavigateToCreateGroupScreen = {},
                onNavigateToExploreGroupScreen = {},
                onNavigateToSelectGroupScreen = {}
            )
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_CREATE_GROUP_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_FIND_GROUP_BUTTON).assertIsDisplayed()
    }
}


