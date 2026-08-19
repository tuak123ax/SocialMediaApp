package com.minhtu.firesocialmedia.presentation.creategroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.group.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.toHex

@Composable
fun AccessPermissionButtonContent(currentAccessPermission: DecentralizationType) {
    CrossPlatformIcon(
        icon = when (currentAccessPermission) {
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
    Text(
        text = when (currentAccessPermission) {
            DecentralizationType.Public -> "Public"
            DecentralizationType.Private -> "Private"
            DecentralizationType.OnlyFriends -> "Only Friends"
        }, color = MaterialTheme.colorScheme.onSurface
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPermissionBottomSheet(
    title: String,
    currentAccess: DecentralizationType,
    onDismiss: () -> Unit,
    onSelected: (selectedAccess: DecentralizationType) -> Unit
) {
    var selectedAccess by remember { mutableStateOf(currentAccess) }
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = rememberModalBottomSheetState(),
    ) {
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
                Button(onClick = { onSelected(selectedAccess) }) {
                    Text("Select")
                }
            }
        }
    }
}

@Composable
fun AccessPermissionRow(
    currentAccess: DecentralizationType,
    onSelect: (DecentralizationType) -> Unit
) {
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
            tint = if (localAccessState == DecentralizationType.Public) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(25.dp)
                .testTag(TestTag.TAG_SELECT_PUBLIC)
                .semantics { contentDescription = TestTag.TAG_SELECT_PUBLIC }
        )
        Text(text = "Public")
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
            tint = if (localAccessState == DecentralizationType.Private) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(25.dp)
                .testTag(TestTag.TAG_SELECT_PRIVATE)
                .semantics { contentDescription = TestTag.TAG_SELECT_PRIVATE }
        )
        Text(text = "Private")
        RadioButton(
            selected = localAccessState == DecentralizationType.Private,
            onClick = {
                localAccessState = DecentralizationType.Private
                onSelect(localAccessState)
            }
        )
    }
}
