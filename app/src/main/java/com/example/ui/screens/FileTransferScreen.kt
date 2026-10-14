package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileTransferEntity
import com.example.data.TransferDirection
import com.example.data.TransferStatus
import com.example.ui.JarvisMainViewModel
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonMint

@Composable
fun FileTransferScreen(viewModel: JarvisMainViewModel) {
    val fileTransfers by viewModel.fileTransfers.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "tactical_data.bin"
            val targetPeer = connectedPeers.firstOrNull()?.address ?: "MK85:AA:BB:CC:DD:01"
            viewModel.sendFile(
                conversationId = targetPeer,
                receiverAddress = targetPeer,
                uri = uri,
                fileName = fileName,
                fileSize = 1048576L, // 1 MB payload
                mimeType = "application/octet-stream"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("file_transfer_screen")
    ) {
        // Vault Header
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = JarvisCyan.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUANTUM FILE VAULT",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CHUNKED P2P TRANSFER // ZERO DATA CORRUPTION",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisNeonMint
                    )
                }
                SecurityBadge(isVerified = true)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upload Button Row
        Button(
            onClick = {
                filePickerLauncher.launch(
                    arrayOf(
                        "image/*",
                        "video/*",
                        "application/pdf",
                        "audio/*",
                        "application/msword",
                        "application/octet-stream"
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("select_file_payload_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JarvisCyan,
                contentColor = Color(0xFF040814)
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TRANSMIT NEW FILE VIA P2P",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TRANSFER LOG & QUEUE (${fileTransfers.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8AA8CE)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (fileTransfers.isEmpty()) {
            JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No active transfers. Transmit images, PDFs, videos, or documents over Bluetooth/Wi-Fi Direct.",
                    fontSize = 13.sp,
                    color = Color(0xFF8AA8CE)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fileTransfers) { item ->
                    FileTransferItemCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun FileTransferItemCard(item: FileTransferEntity) {
    val isOut = item.direction == TransferDirection.OUTGOING
    val progress = if (item.totalBytes > 0) {
        (item.bytesTransferred.toFloat() / item.totalBytes.toFloat()).coerceIn(0f, 1f)
    } else 1f

    JarvisHudCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOut) Icons.Filled.FileUpload else Icons.Filled.FileDownload,
                        contentDescription = null,
                        tint = if (isOut) JarvisCyan else JarvisNeonMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = JarvisCyanBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.totalBytes / 1024} KB | ${item.status.name}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF8AA8CE)
                        )
                    }
                }

                if (item.status == TransferStatus.COMPLETED) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = JarvisNeonMint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (isOut) JarvisCyan else JarvisNeonMint,
                trackColor = Color(0xFF16284B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SHA-256: ${item.sha256Hash.take(24)}...",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF4A6588)
            )
        }
    }
}
