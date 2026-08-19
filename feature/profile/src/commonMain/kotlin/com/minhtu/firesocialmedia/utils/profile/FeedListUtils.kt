package com.minhtu.firesocialmedia.utils.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.profile.TestTag
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.profile.SessionViewModel
import com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel
import com.minhtu.firesocialmedia.profile.utils.UiUtils.Companion.NewsCard
import com.minhtu.firesocialmedia.profile.utils.UiUtils.Companion.NewsCardPlaceholder
import com.minhtu.firesocialmedia.profile.utils.UiUtils.Companion.NewsCardWithSharedContent
import com.minhtu.firesocialmedia.profile.utils.UiUtils.Companion.ThreeDotsLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedListUtils {
    companion object {
        @Composable
        fun LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
            localImageLoaderValue: ProvidedValue<*>,
            listState: LazyListState,
            engagementViewModel: EngagementViewModel,
            sessionViewModel: SessionViewModel,
            list: List<NewsInstance>,
            onNavigateToUploadNews: (updateNew: NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            showBottomSheet: (NewsInstance) -> Unit,
            // UserInformation paginates via its own UserInformationViewModel
            // (isLoadingMoreUserNews), not engagementViewModel, so the loading row below can't
            // just read engagementViewModel.isLoadingMore like Group's feed does. Callers that
            // drive their own pagination pass their own flag here; callers that page through
            // engagementViewModel (e.g. Group) can rely on the default.
            isLoadingMore: Boolean = engagementViewModel.isLoadingMore.value) {
            val coroutineScope = rememberCoroutineScope()
            val likeStatus by engagementViewModel.likedPosts.collectAsState()
            val likeCountList = engagementViewModel.likeCountList.collectAsState()
            val commentCountList = engagementViewModel.commentCountList.collectAsState()
            val loadedUsers by sessionViewModel.loadedUserState.collectAsState()
            // The root Scaffold only reserves the top safe-drawing inset (see SetUpNavigation in
            // Navigation.kt), so on screens without their own bottom bar - like this one - the
            // system navigation bar can overlap the last item's like/comment row. Pad the list's
            // content (not the list itself, which would just clip scrolling) by the nav bar's
            // height so the last post always comes to rest above it.
            val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = listState,
                contentPadding = PaddingValues(bottom = navigationBarPadding.calculateBottomPadding() + 16.dp)
            ) {
                items(
                    items = list,
                    key = { it.id }
                ) { news ->
                    var isVisible by remember(news.id) { mutableStateOf(true) }
                    val sharedNewMap by engagementViewModel.sharedNewsById.collectAsState()
                    LaunchedEffect(news.shareContentId) {
                        engagementViewModel.ensureSharedNew(news.shareContentId)
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        val user = loadedUsers[news.posterId]
                        if(user != null) {
                            when {
                                news.shareContentId.isEmpty() -> {
                                NewsCard(
                                    news = news,
                                    user = user,
                                    isLiked = likeStatus.containsKey(news.id),
                                    likeCountList.value,
                                    commentCountList.value,
                                    localImageLoaderValue,
                                    likeCommentAndShareButtonEnable = true,
                                    hasDropdownMenu = true,
                                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                    onNavigateToUserInformation = onNavigateToUserInformation,
                                    engagementViewModel = engagementViewModel,
                                    sessionViewModel = sessionViewModel,
                                    listState = listState,
                                    onDelete = { action, deletedNews ->
                                        isVisible = false
                                        coroutineScope.launch {
                                            delay(250)
                                            if (action == "Delete") {
                                                engagementViewModel.deleteNews(deletedNews)
                                            }
                                        }
                                    },
                                    onNavigateToUploadNews,
                                    showBottomSheet
                                )
                                }
                                else -> {
                                val sharedNew = sharedNewMap[news.shareContentId]
                                if(sharedNew != null) {
                                    NewsCardWithSharedContent(
                                        news = news,
                                        sharedNew = sharedNew,
                                        user = user,
                                        isLiked = likeStatus.containsKey(news.id),
                                        likeCountList.value,
                                        commentCountList.value,
                                        localImageLoaderValue,
                                        onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        engagementViewModel = engagementViewModel,
                                        sessionViewModel = sessionViewModel,
                                        listState = listState,
                                        onDelete = { action, deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                if (action == "Delete") {
                                                    engagementViewModel.deleteNews(deletedNews)
                                                }
                                            }
                                        },
                                        onNavigateToUploadNews,
                                        showBottomSheet
                                    )
                                } else {
                                    NewsCardPlaceholder()
                                }
                                }
                            }
                        } else {
                            NewsCardPlaceholder()
                        }
                    }
                }

                // Loading row at the bottom
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThreeDotsLoading(
                                modifier = Modifier.padding(bottom = 10.dp),
                                dotSize = 10.dp,
                                spaceBetween = 5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
