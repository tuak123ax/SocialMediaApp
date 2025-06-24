package com.minhtu.firesocialmedia.presentation.comment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.CommentInstance
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlin.collections.contains

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
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current
            val commentBeReplied = commentViewModel.commentBeReplied.collectAsState()
            LaunchedEffect(Unit) {
                Utils.Companion.getAllCommentsOfNew(commentViewModel, selectedNew.id, platform)
            }
            LaunchedEffect(commentStatus.value) {
                if (commentStatus.value != null) {
                    if (commentStatus.value!!) {
                        showToast("Comment successfully!")
                    } else {
                        showToast("Comment failed! Please try again!")
                    }
                }
            }

            val commentsList =  commentViewModel.allComments.collectAsState()

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.Companion
                        .fillMaxSize()
                ) {
                    // Close Button Row
                    if (showCloseIcon) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.Companion.fillMaxWidth()
                        ) {
                            CrossPlatformIcon(
                                icon = "close",
                                color = "#FFFFFFFF",
                                contentDescription = "Close Icon",
                                contentScale = ContentScale.Companion.Fit,
                                modifier = Modifier.Companion
                                    .size(30.dp)
                                    .clickable {
                                        commentViewModel.resetCommentStatus()
                                        onNavigateToHomeScreen()
                                    }
                                    .testTag(TestTag.Companion.TAG_BUTTON_BACK)
                                    .semantics {
                                        contentDescription = TestTag.Companion.TAG_BUTTON_BACK
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.Companion.height(8.dp))

                    // LazyColumn for Messages
                    LazyColumn(
                        modifier = Modifier.Companion
                            .weight(1f) // Expands to take available space
                            .fillMaxWidth()
                            .testTag(TestTag.Companion.TAG_COMMENTS_LIST)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_COMMENTS_LIST
                            },
                        verticalArrangement = Arrangement.spacedBy(5.dp) // Adds spacing between messages
                    ) {
                        //Sort comments by timePosted in descending order
                        items(commentsList.value.sortedByDescending { it.timePosted }) { comment ->
                            CommentCard(
                                comment,
                                commentViewModel,
                                currentUser,
                                platform,
                                selectedNew,
                                true,
                                onNavigateToShowImageScreen,
                                onNavigateToUserInformation,
                                listUsers,
                                onCopyComment = {
                                    commentViewModel.copyToClipboard(comment.message, platform)
                                },
                                onLikeComment = {
                                    commentViewModel.onLikeComment(
                                        selectedNew,
                                        currentUser,
                                        comment,
                                        platform
                                    )
                                },
                                onReplyComment = {
                                    commentViewModel.updateCommentBeReplied(comment)
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                onDeleteComment = {
                                    commentViewModel.onDeleteComment(selectedNew, comment, platform)
                                })
                        }
                    }

                    Spacer(modifier = Modifier.Companion.height(8.dp))

                    if (commentBeReplied.value != null) {
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            modifier = Modifier.Companion.fillMaxWidth()
                        ) {
                            Text(
                                text = "You are replying to ${commentBeReplied.value!!.posterName}",
                                color = Color.Companion.Black
                            )
                        }
                    }
                    // Comment Input Row
                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentViewModel.message,
                            onValueChange = { commentViewModel.updateMessage(it) },
                            modifier = Modifier.Companion
                                .weight(1f) // Allow space for send button
                                .padding(10.dp)
                                .focusRequester(focusRequester)
                                .testTag(TestTag.Companion.TAG_INPUT_COMMENT)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_INPUT_COMMENT
                                },
                            label = { Text(text = "Input your comment here") },
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Companion.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            )
                        )

                        CrossPlatformIcon(
                            icon = "send_message",
                            color = "#FFFFFFFF",
                            contentDescription = "Send Icon",
                            contentScale = ContentScale.Companion.Fit,
                            modifier = Modifier.Companion
                                .size(50.dp)
                                .padding(10.dp)
                                .clickable {
                                    commentViewModel.sendComment(
                                        currentUser,
                                        selectedNew,
                                        listUsers,
                                        platform
                                    )
                                }
                                .testTag(TestTag.Companion.TAG_BUTTON_SEND)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_SEND
                                }
                        )
                    }
                }
            }
        }

        @Composable
        fun CommentCard(comment: CommentInstance,
                        commentViewModel: CommentViewModel,
                        currentUser : UserInstance,
                        platform : PlatformContext,
                        selectedNew : NewsInstance,
                        isMainComment : Boolean,
                        onNavigateToShowImageScreen: (image: String) -> Unit,
                        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
                        listUsers : ArrayList<UserInstance>,
                        onCopyComment: () -> Unit,
                        onLikeComment: () -> Unit,
                        onReplyComment: () -> Unit,
                        onDeleteComment: () -> Unit) {
            val likeStatus by commentViewModel.likedComments.collectAsState()
            val isLiked = likeStatus.contains(comment.id)
            LaunchedEffect(Unit) {
                commentViewModel.updateLikeCommentOfCurrentUser(currentUser)
            }
            LaunchedEffect(likeStatus) {
                commentViewModel.updateLikeStatus()
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                modifier = Modifier.Companion.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Companion.Center) {
                    var showMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.Companion
                            .padding(10.dp)
                            .fillMaxWidth()
                            .testTag(TestTag.Companion.COMMENT_CARD)
                            .semantics {
                                contentDescription = TestTag.Companion.COMMENT_CARD
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        showMenu = true
                                    }
                                )
                            },
                        shape = RoundedCornerShape(30.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Companion.White)
                    ) {
                        Column(modifier = Modifier.Companion.fillMaxSize().padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.Companion.padding(10.dp).fillMaxWidth()
                                    .clickable {
                                        var user = Utils.Companion.findUserById(
                                            comment.posterId,
                                            listUsers
                                        )
                                        if (user == null) {
                                            user = currentUser
                                        }
                                        onNavigateToUserInformation(user)
                                    }) {

                                CompositionLocalProvider(
                                    LocalImageLoader provides remember { generateImageLoader() },
                                ) {
                                    AutoSizeImage(
                                        comment.avatar,
                                        contentDescription = "Poster Avatar",
                                        contentScale = ContentScale.Companion.Crop,
                                        modifier = Modifier.Companion
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.Companion.width(5.dp))

                                Column {
                                    Text(
                                        text = comment.posterName,
                                        color = Color.Companion.Black,
                                        modifier = Modifier.Companion.padding(horizontal = 2.dp)
                                    )
                                    if (isMainComment) {
                                        Text(
                                            text = convertTimeToDateString(comment.timePosted),
                                            color = Color.Companion.Gray,
                                            modifier = Modifier.Companion.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }

                            UiUtils.Companion.ExpandableText(
                                text = comment.message
                            )

                            if (comment.image.isNotEmpty()) {
                                CompositionLocalProvider(
                                    LocalImageLoader provides remember { generateImageLoader() },
                                ) {
                                    AutoSizeImage(
                                        comment.image,
                                        contentDescription = "Image",
                                        contentScale = ContentScale.Companion.Fit,
                                        modifier = Modifier.Companion
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

                        Row(
                            modifier = Modifier.Companion.fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CrossPlatformIcon(
                                icon = "like",
                                color = if (isLiked) "#00FFFF" else "#FFFFFF",
                                contentDescription = TestTag.Companion.TAG_BUTTON_LIKE,
                                modifier = Modifier.Companion
                                    .testTag(TestTag.Companion.TAG_BUTTON_LIKE)
                                    .semantics {
                                        contentDescription = TestTag.Companion.TAG_BUTTON_LIKE
                                    }
                                    .clickable {
                                        onLikeComment()
                                    },
                                tint = if (isLiked) Utils.Companion.hexToColor("#00FFFF") else Color.Companion.Unspecified
                            )
                            Text(
                                text = "${comment.likeCount}",
                                fontSize = 12.sp,
                                color = Color.Companion.Black,
                                modifier = Modifier.Companion.padding(2.dp)
                            )
                            Spacer(modifier = Modifier.Companion.weight(1f))
                            if (isMainComment) {
                                CrossPlatformIcon(
                                    icon = "comment",
                                    color = "#FFFFFF",
                                    contentDescription = TestTag.Companion.TAG_BUTTON_COMMENT,
                                    modifier = Modifier.Companion
                                        .testTag(TestTag.Companion.TAG_BUTTON_COMMENT)
                                        .semantics {
                                            contentDescription =
                                                TestTag.Companion.TAG_BUTTON_COMMENT
                                        }
                                        .clickable {
                                            onReplyComment()
                                        }
                                )
                                Text(
                                    text = "${comment.commentCount}",
                                    fontSize = 12.sp,
                                    color = Color.Companion.Black,
                                    modifier = Modifier.Companion.padding(2.dp)
                                )
                            }
                        }
                    }
                    DropdownMenuForComment(
                        showMenu,
                        isMainComment,
                        comment.posterId == currentUser.uid,
                        onCopyComment = {
                            onCopyComment()
                        },
                        onLikeComment = {
                            onLikeComment()
                        },
                        onReplyComment = {
                            onReplyComment()
                        },
                        onDeleteComment = {
                            onDeleteComment()
                        },
                        onDismissRequest = { showMenu = false }
                    )
                }
                var showReplies by remember { mutableStateOf(false) }
                if (comment.listReplies.isNotEmpty()) {
                    Text(
                        text = if (!showReplies) "Click here to see all replies" else "Click here to close all replies",
                        color = Color.Companion.Black,
                        fontWeight = FontWeight.Companion.Bold,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .clickable {
                                showReplies = !showReplies
                                logMessage("showReplies", showReplies.toString())
                            }
                    )
                }

                if (showReplies) {
                    logMessage("numberReplies", comment.listReplies.values.size.toString())
                    Column(
                        modifier = Modifier.Companion.padding(start = 40.dp)
                    ) {
                        comment.listReplies.values
                            .sortedByDescending { it.timePosted }
                            .forEach { reply ->
                                CommentCard(
                                    reply,
                                    commentViewModel,
                                    currentUser,
                                    platform,
                                    selectedNew,
                                    false,
                                    onNavigateToShowImageScreen,
                                    onNavigateToUserInformation,
                                    listUsers,
                                    onCopyComment = { onCopyComment() },
                                    onLikeComment = {
                                        commentViewModel.onLikeComment(
                                            selectedNew,
                                            currentUser,
                                            reply,
                                            platform
                                        )
                                    },
                                    onReplyComment = {},
                                    onDeleteComment = {
                                        commentViewModel.onDeleteComment(
                                            selectedNew,
                                            reply,
                                            platform
                                        )
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

        @Composable
        fun DropdownMenuForComment(expanded : Boolean,
                                   isMainComment : Boolean,
                                   isYourComment : Boolean,
                                   onCopyComment : () -> Unit,
                                   onLikeComment : () -> Unit,
                                   onReplyComment : () -> Unit,
                                   onDeleteComment : () -> Unit,
                                   onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = {
                        onCopyComment()
                        onDismissRequest()
                    },
                    modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_COPY)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_BUTTON_COPY
                        }
                )
                DropdownMenuItem(
                    text = { Text("Like") },
                    onClick = {
                        onLikeComment()
                        onDismissRequest()
                    },
                    modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_LIKE)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_BUTTON_LIKE
                        }
                )
                if (isMainComment) {
                    DropdownMenuItem(
                        text = { Text("Reply") },
                        onClick = {
                            onReplyComment()
                            onDismissRequest()
                        },
                        modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_REPLY)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_BUTTON_REPLY
                            }
                    )
                }
                if (isYourComment) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteComment()
                            onDismissRequest()
                        },
                        modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_DELETE)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_BUTTON_DELETE
                            }
                    )
                }
            }
        }
    }
}