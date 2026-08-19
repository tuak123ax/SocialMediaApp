package com.minhtu.firesocialmedia.group.platform

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.minhtu.firesocialmedia.core.R
import com.minhtu.firesocialmedia.platform.ToastController

@Composable
actual fun VideoPlayer(
    uri: String,
    modifier: Modifier,
    isLiked : Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    // Toggle to force the inline PlayerView to re-bind the player after exiting fullscreen
    var playerBindVersion by remember { mutableStateOf(0) }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = false
    }

    PlayerViewContent(
        player = player,
        playerBindVersion = playerBindVersion,
        modifier = modifier,
        onFullscreenClick = {
            isFullscreen = true
        }
    )

    if (isFullscreen) {
        FullscreenVideoDialog(
            player = player,
            activity = activity,
            isLiked = isLiked,
            onDismiss = {
                isFullscreen = false
                // Increment version so PlayerViewContent's update block re-binds the player
                playerBindVersion++
            },
            onLikeClick = { onLikeClick() },
            onCommentClick = { onCommentClick() },
            onShareClick = { onShareClick() },
            commentSheetContent = commentSheetContent
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerViewContent(
    player: ExoPlayer,
    playerBindVersion: Int,
    modifier: Modifier,
    onFullscreenClick: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setFullscreenButtonClickListener {
                    onFullscreenClick()
                }
            }
        },
        update = { view ->
            // playerBindVersion change forces this block to run, re-attaching player
            // to the inline surface after exiting fullscreen
            @Suppress("UNUSED_EXPRESSION") playerBindVersion
            view.player = null
            view.player = player
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun FullscreenVideoDialog(
    player: ExoPlayer,
    activity: Activity?,
    isLiked : Boolean,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null
) {
    BackHandler {
        onDismiss()
    }

    // Hold a reference to the like button so we can update it imperatively when isLiked changes
    val likeButtonRef = remember { mutableStateOf<LinearLayout?>(null) }
    // Hold reference to the playerView so we can resize it when comment sheet opens/closes
    val playerViewRef = remember { mutableStateOf<PlayerView?>(null) }

    // Reactively update the like button's visual state whenever isLiked flips
    LaunchedEffect(isLiked) {
        val btn = likeButtonRef.value
        val ctx = activity ?: return@LaunchedEffect
        if (btn != null) updateLikeButtonState(btn, isLiked, ctx)
    }

    // Comment sheet state
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // When sheet opens/closes, resize the player view to fit above the sheet
    LaunchedEffect(showCommentSheet) {
        val pv = playerViewRef.value ?: return@LaunchedEffect
        val window = activity?.window ?: return@LaunchedEffect
        if (showCommentSheet) {
            // Shrink player to top 45% of screen
            val screenHeight = window.decorView.height
            val targetHeight = (screenHeight * 0.45f).toInt()
            pv.layoutParams = (pv.layoutParams as FrameLayout.LayoutParams).apply {
                height = targetHeight
                gravity = Gravity.TOP
            }
        } else {
            // Restore to full screen
            pv.layoutParams = (pv.layoutParams as FrameLayout.LayoutParams).apply {
                height = FrameLayout.LayoutParams.MATCH_PARENT
                gravity = Gravity.NO_GRAVITY
            }
        }
        pv.requestLayout()
    }

    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}

        // Edge-to-edge fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ── Root container ──────────────────────────────────────────────────
        val container = FrameLayout(activity).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ── Player ──────────────────────────────────────────────────────────
        val playerView = PlayerView(activity).apply {
            this.player = player
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            fitsSystemWindows = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setFullscreenButtonClickListener { onDismiss() }
        }
        playerViewRef.value = playerView

        // ── Bottom gradient scrim (makes controller text readable) ──────────
        val bottomScrim = android.view.View(activity).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(
                    android.graphics.Color.argb(180, 0, 0, 0),
                    android.graphics.Color.TRANSPARENT
                )
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                160.dpToPx(activity),
                Gravity.BOTTOM
            )
        }

        // ── Top gradient scrim ───────────────────────────────────────────────
        val topScrim = android.view.View(activity).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    android.graphics.Color.argb(120, 0, 0, 0),
                    android.graphics.Color.TRANSPARENT
                )
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                100.dpToPx(activity),
                Gravity.TOP
            )
        }

        // ── Action column (TikTok-style right side) ──────────────────────────
        val likeButton = modernActionButton(activity, R.drawable.like, "Like", onLikeClick, isLiked)
        likeButtonRef.value = likeButton

        // Comment button opens the in-place sheet instead of navigating away
        val commentClickAction: () -> Unit = if (commentSheetContent != null) {
            { showCommentSheet = true }
        } else {
            onCommentClick
        }

        val actionColumn = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 16.dpToPx(activity), 110.dpToPx(activity))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.BOTTOM
            )

            addView(likeButton)
            addView(modernActionButton(activity, R.drawable.comment, "Comment", commentClickAction))
            addView(modernActionButton(activity, R.drawable.share, "Share", onShareClick))
        }

        // ── Sync visibility + fade with player controller ────────────────────
        playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility ->
                val targetAlpha = if (visibility == android.view.View.VISIBLE) 1f else 0f
                listOf(actionColumn, bottomScrim, topScrim).forEach { v ->
                    ObjectAnimator.ofFloat(v, "alpha", v.alpha, targetAlpha).apply {
                        duration = 250
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }
                }
            }
        )

        // ── Compose layers ───────────────────────────────────────────────────
        container.addView(playerView)
        container.addView(bottomScrim)
        container.addView(topScrim)
        container.addView(actionColumn)

        val decorView = window.decorView as ViewGroup
        decorView.addView(container)

        onDispose {
            likeButtonRef.value = null
            playerViewRef.value = null
            val wasPlaying = player.isPlaying
            val position = player.currentPosition
            playerView.player = null
            decorView.removeView(container)
            WindowCompat.setDecorFitsSystemWindows(window, true)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            player.seekTo(position)
            if (wasPlaying) player.play()
        }
    }

    // ── Comment bottom sheet (Compose layer, shown above the video) ──────────
    if (showCommentSheet && commentSheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Use fillMaxHeight(0.5f) so the sheet content (list + input) is fully visible
            // in the half-expanded state without requiring the user to scroll the sheet up.
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
                commentSheetContent { showCommentSheet = false }
                // Toast host must be placed here so toasts appear above the fullscreen
                // video overlay (which is added directly to the decor view and sits
                // behind the normal Compose ToastHost).
                ToastController.ToastHost()
            }
        }
    }
}
