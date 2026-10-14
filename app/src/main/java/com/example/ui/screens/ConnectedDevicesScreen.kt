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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.components.TransportBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink

@Composable
fun ConnectedDevicesScreen(viewModel: JarvisMainViewModel) {
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("connected_devices_screen")
    ) {
        // Telemetry Diagnostic Banner
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = JarvisNeonMint.copy(alpha = 0.4f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MESH TELEMETRY & DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisNeonMint
                    )
                    SecurityBadge(isVerified = true)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(title = "PING LATENCY", value = "${diagnostics.latencyMs} ms")
                    MetricItem(title = "LINK SPEED", value = "${diagnostics.linkSpeedMbps} Mbps")
                    MetricItem(title = "TX PAYLOAD", value = "${diagnostics.bytesSent} B")
                    MetricItem(title = "RX PAYLOAD", value = "${diagnostics.bytesReceived} B")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ACTIVE PHYSICAL LINKS (${connectedPeers.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = JarvisCyan
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (connectedPeers.isEmpty()) {
            JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No wireless peers connected.",
                        fontSize = 13.sp,
                        color = Color(0xFF8AA8CE)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Scanner) },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF040814))
                    ) {
                        Text("SCAN NEARBY NODES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(connectedPeers) { peer ->
                    JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(JarvisNeonMint)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = peer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = JarvisCyanBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TransportBadge(transport = peer.transport)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "NODE ID: ${peer.address}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8AA8CE)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.disconnectDevice(peer.address) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("disconnect_${peer.address}")
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = JarvisNeonPink, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DISCONNECT", fontSize = 10.sp, color = JarvisNeonPink, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.navigateTo(Screen.Chat(peer.address)) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JarvisCyan,
                                        contentColor = Color(0xFF040814)
                                    ),
                                    modifier = Modifier.testTag("chat_with_${peer.address}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("TRANSMIT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String) {
    Column {
        Text(
            text = title,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8AA8CE)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = JarvisCyanBright
        )
    }
}
