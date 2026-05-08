package com.minhtu.firesocialmedia.presentation.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.platform.getResId
import com.seiko.imageloader.ui.AutoSizeImage
import org.jetbrains.compose.resources.ExperimentalResourceApi

class GifLoading {
    companion object{
        @OptIn(ExperimentalResourceApi::class)
        @Composable
        fun GifLoadingScreen(localImageLoaderValue : ProvidedValue<*>,
                             gifName : String,
                             message : String) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CompositionLocalProvider(
                        localImageLoaderValue
                    ) {
                        AutoSizeImage(
                            getResId(gifName),
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(120.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        fun getScreenName() : String{
            return "GifLoadingScreen"
        }
    }
}