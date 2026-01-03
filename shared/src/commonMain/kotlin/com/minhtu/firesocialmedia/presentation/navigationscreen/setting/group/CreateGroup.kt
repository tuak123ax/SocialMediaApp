package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.PasswordVisibilityIcon
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewsfeed.Companion.AccessPermissionBottomSheet
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewsfeed.Companion.AccessPermissionButtonContent

class CreateGroup {
    companion object {
        @Composable
        fun CreateGroupScreen(
            createGroupViewModel: CreateGroupViewModel,
            loadingViewModel : LoadingViewModel,
            imagePicker: ImagePicker,
            currentUser : UserInstance,
            onCreateGroupSuccess : (GroupInstance) -> Unit
        ) {
            val isLoading by loadingViewModel.isLoading.collectAsState()
            imagePicker.RegisterLauncher { loadingViewModel.hideLoading() }
            val avatarModifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
                .clickable {
                    loadingViewModel.showLoading()
                    imagePicker.pickImage()
                }
                .testTag(TestTag.TAG_SELECT_GROUP_AVATAR)
                .semantics {
                    contentDescription = TestTag.TAG_SELECT_GROUP_AVATAR
                }
            var showAccessPermissionSheet by remember { mutableStateOf(false) }
            val currentAccessPermission = createGroupViewModel.accessPermission.collectAsState()
            val createGroupState by createGroupViewModel.createGroupState.collectAsState()
            LaunchedEffect(createGroupState) {
                if(createGroupState != null) {
                    if(createGroupState!!.id.isNotEmpty()) {
                        showToast("Create group successfully!!!")
                        onCreateGroupSuccess(createGroupState!!)
                    } else {
                        showToast("Create group failed. Please retry!!!")
                    }
                    createGroupViewModel.resetCreateGroupState()
                }
            }
            Box(Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                ) {
                    Text(
                        text = "Please select group avatar",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    val imageBytes =
                        produceState<ByteArray?>(initialValue = null, createGroupViewModel.avatar) {
                            value = if (createGroupViewModel.avatar == Constants.DEFAULT_AVATAR_URL) {
                                getImageBytesFromDrawable("unknownavatar")
                            } else {
                                imagePicker.loadImageBytes(createGroupViewModel.avatar)
                            }
                        }
                    if (imageBytes.value != null) {
                        imagePicker.ByteArrayImage(
                            imageBytes.value,
                            modifier = avatarModifier
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "And input group name below",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    OutlinedTextField(
                        value = createGroupViewModel.groupName.collectAsState().value,
                        onValueChange = { text ->
                            createGroupViewModel.updateGroupName(text)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .focusable(true)
                            .testTag(TestTag.TAG_GROUP_NAME)
                            .semantics {
                                contentDescription = TestTag.TAG_GROUP_NAME
                            },
                        shape = RoundedCornerShape(30.dp),
                        label = { Text(text = "Group Name") },
                        singleLine = true,
                        textStyle = TextStyle(Color.Black)
                    )
                    Text(
                        text = "Who can join this group?",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(contentAlignment = Alignment.Center) {
                        OutlinedButton(
                            onClick = {
                                //Show bottom sheet to choose access permission
                                showAccessPermissionSheet = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
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
                                title = "Who can join this group?",
                                currentAccessPermission.value,
                                onDismiss = {
                                    showAccessPermissionSheet = false
                                },
                                onSelected = { selectedAccess ->
                                    showAccessPermissionSheet = false
                                    createGroupViewModel.updateAccessPermission(selectedAccess)
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    if(currentAccessPermission.value == DecentralizationType.Private) {
                        Text(
                            text = "If this is private group, please input password",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        PasswordTextField(
                            "Group Password",
                            createGroupViewModel,
                            TestTag.TAG_GROUP_PASSWORD
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                createGroupViewModel.createGroup(currentUser)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                                .testTag(TestTag.TAG_BUTTON_NEXT)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_NEXT
                                }) {
                            Text(text = "Continue")
                        }
                    }
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }
        fun getScreenName() : String {
            return "CreateGroupScreen"
        }

        @Composable
        fun PasswordTextField(label : String, createGroupViewModel: CreateGroupViewModel, testTag: String) {
            var passwordVisibility by rememberSaveable {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = createGroupViewModel.password.collectAsState().value,
                onValueChange = { password ->
                    createGroupViewModel.updatePassword(password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.Black),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        PasswordVisibilityIcon(passwordVisibility)
                    }
                }
            )
        }
    }
}