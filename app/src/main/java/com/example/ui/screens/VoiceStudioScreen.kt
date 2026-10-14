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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisNeonPink
import java.io.File

@Composable
fun VoiceStudioScreen(viewModel: JarvisMainViewModel) {
    val isRecording by viewModel.voiceRecorderManager.isRecording.collectAsState()
    val durationSeconds by viewModel.voiceRecorderManager.recordingDurationSeconds.collectAsState()
    val amplitude by viewModel.voiceRecorderManager.amplitude.collectAsState()

    val isPlaying by viewModel.voicePlayerManager.isPlaying.collectAsState()
    val progressFraction by viewModel.voicePlayerManager.progressFraction.collectAsState()

    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    var recordedFile by remember { mutableStateOf<File?>(null) }

    val targetPeer = connectedPeers.firstOrNull()?.address
        ?: contacts.firstOrNull()?.address
        ?: "MK85:AA:BB:CC:DD:01"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("voice_studio_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice Studio Header
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
                        text = "JARVIS VOICE STUDIO",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "QUANTUM AUDIO ENCRYPTION // AAC STEREO",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisNeonMint
                    )
                }
                SecurityBadge(isVerified = true)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Dynamic Waveform Visualizer
        JarvisHudCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isRecording) JarvisNeonPink else JarvisCyan
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRecording) "RECORDING FREQUENCY // $durationSeconds s"
                    else if (recordedFile != null) "RECORDING READY // ${durationSeconds}s PREVIEW"
                    else "VOICE MODULE ARMED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isRecording) JarvisNeonPink else JarvisCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                WaveformVisualizer(
                    amplitude = if (isRecording) amplitude else if (isPlaying) 18000 else 4000,
                    barCount = 28,
                    isPlayingOrRecording = isRecording || isPlaying
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Record Trigger Button
        Button(
            onClick = {
                if (isRecording) {
                    recordedFile = viewModel.voiceRecorderManager.stopRecording()
                } else {
                    recordedFile = null
                    viewModel.voicePlayerManager.stopAudio()
                    viewModel.voiceRecorderManager.startRecording()
                }
            },
            modifier = Modifier
                .size(90.dp)
                .testTag("record_toggle_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) JarvisNeonPink else JarvisCyan,
                contentColor = Color(0xFF040814)
            )
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isRecording) "TAP TO STOP RECORDING" else "TAP TO RECORD VOICE MEMO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8AA8CE)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Actions if file recorded
        if (recordedFile != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Preview button
                OutlinedButton(
                    onClick = {
                        val path = recordedFile?.absolutePath
                        if (path != null) {
                            if (isPlaying) {
                                viewModel.voicePlayerManager.stopAudio()
                            } else {
                                viewModel.voicePlayerManager.playAudio(path)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("preview_audio_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPlaying) "PAUSE" else "PREVIEW", fontSize = 11.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                }

                // Discard Button
                OutlinedButton(
                    onClick = {
                        viewModel.voicePlayerManager.stopAudio()
                        recordedFile?.delete()
                        recordedFile = null
                    },
                    modifier = Modifier.weight(1f).testTag("discard_audio_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = JarvisNeonPink, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DISCARD", fontSize = 11.sp, color = JarvisNeonPink, fontWeight = FontWeight.Bold)
                }

                // Send Button
                Button(
                    onClick = {
                        val file = recordedFile
                        if (file != null) {
                            viewModel.sendVoiceMessage(
                                conversationId = targetPeer,
                                receiverAddress = targetPeer,
                                audioFile = file
                            )
                            recordedFile = null
                            viewModel.showHudNotification("Voice memo transmitted to $targetPeer")
                        }
                    },
                    modifier = Modifier.weight(1.2f).testTag("send_voice_note_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color(0xFF040814)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TRANSMIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
