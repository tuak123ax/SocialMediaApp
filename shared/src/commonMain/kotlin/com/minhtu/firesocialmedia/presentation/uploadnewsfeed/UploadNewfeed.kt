package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.ImagePicker
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.utils.UiUtils

class UploadNewsfeed {
    companion object{
        @Composable
        fun UploadNewsfeedScreen(modifier: Modifier,
                                 platform : PlatformContext,
                                 imagePicker: ImagePicker,
                                 homeViewModel: HomeViewModel,
                                 uploadNewsfeedViewModel: UploadNewfeedViewModel,
                                 loadingViewModel: LoadingViewModel,
                                 updateNew : NewsInstance?,
                                 onNavigateToHomeScreen: () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            uploadNewsfeedViewModel.updateCurrentUser(homeViewModel.currentUser!!)
            uploadNewsfeedViewModel.updateListUsers(homeViewModel.listUsers)

            imagePicker.RegisterLauncher({loadingViewModel.hideLoading()})
            val postStatus = uploadNewsfeedViewModel.createPostStatus.collectAsState()
            val updateStatus = uploadNewsfeedViewModel.updatePostStatus.collectAsState()
            val postError = uploadNewsfeedViewModel.postError.collectAsState()
            var isUpdated by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if(updateNew != null) {
                    isUpdated = true
                    uploadNewsfeedViewModel.updateMessage(updateNew.message)
                    uploadNewsfeedViewModel.updateImage(updateNew.image)
                    uploadNewsfeedViewModel.updateVideo(updateNew.video)
                }
            }

            LaunchedEffect(postStatus.value) {
                if(postStatus.value != null) {
                    if(postStatus.value!!) {
                        showToast("Create post successfully!")
                    } else {
                        showToast("Create post failed! Please try again!")
                    }
                    loadingViewModel.hideLoading()
                    uploadNewsfeedViewModel.resetPostStatus()
                    onNavigateToHomeScreen()
                }
            }
            LaunchedEffect(updateStatus.value) {
                if(updateStatus.value != null) {
                    if(updateStatus.value!!) {
                        showToast("Update post successfully!")
                    } else {
                        showToast("Update post failed! Please try again!")
                    }
                    loadingViewModel.hideLoading()
                    uploadNewsfeedViewModel.resetPostStatus()
                    onNavigateToHomeScreen()
                }
            }
            LaunchedEffect(postError.value) {
                if(postError.value != null) {
                    showToast("Please input message or image!")
                }
            }



            val clickBackButton by uploadNewsfeedViewModel.clickBackButton.collectAsState()
            val showDialog = remember { mutableStateOf(false) }

            if (clickBackButton) {
                showDialog.value = true
                uploadNewsfeedViewModel.resetBackValue() // Reset state to prevent re-triggering
            }
            CommonBackHandler {
                showDialog.value = true
            }
            if (showDialog.value) {
                UiUtils.ShowAlertDialog(
                    title = "Warning",
                    message = "Are you sure you want to exit? All data will be lost!",
                    resetAndBack = {
                        uploadNewsfeedViewModel.resetPostError()
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black
                        ),
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
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            var showMenu by remember { mutableStateOf(false) }
                            Button(onClick = {
                                showMenu = true
                            },
                                modifier = Modifier.testTag(TestTag.TAG_BUTTON_UPLOAD)
                                    .semantics{
                                        contentDescription = TestTag.TAG_BUTTON_UPLOAD
                                    }
                                ) {
                                Text(text = "Upload")
                            }
                            DropdownMenuForUpload(
                                showMenu,
                                onUploadImage = {
                                    imagePicker.pickImage()
//                            loadingViewModel.showLoading()
                                },
                                onUploadVideo = {
                                    imagePicker.pickVideo()
                                },
                                onDismissRequest = {showMenu = false}
                            )
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
                            var imageBytes = produceState<ByteArray?>(initialValue = null, uploadNewsfeedViewModel.image) {
                                value = imagePicker.loadImageBytes(uploadNewsfeedViewModel.image)
                            }
                            if(imageBytes.value != null) {
                                imagePicker.ByteArrayImage(
                                    imageBytes.value,
                                    modifier = Modifier
                                        .height(300.dp)
                                        .padding(20.dp)
                                        .border(1.dp, Color.Gray))
                            }
                        }
                    } else {
                        if(uploadNewsfeedViewModel.video.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                            ) {
                                val video = uploadNewsfeedViewModel.video
                                if(video.isNotEmpty()) {
                                    logMessage("VideoPlayer", { video })
                                    VideoPlayer(video,
                                        modifier = Modifier
                                            .height(300.dp)
                                            .fillMaxWidth()
                                            .padding(20.dp))
                                }
                            }
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
                        if(uploadNewsfeedViewModel.image.isNotEmpty() || uploadNewsfeedViewModel.video.isNotEmpty()) {
                            Button(onClick = {
                                uploadNewsfeedViewModel.updateImage("")
                                uploadNewsfeedViewModel.updateVideo("")
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
                                if(isUpdated) uploadNewsfeedViewModel.updateNewInformation(updateNew!!, platform)
                                else uploadNewsfeedViewModel.createPost(uploadNewsfeedViewModel.currentUser!!, platform)
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

        @Composable
        fun DropdownMenuForUpload(expanded : Boolean,
                                  onUploadImage : () -> Unit,
                                  onUploadVideo : () -> Unit,
                                  onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Upload Image") },
                    onClick = {
                        onUploadImage()
                        onDismissRequest()
                    },
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_SELECTIMAGE)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_SELECTIMAGE
                        }
                )
                DropdownMenuItem(
                    text = { Text("Upload Video") },
                    onClick = {
                        onUploadVideo()
                        onDismissRequest()
                    },
                    modifier = Modifier.testTag(TestTag.TAG_BUTTON_SELECTVIDEO)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_SELECTVIDEO
                        }
                )
            }
        }
    }
}