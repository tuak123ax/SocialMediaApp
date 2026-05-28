package com.minhtu.firesocialmedia.presentation.postinformation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.isDefaultNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.comment.Comment
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.core.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.NewsCardPlaceholder
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.NewsCardUnavailable
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.NewsCardWithSharedContent
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class PostInformation {
    companion object{
        @Composable
        fun PostInformationScreen(modifier : Modifier,
                                  platform : PlatformContext,
                                  localImageLoaderValue : ProvidedValue<*>,
                                  news: NewsInstance,
                                  onNavigateToShowImageScreen: (image: String) -> Unit,
                                  onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                                  onNavigateBack : () -> Unit,
                                  homeViewModel: HomeViewModel,
                                  commentViewModel : CommentViewModel = koinViewModel(),
                                  postInformationViewModel: PostInformationViewModel,
                                  onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
                                  onShareNews : (String, NewsInstance) -> Unit
        ) {
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }
            val sharedNew by postInformationViewModel.sharedNew.collectAsState()
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.containsKey(news.id)
            LaunchedEffect(Unit) {
                homeViewModel.updateLikeStatus()
                if(homeViewModel.currentUser == null) {
                    homeViewModel.getCurrentUserAndFriends()
                }
                //Get shared new from homeViewModel's list new
                val shareNewMatched = homeViewModel.listNews.filter { it.id == news.shareContentId }
                if(shareNewMatched.isNotEmpty()) {
                    postInformationViewModel.updateShareNew(shareNewMatched[0])
                } else {
                    //Cannot find shared new in homeViewModel's list new
                    //Try to get from local DB and remote DB
                    postInformationViewModel.getSharedNew(news.shareContentId)
                }
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()

            var user by remember { mutableStateOf<UserInstance?>(null) }

            LaunchedEffect(news.posterId) {
                user = homeViewModel.findUserById(news.posterId)
            }
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    if(user != null) {
                        if(news.shareContentId.isEmpty()) {
                            UiUtils.BackAndMoreOptionsRow {
                                postInformationViewModel.resetShareNew()
                                onNavigateBack()
                            }
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                                    .padding(10.dp).fillMaxWidth()
                                    .clickable {
                                        if (user == null) {
                                            user = homeViewModel.currentUser
                                        }
                                        if (user != null) {
                                            onNavigateToUserInformation(user)
                                        }
                                    }) {
                                CompositionLocalProvider(
                                    localImageLoaderValue
                                ) {
                                    AutoSizeImage(
                                        news.avatar.toStorageUrl(),
                                        contentDescription = "Poster Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .testTag(TestTag.TAG_POSTER_AVATAR)
                                            .semantics {
                                                contentDescription = TestTag.TAG_POSTER_AVATAR
                                            }
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = news.posterName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                    Text(
                                        text = convertTimeToDateString(news.timePosted),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            UiUtils.ExpandableText(news.message)
                            if (news.image.isNotEmpty()) {
                                CompositionLocalProvider(
                                    localImageLoaderValue
                                ) {
                                    AutoSizeImage(
                                        news.image.toStorageUrl(),
                                        contentDescription = "Image",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .padding(5.dp)
                                            .clickable {
                                                onNavigateToShowImageScreen(news.image)
                                            }
                                            .testTag(TestTag.TAG_POST_IMAGE)
                                            .semantics {
                                                contentDescription = TestTag.TAG_POST_IMAGE
                                            }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "Like: ${likeCountList.value[news.id] ?: 0}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(2.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Comment: ${commentCountList.value[news.id] ?: 0}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(bottom = 5.dp, start = 10.dp, end = 10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        homeViewModel.clickLikeButton(news)
                                    },
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    colors = if (isLiked) ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)
                                    else ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.height(35.dp).weight(1f)
                                        .testTag(TestTag.TAG_BUTTON_LIKE)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_LIKE
                                        }) {
                                    CrossPlatformIcon(
                                        icon = "like",
                                        backgroundColor = if (isLiked) MaterialTheme.colorScheme.primaryContainer.toHex() else MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "Like",
                                        tint = if(isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(end = 5.dp)
                                    )
                                    Text(text = if (isLiked) "Liked" else "Like", color = MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        homeViewModel.clickCommentButton(news)
                                    },
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.height(35.dp).weight(1f)
                                        .testTag(TestTag.TAG_BUTTON_COMMENT)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_COMMENT
                                        }) {
                                    CrossPlatformIcon(
                                        icon = "comment",
                                        backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "Comment",
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(end = 5.dp)
                                    )
                                    Text(text = "Comment", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, start = 10.dp, end = 10.dp)) {
                                Button(onClick = {
                                    //Show bottom sheet
                                    newToBeShared = news
                                    showBottomSheet = true
                                },
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.height(35.dp).weight(1f)
                                        .testTag(TestTag.TAG_BUTTON_SHARE)
                                        .semantics{
                                            contentDescription = TestTag.TAG_BUTTON_SHARE
                                        }){
                                    CrossPlatformIcon(
                                        icon = "share",
                                        backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                        contentDescription = "Share",
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(end = 5.dp)
                                    )
                                    Text(text = "Share", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        } else {
                            if(sharedNew != null) {
                                if(sharedNew!!.isDefaultNewsInstance()) {
                                    NewsCardUnavailable()
                                } else {
                                    NewsCardWithSharedContent(
                                        news = news,
                                        sharedNew = sharedNew!!,
                                        user = user!!,
                                        isLiked = likeStatus.containsKey(news.id),
                                        likeCountList.value,
                                        commentCountList.value,
                                        localImageLoaderValue,
                                        onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                                        onNavigateToUserInformation = onNavigateToUserInformation,
                                        homeViewModel = homeViewModel,
                                        listState = listState,
                                        onDelete = { action, deletedNews ->
                                            coroutineScope.launch {
                                                delay(250)
                                                homeViewModel.deleteOrHideNew(action, deletedNews)
                                            }
                                        },
                                        onNavigateToUploadNews,
                                        showBottomSheet = { news ->
                                            newToBeShared = news
                                            showBottomSheet = true
                                        }
                                    )
                                }
                            } else {
                                NewsCardPlaceholder()
                            }
                        }
                    } else {
                        NewsCardPlaceholder()
                    }

                    //Show comment screen at the end of this page
                    if(homeViewModel.currentUser != null) {
                        Comment.CommentScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.surface),
                            platform = platform,
                            localImageLoaderValue = localImageLoaderValue,
                            showCloseIcon = false,
                            commentViewModel = commentViewModel,
                            currentUser = homeViewModel.currentUser!!,
                            selectedNew = news,
                            onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                            onNavigateToUserInformation = onNavigateToUserInformation,
                            onNavigateToHomeScreen = {
                                postInformationViewModel.resetShareNew()
                                onNavigateBack()
                            }
                        )
                    }
                }

                if(showBottomSheet) {
                    UiUtils.ShareBottomSheet(
                        deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onClick = { message ->
                            showBottomSheet = false
                            //Continue with share process
                            if(newToBeShared != null) {
                                onShareNews(message, newToBeShared!!)
                            } else {
                                showToast("Cannot share now. Please try again!!!")
                            }
                        }
                    )
                }
            }
        }

        fun getScreenName() : String {
            return "PostInformationScreen"
        }
    }
}