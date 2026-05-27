package com.minhtu.firesocialmedia.presentation.showimage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.seiko.imageloader.ui.AutoSizeImage
import org.koin.compose.viewmodel.koinViewModel

class ShowImage {
    companion object {
        @Composable
        fun ShowImageScreen(
            image: String,
            localImageLoaderValue: ProvidedValue<*>,
            showImageViewModel: ShowImageViewModel = koinViewModel(),
            modifier: Modifier,
            onNavigateBack: () -> Unit
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            var showControls by remember { mutableStateOf(true) }

            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                if (scale > 1f) {
                    offset += panChange
                } else {
                    offset = Offset.Zero
                }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Zoomable image
                CompositionLocalProvider(localImageLoaderValue) {
                    AutoSizeImage(
                        image.toStorageUrl(),
                        contentDescription = "image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformState)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.5f
                                        }
                                    },
                                    onTap = {
                                        showControls = !showControls
                                    }
                                )
                            }
                    )
                }

                // Top controls bar
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.Black.copy(alpha = 0.45f)
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            showImageViewModel.downloadImage(
                                                image,
                                                generateRandomImageName(16)
                                            )
                                        }
                                        .testTag(TestTag.TAG_BUTTON_DOWNLOAD)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_DOWNLOAD
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onNavigateBack() }
                                        .testTag(TestTag.TAG_BUTTON_CLOSE)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_CLOSE
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        fun generateRandomImageName(length: Int = 16): String {
            val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..length)
                .map { chars.random() }
                .joinToString("") + ".jpg"
        }

        fun getScreenName(): String {
            return "ShowImageScreen"
        }
    }
}
