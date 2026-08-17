package com.minhtu.firesocialmedia.presentation.creategroup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.storage.group.SupabaseStorageProvider
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.group.ImagePicker
import com.minhtu.firesocialmedia.group.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.group.presentation.loading.Loading
import com.minhtu.firesocialmedia.group.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.group.TitleBarUtils
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.PasswordVisibilityIcon
import com.minhtu.sharedmodule.ui.theme.avatarGrayBackground
import org.koin.compose.viewmodel.koinViewModel

class CreateGroup {
    companion object {
        @Composable
        fun CreateGroupScreen(
            paddingValues: PaddingValues,
            createGroupViewModel: CreateGroupViewModel,
            imagePicker: ImagePicker,
            currentUser : UserInstance,
            onCreateGroupSuccess : (GroupInstance) -> Unit,
            onNavigateBack : () -> Unit
        ) {
            val loadingViewModel: LoadingViewModel = koinViewModel()
            CommonBackHandler {
                createGroupViewModel.resetCreateGroupUiState()
                onNavigateBack()
            }
            val isLoading by loadingViewModel.isLoading.collectAsState()
            imagePicker.RegisterLauncher { loadingViewModel.hideLoading() }
            val avatarModifier = Modifier
                .size(160.dp)
                .background(avatarGrayBackground)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                        currentUser.groups.add(createGroupState!!.id)
                        createGroupViewModel.resetAccessPermission()
                        createGroupViewModel.resetCreateGroupUiState()
                        onCreateGroupSuccess(createGroupState!!)
                    } else {
                        showToast("Create group failed. Please retry!!!")
                    }
                    createGroupViewModel.resetCreateGroupState()
                    loadingViewModel.hideLoading()
                }
            }
            Box(Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(Modifier.height(20.dp))
                    TitleBarUtils.BackAndTitleAndMoreOptionsRow(
                        "Create New Group",
                        titleColor = MaterialTheme.colorScheme.onSurface,
                        titleStyle = MaterialTheme.typography.headlineMedium,
                        "Set up your community in seconds",
                        showBackButton = false,
                        showMoreOptionsMenu = false
                    )
                    Spacer(Modifier.height(20.dp))
                    val imageBytes =
                        produceState<ByteArray?>(initialValue = null, createGroupViewModel.avatar) {
                            value = if (createGroupViewModel.avatar == SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL) {
                                getImageBytesFromDrawable("arkavatar")
                            } else {
                                imagePicker.loadImageBytes(createGroupViewModel.avatar)
                            }
                        }
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBytes.value != null) {
                            imagePicker.ByteArrayImage(
                                imageBytes.value,
                                modifier = avatarModifier
                            )
                        } else {
                            Box(modifier = avatarModifier)
                        }
                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                imagePicker.pickImage()
                            }
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                "Select avatar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "GROUP DETAILS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    OutlinedTextField(
                        value = createGroupViewModel.groupName.collectAsState().value,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                "Name"
                            )
                        },
                        onValueChange = { text ->
                            createGroupViewModel.updateGroupName(text)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag(TestTag.TAG_GROUP_NAME)
                            .semantics {
                                contentDescription = TestTag.TAG_GROUP_NAME
                            },
                        label = { Text(text = "Group Name") },
                        singleLine = true
                    )
                    Text(
                        text = "Who can join this group?",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(20.dp)
                    )
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
                            text = "Since this is a private group, please create a secure password for new members.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                        )
                        PasswordTextField(
                            "Group Password",
                            createGroupViewModel,
                            TestTag.TAG_GROUP_PASSWORD
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            if(createGroupViewModel.groupName.value.isEmpty()) {
                                showToast("Please input group name!")
                            } else {
                                if(createGroupViewModel.accessPermission.value == DecentralizationType.Private &&
                                    createGroupViewModel.password.value.isEmpty()) {
                                    showToast("Please input group password!")
                                } else {
                                    loadingViewModel.showLoading()
                                    createGroupViewModel.createGroup(currentUser)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_BUTTON_NEXT)
                            .semantics {
                                contentDescription = TestTag.TAG_BUTTON_NEXT
                            }) {
                        Text(
                            text = "Continue",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
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
                shape = RoundedCornerShape(20.dp),
                label = { Text(text = label) },
                singleLine = true,
                textStyle = TextStyle(Color.Unspecified),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisibility = !passwordVisibility },
                        modifier = Modifier
                            .testTag(TestTag.TAG_SHOW_PASSWORD)
                            .semantics{
                                contentDescription = TestTag.TAG_SHOW_PASSWORD
                            }) {
                        PasswordVisibilityIcon(
                            passwordVisibility,
                            tint = MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface.toHex())
                    }
                }
            )
        }
    }
}