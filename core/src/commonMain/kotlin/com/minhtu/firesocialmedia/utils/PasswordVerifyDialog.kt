package com.minhtu.firesocialmedia.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Reusable password verification dialog.
 *
 * @param title      Dialog title, e.g. "Verify Your Identity"
 * @param message    Explanatory text shown below the title.
 * @param onConfirm  Called with the entered password when the user taps Confirm.
 * @param onDismiss  Called when the dialog is cancelled.
 */
@Composable
fun PasswordVerifyDialog(
    title: String = "Verify Your Identity",
    message: String = "Enter your password to confirm this action.",
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password"
                                                     else "Show password"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pwd = passwordInput
                    passwordInput = ""
                    passwordVisible = false
                    focusManager.clearFocus()
                    onConfirm(pwd)
                },
                enabled = passwordInput.isNotEmpty()
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = {
                passwordInput = ""
                passwordVisible = false
                onDismiss()
            }) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

