package com.minhtu.firesocialmedia.presentation.comment

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance

/**
 * UI renderer contract for comment screen implementation hosted in :feature:comment.
 */
interface CommentScreenApi {
    @Composable
    fun renderCommentScreen(
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
    )
}

