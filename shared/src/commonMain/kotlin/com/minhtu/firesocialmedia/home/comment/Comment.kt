package com.minhtu.firesocialmedia.home.comment

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.convertTimeToDateString
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.getIconPainter
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.showToast
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage

class Comment {
    companion object{
        @Composable
        fun CommentScreen(modifier: Modifier,
                          platform : PlatformContext,
                          showCloseIcon : Boolean,
                          commentViewModel: CommentViewModel,
                          currentUser : UserInstance,
                          selectedNew : NewsInstance,
                          listUsers : ArrayList<UserInstance>,
                          onNavigateToShowImageScreen: (image: String) -> Unit,
                          onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                          onNavigateToHomeScreen: () -> Unit) {
            val commentStatus = commentViewModel.createCommentStatus.collectAsState()
            LaunchedEffect(Unit) {
                Utils.getAllCommentsOfNew(commentViewModel, selectedNew.id, platform)
            }
            LaunchedEffect(commentStatus.value) {
                logMessage("commentStatus", commentStatus.value.toString())
                if(commentStatus.value != null) {
                    if(commentStatus.value!!) {
                        showToast("Comment successfully!")
                    } else {
                        showToast("Comment failed! Please try again!")
                    }
                }
            }

            //Observe Live Data as State
            val commentsList =  commentViewModel.allComments.collectAsState()

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Close Button Row
                    if(showCloseIcon) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CrossPlatformIcon(
                                icon = "close",
                                color = "#FFFFFFFF",
                                contentDescription = "Close Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        commentViewModel.resetCommentStatus()
                                        onNavigateToHomeScreen()
                                    }
                                    .testTag(TestTag.TAG_BUTTON_BACK)
                                    .semantics{
                                        contentDescription = TestTag.TAG_BUTTON_BACK
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // LazyColumn for Messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f) // Expands to take available space
                            .fillMaxWidth()
                            .testTag(TestTag.TAG_COMMENTS_LIST)
                            .semantics{
                                contentDescription = TestTag.TAG_COMMENTS_LIST
                            },
                        verticalArrangement = Arrangement.spacedBy(5.dp) // Adds spacing between messages
                    ) {
                        //Sort comments by timePosted in descending order
                        items(commentsList.value.sortedByDescending { it.timePosted }) { comment ->
                            CommentCard(comment,onNavigateToShowImageScreen,onNavigateToUserInformation, listUsers, currentUser)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Comment Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentViewModel.message,
                            onValueChange = { commentViewModel.updateMessage(it) },
                            modifier = Modifier
                                .weight(1f) // Allow space for send button
                                .padding(10.dp)
                                .testTag(TestTag.TAG_INPUT_COMMENT)
                                .semantics{
                                    contentDescription = TestTag.TAG_INPUT_COMMENT
                                },
                            label = { Text(text = "Input your comment here") },
                            maxLines = 4
                        )

                        CrossPlatformIcon(
                            icon = "send_message",
                            color = "#FFFFFFFF",
                            contentDescription = "Send Icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp)
                                .clickable {
                                    commentViewModel.sendComment(currentUser, selectedNew, listUsers, platform)
                                }
                                .testTag(TestTag.TAG_BUTTON_SEND)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_SEND
                                }
                        )
                    }
                }
            }
        }

        @Composable
        fun CommentCard(comment: CommentInstance, onNavigateToShowImageScreen: (image: String) -> Unit, onNavigateToUserInformation: (user: UserInstance?) -> Unit, listUsers : ArrayList<UserInstance>, currentUser: UserInstance) {
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .testTag(TestTag.COMMENT_CARD)
                    .semantics{
                        contentDescription = TestTag.COMMENT_CARD
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(10.dp).fillMaxWidth()
                            .clickable {
                                var user = Utils.findUserById(comment.posterId, listUsers)
                                if(user == null) {
                                    user = currentUser
                                }
                                onNavigateToUserInformation(user)
                            }){

                        CompositionLocalProvider(
                            LocalImageLoader provides remember { generateImageLoader() },
                        ) {
                            AutoSizeImage(
                                comment.avatar,
                                contentDescription = "Poster Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        Column {
                            Text(
                                text = comment.posterName,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = convertTimeToDateString(comment.timePosted),
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = comment.message,
                        color = Color.Black,
                        modifier = Modifier.padding(5.dp) // Adds padding around text
                    )

                    if(comment.image.isNotEmpty()){
                        CompositionLocalProvider(
                            LocalImageLoader provides remember { generateImageLoader() },
                        ) {
                            AutoSizeImage(
                                comment.image,
                                contentDescription = "Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(200.dp)
                                    .padding(10.dp)
                                    .clickable {
                                        onNavigateToShowImageScreen(comment.image)
                                    }
                            )
                        }
                    }
                }
            }
        }

        fun getScreenName(): String{
            return "CommentScreen"
        }
    }
}