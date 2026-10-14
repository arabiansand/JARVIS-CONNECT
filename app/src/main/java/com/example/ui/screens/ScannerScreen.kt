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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transport.BluetoothPermissionHelper
import com.example.transport.DiscoveredDevice
import com.example.transport.rememberBluetoothPermissionLauncher
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.RadarScannerView
import com.example.ui.components.TransportBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink

@Composable
fun ScannerScreen(viewModel: JarvisMainViewModel) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    val permissionLauncher = rememberBluetoothPermissionLauncher(
        context = context,
        onAllGranted = {
            viewModel.startScan()
        },
        onDenied = { missing ->
            viewModel.showHudNotification("Bluetooth Scan permission required: ${missing.joinToString { it.substringAfterLast('.') }}")
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("scanner_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Holographic Radar Scanner View
        RadarScannerView(
            isScanning = isScanning,
            detectedCount = discoveredDevices.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Control Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isScanning) "FREQUENCY SWEEP ACTIVE" else "RADAR STANDBY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isScanning) JarvisCyan else Color(0xFF8AA8CE)
                )
                Text(
                    text = "${discoveredDevices.size} NODES IN RANGE",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyanBright
                )
            }

            Button(
                onClick = {
                    if (isScanning) {
                        viewModel.stopScan()
                    } else {
                        if (permissionLauncher.isGranted) {
                            viewModel.startScan()
                        } else {
                            permissionLauncher.launch()
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) JarvisNeonPink else JarvisCyan,
                    contentColor = Color(0xFF040814)
                ),
                modifier = Modifier.testTag("toggle_scan_button")
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Filled.Stop else Icons.Filled.Radar,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isScanning) "STOP SWEEP" else "INITIATE SCAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Discovered List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(discoveredDevices) { device ->
                DiscoveredDeviceCard(
                    device = device,
                    onConnect = {
                        viewModel.connectDevice(device)
                    },
                    onOpenChat = {
                        viewModel.navigateTo(Screen.Chat(device.address))
                    }
                )
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredDevice,
    onConnect: () -> Unit,
    onOpenChat: () -> Unit
) {
    JarvisHudCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.address}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    if (device.isConnected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(JarvisNeonMint)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MAC: ${device.address}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF8AA8CE)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransportBadge(transport = device.transport)
                    Text(
                        text = "RSSI: ${device.rssi} dBm",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4A6588)
                    )
                }
            }

            if (device.isConnected) {
                Button(
                    onClick = onOpenChat,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisNeonMint,
                        contentColor = Color(0xFF040814)
                    ),
                    modifier = Modifier.testTag("open_chat_${device.address}")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SECURE CHAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onConnect,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("connect_btn_${device.address}")
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PAIR LINK", fontSize = 11.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
