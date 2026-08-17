package com.minhtu.firesocialmedia.group.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.viewinterop.UIKitView
import com.minhtu.firesocialmedia.platform.ToastHost
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.QuartzCore.CAGradientLayer
import platform.UIKit.UIApplication
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

private fun AVPlayer.play() {
    this.performSelector(NSSelectorFromString("play"))
}

private fun AVPlayer.pause() {
    this.performSelector(NSSelectorFromString("pause"))
}

// ── iOS action button design tokens (mirrors Android ActionButtonTokens) ────
private object IosActionButtonTokens {
    const val CIRCLE_SIZE: CGFloat = 52.0
    const val ICON_PADDING: CGFloat = 12.0
    const val STROKE_WIDTH: CGFloat = 1.0
    const val BUTTON_BOTTOM_MARGIN: CGFloat = 20.0
    const val LABEL_FONT_SIZE: CGFloat = 11.0
    // default: frosted white
    const val DEFAULT_ALPHA: CGFloat = 1.0
    const val DEFAULT_BG_ALPHA: CGFloat = 80.0 / 255.0
    const val DEFAULT_STROKE_ALPHA: CGFloat = 60.0 / 255.0
    // active: soft pink #FF6987
    const val ACTIVE_R: CGFloat = 255.0 / 255.0
    const val ACTIVE_G: CGFloat = 105.0 / 255.0
    const val ACTIVE_B: CGFloat = 135.0 / 255.0
    const val ACTIVE_BG_ALPHA: CGFloat = 60.0 / 255.0
    const val ACTIVE_STROKE_ALPHA: CGFloat = 120.0 / 255.0
}

/** Helper NSObject acting as tap target. */
@OptIn(BetaInteropApi::class)
private class IosActionTapWrapper(val action: () -> Unit) : NSObject() {
    @ObjCAction
    fun handleTap() { action() }
}

/** Helper NSObject for play/pause toggle. */
@OptIn(BetaInteropApi::class)
private class PlayPauseTapTarget(val action: () -> Unit) : NSObject() {
    @ObjCAction
    fun handleTap() { action() }
}

/**
 * Build a vertical button group (circle icon + label) placed at the given origin.
 * Returns the UIView and a reference to the inner icon UIImageView for later tint updates.
 */
private fun iosActionButtonView(
    iconName: String,
    label: String,
    x: CGFloat,
    y: CGFloat,
    isActive: Boolean = false,
    onClick: () -> Unit
): Pair<UIView, UIImageView?> {
    val t = IosActionButtonTokens

    val iconColor: UIColor
    val bgColor: UIColor
    val strokeColor: UIColor
    if (isActive) {
        iconColor   = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.DEFAULT_ALPHA)
        bgColor     = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.ACTIVE_BG_ALPHA)
        strokeColor = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.ACTIVE_STROKE_ALPHA)
    } else {
        iconColor   = UIColor(white = 1.0, alpha = 1.0)
        bgColor     = UIColor(white = 1.0, alpha = t.DEFAULT_BG_ALPHA)
        strokeColor = UIColor(white = 1.0, alpha = t.DEFAULT_STROKE_ALPHA)
    }

    val totalH = t.CIRCLE_SIZE + 6.0 + 14.0
    val container = UIView(CGRectMake(x, y, t.CIRCLE_SIZE, totalH))
    container.backgroundColor = UIColor.clearColor

    // Circle
    val circle = UIView(CGRectMake(0.0, 0.0, t.CIRCLE_SIZE, t.CIRCLE_SIZE))
    circle.layer.cornerRadius = t.CIRCLE_SIZE / 2
    circle.layer.borderWidth = t.STROKE_WIDTH
    circle.layer.borderColor = strokeColor.CGColor
    circle.backgroundColor = bgColor
    circle.layer.shadowOpacity = 0.4f
    circle.layer.shadowRadius = 4.0

    // Icon
    val iconImage = UIImage.systemImageNamed(iconName) ?: UIImage.imageNamed(iconName)
    val iconView = UIImageView(CGRectMake(t.ICON_PADDING, t.ICON_PADDING,
        t.CIRCLE_SIZE - t.ICON_PADDING * 2, t.CIRCLE_SIZE - t.ICON_PADDING * 2))
    iconView.image = iconImage
    iconView.tintColor = iconColor
    iconView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
    circle.addSubview(iconView)

    val tap = IosActionTapWrapper(onClick)
    circle.addGestureRecognizer(UITapGestureRecognizer(tap, NSSelectorFromString("handleTap")))
    circle.userInteractionEnabled = true

    // Label
    val labelView = UILabel(CGRectMake(0.0, t.CIRCLE_SIZE + 4.0, t.CIRCLE_SIZE, 14.0))
    labelView.text = label
    labelView.font = UIFont.systemFontOfSize(t.LABEL_FONT_SIZE)
    labelView.textColor = iconColor
    labelView.textAlignment = platform.UIKit.NSTextAlignmentCenter

    container.addSubview(circle)
    container.addSubview(labelView)

    return container to (if (iconImage != null) iconView else null)
}

