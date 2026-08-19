package com.minhtu.firesocialmedia.utils.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.toHex

class UiUtils {
    companion object{
        @Composable
        fun PasswordVisibilityIcon(passwordVisibility : Boolean,
                                   tint : Color,
                                   backgroundColor : String) {
            val icon = if(passwordVisibility) "visibility" else "visibility_off"
            val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
            CrossPlatformIcon(
                icon = icon,
                backgroundColor = backgroundColor,
                tint = tint,
                contentDescription = descriptionOfIcon,
                modifier = Modifier.Companion
                    .size(30.dp)
                    .padding(4.dp)
            )
        }

        @Composable
        fun IconAndTitle(hasIcon : Boolean = true,
                         hasTitle : Boolean = true,
                         icon : String = "",
                         title : String = "",
                         titleColor : Color = Color.Companion.Unspecified,
                         modifier: Modifier = Modifier.Companion
        ) {
            val resolvedTitleColor = if (titleColor == Color.Companion.Unspecified) MaterialTheme.colorScheme.primary else titleColor
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = modifier
            ) {
                if (hasIcon) {
                    CrossPlatformIcon(
                        icon = "fire_chat_icon",
                        backgroundColor = MaterialTheme.colorScheme.background.toHex(),
                        modifier = Modifier.Companion
                            .size(30.dp)
                    )
                }
                if (hasTitle) {
                    Text(
                        text = title,
                        color = resolvedTitleColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Companion.Bold
                    )
                }
            }
        }

        @Composable
        fun SubTitle(
            subTitle : String,
            modifier: Modifier = Modifier.Companion
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = modifier
            ) {
                Text(
                    text = subTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth()
                )
            }
        }

        @Composable
        fun TextFieldWithLeadingIcon(
            value : String,
            onValueChange : (String) -> Unit,
            label : String,
            testTag : String
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag(testTag)
                    .semantics {
                        contentDescription = testTag
                    },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        label
                    )
                },
                label = { Text(text = label) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = TextStyle(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}