package com.minhtu.firesocialmedia.presentation.incomingcall

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.sharedmodule.ui.theme.callAcceptColor
import com.minhtu.sharedmodule.ui.theme.callAcceptOnColor
import com.seiko.imageloader.ui.AutoSizeImage

@Composable
fun IncomingCallScreen(
    callerName: String,
    callerAvatar: String,
    localImageLoaderValue: ProvidedValue<*>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "incoming_call")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp
        val isLandscape = maxWidth > maxHeight
        val avatarSize = when { isTablet -> 180.dp; isLandscape -> 120.dp; else -> 145.dp }
        val pulseSize = avatarSize + 36.dp
        val horizontalPadding = when { isTablet -> 64.dp; else -> 24.dp }
        val verticalPadding = when { isLandscape -> 20.dp; else -> 40.dp }
        val buttonSize = when { isTablet -> 90.dp; else -> 76.dp }
        val spacing = when { isLandscape -> 16.dp; else -> 28.dp }

        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.background
            ))
        )) {
            Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-120).dp).size(320.dp)
                .background(brush = Brush.radialGradient(colors = listOf(callAcceptColor.copy(alpha = 0.18f), Color.Transparent)), shape = CircleShape))

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    CallerSection(callerName, callerAvatar, localImageLoaderValue, avatarSize, pulseSize, pulseScale, pulseAlpha)
                    ActionButtonsSection(buttonSize, spacing, onAccept, onReject)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), shape = RoundedCornerShape(50)) {
                        Text(text = "Incoming Call", modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.weight(0.12f))
                    CallerSection(callerName, callerAvatar, localImageLoaderValue, avatarSize, pulseSize, pulseScale, pulseAlpha)
                    Spacer(modifier = Modifier.weight(0.18f))
                    ActionButtonsSection(buttonSize, spacing, onAccept, onReject)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CallerSection(
    callerName: String, callerAvatar: String, localImageLoaderValue: ProvidedValue<*>,
    avatarSize: Dp, pulseSize: Dp, pulseScale: Float, pulseAlpha: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(pulseSize).scale(pulseScale).clip(CircleShape).background(callAcceptColor.copy(alpha = pulseAlpha)))
            Box(modifier = Modifier.size(pulseSize - 14.dp).clip(CircleShape)
                .border(width = 1.5.dp, color = callAcceptColor.copy(alpha = 0.25f), shape = CircleShape))
            CompositionLocalProvider(localImageLoaderValue) {
                Surface(modifier = Modifier.size(avatarSize), shape = CircleShape, tonalElevation = 10.dp, shadowElevation = 16.dp,
                    border = BorderStroke(3.dp, Brush.linearGradient(listOf(callAcceptColor, callAcceptColor.copy(alpha = 0.45f))))) {
                    AutoSizeImage(url = callerAvatar, contentDescription = "Caller avatar", contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = callerName.ifBlank { "Unknown Caller" },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 320.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "is calling you...", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ActionButtonsSection(buttonSize: Dp, spacing: Dp, onAccept: () -> Unit, onReject: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        CallActionButton("Decline", Icons.Default.CallEnd, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, buttonSize, onReject)
        CallActionButton("Accept", Icons.Default.Call, callAcceptColor, callAcceptOnColor, buttonSize, onAccept)
    }
}

@Composable
private fun CallActionButton(label: String, icon: ImageVector, containerColor: Color, contentColor: Color, buttonSize: Dp, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LargeFloatingActionButton(onClick = onClick, modifier = Modifier.size(buttonSize), shape = CircleShape,
            containerColor = containerColor, contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 14.dp, hoveredElevation = 10.dp)) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(buttonSize * 0.38f))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = containerColor, fontWeight = FontWeight.SemiBold)
    }
}

