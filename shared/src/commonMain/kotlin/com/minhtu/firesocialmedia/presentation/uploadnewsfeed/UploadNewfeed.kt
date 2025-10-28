package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.ui.AutoSizeImage

class UploadNewsfeed {
    companion object{
        @Composable
        fun UploadNewsfeedScreen(modifier: Modifier,
                                 imagePicker: ImagePicker,
                                 localImageLoaderValue : ProvidedValue<*>,
                                 homeViewModel: HomeViewModel,
                                 uploadNewsfeedViewModel: UploadNewfeedViewModel,
                                 loadingViewModel: LoadingViewModel,
                                 updateNew : NewsInstance?,
                                 onNavigateToHomeScreen: () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            uploadNewsfeedViewModel.updateCurrentUser(homeViewModel.currentUser!!)

            imagePicker.RegisterLauncher({loadingViewModel.hideLoading()})
            val postStatus = uploadNewsfeedViewModel.createPostStatus.collectAsState()
            val updateStatus = uploadNewsfeedViewModel.updatePostStatus.collectAsState()
            val postError = uploadNewsfeedViewModel.postError.collectAsState()
            var isUpdated by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if(updateNew != null) {
                    isUpdated = true
                    uploadNewsfeedViewModel.updatePostData(
                        updateNew.message,
                        updateNew.image,
                        updateNew.video)
                } else {
                    uploadNewsfeedViewModel.loadNewsPostedWhenOffline()
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

            val newsPostedWhenOffline by uploadNewsfeedViewModel.newsPostedWhenOffline.collectAsState()

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
            var showDraftPickerDialog by remember { mutableStateOf(false) }
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
                            val imageBytes = produceState<ByteArray?>(initialValue = null, uploadNewsfeedViewModel.image) {
                                value = imagePicker.loadImageBytes(uploadNewsfeedViewModel.image)
                            }
                            if(imageBytes.value != null) {
                                imagePicker.ByteArrayImage(
                                    imageBytes.value,
                                    modifier = Modifier
                                        .height(300.dp)
                                        .padding(20.dp)
                                        .border(1.dp, Color.Gray))
                            } else {
                                if(uploadNewsfeedViewModel.image.isNotEmpty()){
                                    CompositionLocalProvider(
                                        localImageLoaderValue
                                    ) {
                                        AutoSizeImage(
                                            uploadNewsfeedViewModel.image,
                                            contentDescription = "Image",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .padding(5.dp)
                                                .testTag(TestTag.TAG_POST_IMAGE)
                                                .semantics{
                                                    contentDescription = TestTag.TAG_POST_IMAGE
                                                }
                                        )
                                    }
                                }
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
                                val videoUri: String = if(uploadNewsfeedViewModel.localPathOfSelectedDraft.value.isNotEmpty()) {
                                    //Load video from local storage
                                    getUriStringFromLocalPath(uploadNewsfeedViewModel.localPathOfSelectedDraft.value)
                                } else {
                                    uploadNewsfeedViewModel.video
                                }
                                if(video.isNotEmpty()) {
                                    VideoPlayer(videoUri,
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
                Row(horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp)) {
                    OutlinedButton(
                        onClick = {
                            showDraftPickerDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Companion.White,
                            contentColor = Color.Companion.Black
                        ),
                        modifier = Modifier.Companion
                            .testTag(TestTag.Companion.TAG_BUTTON_DRAFTPOST)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_BUTTON_DRAFTPOST
                            }
                    ) {
                        BadgedBox(
                            badge = {
                                if (newsPostedWhenOffline.isNotEmpty()) Badge { Text(if (newsPostedWhenOffline.size > 99) "99+" else "${newsPostedWhenOffline.size}") }
                            }
                        ) {
                            CrossPlatformIcon(
                                "draft",
                                backgroundColor = "#FFFFFFFF",
                                "Draft",
                                Modifier.Companion
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                        }
                        Spacer(Modifier.padding(horizontal = 5.dp))
                        Text(text = "Your draft posts", color = Color.Companion.Black)
                    }
                }
                DraftPostPickerDialog(
                    localImageLoaderValue = localImageLoaderValue,
                    visible = showDraftPickerDialog,
                    drafts = newsPostedWhenOffline,
                    onDismiss = { showDraftPickerDialog = false },
                    onSelect = { draft ->
                        uploadNewsfeedViewModel.updatePostData(
                            draft.message,
                            draft.image,
                            draft.video
                        )
                        if(draft.localPath.isNotEmpty()) {
                            uploadNewsfeedViewModel.updateLocalPath(draft.localPath)
                        }
                        showDraftPickerDialog = false
                    }
                )
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

        @Composable
        fun DraftPostPickerDialog(
            localImageLoaderValue: ProvidedValue<*>,
            visible: Boolean,
            drafts: List<NewsInstance>,
            onDismiss: () -> Unit,
            onSelect: (NewsInstance) -> Unit
        ) {
            if (!visible) return

            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // cap at 80% of window height
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Choose your draft", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        val listState = rememberLazyListState()
                        if(drafts.isNotEmpty()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = true), // list scrolls within remaining space
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(drafts, key = { it.id }) { draft ->
                                    UiUtils.SimpleNewsCard(
                                        draft,
                                        localImageLoaderValue,
                                        onSelected = { onSelect(draft) }
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)) {
                                Text(
                                    text = "No draft here!",
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                CrossPlatformIcon(
                                    "nothing_here",
                                    backgroundColor = "#FFFFFFFF",
                                    "nothing",
                                    Modifier.Companion
                                        .fillMaxSize()
                                        .padding(vertical = 20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text("Close") }
                        }
                    }
                }
            }
        }

    }
}