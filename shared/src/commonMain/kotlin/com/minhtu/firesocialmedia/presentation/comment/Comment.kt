package com.minhtu.firesocialmedia.presentation.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.convertTimeToDateString
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.UiUtils
import com.rickclephas.kmp.observableviewmodel.launch
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class Comment {
    companion object {
        @Composable
        fun CommentScreen(
            paddingValues: PaddingValues = PaddingValues(0.dp),
            modifier: Modifier = Modifier,
            platform: PlatformContext,
            localImageLoaderValue: ProvidedValue<*>,
            showCloseIcon: Boolean,
            commentViewModel: CommentViewModel = koinViewModel(),
            currentUser: UserInstance,
            selectedNew: NewsInstance,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            onNavigateToHomeScreen: (numberOfComments: Int) -> Unit
        ) {
            val commentStatus = commentViewModel.createCommentStatus.collectAsState()
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current
            val commentBeReplied = commentViewModel.commentBeReplied.collectAsState()
            LaunchedEffect(Unit) {
                commentViewModel.getAllCommentsOfNew(selectedNew.id)
            }
            // Reset comment status and clear list when this screen leaves composition
            // (e.g. bottom sheet swiped away in fullscreen video) so the stale status
            // doesn't trigger a toast the next time CommentScreen is opened.
            DisposableEffect(Unit) {
                onDispose {
                    commentViewModel.resetCommentStatus()
                    commentViewModel.clearCommentList()
                }
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

            val commentsList = commentViewModel.allComments.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            CommonBackHandler {
                onNavigateToHomeScreen(commentsList.value.size)
                coroutineScope.launch(Dispatchers.IO) {
                    //Delay to wait for animation finished before reset comment list
                    delay(700)
                    commentViewModel.resetCommentStatus()
                    commentViewModel.clearCommentList()
                }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Close Button Row
                    if (showCloseIcon) {
                        UiUtils.BackAndTitleAndMoreOptionsRow(
                            title = "Comment",
                            titleStyle = MaterialTheme.typography.titleLarge,
                            trailingIcon = "more_horiz",
                            navigateBack = {
                                onNavigateToHomeScreen(commentsList.value.size)
                                coroutineScope.launch(Dispatchers.IO) {
                                    //Delay to wait for animation finished before reset comment list
                                    delay(700)
                                    commentViewModel.resetCommentStatus()
                                    commentViewModel.clearCommentList()
                                }
                            },
                            onClickMoreOptions = {
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // LazyColumn for Messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f) // Expands to take available space
                            .fillMaxWidth()
                            .testTag(TestTag.TAG_COMMENTS_LIST)
                            .semantics {
                                contentDescription = TestTag.TAG_COMMENTS_LIST
                            },
                        verticalArrangement = Arrangement.spacedBy(5.dp) // Adds spacing between messages
                    ) {
                        //Sort comments by timePosted in descending order
                        items(
                            items = commentsList.value.sortedByDescending { it.timePosted },
                            key = { it.id }) { comment ->
                            CommentCard(
                                comment,
                                commentViewModel,
                                localImageLoaderValue,
                                currentUser,
                                platform,
                                selectedNew,
                                true,
                                onNavigateToShowImageScreen,
                                onNavigateToUserInformation,
                                onCopyComment = {
                                    commentViewModel.copyToClipboard(comment.message, platform)
                                },
                                onLikeComment = {
                                    commentViewModel.onLikeComment(
                                        selectedNew,
                                        currentUser,
                                        comment
                                    )
                                },
                                onReplyComment = {
                                    commentViewModel.updateCommentBeReplied(comment)
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                onDeleteComment = {
                                    commentViewModel.onDeleteComment(selectedNew, comment)
                                })
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (commentBeReplied.value != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "You are replying to ${commentBeReplied.value!!.posterName}",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Comment Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        UiUtils.CustomEditText(
                            text = commentViewModel.message,
                            onTextChange = {
                                commentViewModel.updateMessage(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TestTag.TAG_INPUT_COMMENT)
                                .semantics {
                                    contentDescription = TestTag.TAG_INPUT_COMMENT
                                },
                            placeholder = "Share your thought...",
                            keyboardController = keyboardController
                        )

                        Spacer(Modifier.width(8.dp))
                        SendCommentButton(
                            onClick = {
                                commentViewModel.sendComment(
                                    currentUser,
                                    selectedNew
                                )
                            },
                            modifier = Modifier
                                .testTag(TestTag.TAG_BUTTON_SEND)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_SEND
                                }
                        )
                    }
                }
            }
        }

        @Composable
        fun SendCommentButton(
            modifier: Modifier = Modifier,
            onClick: () -> Unit = {}
        ) {
            Box(
                modifier = modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Send Comment",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        @Composable
        fun CommentCard(
            comment: CommentInstance,
            commentViewModel: CommentViewModel,
            localImageLoaderValue: ProvidedValue<*>,
            currentUser: UserInstance,
            platform: PlatformContext,
            selectedNew: NewsInstance,
            isMainComment: Boolean,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            onCopyComment: () -> Unit,
            onLikeComment: () -> Unit,
            onReplyComment: () -> Unit,
            onDeleteComment: () -> Unit
        ) {
            val isAuthor = comment.posterId == selectedNew.posterId
            val likeStatus by commentViewModel.likedComments.collectAsState()
            val isLiked = likeStatus.contains(comment.id)
            LaunchedEffect(Unit) {
                commentViewModel.updateLikeCommentOfCurrentUser(currentUser)
            }
            LaunchedEffect(likeStatus) {
                commentViewModel.updateLikeStatus()
            }
            val likeCountList = commentViewModel.likeCountList.collectAsState()

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    var showMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .testTag(TestTag.COMMENT_CARD)
                            .semantics {
                                contentDescription = TestTag.COMMENT_CARD
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.padding(10.dp).fillMaxWidth()
                                    .clickable {
                                        commentViewModel.viewModelScope.launch {
                                            val user =
                                                commentViewModel.findUserById(comment.posterId)
                                            onNavigateToUserInformation(user)
                                        }
                                    }) {
                                CompositionLocalProvider(
                                    localImageLoaderValue
                                ) {
                                    AutoSizeImage(
                                        comment.avatar.toStorageUrl(),
                                        contentDescription = "Poster Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(5.dp))

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = comment.posterName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                        )

                                        if (isAuthor) {
                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Author",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (isMainComment) {
                                        Text(
                                            text = convertTimeToDateString(comment.timePosted),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }

                            UiUtils.ExpandableText(
                                text = comment.message
                            )

                            if (comment.image.isNotEmpty()) {
                                CompositionLocalProvider(
                                    localImageLoaderValue
                                ) {
                                    AutoSizeImage(
                                        comment.image.toStorageUrl(),
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

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CrossPlatformIcon(
                                icon = "like",
                                backgroundColor = if (isLiked) MaterialTheme.colorScheme.primaryContainer.toHex() else MaterialTheme.colorScheme.surface.toHex(),
                                contentDescription = TestTag.TAG_BUTTON_LIKE,
                                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag(TestTag.TAG_BUTTON_LIKE)
                                    .semantics {
                                        contentDescription = TestTag.TAG_BUTTON_LIKE
                                    }
                                    .clickable {
                                        onLikeComment()
                                    }
                            )
                            Text(
                                text = "${likeCountList.value[comment.id] ?: 0}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(2.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isMainComment) {
                                CrossPlatformIcon(
                                    icon = "comment",
                                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = TestTag.TAG_BUTTON_COMMENT,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .testTag(TestTag.TAG_BUTTON_COMMENT)
                                        .semantics {
                                            contentDescription =
                                                TestTag.TAG_BUTTON_COMMENT
                                        }
                                        .clickable {
                                            onReplyComment()
                                        }
                                )
                                Text(
                                    text = "${comment.commentCount}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(2.dp)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .clickable {
                                showReplies = !showReplies
                                logMessage("showReplies", { showReplies.toString() })
                            }
                    )
                }

                if (showReplies) {
                    logMessage("numberReplies", { comment.listReplies.values.size.toString() })
                    Column(
                        modifier = Modifier.padding(start = 40.dp)
                    ) {
                        comment.listReplies.values
                            .sortedByDescending { it.timePosted }
                            .forEach { reply ->
                                CommentCard(
                                    reply,
                                    commentViewModel,
                                    localImageLoaderValue,
                                    currentUser,
                                    platform,
                                    selectedNew,
                                    false,
                                    onNavigateToShowImageScreen,
                                    onNavigateToUserInformation,
                                    onCopyComment = { onCopyComment() },
                                    onLikeComment = {
                                        commentViewModel.onLikeComment(
                                            selectedNew,
                                            currentUser,
                                            reply
                                        )
                                    },
                                    onReplyComment = {},
                                    onDeleteComment = {
                                        commentViewModel.onDeleteComment(
                                            selectedNew,
                                            reply
                                        )
                                    }
                                )
                            }
                    }
                }
            }

        }

        fun getScreenName(): String {
            return "CommentScreen"
        }

        @Composable
        fun DropdownMenuForComment(
            expanded: Boolean,
            isMainComment: Boolean,
            isYourComment: Boolean,
            onCopyComment: () -> Unit,
            onLikeComment: () -> Unit,
            onReplyComment: () -> Unit,
            onDeleteComment: () -> Unit,
            onDismissRequest: () -> Unit
        ) {
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
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_COPY)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_COPY
                        }
                )
                DropdownMenuItem(
                    text = { Text("Like") },
                    onClick = {
                        onLikeComment()
                        onDismissRequest()
                    },
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_LIKE)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_LIKE
                        }
                )
                if (isMainComment) {
                    DropdownMenuItem(
                        text = { Text("Reply") },
                        onClick = {
                            onReplyComment()
                            onDismissRequest()
                        },
                        modifier = Modifier.testTag(TestTag.TAG_BUTTON_REPLY)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_REPLY
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
                        modifier = Modifier.testTag(TestTag.TAG_BUTTON_DELETE)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_DELETE
                            }
                    )
                }
            }
        }
    }
}