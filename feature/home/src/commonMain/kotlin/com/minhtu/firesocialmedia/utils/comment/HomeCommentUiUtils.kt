package com.minhtu.firesocialmedia.utils.comment

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

class HomeCommentUiUtils {
    companion object{
        @Composable
        fun ExpandableText(
            text: String,
            collapsedMaxLines: Int = 3
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflowing by remember { mutableStateOf(false) }

            Box(modifier = Modifier.Companion.padding(horizontal = 10.dp)) {
                Column {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                        overflow = TextOverflow.Companion.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            isOverflowing = textLayoutResult.hasVisualOverflow
                        },
                        modifier = Modifier.Companion.animateContentSize()
                    )

                    if (isOverflowing || isExpanded) {
                        Text(
                            text = if (isExpanded) "See less" else "See more",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.Companion
                                .padding(top = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }
            }
        }

        @Composable
        fun CustomEditText(
            text: String,
            onTextChange: (String) -> Unit,
            modifier: Modifier,
            placeholder: String,
            keyboardController : SoftwareKeyboardController? = null,
            focusRequester: FocusRequester? = null
        ) {
            Box(
                modifier = modifier
                    .height(45.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Companion.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    modifier = Modifier.Companion.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        maxLines = 4,
                        modifier = Modifier.Companion
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .let { baseMod ->
                                if (focusRequester != null) {
                                    baseMod.focusRequester(focusRequester)
                                } else {
                                    baseMod
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Companion.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}
