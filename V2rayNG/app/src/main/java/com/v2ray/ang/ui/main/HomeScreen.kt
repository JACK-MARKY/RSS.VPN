package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
import kotlinx.coroutines.Job
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
    isConnecting: Boolean,
    serverName: String,
    statusText: String,
    onToggle: () -> Unit,
    onTestConnection: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSettings: () -> Unit,
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
                StatusOrb(
                    isRunning = isRunning,
                    isConnecting = isConnecting,
                    modifier = Modifier.size(orbSize)
                )
            }

            ServerChip(serverName = serverName, isRunning = isRunning, onClick = onOpenServers)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(
                    when {
                        isConnecting -> R.string.home_headline_connecting
                        isRunning -> R.string.home_headline_on
                        else -> R.string.home_headline_off
                    }
                ),
                fontSize = 48.sp,
                lineHeight = 54.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isConnecting) stringResource(R.string.home_connecting_status) else statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isConnecting, role = Role.Button, onClick = onTestConnection)
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
                    isConnecting = isConnecting,
                    onToggle = onToggle,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(TRACK_HEIGHT)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .clickable(role = Role.Button, onClick = onOpenSettings),
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

// Release thresholds as a fraction of the track: past 55% connects, below 45% disconnects.
private const val RELEASE_TO_CONNECT = 0.55f
private const val RELEASE_TO_DISCONNECT = 0.45f

// How long the knob keeps its new side while waiting for the service (VPN permission dialog etc.).
private const val EXPECT_TIMEOUT_MS = 6_000L

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
private fun StatusOrb(isRunning: Boolean, isConnecting: Boolean, modifier: Modifier = Modifier) {
    val isDark = LocalDarkTheme.current

    // The orb breathes (shrinks and grows) only while a connection is being set up.
    val pulse = if (isConnecting) {
        rememberInfiniteTransition(label = "orb").animateFloat(
            initialValue = 0.9f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbPulse"
        )
    } else {
        null
    }

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
            val scale = pulse?.value ?: 1f
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

/**
 * Slide the knob right to connect. While connected (or connecting) the knob rests on the
 * right; sliding it back to the left disconnects. The knob follows the finger exactly
 * (state is written straight from the pointer events, no per-event coroutines) and the
 * rest of the time it simply mirrors the real service state.
 */
@Composable
private fun SlideToConnect(
    isRunning: Boolean,
    isConnecting: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var trackWidthPx by remember { mutableIntStateOf(0) }
    var knobX by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    // Side the knob should rest on right after a completed slide, until the service agrees.
    var expected by remember { mutableStateOf<Boolean?>(null) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    val thumbPx = with(density) { THUMB_SIZE.toPx() }
    val insetPx = with(density) { THUMB_INSET.toPx() }
    val maxOffset = (trackWidthPx - thumbPx - 2 * insetPx).coerceAtLeast(0f)
    val engaged = isRunning || isConnecting
    val restEngaged = expected ?: engaged

    val latestMax by rememberUpdatedState(maxOffset)
    val latestRest by rememberUpdatedState(restEngaged)
    val latestLocked by rememberUpdatedState(isConnecting)
    val latestToggle by rememberUpdatedState(onToggle)

    fun settleTo(target: Float) {
        settleJob?.cancel()
        settleJob = scope.launch {
            animate(knobX, target, animationSpec = tween(250)) { value, _ -> knobX = value }
        }
    }

    // First measurement: jump straight to the resting side (no slide-in when reopening connected).
    LaunchedEffect(maxOffset) {
        if (!dragging) knobX = if (restEngaged) maxOffset else 0f
    }
    // Follow the real service state; ignored while the user is holding the knob.
    LaunchedEffect(restEngaged) {
        if (!dragging) settleTo(if (restEngaged) latestMax else 0f)
    }
    // Drop the expectation once the service agrees, or give up after a while.
    LaunchedEffect(expected, engaged) {
        val wanted = expected ?: return@LaunchedEffect
        if (wanted == engaged) {
            expected = null
        } else {
            delay(EXPECT_TIMEOUT_MS)
            expected = null
        }
    }

    val label = stringResource(
        when {
            isConnecting -> R.string.home_headline_connecting
            isRunning -> R.string.home_slide_to_disconnect
            else -> R.string.home_slide_to_connect
        }
    )

    // The knob maths is absolute, so keep the track left-to-right in RTL locales too.
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
                        if (!latestLocked) latestToggle()
                        true
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (restEngaged) 0.dp else THUMB_SIZE,
                        end = if (restEngaged) THUMB_SIZE else 0.dp
                    )
                    .graphicsLayer {
                        val fraction = if (latestMax > 0f) (knobX / latestMax).coerceIn(0f, 1f) else 0f
                        alpha = when {
                            latestLocked -> 1f
                            latestRest -> fraction
                            else -> 1f - fraction
                        }
                    }
            )
            Box(
                modifier = Modifier
                    .padding(THUMB_INSET)
                    .offset { IntOffset(knobX.roundToInt(), 0) }
                    .size(THUMB_SIZE)
                    .shadow(6.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val max = latestMax
                            // Ignore touches while a start is in flight, or before the track is measured.
                            if (latestLocked || max <= 0f) return@awaitEachGesture

                            settleJob?.cancel()
                            dragging = true
                            val startedEngaged = latestRest
                            var x = knobX
                            horizontalDrag(down.id) { change ->
                                x = (x + change.positionChange().x).coerceIn(0f, max)
                                knobX = x
                                change.consume()
                            }
                            dragging = false

                            val progress = x / max
                            val fire = if (startedEngaged) progress <= RELEASE_TO_DISCONNECT
                            else progress >= RELEASE_TO_CONNECT
                            if (fire) {
                                val nowEngaged = !startedEngaged
                                expected = nowEngaged
                                settleTo(if (nowEngaged) max else 0f)
                                latestToggle()
                            } else {
                                settleTo(if (startedEngaged) max else 0f)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.background
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            if (restEngaged) R.drawable.ic_stop_24dp else R.drawable.ic_arrow_back_24dp
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = if (restEngaged) 0f else 180f }
                    )
                }
            }
        }
    }
}
