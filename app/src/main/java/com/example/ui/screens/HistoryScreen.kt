package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.ui.JarvisMainViewModel
import com.example.ui.Screen
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.SecurityBadge
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonPink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: JarvisMainViewModel) {
    val allMessages by viewModel.allMessages.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredMessages = allMessages.filter {
        if (searchQuery.isBlank()) true
        else it.content.contains(searchQuery, ignoreCase = true) || it.senderName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_screen")
    ) {
        // History Header
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
                        text = "COMMUNICATION HISTORY",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LOCAL ENCRYPTED STORE // ${filteredMessages.size} RECORDS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8AA8CE)
                    )
                }
                SecurityBadge(isVerified = true)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().testTag("history_search_input"),
            placeholder = { Text("Search encrypted transmissions...", fontSize = 12.sp, color = Color(0xFF6B87A8)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = JarvisCyan) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = Color(0xFF1E355B),
                focusedTextColor = Color(0xFFE2F1FF),
                unfocusedTextColor = Color(0xFFE2F1FF),
                focusedContainerColor = Color(0xFF0A1324),
                unfocusedContainerColor = Color(0xFF0A1324)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Clear History Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = { showClearDialog = true },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = JarvisNeonPink, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PURGE LOCAL LOGS", fontSize = 10.sp, color = JarvisNeonPink, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Log List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredMessages) { msg ->
                val timeFormatter = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                val formattedTime = timeFormatter.format(Date(msg.timestamp))

                JarvisHudCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.Chat(msg.conversationId)) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${msg.senderName} ➔ ${msg.receiverAddress}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyanBright,
                                fontFamily = FontFamily.Monospace
                            )
                            if (msg.isPinned) {
                                Icon(Icons.Filled.PushPin, contentDescription = "Pinned", tint = JarvisCyan, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.content,
                            fontSize = 12.sp,
                            color = Color(0xFFE2F1FF),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$formattedTime | Status: ${msg.status.name}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF8AA8CE)
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("CONFIRM MEMORY PURGE", color = JarvisNeonPink, fontFamily = FontFamily.Monospace) },
            text = { Text("This will permanently delete all stored local encrypted chat records on this device.", color = Color(0xFFE2F1FF)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNeonPink)
                ) {
                    Text("PURGE NOW", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = JarvisCyan)
                }
            },
            containerColor = Color(0xFF0F1E38)
        )
    }
}
