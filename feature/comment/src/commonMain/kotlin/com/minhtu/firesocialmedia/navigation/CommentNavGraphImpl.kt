package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureScreen
import com.minhtu.firesocialmedia.presentation.comment.CommentScreenApi
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModelContract

class CommentNavGraphImpl : CommentScreenApi {
    @Composable
    override fun renderCommentScreen(
        paddingValues: PaddingValues,
        modifier: Modifier,
        platform: PlatformContext,
        localImageLoaderValue: ProvidedValue<*>,
        showCloseIcon: Boolean,
        commentViewModel: CommentViewModelContract,
        currentUser: UserInstance,
        selectedNew: NewsInstance,
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
            selectedNew = selectedNew,
            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
            onNavigateToUserInformation = onNavigateToUserInformation,
            onNavigateToHomeScreen = onNavigateToHomeScreen
        )
    }
}
