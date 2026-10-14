package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.ConversationEntity
import com.example.data.TransportType
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.components.TransportBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonMint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: JarvisMainViewModel) {
    val conversations by viewModel.conversations.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val activeTransport by viewModel.activeTransport.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Arc Reactor HUD status section
                JarvisHudCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = JarvisCyan.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (connectedPeers.isNotEmpty()) JarvisNeonMint else JarvisCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectedPeers.isNotEmpty()) "MESH ONLINE (${connectedPeers.size} PEERS)" else "STANDBY // READY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (connectedPeers.isNotEmpty()) JarvisNeonMint else JarvisCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CORE TELEMETRY",
                                style = MaterialTheme.typography.titleLarge,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Lat: ${diagnostics.latencyMs}ms | Bandwidth: ${diagnostics.linkSpeedMbps} Mbps",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8AA8CE)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SecurityBadge(isVerified = true)
                        }

                        ArcReactorCore(
                            size = 85.dp,
                            onClick = { viewModel.openVoiceAssistant() }
                        )
                    }
                }
            }

            // Transport Selector Bar
            item {
                Column {
                    Text(
                        text = "COMMUNICATION TRANSPORT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransportOptionPill(
                            modifier = Modifier.weight(1f),
                            title = "BT CLASSIC",
                            subtitle = "RFCOMM",
                            isSelected = activeTransport == TransportType.BLUETOOTH_CLASSIC,
                            onClick = { viewModel.switchTransport(TransportType.BLUETOOTH_CLASSIC) }
                        )
                        TransportOptionPill(
                            modifier = Modifier.weight(1f),
                            title = "BLE GATT",
                            subtitle = "Beacon/Low",
                            isSelected = activeTransport == TransportType.BLUETOOTH_LE,
                            onClick = { viewModel.switchTransport(TransportType.BLUETOOTH_LE) }
                        )
                        TransportOptionPill(
                            modifier = Modifier.weight(1f),
                            title = "WI-FI DIRECT",
                            subtitle = "High Speed",
                            isSelected = activeTransport == TransportType.WIFI_DIRECT,
                            onClick = { viewModel.switchTransport(TransportType.WIFI_DIRECT) }
                        )
                    }
                }
            }

            // Quick Actions Hub
            item {
                Column {
                    Text(
                        text = "TACTICAL MODULES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "RADAR SCAN",
                            desc = "Find nearby nodes",
                            icon = Icons.Filled.Radar,
                            tag = "quick_radar_scan",
                            onClick = {
                                viewModel.navigateTo(Screen.Scanner)
                                viewModel.startScan()
                            }
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "GROUP MESH",
                            desc = "Avengers Grid",
                            icon = Icons.Filled.Group,
                            tag = "quick_group_mesh",
                            onClick = { viewModel.navigateTo(Screen.GroupChat) }
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "FILE VAULT",
                            desc = "Chunked transfer",
                            icon = Icons.Filled.Share,
                            tag = "quick_file_vault",
                            onClick = { viewModel.navigateTo(Screen.FileTransfer) }
                        )
                    }
                }
            }

            // Recent Conversations Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURE CONVERSATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyan
                    )
                    Text(
                        text = "${conversations.size} ENCRYPTED CHANNELS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                }
            }

            // Conversations List
            if (conversations.isEmpty()) {
                item {
                    JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No active conversations. Use RADAR SCAN to discover nearby nodes.",
                            fontSize = 13.sp,
                            color = Color(0xFF8AA8CE)
                        )
                    }
                }
            } else {
                items(conversations) { conv ->
                    ConversationItemCard(
                        conversation = conv,
                        onClick = {
                            viewModel.markConversationRead(conv.id)
                            if (conv.isGroup) {
                                viewModel.navigateTo(Screen.GroupChat)
                            } else {
                                viewModel.navigateTo(Screen.Chat(conv.id))
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button for Voice Assistant
        FloatingActionButton(
            onClick = { viewModel.openVoiceAssistant() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("floating_voice_button"),
            containerColor = JarvisCyan,
            contentColor = Color(0xFF040814)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "JARVIS Voice Assistant")
        }
    }
}

@Composable
private fun TransportOptionPill(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    JarvisHudCard(
        modifier = modifier.clickable { onClick() },
        borderColor = if (isSelected) JarvisCyanBright else Color(0xFF1E355B),
        backgroundColor = if (isSelected) Color(0xFF122340) else Color(0xFF0A1324)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) JarvisCyanBright else Color(0xFF8AA8CE)
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isSelected) JarvisNeonMint else Color(0xFF4A6588)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    JarvisHudCard(
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() }
    ) {
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
                fontFamily = FontFamily.Monospace,
                color = JarvisCyanBright
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF8AA8CE),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun ConversationItemCard(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(conversation.lastMessageTimestamp))

    JarvisHudCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_card_${conversation.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar HUD circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10213D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (conversation.isGroup) Icons.Filled.Group else Icons.Filled.Bluetooth,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessageText,
                        fontSize = 12.sp,
                        color = Color(0xFF8AA8CE),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(JarvisCyan)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF040814)
                            )
                        }
                    }
                }
            }
        }
    }
}
