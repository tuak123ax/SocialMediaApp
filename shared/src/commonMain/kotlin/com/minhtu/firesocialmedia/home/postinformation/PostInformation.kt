package com.minhtu.firesocialmedia.home.postinformation

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.convertTimeToDateString
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.getIconPainter
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.Comment
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ExpandableText
import com.minhtu.firesocialmedia.utils.Utils
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
                                  navController : NavigationHandler) {
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.contains(news.id)
            LaunchedEffect(likeStatus) {
                homeViewModel.updateLikeStatus()
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            Column(modifier = Modifier.fillMaxSize().background(Color.White), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                UiUtils.BackAndMoreOptionsRow(navController)
                Row(horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.background(color = Color.White).padding(10.dp).fillMaxWidth()
                        .clickable {
                            var user =
                                Utils.findUserById(news.posterId, homeViewModel.listUsers)
                            if(user == null) {
                                user = homeViewModel.currentUser
                            }
                            if(user != null) {
                                onNavigateToUserInformation(user)
                            }
                        }){
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            news.avatar,
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag(TestTag.TAG_POSTER_AVATAR)
                                .semantics{
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
                ExpandableText(news.message)
                if(news.image.isNotEmpty()){
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
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
                                .semantics{
                                    contentDescription = TestTag.TAG_POST_IMAGE
                                }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Start) {
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
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, start = 10.dp, end = 10.dp)) {
                    Button(onClick = {
                        homeViewModel.clickLikeButton(news, platform)
                    },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = if(isLiked) ButtonDefaults.buttonColors(Color.Cyan)
                        else ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.height(35.dp).weight(1f)
                            .testTag(TestTag.TAG_BUTTON_LIKE)
                            .semantics{
                                contentDescription = TestTag.TAG_BUTTON_LIKE
                            }){
                        CrossPlatformIcon(
                            icon = "like",
                            color = if(isLiked) "#00FFFF" else "#FFFFFFFF",
                            contentDescription = "Like",
                            modifier = Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = if(isLiked) "Liked" else "Like", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        homeViewModel.clickCommentButton(news)
                    },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.height(35.dp).weight(1f)
                            .testTag(TestTag.TAG_BUTTON_COMMENT)
                            .semantics{
                                contentDescription = TestTag.TAG_BUTTON_COMMENT
                            }){
                        CrossPlatformIcon(
                            icon = "comment",
                            color = "#FFFFFFFF",
                            contentDescription = "Comment",
                            modifier = Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "Comment", color = Color.Black)
                    }
                }

                //Show comment screen at the end of this page
                Comment.CommentScreen(modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                    platform,
                    showCloseIcon = false,
                    commentViewModel = commentViewModel,
                    currentUser = homeViewModel.currentUser!!,
                    selectedNew = news,
                    listUsers = homeViewModel.listUsers,
                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                    onNavigateToUserInformation = onNavigateToUserInformation,
                    onNavigateToHomeScreen = onNavigateToHomeScreen)
            }
        }

        fun getScreenName() : String {
            return "PostInformationScreen"
        }
    }
}