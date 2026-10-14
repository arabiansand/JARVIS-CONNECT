package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisMainViewModel
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink

@Composable
fun SecurityScreen(viewModel: JarvisMainViewModel) {
    val scrollState = rememberScrollState()
    val readAloud by viewModel.readAloudEnabled.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    var showWipeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("security_screen")
    ) {
        // Main Header
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = JarvisNeonMint.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURITY PROTOCOL CONSOLE",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ZERO-TRUST PEER MESH ARCHITECTURE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisNeonMint
                    )
                }
                SecurityBadge(isVerified = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Identity Fingerprint
        JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TERMINAL IDENTITY FINGERPRINT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewModel.deviceFingerprint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisNeonMint
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Derived via SHA-256 cryptographic hardware anchor. Shared exclusively during peer handshakes.",
                    fontSize = 10.sp,
                    color = Color(0xFF8AA8CE),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cryptographic Specifications Card
        JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE CIPHER SUITE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                CryptoSpecRow("ENCRYPTION ALGORITHM", "AES-256-GCM (Authenticated)")
                CryptoSpecRow("AUTHENTICATION TAG", "128-bit Poly1305/GCM Tag")
                CryptoSpecRow("INIT VECTOR (IV)", "96-bit SecureRandom per packet")
                CryptoSpecRow("MASTER KEY STORAGE", "Android Keystore (Hardware TEE)")
                CryptoSpecRow("FILE INTEGRITY", "SHA-256 Hash Verification")
                CryptoSpecRow("REPLAY DEFENSE", "Monotonic Seq & 300s window")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preferences & Toggles
        JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "INTERFACE & ASSISTANT SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyanBright
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Read Inbound Messages Aloud",
                            fontSize = 13.sp,
                            color = Color(0xFFE2F1FF)
                        )
                        Text(
                            text = "JARVIS synthesizes text-to-speech for incoming messages",
                            fontSize = 10.sp,
                            color = Color(0xFF8AA8CE)
                        )
                    }
                    Switch(
                        checked = readAloud,
                        onCheckedChange = { viewModel.voiceAssistant.setReadAloud(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF040814),
                            checkedTrackColor = JarvisCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode HUD Theme",
                            fontSize = 13.sp,
                            color = Color(0xFFE2F1FF)
                        )
                        Text(
                            text = "Switch between Dark Space HUD and Titanium Light",
                            fontSize = 10.sp,
                            color = Color(0xFF8AA8CE)
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF040814),
                            checkedTrackColor = JarvisCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy Policy Card
        JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "ZERO-CLOUD PRIVACY POLICY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisNeonMint
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "JARVIS CONNECT operates entirely offline through direct peer-to-peer RFCOMM Bluetooth and Wi-Fi Direct. No servers, telemetry loggers, or cloud services touch your messages, files, or voice records.",
                    fontSize = 11.sp,
                    color = Color(0xFF8AA8CE),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Factory Reset Data
        Button(
            onClick = { showWipeDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("wipe_data_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JarvisNeonPink,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("WIPE ENCRYPTED LOCAL STORE", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("CONFIRM TOTAL PURGE", color = JarvisNeonPink, fontFamily = FontFamily.Monospace) },
            text = { Text("This will permanently wipe all local database keys, contacts, conversations, and file logs.", color = Color(0xFFE2F1FF)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showWipeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNeonPink)
                ) {
                    Text("ERASE EVERYTHING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showWipeDialog = false }) {
                    Text("CANCEL", color = JarvisCyan)
                }
            },
            containerColor = Color(0xFF0F1E38)
        )
    }
}

@Composable
private fun CryptoSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8AA8CE)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = JarvisCyanBright
        )
    }
}