// ── Inline VideoPlayerView ────────────────────────────────────────────────────
@OptIn(BetaInteropApi::class)
private class InlineVideoPlayerView(
    private val initialUri: String,
    private val onFullscreenClick: () -> Unit
) : UIView(CGRectZero.readValue()) {

    private val playerLayer = AVPlayerLayer()
    val player: AVPlayer = AVPlayer.playerWithURL(NSURL(string = initialUri))
    private var isPlaying = false
    private val playPauseButton = UIButton.buttonWithType(UIButtonTypeSystem)
    private val fullscreenButton = UIButton.buttonWithType(UIButtonTypeSystem)
    private var hideButtonTimer: NSTimer? = null

    private val playPauseTarget = PlayPauseTapTarget { togglePlayPause() }
    private val fullscreenTarget = IosActionTapWrapper { onFullscreenClick() }

    init {
        playerLayer.player = player
        playerLayer.videoGravity = AVLayerVideoGravityResizeAspect
        layer.addSublayer(playerLayer)
        backgroundColor = UIColor.blackColor

        playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        playPauseButton.backgroundColor = UIColor.clearColor
        playPauseButton.titleLabel?.font = UIFont.systemFontOfSize(36.0)
        playPauseButton.addTarget(
            target = playPauseTarget,
            action = NSSelectorFromString("handleTap"),
            forControlEvents = UIControlEventTouchUpInside
        )
        addSubview(playPauseButton)

        fullscreenButton.setTitle("⛶", forState = UIControlStateNormal)
        fullscreenButton.backgroundColor = UIColor.clearColor
        fullscreenButton.titleLabel?.font = UIFont.systemFontOfSize(22.0)
        fullscreenButton.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        fullscreenButton.addTarget(
            target = fullscreenTarget,
            action = NSSelectorFromString("handleTap"),
            forControlEvents = UIControlEventTouchUpInside
        )
        addSubview(fullscreenButton)

        val tapGesture = UITapGestureRecognizer(playPauseTarget, NSSelectorFromString("handleTap"))
        addGestureRecognizer(tapGesture)
        userInteractionEnabled = true
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val (w, h) = bounds.useContents { size.width to size.height }
        playerLayer.setFrame(CGRectMake(0.0, 0.0, w, h))
        val btnSize = 60.0
        playPauseButton.setFrame(CGRectMake((w - btnSize) / 2, (h - btnSize) / 2, btnSize, btnSize))
        val fsSize = 36.0
        fullscreenButton.setFrame(CGRectMake(w - fsSize - 8.0, h - fsSize - 8.0, fsSize, fsSize))
    }

    fun updateVideo(newUri: String) {
        val newUrl = NSURL(string = newUri)
        val newItem = AVPlayerItem(newUrl)
        if (player.currentItem?.asset?.isEqual(newItem.asset) == false) {
            player.replaceCurrentItemWithPlayerItem(newItem)
            isPlaying = false
            playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        }
    }

    private fun showControlsTemporarily() {
        playPauseButton.hidden = false
        fullscreenButton.hidden = false
        hideButtonTimer?.invalidate()
        hideButtonTimer = NSTimer.scheduledTimerWithTimeInterval(3.0, repeats = false) {
            playPauseButton.hidden = true
            fullscreenButton.hidden = true
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            player.pause()
            playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        } else {
            player.play()
            playPauseButton.setTitle("⏸", forState = UIControlStateNormal)
        }
        isPlaying = !isPlaying
        showControlsTemporarily()
    }
}

