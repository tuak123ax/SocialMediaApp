package com.minhtu.firesocialmedia.utils.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.minhtu.firesocialmedia.constants.home.TestTag
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.comment.HomeCommentFeatureViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.home.utils.UiUtils.Companion.NewsCard
import com.minhtu.firesocialmedia.home.utils.UiUtils.Companion.NewsCardPlaceholder
import com.minhtu.firesocialmedia.home.utils.UiUtils.Companion.NewsCardWithSharedContent
import com.minhtu.firesocialmedia.home.utils.UiUtils.Companion.ThreeDotsLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedListUtils {
    companion object {
        @Composable
        fun LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
            localImageLoaderValue: ProvidedValue<*>,
            listState: LazyListState,
            homeViewModel: HomeViewModel,
            list: List<NewsInstance>,
            onNavigateToUploadNews: (updateNew: NewsInstance?) -> Unit,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            showBottomSheet: (NewsInstance) -> Unit,
            commentViewModel: HomeCommentFeatureViewModel? = null,
            platform: PlatformContext? = null,
            currentUser: UserInstance? = null) {
            val coroutineScope = rememberCoroutineScope()
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            val loadedUsers by homeViewModel.loadedUserState.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag(TestTag.TAG_POSTS_COLUMN)
                    .semantics{
                        contentDescription = TestTag.TAG_POSTS_COLUMN
                    },
                state = listState
            ) {
                items(
                    items = list,
                    key = { it.id }
                ) { news ->
                    var isVisible by remember(news.id) { mutableStateOf(true) }
                    val sharedNewMap by homeViewModel.sharedNewsById.collectAsState()
                    LaunchedEffect(news.shareContentId) {
                        homeViewModel.ensureSharedNew(news.shareContentId)
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
                                    homeViewModel = homeViewModel,
                                    listState = listState,
                                    onDelete = { action, deletedNews ->
                                        isVisible = false
                                        coroutineScope.launch {
                                            delay(250)
                                            homeViewModel.deleteOrHideNew(action, deletedNews.id)
                                        }
                                    },
                                    onNavigateToUploadNews,
                                    showBottomSheet,
                                    commentViewModel = commentViewModel,
                                    platform = platform,
                                    currentUser = currentUser
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
                                        homeViewModel = homeViewModel,
                                        listState = listState,
                                        onDelete = { action, deletedNews ->
                                            isVisible = false
                                            coroutineScope.launch {
                                                delay(250)
                                                homeViewModel.deleteOrHideNew(action, deletedNews.id)
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
                if (homeViewModel.isLoadingMore.value) {
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
