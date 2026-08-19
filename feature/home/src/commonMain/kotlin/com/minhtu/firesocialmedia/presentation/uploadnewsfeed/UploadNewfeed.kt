package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minhtu.firesocialmedia.constants.home.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.home.ImagePicker
import com.minhtu.firesocialmedia.home.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.home.platform.VideoPlayer
import com.minhtu.firesocialmedia.platform.getUriStringFromLocalPath
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.home.presentation.loading.Loading
import com.minhtu.firesocialmedia.home.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.home.DialogUtils
import com.minhtu.firesocialmedia.utils.home.NewsCardUtils
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

class UploadNewsfeed {
    companion object{
        @Composable
        fun UploadNewsfeedScreen(paddingValues : PaddingValues,
                                 imagePicker: ImagePicker,
                                 localImageLoaderValue : ProvidedValue<*>,
                                 homeViewModel: HomeViewModel,
                                 uploadNewsfeedViewModel: UploadNewfeedViewModel,
                                 updateNew : NewsInstance?,
                                 onNavigateBack: () -> Unit){
            val loadingViewModel: LoadingViewModel = koinViewModel()
            val isLoading by loadingViewModel.isLoading.collectAsState()
            uploadNewsfeedViewModel.updateCurrentUser(homeViewModel.currentUser!!)
            val textScrollState = rememberScrollState()
            imagePicker.RegisterLauncher({loadingViewModel.hideLoading()})
            val postStatus = uploadNewsfeedViewModel.createPostStatus.collectAsState()
            val updateStatus = uploadNewsfeedViewModel.updatePostStatus.collectAsState()
            val postError by uploadNewsfeedViewModel.postError.collectAsState()
            var isUpdated by remember { mutableStateOf(false) }
            var showAccessPermissionSheet by remember { mutableStateOf(false) }
            val currentAccessPermission = uploadNewsfeedViewModel.accessPermission.collectAsState()
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
                    onNavigateBack()
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
                    onNavigateBack()
                }
            }
            LaunchedEffect(postError) {
                if(postError != null) {
                    showToast("Please input message or image!")
                    loadingViewModel.hideLoading()
                    uploadNewsfeedViewModel.resetPostError()
                }
            }

            val newsPostedWhenOffline by uploadNewsfeedViewModel.newsPostedWhenOffline.collectAsState()

