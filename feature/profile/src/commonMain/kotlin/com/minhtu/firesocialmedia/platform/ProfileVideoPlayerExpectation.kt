package com.minhtu.firesocialmedia.profile.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    uri: String, modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null
)
