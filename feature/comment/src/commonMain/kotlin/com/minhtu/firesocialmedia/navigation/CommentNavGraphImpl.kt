package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.comment.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureScreen
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureViewModel

class CommentNavGraphImpl {
    @Composable
    fun renderCommentScreen(
        paddingValues: PaddingValues,
        modifier: Modifier,
        platform: PlatformContext,
        localImageLoaderValue: ProvidedValue<*>,
        showCloseIcon: Boolean,
        commentViewModel: CommentFeatureViewModel,
        currentUser: UserInstance,
        selectedNewId: String,
        selectedNewPosterId: String,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToHomeScreen: (numberOfComments: Int) -> Unit
    ) {
        CommentFeatureScreen.CommentScreen(
            paddingValues = paddingValues,
            modifier = modifier,
            platform = platform,
            localImageLoaderValue = localImageLoaderValue,
            showCloseIcon = showCloseIcon,
            commentViewModel = commentViewModel,
            currentUser = currentUser,
            selectedNewId = selectedNewId,
            selectedNewPosterId = selectedNewPosterId,
            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
            onNavigateToUserInformation = onNavigateToUserInformation,
            onNavigateToHomeScreen = onNavigateToHomeScreen
        )
    }
}
