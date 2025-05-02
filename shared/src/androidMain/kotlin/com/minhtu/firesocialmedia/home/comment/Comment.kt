package com.minhtu.firesocialmedia.home.comment

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.Utils

class Comment {
    companion object{
        @Composable
        fun CommentScreen(modifier: Modifier,
                          showCloseIcon : Boolean,
                          commentViewModel: CommentViewModel = viewModel(),
                          currentUser : UserInstance,
                          selectedNew : NewsInstance,
                          listUsers : ArrayList<UserInstance>,
                          onNavigateToShowImageScreen: (image: String) -> Unit,
                          onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                          onNavigateToHomeScreen: () -> Unit) {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                Utils.getAllCommentsOfNew(commentViewModel, selectedNew.id)
                commentViewModel.createCommentStatus.observe(lifecycleOwner) { createCommentStatus ->
                    if(createCommentStatus != null) {
                        if(createCommentStatus) {
                            Toast.makeText(context, "Comment successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Comment failed! Please try again!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            //Observe Live Data as State
            val commentsList =  commentViewModel.allComments.collectAsState(ArrayList())

            Box(
                modifier = Modifier
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
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(R.drawable.close)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Close Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        commentViewModel.createCommentStatus.removeObservers(lifecycleOwner)
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
                            CommentCard(comment,context,onNavigateToShowImageScreen,onNavigateToUserInformation, listUsers, currentUser)
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

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(R.drawable.send_message)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Send Icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp)
                                .clickable {
                                    commentViewModel.sendComment(currentUser, selectedNew, listUsers)
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
        fun CommentCard(comment: CommentInstance, context: Context, onNavigateToShowImageScreen: (image: String) -> Unit, onNavigateToUserInformation: (user: UserInstance?) -> Unit, listUsers : ArrayList<UserInstance>, currentUser: UserInstance) {
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .testTag(TestTag.COMMENT_CARD),
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

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(comment.avatar))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Column {
                            Text(
                                text = comment.posterName,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = Utils.convertTimeToDateString(comment.timePosted),
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
                        AsyncImage(
                            model = comment.image,
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

        fun getScreenName(): String{
            return "CommentScreen"
        }
    }
}