package com.minhtu.firesocialmedia.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

class UiUtils {
    companion object{
//        @Composable
//        fun PasswordTextField(label : String) {
//            var password by rememberSaveable {
//                mutableStateOf("")
//            }
//            var passwordVisibility by rememberSaveable {
//                mutableStateOf(false)
//            }
//            OutlinedTextField(
//                value = password, onValueChange = {
//                    password = it
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//                    //Fix crash: java.lang.IllegalStateException: Already in the pool! when using visualTransformation
//                    .clearAndSetSemantics {  },
//                label = { Text(text = label) },
//                singleLine = true,
//                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                trailingIcon = {
//                    val icon = if(passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
//                    IconButton(onClick = {passwordVisibility = !passwordVisibility}) {
//                        Icon(imageVector = icon, descriptionOfIcon)
//                    }
//                }
//            )
//        }
    }
}