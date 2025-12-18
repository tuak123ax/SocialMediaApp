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
import androidx.compose.runtime.ProvidedValue
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
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.presentation.comment.Comment
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.ui.AutoSizeImage

class PostInformation {
    companion object{
        @Composable
        fun PostInformationScreen(modifier : Modifier,
                                  platform : PlatformContext,
                                  localImageLoaderValue : ProvidedValue<*>,
                                  news: NewsInstance,
                                  onNavigateToShowImageScreen: (image: String) -> Unit,
                                  onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                                  onNavigateToHomeScreen: (numberOfComments : Int) -> Unit,
                                  onNavigateBack : () -> Unit,
                                  homeViewModel: HomeViewModel,
                                  commentViewModel : CommentViewModel,
                                  postInformationViewModel: PostInformationViewModel
        ) {
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.containsKey(news.id)
            LaunchedEffect(Unit) {
                homeViewModel.updateLikeStatus()
                if(homeViewModel.currentUser == null) {
                    homeViewModel.getCurrentUserAndFriends()
                }
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()

            var user by remember { mutableStateOf<UserInstance?>(null) }

            LaunchedEffect(news.posterId) {
                user = homeViewModel.findUserById(news.posterId)
            }
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                UiUtils.BackAndMoreOptionsRow(onNavigateBack)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.background(color = Color.White)
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
                            news.avatar,
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
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                        text = convertTimeToDateString(news.timePosted),
                        color = Color.Gray,
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
                            news.image,
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
                        color = Color.Black,
                        modifier = Modifier.padding(2.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Comment: ${commentCountList.value[news.id] ?: 0}",
                        fontSize = 12.sp,
                        color = Color.Black,
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
                        colors = if (isLiked) ButtonDefaults.buttonColors(Color.Cyan)
                        else ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.height(35.dp).weight(1f)
                            .testTag(TestTag.TAG_BUTTON_LIKE)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_LIKE
                            }) {
                        CrossPlatformIcon(
                            icon = "like",
                            backgroundColor = if (isLiked) "#00FFFF" else "#FFFFFFFF",
                            contentDescription = "Like",
                            modifier = Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = if (isLiked) "Liked" else "Like", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            homeViewModel.clickCommentButton(news)
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.height(35.dp).weight(1f)
                            .testTag(TestTag.TAG_BUTTON_COMMENT)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_COMMENT
                            }) {
                        CrossPlatformIcon(
                            icon = "comment",
                            backgroundColor = "#FFFFFFFF",
                            contentDescription = "Comment",
                            modifier = Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "Comment", color = Color.Black)
                    }
                }

                //Show comment screen at the end of this page
                if(homeViewModel.currentUser != null) {
                    Comment.CommentScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White),
                        platform,
                        localImageLoaderValue,
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
        }

        fun getScreenName() : String {
            return "PostInformationScreen"
        }
    }
}