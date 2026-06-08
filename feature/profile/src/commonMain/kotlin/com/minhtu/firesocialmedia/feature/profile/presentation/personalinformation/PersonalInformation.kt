package com.minhtu.firesocialmedia.feature.profile.presentation.personalinformation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.core.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.PasswordVerifyDialog
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage

class PersonalInformation {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun PersonalInformationScreen(
            currentUser: UserInstance,
            imagePicker: ImagePicker,
            paddingValues: PaddingValues = PaddingValues(0.dp),
            personalInformationViewModel: PersonalInformationViewModel,
            onNavigateBack: () -> Unit
        ) {
            CommonBackHandler { onNavigateBack() }

            // Register image picker launcher — when an image is picked, store URI and trigger upload
            imagePicker.RegisterLauncher {
                // hideLoading no-op here; loading is handled by ViewModel
            }

            val isLoading by personalInformationViewModel.isLoading.collectAsState()
            val updateStatus by personalInformationViewModel.updateStatus.collectAsState()
            val reAuthStatus by personalInformationViewModel.reAuthStatus.collectAsState()
            val fetchedUser by personalInformationViewModel.fetchedUser.collectAsState()

            // Use freshly fetched data if available, otherwise fall back to passed-in currentUser
            val user = fetchedUser ?: currentUser

            // Trigger fetch on first entry
            LaunchedEffect(Unit) {
                personalInformationViewModel.fetchCurrentUser(currentUser.uid)
            }

            // Editable field states — update when fresh data arrives
            var name by remember { mutableStateOf(currentUser.name) }
            var status by remember { mutableStateOf(currentUser.status) }
            var phone by remember { mutableStateOf(currentUser.phone) }

            LaunchedEffect(fetchedUser) {
                fetchedUser?.let {
                    name = it.name
                    status = it.status
                    phone = it.phone
                }
            }

            // Local avatar preview (null = use remote URL)
            val pickedAvatarUri = personalInformationViewModel.avatarUri
            val uploadedAvatarUri = personalInformationViewModel.uploadedAvatarUri

            // Avatar confirmation dialog state
            var showAvatarConfirmDialog by remember { mutableStateOf(false) }

            // Dialog state
            var editingField by remember { mutableStateOf<String?>(null) }
            var dialogValue by remember { mutableStateOf("") }

            // Password re-auth dialog state
            var showPasswordVerifyDialog by remember { mutableStateOf(false) }
            var pendingPhone by remember { mutableStateOf("") }

            LaunchedEffect(updateStatus) {
                updateStatus?.let { success ->
                    showToast(if (success) "Updated successfully!" else "Update failed. Please try again.")
                    if (success && pendingPhone.isNotEmpty()) {
                        phone = pendingPhone
                        pendingPhone = ""
                    }
                    personalInformationViewModel.resetUpdateStatus()
                }
            }

            LaunchedEffect(reAuthStatus) {
                reAuthStatus?.let { success ->
                    if (!success) {
                        showToast("Incorrect password. Phone number was not updated.")
                    }
                    personalInformationViewModel.resetReAuthStatus()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isTablet = maxWidth > 600.dp
                    val contentWidth = if (isTablet) 560.dp else maxWidth

                    Column(
                        modifier = Modifier
                            .width(contentWidth)
                            .align(Alignment.TopCenter)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Top bar
                        UiUtils.BackAndTitleAndMoreOptionsRow(
                            title = "Personal Information",
                            isMember = false,
                            navigateBack = { onNavigateBack() }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val localImageLoaderValue = LocalImageLoader provides remember { generateImageLoader() }

                            // Avatar with camera overlay
                            Box(contentAlignment = Alignment.BottomEnd) {
                                if (pickedAvatarUri != null) {
                                    // Show local preview of the newly picked image
                                    val imageBytes = produceState<ByteArray?>(initialValue = null, pickedAvatarUri) {
                                        value = imagePicker.loadImageBytes(pickedAvatarUri)
                                    }
                                    imagePicker.ByteArrayImage(
                                        imageBytes.value,
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                } else if (uploadedAvatarUri != null) {
                                    // Show the just-uploaded image from local URI (no refetch needed)
                                    val imageBytes = produceState<ByteArray?>(initialValue = null, uploadedAvatarUri) {
                                        value = imagePicker.loadImageBytes(uploadedAvatarUri!!)
                                    }
                                    imagePicker.ByteArrayImage(
                                        imageBytes.value,
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                } else {
                                    val avatarUrl = user.image.toStorageUrl()
                                    if (avatarUrl.isNotBlank()) {
                                        CompositionLocalProvider(localImageLoaderValue) {
                                            AutoSizeImage(
                                                avatarUrl,
                                                contentDescription = "User avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(88.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(88.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                }

                                // Camera icon button overlay
                                IconButton(
                                    onClick = { imagePicker.pickImage() },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Change avatar",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Show "Save avatar" button when a new image has been picked
                            if (pickedAvatarUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showAvatarConfirmDialog = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Avatar")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = name.ifBlank { currentUser.name },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Info card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                PersonalInfoRow(
                                    icon = Icons.Default.Badge,
                                    label = "Display Name",
                                    value = name.ifBlank { "—" },
                                    onEdit = {
                                        dialogValue = name
                                        editingField = "name"
                                    }
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                PersonalInfoRow(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = user.email.ifBlank { "—" },
                                    onEdit = null  // email is read-only
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                PersonalInfoRow(
                                    icon = Icons.Default.Info,
                                    label = "Bio / Status",
                                    value = status.ifBlank { "—" },
                                    onEdit = {
                                        dialogValue = status
                                        editingField = "status"
                                    }
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                PersonalInfoRow(
                                    icon = Icons.Default.Phone,
                                    label = "Phone Number",
                                    value = phone.ifBlank { "—" },
                                    onEdit = {
                                        dialogValue = phone
                                        editingField = "phone"
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Loading overlay
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // SnackbarHost removed — using showToast instead
            }

        // Avatar confirmation dialog
        if (showAvatarConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarConfirmDialog = false },
                title = { Text("Update Profile Picture") },
                text = { Text("Are you sure you want to set this as your new profile picture?") },
                confirmButton = {
                    Button(onClick = {
                        showAvatarConfirmDialog = false
                        personalInformationViewModel.updateAvatar(currentUser.uid)
                    }) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAvatarConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

            // Password verification dialog -- shown before saving phone number
            if (showPasswordVerifyDialog) {
                PasswordVerifyDialog(
                    title = "Verify Your Identity",
                    message = "Enter your password to confirm changing your phone number.",
                    onConfirm = { password ->
                        showPasswordVerifyDialog = false
                        personalInformationViewModel.reAuthAndUpdatePhone(
                            currentUser.email,
                            password,
                            currentUser.uid,
                            pendingPhone
                        )
                    },
                    onDismiss = { showPasswordVerifyDialog = false }
                )
            }

            // Edit dialog
            editingField?.let { field ->
                val fieldLabel = when (field) {
                    "name" -> "Display Name"
                    "phone" -> "Phone Number"
                    else -> "Bio / Status"
                }
                val maxLen = if (field == "name") 50 else if (field == "phone") 20 else 150
                val focusManager = LocalFocusManager.current

                AlertDialog(
                    onDismissRequest = { editingField = null },
                    title = { Text("Edit $fieldLabel") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = dialogValue,
                                onValueChange = { if (it.length <= maxLen) dialogValue = it },
                                label = { Text(fieldLabel) },
                                placeholder = { Text("Enter your $fieldLabel") },
                                singleLine = field == "name" || field == "phone",
                                maxLines = if (field == "status") 4 else 1,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (field == "status") ImeAction.Default else ImeAction.Done,
                                    keyboardType = if (field == "phone") KeyboardType.Phone else KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = if (dialogValue.isNotEmpty()) ({
                                    IconButton(onClick = { dialogValue = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }) else null
                            )
                            Text(
                                text = "${dialogValue.length} / $maxLen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmed = dialogValue.trim()
                                if (trimmed.isNotEmpty()) {
                                    when (field) {
                                        "name" -> {
                                            name = trimmed
                                            personalInformationViewModel.updateName(currentUser.uid, trimmed)
                                            editingField = null
                                        }
                                        "phone" -> {
                                            // Don't save yet — open password verification first
                                            pendingPhone = trimmed
                                            editingField = null
                                            showPasswordVerifyDialog = true
                                        }
                                        else -> {
                                            status = trimmed
                                            personalInformationViewModel.updateStatus(currentUser.uid, trimmed)
                                            editingField = null
                                        }
                                    }
                                } else {
                                    editingField = null
                                }
                            },
                            enabled = dialogValue.trim().isNotEmpty()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingField = null }) { Text("Cancel") }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        @Composable
        private fun PersonalInfoRow(
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            label: String,
            value: String,
            onEdit: (() -> Unit)?
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit $label",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        fun getScreenName(): String {
            return "PersonalInformationScreen"
        }
    }
}
