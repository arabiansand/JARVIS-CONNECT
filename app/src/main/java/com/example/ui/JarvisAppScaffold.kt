package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.JarvisHudCard
import com.example.ui.components.TransportBadge
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectedDevicesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FileTransferScreen
import com.example.ui.screens.GroupChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDarkBackground
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink

@Composable
fun JarvisAppScaffold(
    viewModel: JarvisMainViewModel,
    onRequestPermissions: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val activeTransport by viewModel.activeTransport.collectAsState()
    val hudNotification by viewModel.hudNotification.collectAsState()
    val voiceDialogActive by viewModel.voiceDialogActive.collectAsState()
    val pendingVoiceCommand by viewModel.pendingVoiceCommand.collectAsState()

    // Back button handling
    BackHandler(enabled = currentScreen != Screen.Dashboard) {
        viewModel.navigateBack()
    }

    if (currentScreen == Screen.Welcome) {
        WelcomeScreen(
            viewModel = viewModel,
            onRequestPermissions = onRequestPermissions
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("jarvis_app_scaffold"),
        topBar = {
            JarvisTopAppBar(
                currentScreen = currentScreen,
                viewModel = viewModel,
                isDark = isDark,
                onBack = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            JarvisBottomNavigation(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switcher
            when (val screen = currentScreen) {
                is Screen.Welcome -> WelcomeScreen(viewModel, onRequestPermissions)
                is Screen.Dashboard -> DashboardScreen(viewModel)
                is Screen.Scanner -> ScannerScreen(viewModel)
                is Screen.ConnectedDevices -> ConnectedDevicesScreen(viewModel)
                is Screen.Chat -> ChatScreen(screen.conversationId, viewModel)
                is Screen.GroupChat -> GroupChatScreen(viewModel)
                is Screen.FileTransfer -> FileTransferScreen(viewModel)
                is Screen.VoiceStudio -> VoiceStudioScreen(viewModel)
                is Screen.History -> HistoryScreen(viewModel)
                is Screen.Security -> SecurityScreen(viewModel)
            }

            // Floating HUD Notification
            AnimatedVisibility(
                visible = hudNotification != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                hudNotification?.let { msg ->
                    JarvisHudCard(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        borderColor = JarvisCyan,
                        backgroundColor = Color(0xEE091426)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(JarvisNeonMint)
                            )
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.clearHudNotification() },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Voice Assistant Dialog
    if (voiceDialogActive) {
        JarvisVoiceModal(
            viewModel = viewModel,
            onDismiss = { viewModel.closeVoiceAssistant() }
        )
    }

    // Voice Command Safety Confirmation Modal
    if (pendingVoiceCommand != null) {
        val pending = pendingVoiceCommand
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingVoiceMessage() },
            title = {
                Text(
                    text = "CONFIRM TRANSMISSION",
                    color = JarvisCyanBright,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "JARVIS safety protocol requires confirmation before transmitting outbound payload:",
                        color = Color(0xFF8AA8CE),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TARGET: ${pending?.targetContact}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyan
                    )
                    Text(
                        text = "CONTENT: \"${pending?.textContent}\"",
                        fontSize = 12.sp,
                        color = Color(0xFFE2F1FF)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingVoiceMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF040814))
                ) {
                    Text("AUTHORIZE TRANSMISSION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelPendingVoiceMessage() }) {
                    Text("ABORT", color = JarvisNeonPink)
                }
            },
            containerColor = Color(0xFF0B172E)
        )
    }
}

@Composable
private fun JarvisTopAppBar(
    currentScreen: Screen,
    viewModel: JarvisMainViewModel,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val activeTransport by viewModel.activeTransport.collectAsState()

    val screenTitle = when (currentScreen) {
        is Screen.Welcome -> "STARTUP"
        is Screen.Dashboard -> "DASHBOARD"
        is Screen.Scanner -> "RADAR SCANNER"
        is Screen.ConnectedDevices -> "CONNECTED PEERS"
        is Screen.Chat -> "SECURE CHAT"
        is Screen.GroupChat -> "AVENGERS MESH"
        is Screen.FileTransfer -> "FILE VAULT"
        is Screen.VoiceStudio -> "VOICE STUDIO"
        is Screen.History -> "HISTORY LOGS"
        is Screen.Security -> "SECURITY CONSOLE"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0xFF07101E))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentScreen != Screen.Dashboard) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan
                    )
                }
            } else {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Welcome) },
                    modifier = Modifier.testTag("top_bar_home_button")
                ) {
                    ArcReactorCore(size = 32.dp)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = "JARVIS // $screenTitle",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyanBright
                )
                Text(
                    text = "OFFLINE QUANTUM PROTOCOL",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF8AA8CE)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TransportBadge(transport = activeTransport)

            IconButton(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier.testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                    contentDescription = "Theme Toggle",
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { viewModel.openVoiceAssistant() },
                modifier = Modifier.testTag("top_bar_voice_assistant_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "JARVIS Assistant",
                    tint = JarvisCyanBright,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun JarvisBottomNavigation(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("jarvis_bottom_nav"),
        containerColor = Color(0xFF07101E),
        tonalElevation = 6.dp
    ) {
        val navItems = listOf(
            Triple(Screen.Dashboard, Icons.Filled.Dashboard, "Core"),
            Triple(Screen.Scanner, Icons.Filled.Radar, "Radar"),
            Triple(Screen.GroupChat, Icons.Filled.Group, "Mesh"),
            Triple(Screen.FileTransfer, Icons.Filled.Folder, "Vault"),
            Triple(Screen.VoiceStudio, Icons.Filled.Mic, "Voice"),
            Triple(Screen.Security, Icons.Filled.Security, "Shield")
        )

        navItems.forEach { (screen, icon, label) ->
            val isSelected = currentScreen::class == screen::class
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JarvisCyanBright,
                    selectedTextColor = JarvisCyanBright,
                    indicatorColor = Color(0xFF102444),
                    unselectedIconColor = Color(0xFF6B87A8),
                    unselectedTextColor = Color(0xFF6B87A8)
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}

@Composable
private fun JarvisVoiceModal(
    viewModel: JarvisMainViewModel,
    onDismiss: () -> Unit
) {
    var commandInput by remember { mutableStateOf("") }

    val presetCommands = listOf(
        "JARVIS, scan nearby devices.",
        "JARVIS, show connected devices.",
        "JARVIS, open my messages.",
        "JARVIS, start a voice recording.",
        "JARVIS, disconnect the current device.",
        "JARVIS, enable dark mode."
    )

    Dialog(onDismissRequest = onDismiss) {
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth().testTag("jarvis_voice_modal"),
            borderColor = JarvisCyan,
            backgroundColor = Color(0xFF060F1E)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JARVIS VOICE AI COMMAND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ArcReactorCore(
                    size = 100.dp,
                    onClick = { viewModel.voiceAssistant.speakGreeting() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Awaiting verbal instruction or prompt:",
                    fontSize = 11.sp,
                    color = Color(0xFF8AA8CE),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = { Text("Enter command or tap below...", fontSize = 11.sp, color = Color(0xFF6B87A8)) },
                    modifier = Modifier.fillMaxWidth().testTag("voice_command_text_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = Color(0xFF1E355B),
                        focusedTextColor = Color(0xFFE2F1FF),
                        unfocusedTextColor = Color(0xFFE2F1FF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            viewModel.executeVoiceText(commandInput)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("execute_command_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF040814))
                ) {
                    Text("EXECUTE PROTOCOL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "QUICK TACTICAL VOICE COMMANDS:",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6B87A8),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCommands.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0E1E38))
                                .clickable {
                                    viewModel.executeVoiceText(preset)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
