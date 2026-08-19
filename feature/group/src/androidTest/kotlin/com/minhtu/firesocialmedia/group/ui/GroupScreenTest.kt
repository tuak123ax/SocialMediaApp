package com.minhtu.firesocialmedia.group.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.group.Group
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
                paddingValues = PaddingValues(0.dp),
                onNavigateToCreateGroupScreen = {},
                onNavigateToExploreGroupScreen = {},
                onNavigateToSelectGroupScreen = {},
                onNavigateBack = {}
            )
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_CREATE_GROUP_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_FIND_GROUP_BUTTON).assertIsDisplayed()
    }
}





