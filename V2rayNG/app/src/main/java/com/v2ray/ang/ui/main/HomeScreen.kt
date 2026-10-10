package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.LocalDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * RSS.VPN landing screen: developer logo, status orb, selected server chip,
 * headline and a slide-to-connect control. The server list lives behind the chip.
 */
@Composable
fun HomeScreen(
    isRunning: Boolean,
    serverName: String,
    statusText: String,
    onToggle: () -> Unit,
    onTestConnection: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            HomeHeader()

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val orbSize = minOf(maxWidth * 0.6f, maxHeight * 0.9f)
                StatusOrb(isRunning = isRunning, modifier = Modifier.size(orbSize))
            }

            ServerChip(serverName = serverName, isRunning = isRunning, onClick = onOpenServers)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(
                    if (isRunning) R.string.home_headline_on else R.string.home_headline_off
                ),
                fontSize = 48.sp,
                lineHeight = 54.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onTestConnection)
                    .padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SlideToConnect(
                    isRunning = isRunning,
                    onToggle = onToggle,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(TRACK_HEIGHT)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .clickable(role = Role.Button, onClick = onOpenMenu),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_24dp),
                        contentDescription = stringResource(R.string.title_settings),
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

private val TRACK_HEIGHT = 64.dp
private val THUMB_SIZE = 56.dp
private val THUMB_INSET = 4.dp

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Developer logo (res/drawable-nodpi/developer_logo.png). Its artwork has a dark
        // background, so it is shown as a rounded badge that works on both themes.
        Image(
            painter = painterResource(R.drawable.developer_logo),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun StatusOrb(isRunning: Boolean, modifier: Modifier = Modifier) {
    val isDark = LocalDarkTheme.current
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    val topEnd by animateColorAsState(
        targetValue = if (isRunning) Color(0xFF9EE0C2) else Color(0xFFB3BDE0),
        animationSpec = tween(900),
        label = "orbTopEnd"
    )
    val bottomStart by animateColorAsState(
        targetValue = if (isRunning) Color(0xFF8A63D2) else Color(0xFFF9AE62),
        animationSpec = tween(900),
        label = "orbBottomStart"
    )
    val center = if (isDark) Color(0xFF9C8794) else Color(0xFFEFD6C4)

    Canvas(
        modifier = modifier.graphicsLayer {
            val scale = if (isRunning) pulse.value else 1f
            scaleX = scale
            scaleY = scale
        }
    ) {
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(topEnd, center, bottomStart),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
            )
        )
    }
}

@Composable
private fun ServerChip(serverName: String, isRunning: Boolean, onClick: () -> Unit) {
    val name = serverName.ifBlank { stringResource(R.string.home_no_server) }
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(shape)
            .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.onBackground), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    if (isRunning) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_expand_more_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SlideToConnect(
    isRunning: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(if (isRunning) 1f else 0f) }
    var trackWidthPx by remember { mutableIntStateOf(0) }
    var awaiting by remember { mutableStateOf(false) }
    val latestRunning by rememberUpdatedState(isRunning)

    val thumbPx = with(density) { THUMB_SIZE.toPx() }
    val insetPx = with(density) { THUMB_INSET.toPx() }
    val maxOffset = (trackWidthPx - thumbPx - 2 * insetPx).coerceAtLeast(1f)

    // The service state is the source of truth: settle the thumb whenever it changes.
    LaunchedEffect(isRunning) {
        awaiting = false
        progress.animateTo(if (isRunning) 1f else 0f, tween(300))
    }
    // If the service never reports back (e.g. VPN permission refused), return the thumb.
    LaunchedEffect(awaiting) {
        if (awaiting) {
            delay(10_000)
            awaiting = false
            progress.animateTo(if (latestRunning) 1f else 0f, tween(300))
        }
    }

    val label = stringResource(
        if (isRunning) R.string.home_slide_to_disconnect else R.string.home_slide_to_connect
    )
    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            progress.snapTo((progress.value + delta / maxOffset).coerceIn(0f, 1f))
        }
    }

    // The thumb math is absolute, so keep the slider left-to-right in RTL locales too.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(TRACK_HEIGHT)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .onSizeChanged { trackWidthPx = it.width }
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = label
                    onClick(label = label) {
                        onToggle()
                        true
                    }
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    enabled = !awaiting,
                    onDragStopped = {
                        val triggered = if (latestRunning) progress.value < 0.2f
                        else progress.value > 0.8f
                        if (triggered) {
                            awaiting = true
                            progress.animateTo(if (latestRunning) 0f else 1f, tween(200))
                            onToggle()
                        } else {
                            progress.animateTo(if (latestRunning) 1f else 0f, tween(200))
                        }
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val p = progress.value
                        alpha = if (latestRunning) p else 1f - p
                    }
            )
            Box(
                modifier = Modifier
                    .padding(THUMB_INSET)
                    .offset { IntOffset((progress.value * maxOffset).roundToInt(), 0) }
                    .size(THUMB_SIZE)
                    .shadow(6.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_arrow_back_24dp
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = if (isRunning) 0f else 180f }
                )
            }
        }
    }
}
