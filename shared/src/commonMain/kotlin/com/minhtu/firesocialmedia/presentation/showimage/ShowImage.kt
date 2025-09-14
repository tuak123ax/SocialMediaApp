package com.minhtu.firesocialmedia.presentation.showimage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage

class ShowImage {
    companion object {
        @Composable
        fun ShowImageScreen(
                            image: String,
                            showImageViewModel: ShowImageViewModel,
                            modifier: Modifier,
                            onNavigateToHomeScreen: () -> Unit) {
            Box(
                modifier = modifier
            ) {
                Column(
                    verticalArrangement = Arrangement.Center, // Centers children vertically
                    horizontalAlignment = Alignment.Companion.CenterHorizontally, // Centers children horizontally
                    modifier = Modifier.Companion
                        .fillMaxSize() // Makes the Column take full screen
                        .padding(16.dp)
                ) {
                    // Close Button Row
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(top = 8.dp) // Adjust padding if needed
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Icon",
                            tint = Color.Companion.White,
                            modifier = Modifier.Companion
                                .size(30.dp)
                                .clickable {
                                    showImageViewModel.downloadImage(
                                        image,
                                        generateRandomImageName(16)
                                    )
                                }
                                .testTag(TestTag.Companion.TAG_BUTTON_DOWNLOAD)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_DOWNLOAD
                                }
                        )
                        Spacer(modifier = Modifier.Companion.width(10.dp))
                        CrossPlatformIcon(
                            icon = "white_close",
                            backgroundColor = "#000000",
                            contentDescription = "Close Icon",
                            contentScale = ContentScale.Companion.Fit,
                            modifier = Modifier.Companion
                                .size(30.dp)
                                .clickable {
                                    onNavigateToHomeScreen()
                                }
                                .testTag(TestTag.Companion.TAG_BUTTON_CLOSE)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_CLOSE
                                }
                        )
                    }

                    Spacer(modifier = Modifier.Companion.weight(1f)) // Pushes the image to the center

                    // Centered Image
                    CompositionLocalProvider(
                        LocalImageLoader provides remember { generateImageLoader() },
                    ) {
                        AutoSizeImage(
                            image,
                            contentDescription = "image",
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.Companion.weight(1f)) // Pushes everything up/down evenly
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