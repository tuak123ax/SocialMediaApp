package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.showToast
import org.koin.compose.viewmodel.koinViewModel

class CreatePoll {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun CreatePollScreen(
            groupId : String,
            currentUser : UserInstance,
            createPollViewModel: CreatePollViewModel = koinViewModel(),
            onClose: () -> Unit
        ) {
            val question by createPollViewModel.question.collectAsState()
            val options by createPollViewModel.options.collectAsState()
            val allowMultipleAnswers by createPollViewModel.allowMultipleAnswers.collectAsState()
            val selectedDuration by createPollViewModel.selectedDuration.collectAsState()
            val durationExpanded by createPollViewModel.durationExpanded.collectAsState()

            val durationOptions = listOf("1 day", "3 days", "7 days", "14 days", "30 days")
            val canPost = question.isNotBlank() && options.count { it.isNotBlank() } >= 2

            val createPollState by createPollViewModel.createPollState.collectAsState()
            LaunchedEffect(createPollState) {
                if(createPollState != null) {
                    if(createPollState!!) {
                        showToast("Poll created successfully!")
                        onClose()
                        createPollViewModel.resetCreatePollData()
                    } else {
                        showToast("Failed to create poll. Please try again.")
                    }
                    createPollViewModel.resetCreatePollState()
                }
            }
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "Create Poll",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        actions = {
                            TextButton(
                                enabled = canPost,
                                onClick = {
                                    if(!currentUser.isDefault()) {
                                        createPollViewModel.createPoll(
                                            currentUser.uid,
                                            currentUser.name,
                                            currentUser.image,
                                            question.trim(),
                                            options.map { it.trim() }.filter { it.isNotBlank() },
                                            allowMultipleAnswers,
                                            selectedDuration,
                                            groupId
                                        )
                                    } else {
                                        showToast("Cannot create poll right now. Please try again later.")
                                    }
                                }
                            ) {
                                Text(
                                    text = "Post",
                                    fontWeight = FontWeight.Bold,
                                    color = if (canPost) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) { paddingValues ->

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        BasicTextField(
                            value = question,
                            onValueChange = { createPollViewModel.setQuestion(it) },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            decorationBox = { innerTextField ->
                                if (question.isBlank()) {
                                    Text(
                                        text = "Ask a question...",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Poll Options",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${options.size}/10",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    itemsIndexed(options) { index, option ->
                        PollOptionField(
                            value = option,
                            placeholder = "Option ${index + 1}",
                            showRemove = options.size > 2,
                            onValueChange = { newValue ->
                                createPollViewModel.setOption(index, newValue)
                            },
                            onRemove = {
                                createPollViewModel.removeOption(index)
                            }
                        )
                    }

                    item {
                        TextButton(
                            enabled = options.size < 10,
                            onClick = {
                                createPollViewModel.addOption()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Add option",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        PollSettingsCard(
                            allowMultipleAnswers = allowMultipleAnswers,
                            onAllowMultipleAnswersChange = { createPollViewModel.setAllowMultipleAnswers(it) },
                            selectedDuration = selectedDuration,
                            durationOptions = durationOptions,
                            durationExpanded = durationExpanded,
                            onDurationExpandedChange = { createPollViewModel.setDurationExpanded(it) },
                            onDurationSelected = {
                                createPollViewModel.setSelectedDuration(it)
                                createPollViewModel.setDurationExpanded(false)
                            }
                        )
                    }

                    item {
                        EngageCommunityCard()
                    }
                }
            }
        }

        fun getScreenName() = "CreatePollScreen"

        @Composable
        private fun PollOptionField(
            value: String,
            placeholder: String,
            showRemove: Boolean,
            onValueChange: (String) -> Unit,
            onRemove: () -> Unit
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(start = 20.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (value.isBlank()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (showRemove) {
                        IconButton(onClick = onRemove) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Remove option",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        @Composable
        private fun PollSettingsCard(
            allowMultipleAnswers: Boolean,
            onAllowMultipleAnswersChange: (Boolean) -> Unit,
            selectedDuration: String,
            durationOptions: List<String>,
            durationExpanded: Boolean,
            onDurationExpandedChange: (Boolean) -> Unit,
            onDurationSelected: (String) -> Unit
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Poll Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow multiple answers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Voters can select more than one choice",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = allowMultipleAnswers,
                            onCheckedChange = onAllowMultipleAnswersChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Poll duration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Length of time the poll stays open",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            FilledTonalButton(
                                onClick = {
                                    onDurationExpandedChange(true)
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = selectedDuration,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }

                            DropdownMenu(
                                expanded = durationExpanded,
                                onDismissRequest = {
                                    onDurationExpandedChange(false)
                                }
                            ) {
                                durationOptions.forEach { duration ->
                                    DropdownMenuItem(
                                        text = { Text(duration) },
                                        onClick = {
                                            onDurationSelected(duration)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        private fun EngageCommunityCard() {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(end = 56.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Engage your community",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Polls are a great way to gather feedback and spark discussions within your group. Keep it simple and direct for the best results.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError.copy(alpha = 0.16f),
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}