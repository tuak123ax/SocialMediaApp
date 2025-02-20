package com.minhtu.firesocialmedia.home.uploadnewsfeed

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.minhtu.firesocialmedia.home.Home
import com.minhtu.firesocialmedia.home.HomeViewModel

class UploadNewsfeed {
    companion object{
        @Composable
        fun UploadNewsfeedScreen(modifier: Modifier,
                                 homeViewModel: HomeViewModel,
                                 onNavigateToHomeScreen: () -> Unit){
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val galleryIntent = intentNavigateToGallery()
            val getAvatarFromGalleryLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()){
                    result ->
                if(result.resultCode == Activity.RESULT_OK){
                    Log.e("getAvatarFromGalleryLauncher", "RESULT_OK")
                    val image_url = result.data?.data
                    if(image_url != null){
                        Log.e("getAvatarFromGalleryLauncher", "update avatar: $image_url")
                        homeViewModel.updateImage(image_url.toString())
                    }
                }
            }
            LaunchedEffect(lifecycleOwner) {
                 homeViewModel.createPostStatus.observe(lifecycleOwner){createPostStatus ->
                     if(createPostStatus != null) {
                         if(createPostStatus) {
                             Toast.makeText(context, "Create post successfully!", Toast.LENGTH_SHORT).show()
                         } else {
                             Toast.makeText(context, "Create post failed! Please try again!", Toast.LENGTH_SHORT).show()
                         }
                         homeViewModel.resetPostStatus()
                         homeViewModel.createPostStatus.removeObservers(lifecycleOwner)
                         onNavigateToHomeScreen()
                     }
                }
            }
            Column(verticalArrangement = Arrangement.Center, modifier = modifier) {
                //Title
                Text(
                    text = "Create Post",
                    color = Color.Black,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(bottom = 20.dp))
                OutlinedTextField(
                    value = homeViewModel.message,
                    onValueChange = {
                        homeViewModel.updateMessage(it)
                    },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    label = { Text(text = "Message") }
                )
                Spacer(modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Button(onClick = {
                        getAvatarFromGalleryLauncher.launch(galleryIntent)
                    }) {
                        Text(text = "Select image")
                    }
                }
                Spacer(modifier = Modifier.padding(bottom = 10.dp))
                if(homeViewModel.image.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                        AsyncImage(model = homeViewModel.image,
                            contentDescription = "Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(400.dp)
                                .padding(20.dp)
                                .border(1.dp, Color.Gray)
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth().padding(5.dp)){
                    Button(onClick = {
                        onNavigateToHomeScreen()
                    }) {
                        Text(text = "Back")
                    }
                    Button(onClick = {
                        homeViewModel.createPost(homeViewModel.currentUser)
                    }) {
                        Text(text = "Post")
                    }
                }
            }
        }
        fun getScreenName() : String{
            return "UploadNewsfeedScreen"
        }
        private fun intentNavigateToGallery(): Intent {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            return intent
        }
    }
}