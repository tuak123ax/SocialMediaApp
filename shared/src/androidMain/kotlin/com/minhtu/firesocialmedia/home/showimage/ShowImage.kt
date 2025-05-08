package com.minhtu.firesocialmedia.home.showimage

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.TestTag

class ShowImage {
    companion object {
        @Composable
        fun ShowImageScreen(image: String,
                            showImageViewModel: ShowImageViewModel = viewModel(),
                            modifier: Modifier,
                            onNavigateToHomeScreen: () -> Unit) {
            val context = LocalContext.current
            Box(
                modifier = modifier
            ){
                Column(
                    verticalArrangement = Arrangement.Center, // Centers children vertically
                    horizontalAlignment = Alignment.CenterHorizontally, // Centers children horizontally
                    modifier = Modifier
                        .fillMaxSize() // Makes the Column take full screen
                        .padding(16.dp)
                ) {
                    // Close Button Row
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp) // Adjust padding if needed
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Icon",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    showImageViewModel.downloadImage(context, image, generateRandomImageName(16))
                                }
                                .testTag(TestTag.TAG_BUTTON_DOWNLOAD)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_DOWNLOAD
                                }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(R.drawable.white_close)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Close Icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    onNavigateToHomeScreen()
                                }
                                .testTag(TestTag.TAG_BUTTON_CLOSE)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_CLOSE
                                }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Pushes the image to the center

                    // Centered Image
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth() // Ensures it scales properly
                            .padding(20.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Pushes everything up/down evenly
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