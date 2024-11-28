package com.minhtu.firesocialmedia.utils

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.instance.NewsInstance

class UiUtils {
    companion object{
//        @Composable
//        fun PasswordTextField(label : String) {
//            var password by rememberSaveable {
//                mutableStateOf("")
//            }
//            var passwordVisibility by rememberSaveable {
//                mutableStateOf(false)
//            }
//            OutlinedTextField(
//                value = password, onValueChange = {
//                    password = it
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//                    //Fix crash: java.lang.IllegalStateException: Already in the pool! when using visualTransformation
//                    .clearAndSetSemantics {  },
//                label = { Text(text = label) },
//                singleLine = true,
//                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                trailingIcon = {
//                    val icon = if(passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
//                    IconButton(onClick = {passwordVisibility = !passwordVisibility}) {
//                        Icon(imageVector = icon, descriptionOfIcon)
//                    }
//                }
//            )
//        }
@Composable
fun NewsCard(news: NewsInstance, context: Context, onNavigateToShowImageScreen: (image: String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(300.dp)
            .border(3.dp, Color.Gray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.Start,
                modifier = Modifier.background(color = Color.Cyan).padding(10.dp).fillMaxWidth()){
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(news.avatar))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Poster Avatar",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            // Handle image click
                        }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = news.posterName,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 2.dp) // Adds padding around text
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = news.message,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 4.dp) // Adds padding around text
            )
            Spacer(modifier = Modifier.height(1.dp))
            AsyncImage(
                model = news.image,
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(10.dp)
                    .clickable {
                        onNavigateToShowImageScreen(news.image)
                    }
            )
        }
    }
}
    }
}