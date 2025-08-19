package com.minhtu.firesocialmedia.presentation.postinformation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.presentation.comment.Comment
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage

class PostInformation {
    companion object{
        @Composable
        fun PostInformationScreen(platform : PlatformContext,
                                  news: NewsInstance,
                                  onNavigateToShowImageScreen: (image: String) -> Unit,
                                  onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                                  onNavigateToHomeScreen: () -> Unit,
                                  homeViewModel: HomeViewModel,
                                  commentViewModel : CommentViewModel,
                                  navController : NavigationHandler
        ) {
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.contains(news.id)
            LaunchedEffect(likeStatus) {
                homeViewModel.updateLikeStatus()
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()

            var user by remember { mutableStateOf<UserInstance?>(null) }

            LaunchedEffect(news.posterId) {
                user = homeViewModel.findUserById(news.posterId, platform)
            }
            Column(
                modifier = Modifier.Companion.fillMaxSize().background(Color.Companion.White),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                UiUtils.Companion.BackAndMoreOptionsRow(navController)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.Companion.background(color = Color.Companion.White)
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
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            news.avatar,
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.Companion.Crop,
                            modifier = Modifier.Companion
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag(TestTag.Companion.TAG_POSTER_AVATAR)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_POSTER_AVATAR
                                }
                        )
                    }
                    Spacer(modifier = Modifier.Companion.width(10.dp))
                    Column {
                        Text(
                            text = news.posterName,
                            color = Color.Companion.Black,
                            modifier = Modifier.Companion.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = convertTimeToDateString(news.timePosted),
                            color = Color.Companion.Gray,
                            modifier = Modifier.Companion.padding(horizontal = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.Companion.weight(1f))
                }
                UiUtils.Companion.ExpandableText(news.message)
                if (news.image.isNotEmpty()) {
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            news.image,
                            contentDescription = "Image",
                            contentScale = ContentScale.Companion.Fit,
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(5.dp)
                                .clickable {
                                    onNavigateToShowImageScreen(news.image)
                                }
                                .testTag(TestTag.Companion.TAG_POST_IMAGE)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_POST_IMAGE
                                }
                        )
                    }
                }
                Row(
                    modifier = Modifier.Companion.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Like: ${likeCountList.value[news.id] ?: 0}",
                        fontSize = 12.sp,
                        color = Color.Companion.Black,
                        modifier = Modifier.Companion.padding(2.dp)
                    )
                    Spacer(modifier = Modifier.Companion.weight(1f))
                    Text(
                        text = "Comment: ${commentCountList.value[news.id] ?: 0}",
                        fontSize = 12.sp,
                        color = Color.Companion.Black,
                        modifier = Modifier.Companion.padding(2.dp)
                    )
                }
                Row(
                    modifier = Modifier.Companion.fillMaxWidth()
                        .padding(bottom = 5.dp, start = 10.dp, end = 10.dp)
                ) {
                    Button(
                        onClick = {
                            homeViewModel.clickLikeButton(news, platform)
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = if (isLiked) ButtonDefaults.buttonColors(Color.Companion.Cyan)
                        else ButtonDefaults.buttonColors(Color.Companion.White),
                        modifier = Modifier.Companion.height(35.dp).weight(1f)
                            .testTag(TestTag.Companion.TAG_BUTTON_LIKE)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_BUTTON_LIKE
                            }) {
                        CrossPlatformIcon(
                            icon = "like",
                            backgroundColor = if (isLiked) "#00FFFF" else "#FFFFFFFF",
                            contentDescription = "Like",
                            modifier = Modifier.Companion
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = if (isLiked) "Liked" else "Like", color = Color.Companion.Black)
                    }
                    Spacer(modifier = Modifier.Companion.width(10.dp))
                    Button(
                        onClick = {
                            homeViewModel.clickCommentButton(news)
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(Color.Companion.White),
                        modifier = Modifier.Companion.height(35.dp).weight(1f)
                            .testTag(TestTag.Companion.TAG_BUTTON_COMMENT)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_BUTTON_COMMENT
                            }) {
                        CrossPlatformIcon(
                            icon = "comment",
                            backgroundColor = "#FFFFFFFF",
                            contentDescription = "Comment",
                            modifier = Modifier.Companion
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "Comment", color = Color.Companion.Black)
                    }
                }

                //Show comment screen at the end of this page
                Comment.Companion.CommentScreen(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(color = Color.Companion.White),
                    platform,
                    showCloseIcon = false,
                    commentViewModel = commentViewModel,
                    currentUser = homeViewModel.currentUser!!,
                    selectedNew = news,
                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                    onNavigateToUserInformation = onNavigateToUserInformation,
                    onNavigateToHomeScreen = onNavigateToHomeScreen
                )
            }
        }

        fun getScreenName() : String {
            return "PostInformationScreen"
        }
    }
}