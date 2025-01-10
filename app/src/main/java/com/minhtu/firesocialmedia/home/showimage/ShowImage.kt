package com.minhtu.firesocialmedia.home.showimage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

class ShowImage {
    companion object{
        @Composable
        fun ShowImageScreen(image : String, modifier: Modifier){
            val context = LocalContext.current
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(20.dp)){
                AsyncImage(model = ImageRequest.Builder(context)
                    .data(image)
                    .crossfade(true)
                    .build(),
                    contentDescription = "image",
                    contentScale = ContentScale.Fit)
            }
        }

        fun getScreenName() : String{
            return "ShowImageScreen"
        }
    }
}