@OptIn(BetaInteropApi::class)
@Composable
actual fun VideoPlayer(
    uri: String,
    modifier: Modifier,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    val player = remember { AVPlayer.playerWithURL(NSURL(string = uri)) }

    LaunchedEffect(uri) {
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(NSURL(string = uri)))
    }

    UIKitView(
        factory = { InlineVideoPlayerView(uri) { isFullscreen = true } },
        update = { view -> view.updateVideo(uri) },
        modifier = modifier
    )

    if (isFullscreen) {
        IosFullscreenVideoOverlay(
            player = player,
            isLiked = isLiked,
            onDismiss = { isFullscreen = false },
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            commentSheetContent = commentSheetContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, BetaInteropApi::class)
@Composable
private fun IosFullscreenVideoOverlay(
    player: AVPlayer,
    isLiked: Boolean,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val playerLayerRef  = remember { mutableStateOf<AVPlayerLayer?>(null) }
    val likeIconViewRef = remember { mutableStateOf<UIImageView?>(null) }
    val containerRef    = remember { mutableStateOf<UIView?>(null) }

    // Reactively update like icon tint
    LaunchedEffect(isLiked) {
        val iv = likeIconViewRef.value ?: return@LaunchedEffect
        val t = IosActionButtonTokens
        iv.tintColor = if (isLiked)
            UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.DEFAULT_ALPHA)
        else UIColor(white = 1.0, alpha = 1.0)
    }

    // Resize player layer when comment sheet visible
    LaunchedEffect(showCommentSheet) {
        val c = containerRef.value ?: return@LaunchedEffect
        val pLayer = playerLayerRef.value ?: return@LaunchedEffect
        val (cw, ch) = c.bounds.useContents { size.width to size.height }
        val newH = if (showCommentSheet) ch * 0.45 else ch
        pLayer.setFrame(CGRectMake(0.0, 0.0, cw, newH))
    }

    DisposableEffect(Unit) {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> @Suppress("UNCHECKED_CAST") (scene.windows as? List<UIWindow>) ?: emptyList() }
            .firstOrNull { it.isKeyWindow() }
            ?: return@DisposableEffect onDispose {}

        val (screenW, screenH) = window.bounds.useContents { size.width to size.height }

        val container = UIView(CGRectMake(0.0, 0.0, screenW, screenH))
        container.backgroundColor = UIColor.blackColor
        containerRef.value = container

        // Player layer (fullscreen)
        val pLayer = AVPlayerLayer()
        pLayer.player = player
        pLayer.videoGravity = AVLayerVideoGravityResizeAspect
        pLayer.setFrame(CGRectMake(0.0, 0.0, screenW, screenH))
        playerLayerRef.value = pLayer
        container.layer.addSublayer(pLayer)

        // Bottom gradient scrim
        val bottomGrad = CAGradientLayer()
        val opaqueBlack = UIColor.blackColor.colorWithAlphaComponent(0.70).CGColor
        val clearBlack  = UIColor.clearColor.CGColor
        bottomGrad.colors = listOf(clearBlack, opaqueBlack)
        bottomGrad.setFrame(CGRectMake(0.0, screenH - 160.0, screenW, 160.0))
        container.layer.addSublayer(bottomGrad)

        // Top gradient scrim
        val topGrad = CAGradientLayer()
        topGrad.colors = listOf(UIColor.blackColor.colorWithAlphaComponent(0.47).CGColor, clearBlack)
        topGrad.setFrame(CGRectMake(0.0, 0.0, screenW, 100.0))
        container.layer.addSublayer(topGrad)

        // ── Action column (right side, above bottom) ────────────────────────
        val t = IosActionButtonTokens
        val itemH  = t.CIRCLE_SIZE + 6.0 + 14.0 + t.BUTTON_BOTTOM_MARGIN
        val colX   = screenW - t.CIRCLE_SIZE - 20.0
        val colY   = screenH - itemH * 3 - 100.0

        val commentAction: () -> Unit = if (commentSheetContent != null) {
            { showCommentSheet = true }
        } else onCommentClick

        val (likeView, likeIcon) = iosActionButtonView("like", "Like", colX, colY, isLiked, onLikeClick)
        likeIconViewRef.value = likeIcon
        val (commentView, _) = iosActionButtonView("bubble.right", "Comment", colX, colY + itemH, onClick = commentAction)
        val (shareView, _)   = iosActionButtonView("square.and.arrow.up", "Share", colX, colY + itemH * 2, onClick = onShareClick)

        container.addSubview(likeView)
        container.addSubview(commentView)
        container.addSubview(shareView)

        // ── Close button (top-left) ──────────────────────────────────────────
        val closeTarget = IosActionTapWrapper(onDismiss)
        val closeBtn = UIButton.buttonWithType(UIButtonTypeSystem)
        closeBtn.setTitle("✕", forState = UIControlStateNormal)
        closeBtn.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        closeBtn.titleLabel?.font = UIFont.systemFontOfSize(22.0)
        closeBtn.setFrame(CGRectMake(16.0, 48.0, 44.0, 44.0))
        closeBtn.addTarget(closeTarget, NSSelectorFromString("handleTap"), UIControlEventTouchUpInside)
        container.addSubview(closeBtn)

        // ── Play/pause tap overlay ───────────────────────────────────────────
        var fsPlaying = false
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        val ppTarget = PlayPauseTapTarget {
            if (fsPlaying) player.pause() else player.play()
            @Suppress("UNUSED_VALUE")
            fsPlaying = !fsPlaying
        }
        container.addGestureRecognizer(UITapGestureRecognizer(ppTarget, NSSelectorFromString("handleTap")))
        container.userInteractionEnabled = true

        window.addSubview(container)

        onDispose {
            containerRef.value = null
            playerLayerRef.value = null
            likeIconViewRef.value = null
            pLayer.player = null
            container.removeFromSuperview()
        }
    }

    // Comment bottom sheet
    if (showCommentSheet && commentSheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
                commentSheetContent { showCommentSheet = false }
                ToastHost()
            }
        }
    }
}