            val deleteDraftState by uploadNewsfeedViewModel.deleteDraftStatus.collectAsState()
            LaunchedEffect(deleteDraftState) {
                if(deleteDraftState != null) {
                    if(deleteDraftState!!) {
                        showToast("Delete successfully!!!")
                    } else {
                        showToast("Error happened! Load draft posts again!")
                    }
                    uploadNewsfeedViewModel.resetDeleteDraftStatus()
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
                DialogUtils.ShowDiscardDialog(
                    title = "Warning",
                    message = "Are you sure you want to exit? All data will be lost!",
                    icon = Icons.Default.Warning,
                    iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                    onDiscard = {
                        uploadNewsfeedViewModel.resetPostError()
                        uploadNewsfeedViewModel.resetBackValue()
                        uploadNewsfeedViewModel.resetPostStatus()
                        uploadNewsfeedViewModel.resetGroupId()
                        onNavigateBack()
                        showDialog.value = false // Close the dialog
                    },
                    showDialog = showDialog
                )
            }
            var showDraftPickerDialog by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    //Close and draft buttons
                    CloseAndDraftButtons(
                        newsPostedWhenOffline,
                        onClickCloseButton = {
                            uploadNewsfeedViewModel.onClickBackButton()
                        },
                        onClickDraftBoxButton = {
                            showDraftPickerDialog = true
                        }
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .background(MaterialTheme.colorScheme.background)) {
                        //Title
                        Text(
                            text = if(isUpdated) "Update Post" else "Create Post",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                OutlinedButton(
                                    onClick = {
                                        //Show bottom sheet to choose access permission
                                        showAccessPermissionSheet = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .testTag(TestTag.TAG_BUTTON_ACCESS_MODIFIER)
                                        .semantics {
                                            contentDescription = TestTag.TAG_BUTTON_ACCESS_MODIFIER
                                        }
                                ) {
                                    AccessPermissionButtonContent(currentAccessPermission.value)
                                }
                                if(showAccessPermissionSheet) {
                                    //Access permission sheet
                                    AccessPermissionBottomSheet(
                                        title = "Who can see your post?",
                                        currentAccessPermission.value,
                                        onDismiss = {
                                            showAccessPermissionSheet = false
                                        },
                                        onSelected = { selectedAccess ->
                                            showAccessPermissionSheet = false
                                            uploadNewsfeedViewModel.updateAccessPermission(selectedAccess)
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uploadNewsfeedViewModel.message,
                            onValueChange = {
                                uploadNewsfeedViewModel.updateMessage(it)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .heightIn(min = 100.dp, max = 140.dp)
                                .verticalScroll(textScrollState)
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                            label = { Text("What's on your mind?") },
                            maxLines = Int.MAX_VALUE,
                            singleLine = false
                        )
                        if (uploadNewsfeedViewModel.image.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                val imageBytes = produceState<ByteArray?>(initialValue = null, uploadNewsfeedViewModel.image) {
                                    value = imagePicker.loadImageBytes(uploadNewsfeedViewModel.image)
                                }
                                if(imageBytes.value != null) {
                                    imagePicker.ByteArrayImage(
                                        imageBytes.value,
                                        modifier = Modifier
                                            .height(250.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline)
                                    )
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
                                                    .height(250.dp)
                                                    .padding(horizontal = 20.dp)
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
                                ) {
                                    val video = uploadNewsfeedViewModel.video
                                    val videoUri: String = if(uploadNewsfeedViewModel.localPathOfSelectedDraft.value.isNotEmpty()) {
                                        //Load video from local storage
                                        getUriStringFromLocalPath(uploadNewsfeedViewModel.localPathOfSelectedDraft.value)
                                    } else {
                                        uploadNewsfeedViewModel.video
                                    }
                                    if(video.isNotEmpty()) {
                                        VideoPlayer(
                                            uri = videoUri,
                                            modifier = Modifier
                                                .height(250.dp)
                                                .fillMaxWidth()
                                                .padding(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    //Delete and upload image row
                    Row(horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)){
                        if(uploadNewsfeedViewModel.image.isNotEmpty() || uploadNewsfeedViewModel.video.isNotEmpty()) {
                            Button(
                                onClick = {
                                uploadNewsfeedViewModel.updateImage("")
                                uploadNewsfeedViewModel.updateVideo("")
                            },
                                shape = RoundedCornerShape(10.dp),
                                elevation = ButtonDefaults.buttonElevation(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier
                                    .testTag(TestTag.TAG_BUTTON_DELETE)
                                    .semantics{
                                        contentDescription = TestTag.TAG_BUTTON_DELETE
                                    }
                            ) {
                                Text(text = "Delete")
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        Box(contentAlignment = Alignment.Center) {
                            var showMenu by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.outline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .testTag(TestTag.TAG_BUTTON_UPLOAD)
                                    .semantics {
                                        contentDescription = TestTag.TAG_BUTTON_UPLOAD
                                    }
                            ) {
                                CrossPlatformIcon(
                                    icon = "image",
                                    contentDescription = "Upload",
                                    tint = MaterialTheme.colorScheme.primary,
                                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Upload",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                            DropdownMenuForUpload(
                                showMenu,
                                onUploadImage = { imagePicker.pickImage() },
                                onUploadVideo = { imagePicker.pickVideo() },
                                onDismissRequest = { showMenu = false }
                            )
                        }
                    }
                    //Post button
                    Button(
                        onClick = {
                            loadingViewModel.showLoading()
                            if(isUpdated) uploadNewsfeedViewModel.updateNewInformation(updateNew!!)
                            else uploadNewsfeedViewModel.createPost(uploadNewsfeedViewModel.currentUser!!)
                        },
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag(TestTag.TAG_BUTTON_POST)
                            .semantics{
                                contentDescription = TestTag.TAG_BUTTON_POST
                            }
                    ) {
                        Text(text = if(isUpdated) "Update" else "Post")
                    }
                    //Back button
                    Button(
                        onClick = {
                            uploadNewsfeedViewModel.onClickBackButton()
                        },
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(vertical = 10.dp)
                            .testTag(TestTag.TAG_BUTTON_BACK)
                            .semantics{
                                contentDescription = TestTag.TAG_BUTTON_BACK
                            }
                    ) {
                        Text(
                            text = "Back",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // The root Scaffold only reserves the top safe-drawing inset (see
                    // SetUpNavigation in Navigation.kt), so this trailing spacer must clear the
                    // system navigation bar itself instead of using a fixed height. Use
                    // coerceAtLeast so the original 10.dp gap is preserved on devices with a
                    // thin/no nav bar inset.
                    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
                    Spacer(Modifier.height(navigationBarPadding.calculateBottomPadding().coerceAtLeast(10.dp)))
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
                    },
                    onDeleteAll = {
                        uploadNewsfeedViewModel.deleteAllDraftPosts()
                    },
                    onDeleteANew = { new ->
                        uploadNewsfeedViewModel.deleteDraftPost(new.id)
                    }
                )
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        @Composable
        fun CloseAndDraftButtons(
            newsPostedWhenOffline : List<NewsInstance>,
            onClickCloseButton : () -> Unit,
            onClickDraftBoxButton : () -> Unit
        ) {
            Row(horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)) {
                CrossPlatformIcon(
                    icon = "close",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .clickable {
                            onClickCloseButton()
                        }
                        .testTag(TestTag.TAG_BUTTON_CLOSE)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_CLOSE
                        }
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        onClickDraftBoxButton()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .testTag(TestTag.TAG_BUTTON_DRAFTPOST)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_DRAFTPOST
                        }
                ) {
                    BadgedBox(
                        badge = {
                            if (newsPostedWhenOffline.isNotEmpty()) Badge { Text(if (newsPostedWhenOffline.size > 99) "99+" else "${newsPostedWhenOffline.size}") }
                        }
                    ) {
                        CrossPlatformIcon(
                            "draft",
                            backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                            "Draft",
                            Modifier
                                .size(25.dp)
                                .padding(end = 5.dp)
                        )
                    }
                    Spacer(Modifier.padding(horizontal = 5.dp))
                    Text(text = "Your draft posts", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        @Composable
        fun AccessPermissionButtonContent(currentAccessPermission: DecentralizationType) {
            CrossPlatformIcon(
                icon = when(currentAccessPermission) {
                    DecentralizationType.Public -> "public"
                    DecentralizationType.Private -> "private"
                    DecentralizationType.OnlyFriends -> "onlyFriends"
                },
                backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                contentDescription = "accessPermission",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(25.dp)
                    .padding(end = 5.dp)
            )
            Text(text = when(currentAccessPermission) {
                DecentralizationType.Public -> {"Public"}
                DecentralizationType.Private -> {"Private"}
                DecentralizationType.OnlyFriends -> {"Only Friends"}
            }, color = MaterialTheme.colorScheme.onSurface)
            CrossPlatformIcon(
                icon = "down_arrow",
                backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "down_arrow",
                modifier = Modifier
                    .size(25.dp)
                    .padding(end = 5.dp)
            )
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
            onSelect: (NewsInstance) -> Unit,
            onDeleteAll : () -> Unit,
            onDeleteANew : (NewsInstance) -> Unit
        ) {
            if (!visible) return

            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // cap at 80% of window height
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Choose your draft",
                                style = MaterialTheme.typography.titleMedium)
                            if(drafts.isNotEmpty()) {
                                TextButton(onClick = onDeleteAll) {
                                    Text("Delete All",
                                        style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val listState = rememberLazyListState()
                        if(drafts.isNotEmpty()) {
                            val sortedDrafts by remember(drafts) {
                                derivedStateOf { drafts.sortedByDescending { it.timePosted } }
                            }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = true), // list scrolls within remaining space
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(sortedDrafts, key = { it.id }) { draft ->
                                    //State to track visibility of a notification
                                    var visible by remember { mutableStateOf(true) }
                                    //State to track to delay before delete data from db
                                    var pendingDelete by remember { mutableStateOf(false) }
                                    if (pendingDelete) {
                                        // wait for animation before removing
                                        LaunchedEffect(Unit) {
                                            delay(200)
                                            onDeleteANew(draft)
                                        }
                                    }
                                    AnimatedVisibility(
                                        visible = visible,
                                        exit = slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                    ) {
                                        NewsCardUtils.SimpleNewsCardSlideable(
                                            draft,
                                            localImageLoaderValue,
                                            onSelected = {
                                                onSelect(draft)
                                            },
                                            onDelete = {
                                                visible = false
                                                pendingDelete = true
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)) {
                                Text(
                                    text = "No draft here!",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                CrossPlatformIcon(
                                    "nothing_here",
                                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                                    "nothing",
                                    Modifier
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

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun AccessPermissionBottomSheet(
            title : String,
            currentAccess : DecentralizationType,
            onDismiss: () -> Unit,
            onSelected: (selectedAccess : DecentralizationType) -> Unit
        ) {
            var selectedAccess by remember { mutableStateOf(currentAccess) }
            ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = rememberModalBottomSheetState(),
            ) {
                // Sheet Content
                Column(Modifier.padding(16.dp)) {
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    AccessPermissionRow(currentAccess) { access ->
                        selectedAccess = access
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(onClick = {
                            onSelected(selectedAccess)
                            }) {
                            Text("Select")
                        }
                    }
                }
            }
        }

        @Composable
        fun AccessPermissionRow(
            currentAccess: DecentralizationType,
            onSelect: (DecentralizationType) -> Unit) {
            var localAccessState by remember { mutableStateOf(currentAccess) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                CrossPlatformIcon(
                    icon = "public",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "public",
                    tint = if(localAccessState == DecentralizationType.Public) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(25.dp)
                        .testTag(TestTag.TAG_SELECT_PUBLIC)
                        .semantics {
                            contentDescription = TestTag.TAG_SELECT_PUBLIC
                        }
                )
                Text(
                    text = "Public"
                )
                RadioButton(
                    selected = localAccessState == DecentralizationType.Public,
                    onClick = {
                        localAccessState = DecentralizationType.Public
                        onSelect(localAccessState)
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                CrossPlatformIcon(
                    icon = "private",
                    backgroundColor = MaterialTheme.colorScheme.surface.toHex(),
                    contentDescription = "private",
                    tint = if(localAccessState == DecentralizationType.Private) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(25.dp)
                        .testTag(TestTag.TAG_SELECT_PRIVATE)
                        .semantics {
                            contentDescription = TestTag.TAG_SELECT_PRIVATE
                        }
                )
                Text(
                    text = "Private"
                )
                RadioButton(
                    selected = localAccessState == DecentralizationType.Private,
                    onClick = {
                        localAccessState = DecentralizationType.Private
                        onSelect(localAccessState)
                    }
                )
            }
        }
    }
}