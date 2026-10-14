package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransportType
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDarkCard
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Iron Man Arc Reactor Animated Core
 * Pulsing concentric energy rings, rotating segmented HUD arcs, and cyan core.
 */
@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isActive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactor")

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotateOuter"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotateInner"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .testTag("arc_reactor_core"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            // Outer glow pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyanBright.copy(alpha = 0.35f * pulseGlow),
                        JarvisCyan.copy(alpha = 0.12f * pulseGlow),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer ring
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.4f),
                radius = radius * 0.92f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Segmented arc ring (Outer)
            val outerArcRadius = radius * 0.82f
            for (i in 0 until 12) {
                val startAngle = rotationOuter + (i * 30f)
                drawArc(
                    color = if (i % 2 == 0) JarvisCyan else JarvisElectricBlue,
                    startAngle = startAngle,
                    sweepAngle = 18f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerArcRadius, center.y - outerArcRadius),
                    size = androidx.compose.ui.geometry.Size(outerArcRadius * 2, outerArcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner counter-rotating segments
            val innerArcRadius = radius * 0.58f
            for (i in 0 until 8) {
                val startAngle = rotationInner + (i * 45f)
                drawArc(
                    color = JarvisCyanBright.copy(alpha = 0.8f),
                    startAngle = startAngle,
                    sweepAngle = 24f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerArcRadius, center.y - innerArcRadius),
                    size = androidx.compose.ui.geometry.Size(innerArcRadius * 2, innerArcRadius * 2),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Center glowing core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanBright,
                        JarvisCyan.copy(alpha = 0.8f)
                    ),
                    center = center,
                    radius = radius * 0.28f
                ),
                radius = radius * 0.28f * pulseGlow,
                center = center
            )
        }
    }
}

/**
 * Holographic Radar Scanner View
 * Rotating sweep angle, concentric distance rings, and detected peer blips
 */
@Composable
fun RadarScannerView(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    detectedCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sweep"
    )

    Box(
        modifier = modifier
            .testTag("radar_scanner_view")
            .size(240.dp)
            .clip(CircleShape)
            .border(1.5.dp, JarvisCyan.copy(alpha = 0.6f), CircleShape)
            .background(Color(0xFF040A16)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Concentric range rings
            val ringFractions = listOf(0.25f, 0.5f, 0.75f, 0.95f)
            ringFractions.forEach { fraction ->
                drawCircle(
                    color = JarvisCyan.copy(alpha = 0.25f),
                    radius = radius * fraction,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Crosshairs
            drawLine(
                color = JarvisCyan.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = JarvisCyan.copy(alpha = 0.2f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Radar sweep sector
            if (isScanning) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            JarvisCyan.copy(alpha = 0.05f),
                            JarvisCyan.copy(alpha = 0.35f)
                        ),
                        center = center
                    ),
                    startAngle = sweepAngle - 60f,
                    sweepAngle = 60f,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = size
                )
            }

            // Simulated detected blips
            val blipAngles = listOf(45.0, 130.0, 260.0)
            val blipDistances = listOf(0.4f, 0.65f, 0.8f)
            for (i in 0 until detectedCount.coerceAtMost(blipAngles.size)) {
                val rad = (blipAngles[i] * PI / 180.0)
                val dist = radius * blipDistances[i]
                val bx = center.x + (dist * cos(rad)).toFloat()
                val by = center.y + (dist * sin(rad)).toFloat()

                drawCircle(
                    color = JarvisNeonMint,
                    radius = 4.dp.toPx(),
                    center = Offset(bx, by)
                )
                drawCircle(
                    color = JarvisNeonMint.copy(alpha = 0.35f),
                    radius = 9.dp.toPx(),
                    center = Offset(bx, by),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Center HUD telemetry dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(JarvisCyanBright)
        )
    }
}

/**
 * Futuristic JARVIS HUD Card with subtle glowing borders and corner highlights
 */
@Composable
fun JarvisHudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = JarvisCyan.copy(alpha = 0.35f),
    backgroundColor: Color = JarvisDarkCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(borderColor, borderColor.copy(alpha = 0.15f), borderColor)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

/**
 * End-to-End Encryption Security Badge
 */
@Composable
fun SecurityBadge(
    modifier: Modifier = Modifier,
    isVerified: Boolean = true,
    cipherName: String = "AES-256-GCM"
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isVerified) Color(0x2200E5FF) else Color(0x22FF2A6D))
            .border(
                1.dp,
                if (isVerified) JarvisCyan.copy(alpha = 0.5f) else JarvisNeonPink.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Filled.Security else Icons.Outlined.Lock,
            contentDescription = "Security Status",
            tint = if (isVerified) JarvisCyan else JarvisNeonPink,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = if (isVerified) "E2EE $cipherName VERIFIED" else "UNVERIFIED CHANNEL",
            color = if (isVerified) JarvisCyanBright else JarvisNeonPink,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

/**
 * Transport Type Badge Pill
 */
@Composable
fun TransportBadge(
    transport: TransportType,
    modifier: Modifier = Modifier
) {
    val (label, icon, color) = when (transport) {
        TransportType.BLUETOOTH_CLASSIC -> Triple("BT CLASSIC", Icons.Filled.Bluetooth, JarvisCyan)
        TransportType.BLUETOOTH_LE -> Triple("BLE GATT", Icons.Filled.Bluetooth, JarvisElectricBlue)
        TransportType.WIFI_DIRECT -> Triple("WI-FI DIRECT", Icons.Filled.Wifi, JarvisNeonMint)
        TransportType.SIMULATED -> Triple("JARVIS MESH", Icons.Filled.Security, JarvisCyanBright)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

/**
 * Waveform Audio Amplitude Visualizer
 */
@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    amplitude: Int = 12000,
    barCount: Int = 20,
    isPlayingOrRecording: Boolean = true
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val normalized = (amplitude.toFloat() / 32767f).coerceIn(0.1f, 1f)
        for (i in 0 until barCount) {
            val factor = if (isPlayingOrRecording) {
                val wave = sin((i * 0.4f) + (normalized * 6f))
                (0.2f + (0.8f * normalized * ((wave + 1f) / 2f))).coerceIn(0.15f, 1f)
            } else {
                0.2f
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((36.dp * factor).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(JarvisCyanBright, JarvisElectricBlue)
                        )
                    )
            )
        }
    }
}
