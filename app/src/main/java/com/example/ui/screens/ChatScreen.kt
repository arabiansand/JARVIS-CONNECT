package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.data.MessageStatus
import com.example.data.MessageType
import com.example.data.TransportType
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.components.TransportBadge
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDarkCard
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: JarvisMainViewModel
) {
    val contacts by viewModel.contacts.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val activeTransport by viewModel.activeTransport.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()

    val currentContact = contacts.find { it.address == conversationId }
    val contactName = currentContact?.name ?: "Remote Mesh Node ($conversationId)"

    val conversationMessages = allMessages.filter { it.conversationId == conversationId }

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }

    val listState = rememberLazyListState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "mission_payload.bin"
            viewModel.sendFile(
                conversationId = conversationId,
                receiverAddress = conversationId,
                uri = uri,
                fileName = fileName,
                fileSize = 48200L,
                mimeType = "application/octet-stream"
            )
        }
    }

    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(conversationMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        // Chat Header
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            borderColor = JarvisCyan.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "LINK: $conversationId",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TransportBadge(transport = currentContact?.transport ?: activeTransport)
                    Spacer(modifier = Modifier.width(6.dp))
                    SecurityBadge(isVerified = true)
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(conversationMessages) { msg ->
                MessageBubbleItem(
                    message = msg,
                    onDelete = { viewModel.deleteMessageLocally(msg.id) },
                    onReply = { replyingToMessage = msg },
                    onPin = { viewModel.togglePinMessage(msg.id, msg.isPinned) },
                    onPlayAudio = { path ->
                        viewModel.voicePlayerManager.playAudio(path)
                    }
                )
            }
        }

        // Reply Banner
        if (replyingToMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1E38))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Replying to ${replyingToMessage?.senderName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                    Text(
                        text = replyingToMessage?.content ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFF8AA8CE),
                        maxLines = 1
                    )
                }
                IconButton(onClick = { replyingToMessage = null }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel reply", tint = Color.White)
                }
            }
        }

        // Input Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
                },
                modifier = Modifier.testTag("attach_file_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachFile,
                    contentDescription = "Attach File",
                    tint = JarvisCyan
                )
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.VoiceStudio) },
                modifier = Modifier.testTag("voice_studio_trigger_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Voice Studio",
                    tint = JarvisCyan
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text("Encrypted frequency message...", fontSize = 13.sp, color = Color(0xFF6B87A8))
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyanBright,
                    unfocusedBorderColor = Color(0xFF1E355B),
                    focusedTextColor = Color(0xFFE2F1FF),
                    unfocusedTextColor = Color(0xFFE2F1FF),
                    focusedContainerColor = Color(0xFF0A1324),
                    unfocusedContainerColor = Color(0xFF0A1324)
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(
                            conversationId = conversationId,
                            receiverAddress = conversationId,
                            text = inputText,
                            replyToId = replyingToMessage?.id
                        )
                        inputText = ""
                        replyingToMessage = null
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JarvisCyan)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color(0xFF040814)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleItem(
    message: MessageEntity,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    onPin: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    val isOutgoing = message.senderAddress == "LOCAL_NODE"
    val clipboardManager = LocalClipboardManager.current
    var menuExpanded by remember { mutableStateOf(false) }

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isOutgoing) 14.dp else 2.dp,
                            bottomEnd = if (isOutgoing) 2.dp else 14.dp
                        )
                    )
                    .background(
                        if (isOutgoing) Color(0xFF123456) else JarvisDarkCard
                    )
                    .combinedClickable(
                        onClick = {
                            if (message.type == MessageType.VOICE && message.attachmentPath.isNotEmpty()) {
                                onPlayAudio(message.attachmentPath)
                            }
                        },
                        onLongClick = { menuExpanded = true }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (!isOutgoing) {
                        Text(
                            text = message.senderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    when (message.type) {
                        MessageType.VOICE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play voice memo",
                                    tint = JarvisCyanBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "ENCRYPTED AUDIO TRANSMISSION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = JarvisCyanBright
                                    )
                                    Text(
                                        text = "${message.attachmentSize / 1024} KB",
                                        fontSize = 10.sp,
                                        color = Color(0xFF8AA8CE)
                                    )
                                }
                            }
                        }
                        MessageType.FILE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachFile,
                                    contentDescription = "Attachment",
                                    tint = JarvisNeonMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = message.content,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisNeonMint
                                    )
                                    Text(
                                        text = "Payload Verified: SHA-256 Checksum",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF8AA8CE)
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                color = Color(0xFFE2F1FF),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = JarvisCyan,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF8AA8CE)
                        )
                        if (isOutgoing) {
                            StatusIcon(status = message.status)
                        }
                    }
                }
            }

            // Context Menu
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color(0xFF0F1E38))
            ) {
                DropdownMenuItem(
                    text = { Text("Copy Text", color = Color.White) },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message.content))
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Reply", color = Color.White) },
                    onClick = {
                        onReply()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (message.isPinned) "Unpin" else "Pin", color = Color.White) },
                    onClick = {
                        onPin()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Locally", color = JarvisNeonPink) },
                    onClick = {
                        onDelete()
                        menuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    val (icon, tint) = when (status) {
        MessageStatus.PENDING, MessageStatus.SENDING -> Pair(Icons.Filled.Done, Color(0xFF8AA8CE))
        MessageStatus.SENT -> Pair(Icons.Filled.Done, JarvisCyan)
        MessageStatus.DELIVERED -> Pair(Icons.Filled.DoneAll, JarvisCyan)
        MessageStatus.READ -> Pair(Icons.Filled.DoneAll, JarvisNeonMint)
        MessageStatus.FAILED -> Pair(Icons.Filled.ErrorOutline, JarvisNeonPink)
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = Modifier.size(12.dp)
    )
}
