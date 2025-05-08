package com.minhtu.firesocialmedia.home.uploadnewsfeed

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class UploadNewsfeed {
    companion object{
        @Composable
        fun UploadNewsfeedScreen(modifier: Modifier,
                                 homeViewModel: HomeViewModel,
                                 uploadNewsfeedViewModel: UploadNewfeedViewModel = viewModel(),
                                 loadingViewModel: LoadingViewModel = viewModel(),
                                 updateNew : NewsInstance?,
                                 onNavigateToHomeScreen: () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            uploadNewsfeedViewModel.updateCurrentUser(homeViewModel.currentUser!!)
            uploadNewsfeedViewModel.updateListUsers(homeViewModel.listUsers)
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val galleryIntent = intentNavigateToGallery()
            val getAvatarFromGalleryLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()){
                    result ->
                if(result.resultCode == Activity.RESULT_OK){
                    Log.e("getAvatarFromGalleryLauncher", "RESULT_OK")
                    val imageUrl = result.data?.data
                    if(imageUrl != null){
                        uploadNewsfeedViewModel.updateImage(imageUrl.toString())
                    }
                }
            }
            var isUpdated by remember { mutableStateOf(false) }
            LaunchedEffect(lifecycleOwner) {
                if(updateNew != null) {
                    isUpdated = true
                    uploadNewsfeedViewModel.updateMessage(updateNew.message)
                    uploadNewsfeedViewModel.updateImage(updateNew.image)
                }
                uploadNewsfeedViewModel.createPostStatus.observe(lifecycleOwner){createPostStatus ->
                     if(createPostStatus != null) {
                         if(createPostStatus) {
                             Toast.makeText(context, "Create post successfully!", Toast.LENGTH_SHORT).show()
                         } else {
                             Toast.makeText(context, "Create post failed! Please try again!", Toast.LENGTH_SHORT).show()
                         }
                         loadingViewModel.hideLoading()
                         uploadNewsfeedViewModel.resetPostStatus()
                         uploadNewsfeedViewModel.createPostStatus.removeObservers(lifecycleOwner)
                         onNavigateToHomeScreen()
                     }
                }
                uploadNewsfeedViewModel.updatePostStatus.observe(lifecycleOwner){updatePostStatus ->
                    if(updatePostStatus != null) {
                        if(updatePostStatus) {
                            Toast.makeText(context, "Update post successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Update post failed! Please try again!", Toast.LENGTH_SHORT).show()
                        }
                        uploadNewsfeedViewModel.resetPostStatus()
                        uploadNewsfeedViewModel.updatePostStatus.removeObservers(lifecycleOwner)
                        onNavigateToHomeScreen()
                    }
                }
                uploadNewsfeedViewModel.postError.observe(lifecycleOwner){postError ->
                    if(postError != null) {
                        Toast.makeText(context, "Please input message or image!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val clickBackButton by uploadNewsfeedViewModel.clickBackButton.collectAsState()
            val showDialog = remember { mutableStateOf(false) }

            if (clickBackButton) {
                showDialog.value = true
                uploadNewsfeedViewModel.resetBackValue() // Reset state to prevent re-triggering
            }
            BackHandler {
                showDialog.value = true
            }
            if (showDialog.value) {
                UiUtils.ShowAlertDialog(
                    title = "Warning",
                    message = "Are you sure you want to exit? All data will be lost!",
                    resetAndBack = {
                        uploadNewsfeedViewModel.resetPostError()
                        uploadNewsfeedViewModel.postError.removeObservers(lifecycleOwner)
                        uploadNewsfeedViewModel.resetBackValue()
                        uploadNewsfeedViewModel.resetPostStatus()
                        onNavigateToHomeScreen()
                        showDialog.value = false // Close the dialog
                    },
                    showDialog = showDialog
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(verticalArrangement = Arrangement.Center, modifier = modifier) {
                    //Title
                    Text(
                        text = if(isUpdated) "Update Post" else "Create Post",
                        color = Color.Black,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    OutlinedTextField(
                        value = uploadNewsfeedViewModel.message,
                        onValueChange = {
                            uploadNewsfeedViewModel.updateMessage(it)
                        },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_POST_MESSAGE)
                            .semantics{
                                contentDescription = TestTag.TAG_POST_MESSAGE
                            },
                        label = { Text(text = "Message") }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    Row(horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                            .testTag(TestTag.TAG_BUTTON_SELECTIMAGE)
                            .semantics{
                                contentDescription = TestTag.TAG_BUTTON_SELECTIMAGE
                            }) {
                        Button(onClick = {
                            getAvatarFromGalleryLauncher.launch(galleryIntent)
                        }) {
                            Text(text = "Select image")
                        }
                    }
                    Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    if (uploadNewsfeedViewModel.image.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            AsyncImage(
                                model = uploadNewsfeedViewModel.image,
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
                        Button(
                            onClick = {
                                uploadNewsfeedViewModel.onClickBackButton()
                            },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_BACK)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_BACK
                                }

                        ) {
                            Text(text = "Back")
                        }
                        if(uploadNewsfeedViewModel.image.isNotEmpty()) {
                            Button(onClick = {
                                uploadNewsfeedViewModel.updateImage("")
                            },
                                modifier = Modifier.testTag(TestTag.TAG_BUTTON_DELETE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_BUTTON_DELETE
                                    }
                            ) {
                                Text(text = "Delete")
                            }
                        }
                        Button(
                            onClick = {
                                loadingViewModel.showLoading()
                                if(isUpdated) uploadNewsfeedViewModel.updateNewInformation(updateNew!!)
                                else uploadNewsfeedViewModel.createPost(uploadNewsfeedViewModel.currentUser!!)
                            },
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_POST)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_POST
                                }
                        ) {
                            Text(text = if(isUpdated) "Update" else "Post")
                        }
                    }
                }
                if (isLoading) {
                    Loading.LoadingScreen()
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