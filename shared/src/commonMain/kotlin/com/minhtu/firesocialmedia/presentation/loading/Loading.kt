package com.minhtu.firesocialmedia.presentation.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class Loading{
    companion object{
        @Composable
        fun LoadingScreen(){
            Box(
                contentAlignment = Alignment.Companion.Center,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .background(Color.Companion.White.copy(alpha = 0.8f))
            ) {
                Column(
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.Companion.size(50.dp),
                        color = Color.Companion.Blue,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.Companion.height(16.dp))
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Companion.Gray
                    )
                }
            }
        }
        fun getScreenName() : String{
            return "LoadingScreen"
        }
    }
}