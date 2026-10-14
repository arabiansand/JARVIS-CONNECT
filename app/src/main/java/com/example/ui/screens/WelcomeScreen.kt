package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDarkBackground
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonMint

@Composable
fun WelcomeScreen(
    viewModel: JarvisMainViewModel,
    onRequestPermissions: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF040814),
                        JarvisDarkBackground,
                        Color(0xFF071224)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .testTag("welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Arc Reactor Visual
            ArcReactorCore(
                size = 140.dp,
                onClick = { viewModel.voiceAssistant.speakGreeting() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "JARVIS CONNECT",
                style = MaterialTheme.typography.displayLarge,
                color = JarvisCyanBright,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "NEXT-GEN SECURE MESH MESSENGER",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisElectricBlue,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting card
            JarvisHudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = JarvisCyan.copy(alpha = 0.5f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\"As salaam alaikum. JARVIS CONNECT is online. Ready for secure communication.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisCyan,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.voiceAssistant.speakGreeting() },
                            modifier = Modifier.testTag("listen_greeting_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play voice greeting",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Replay JARVIS Voice", fontSize = 12.sp, color = JarvisCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Bluetooth,
                    title = "OFFLINE MESH",
                    desc = "BT Classic RFCOMM & BLE"
                )
                FeaturePill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Wifi,
                    title = "WI-FI DIRECT",
                    desc = "High-speed peer P2P"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Security,
                    title = "E2EE ENCRYPTED",
                    desc = "AES-256-GCM Keystore"
                )
                FeaturePill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Mic,
                    title = "JARVIS VOICE",
                    desc = "Voice commands & TTS"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SecurityBadge(isVerified = true)

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Enter Action
            Button(
                onClick = {
                    onRequestPermissions()
                    viewModel.navigateTo(Screen.Dashboard)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("enter_jarvis_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = Color(0xFF040814)
                )
            ) {
                Text(
                    text = "INITIALIZE JARVIS TERMINAL",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeaturePill(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    JarvisHudCard(modifier = modifier) {
        Column {
            Icon(
                icon,
                contentDescription = title,
                tint = JarvisCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisCyanBright,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF8AA8CE),
                lineHeight = 14.sp
            )
        }
    }
}
