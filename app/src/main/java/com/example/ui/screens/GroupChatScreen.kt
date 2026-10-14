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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.ui.JarvisMainViewModel
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDarkCard
import com.example.ui.theme.JarvisNeonMint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GroupChatScreen(viewModel: JarvisMainViewModel) {
    val allMessages by viewModel.allMessages.collectAsState()
    val groupMessages = allMessages.filter { it.conversationId == "GROUP_AVENGERS_GRID" }

    var inputText by remember { mutableStateOf("") }

    val members = listOf(
        "MARK-85 HUD Uplink",
        "FRIDAY Tactical AI",
        "STARK-PAD Alpha",
        "JARVIS Command"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("group_chat_screen")
    ) {
        // Group Header Banner
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            borderColor = JarvisCyan.copy(alpha = 0.5f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10213D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Group, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Avengers Tactical Mesh",
                                style = MaterialTheme.typography.titleMedium,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "4 Nodes Active // Multicast Relay Protocol",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisNeonMint
                            )
                        }
                    }
                    SecurityBadge(isVerified = true)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Node tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { member ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF122340))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = member,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8AA8CE)
                            )
                        }
                    }
                }
            }
        }

        // Group Messages Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(groupMessages) { msg ->
                val isOutgoing = msg.senderAddress == "LOCAL_NODE"
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormatter.format(Date(msg.timestamp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isOutgoing) 12.dp else 2.dp,
                                    bottomEnd = if (isOutgoing) 2.dp else 12.dp
                                )
                            )
                            .background(if (isOutgoing) Color(0xFF123456) else JarvisDarkCard)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.senderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOutgoing) JarvisCyanBright else JarvisNeonMint
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = msg.content,
                                fontSize = 13.sp,
                                color = Color(0xFFE2F1FF),
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ack: 3/3 nodes",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisNeonMint
                                )
                                Text(
                                    text = formattedTime,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF8AA8CE)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("group_chat_input"),
                placeholder = {
                    Text("Broadcast to tactical mesh...", fontSize = 13.sp, color = Color(0xFF6B87A8))
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyanBright,
                    unfocusedBorderColor = Color(0xFF1E355B),
                    focusedTextColor = Color(0xFFE2F1FF),
                    unfocusedTextColor = Color(0xFFE2F1FF),
                    focusedContainerColor = Color(0xFF0A1324),
                    unfocusedContainerColor = Color(0xFF0A1324)
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(
                            conversationId = "GROUP_AVENGERS_GRID",
                            receiverAddress = "GROUP_AVENGERS_GRID",
                            text = inputText
                        )
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JarvisCyan)
                    .testTag("send_group_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Broadcast",
                    tint = Color(0xFF040814)
                )
            }
        }
    }
}
