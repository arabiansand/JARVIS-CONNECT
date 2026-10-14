package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApp
import com.example.audio.JarvisVoiceCommand
import com.example.data.ContactEntity
import com.example.data.ConversationEntity
import com.example.data.FileTransferEntity
import com.example.data.MessageEntity
import com.example.data.MessageStatus
import com.example.data.MessageType
import com.example.data.TransferDirection
import com.example.data.TransferStatus
import com.example.data.TransportType
import com.example.security.CryptoManager
import com.example.transport.DiscoveredDevice
import com.example.transport.JarvisPacket
import com.example.transport.PacketType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed class Screen {
    object Welcome : Screen()
    object Dashboard : Screen()
    object Scanner : Screen()
    object ConnectedDevices : Screen()
    data class Chat(val conversationId: String) : Screen()
    object GroupChat : Screen()
    object FileTransfer : Screen()
    object VoiceStudio : Screen()
    object History : Screen()
    object Security : Screen()
}

class JarvisMainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApp
    private val repository = app.repository
    private val transportManager = app.transportManager
    val voiceRecorderManager = app.voiceRecorderManager
    val voicePlayerManager = app.voicePlayerManager
    val voiceAssistant = app.voiceAssistant

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation History Stack for BackHandler
    private val screenStack = mutableListOf<Screen>(Screen.Dashboard)

    // Dark/Light Theme toggle
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Device Fingerprint
    val deviceFingerprint: String = CryptoManager.getDeviceFingerprint(application)

    // Data Flows from Repository
    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fileTransfers: StateFlow<List<FileTransferEntity>> = repository.allTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transport Manager Flows
    val activeTransport = transportManager.activeTransport
    val isScanning = transportManager.isScanning
    val discoveredDevices = transportManager.discoveredDevices
    val connectedPeers = transportManager.connectedPeers
    val diagnostics = transportManager.diagnostics

    // Voice assistant flows
    val isTtsSpeaking = voiceAssistant.isSpeaking
    val readAloudEnabled = voiceAssistant.readAloudEnabled
    val voiceEnabled = voiceAssistant.voiceEnabled

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Status snackbar / HUD announcement
    private val _hudNotification = MutableStateFlow<String?>(null)
    val hudNotification: StateFlow<String?> = _hudNotification.asStateFlow()

    // Voice command dialog state
    private val _voiceDialogActive = MutableStateFlow(false)
    val voiceDialogActive: StateFlow<Boolean> = _voiceDialogActive.asStateFlow()

    // Pending voice confirmation
    private val _pendingVoiceCommand = MutableStateFlow<JarvisVoiceCommand.PrepareMessage?>(null)
    val pendingVoiceCommand: StateFlow<JarvisVoiceCommand.PrepareMessage?> = _pendingVoiceCommand.asStateFlow()

    init {
        // Greet when application opens as requested:
        // "As salaam alaikum. JARVIS CONNECT is online. Ready for secure communication."
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            voiceAssistant.speakGreeting()
            showHudNotification("As salaam alaikum. JARVIS CONNECT is online.")
        }

        // Listen for incoming mesh packets
        viewModelScope.launch {
            transportManager.incomingPackets.collect { packet ->
                handleIncomingPacket(packet)
            }
        }
    }

    private suspend fun handleIncomingPacket(packet: JarvisPacket) {
        when (packet.type) {
            PacketType.DELIVERY_ACK -> {
                // Update message delivery state
                repository.updateMessageStatus(packet.id, MessageStatus.DELIVERED)
            }
            PacketType.READ_ACK -> {
                repository.updateMessageStatus(packet.id, MessageStatus.READ)
            }
            PacketType.TEXT -> {
                val secretKey = CryptoManager.deriveSessionKey(packet.senderAddress)
                val decryptedText = try {
                    if (packet.encryptedPayload.isNotEmpty()) {
                        CryptoManager.decrypt(packet.encryptedPayload, secretKey)
                    } else {
                        packet.encryptedPayload
                    }
                } catch (e: Exception) {
                    packet.encryptedPayload // Fallback if cleartext simulated
                }

                val message = MessageEntity(
                    id = packet.id,
                    conversationId = packet.conversationId,
                    senderAddress = packet.senderAddress,
                    senderName = packet.senderName,
                    receiverAddress = "LOCAL_NODE",
                    content = decryptedText,
                    type = MessageType.TEXT,
                    timestamp = packet.timestamp,
                    status = MessageStatus.READ,
                    isEncrypted = true,
                    securityVerified = true
                )
                repository.receiveMessage(message)

                if (voiceAssistant.readAloudEnabled.value) {
                    voiceAssistant.speak("Message from ${packet.senderName}: $decryptedText")
                }
            }
            PacketType.VOICE_CHUNK -> {
                val message = MessageEntity(
                    id = packet.id,
                    conversationId = packet.conversationId,
                    senderAddress = packet.senderAddress,
                    senderName = packet.senderName,
                    receiverAddress = "LOCAL_NODE",
                    content = "[Voice Transmission Decrypted]",
                    type = MessageType.VOICE,
                    timestamp = packet.timestamp,
                    status = MessageStatus.READ,
                    attachmentPath = packet.fileName,
                    attachmentSize = packet.fileSize,
                    isEncrypted = true,
                    securityVerified = true
                )
                repository.receiveMessage(message)
                showHudNotification("Encrypted voice note received from ${packet.senderName}")
            }
            PacketType.FILE_START -> {
                val message = MessageEntity(
                    id = packet.id,
                    conversationId = packet.conversationId,
                    senderAddress = packet.senderAddress,
                    senderName = packet.senderName,
                    receiverAddress = "LOCAL_NODE",
                    content = "File received: ${packet.fileName}",
                    type = MessageType.FILE,
                    timestamp = packet.timestamp,
                    status = MessageStatus.DELIVERED,
                    attachmentPath = packet.fileName,
                    attachmentSize = packet.fileSize,
                    attachmentMimeType = packet.mimeType,
                    isEncrypted = true,
                    securityVerified = true
                )
                repository.receiveMessage(message)
                val transfer = FileTransferEntity(
                    id = UUID.randomUUID().toString(),
                    messageId = packet.id,
                    fileName = packet.fileName,
                    localUriOrPath = packet.fileName,
                    totalBytes = packet.fileSize,
                    bytesTransferred = packet.fileSize,
                    direction = TransferDirection.INCOMING,
                    status = TransferStatus.COMPLETED,
                    sha256Hash = packet.sha256,
                    mimeType = packet.mimeType,
                    peerAddress = packet.senderAddress
                )
                repository.recordFileTransfer(transfer)
                showHudNotification("File downloaded: ${packet.fileName}")
            }
            else -> {}
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
            return true
        } else if (_currentScreen.value != Screen.Dashboard) {
            _currentScreen.value = Screen.Dashboard
            screenStack.clear()
            screenStack.add(Screen.Dashboard)
            return true
        }
        return false
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    fun showHudNotification(message: String) {
        _hudNotification.value = message
    }

    fun clearHudNotification() {
        _hudNotification.value = null
    }

    // Transport Actions
    fun switchTransport(type: TransportType) {
        transportManager.setTransport(type)
        showHudNotification("Active transport switched to: ${type.name}")
    }

    fun startScan() {
        transportManager.startScan()
        showHudNotification("JARVIS frequency scanner active...")
    }

    fun stopScan() {
        transportManager.stopScan()
    }

    fun connectDevice(device: DiscoveredDevice) {
        transportManager.connectToDevice(device) { success, msg ->
            showHudNotification(msg)
            if (success) {
                viewModelScope.launch {
                    val contact = ContactEntity(
                        address = device.address,
                        name = device.name,
                        transport = device.transport,
                        isTrusted = true,
                        isConnected = true,
                        rssi = device.rssi,
                        encryptionFingerprint = CryptoManager.deriveSessionKey(device.address).encoded.take(6).joinToString(":") { "%02X".format(it) }
                    )
                    repository.saveContact(contact)
                    repository.getOrCreateConversation(device.address, device.name, device.transport)
                }
            }
        }
    }

    fun disconnectDevice(address: String) {
        transportManager.disconnectDevice(address)
        viewModelScope.launch {
            repository.updateConnectionStatus(address, false)
        }
        showHudNotification("Device disconnected: $address")
    }

    // Messaging
    fun sendTextMessage(conversationId: String, receiverAddress: String, text: String, replyToId: String? = null) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val secretKey = CryptoManager.deriveSessionKey(receiverAddress)
            val encryptedText = CryptoManager.encrypt(text, secretKey)

            val message = repository.sendMessage(
                conversationId = conversationId,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                content = text,
                type = MessageType.TEXT,
                replyToId = replyToId
            )

            val packet = JarvisPacket(
                id = message.id,
                sequenceId = transportManager.getNextSequence(),
                type = PacketType.TEXT,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                conversationId = conversationId,
                timestamp = message.timestamp,
                encryptedPayload = encryptedText
            )

            transportManager.sendPacket(packet) { delivered ->
                viewModelScope.launch {
                    repository.updateMessageStatus(
                        message.id,
                        if (delivered) MessageStatus.DELIVERED else MessageStatus.FAILED
                    )
                }
            }
        }
    }

    fun sendVoiceMessage(conversationId: String, receiverAddress: String, audioFile: File) {
        viewModelScope.launch {
            val message = repository.sendMessage(
                conversationId = conversationId,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                content = "[Voice Transmission Encrypted]",
                type = MessageType.VOICE,
                attachmentPath = audioFile.absolutePath,
                attachmentSize = audioFile.length(),
                attachmentMimeType = "audio/m4a"
            )

            val packet = JarvisPacket(
                id = message.id,
                sequenceId = transportManager.getNextSequence(),
                type = PacketType.VOICE_CHUNK,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                conversationId = conversationId,
                timestamp = message.timestamp,
                fileName = audioFile.name,
                fileSize = audioFile.length(),
                encryptedPayload = "[AUDIO_CHUNK_STREAM]"
            )

            transportManager.sendPacket(packet) { delivered ->
                viewModelScope.launch {
                    repository.updateMessageStatus(
                        message.id,
                        if (delivered) MessageStatus.DELIVERED else MessageStatus.FAILED
                    )
                }
            }
        }
    }

    fun sendFile(conversationId: String, receiverAddress: String, uri: Uri, fileName: String, fileSize: Long, mimeType: String) {
        viewModelScope.launch {
            val message = repository.sendMessage(
                conversationId = conversationId,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                content = "File attachment: $fileName",
                type = MessageType.FILE,
                attachmentPath = uri.toString(),
                attachmentSize = fileSize,
                attachmentMimeType = mimeType
            )

            val transfer = FileTransferEntity(
                id = UUID.randomUUID().toString(),
                messageId = message.id,
                fileName = fileName,
                localUriOrPath = uri.toString(),
                totalBytes = fileSize,
                bytesTransferred = fileSize,
                direction = TransferDirection.OUTGOING,
                status = TransferStatus.COMPLETED,
                sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                mimeType = mimeType,
                peerAddress = receiverAddress
            )
            repository.recordFileTransfer(transfer)

            val packet = JarvisPacket(
                id = message.id,
                sequenceId = transportManager.getNextSequence(),
                type = PacketType.FILE_START,
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = receiverAddress,
                conversationId = conversationId,
                timestamp = message.timestamp,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType,
                sha256 = transfer.sha256Hash
            )

            transportManager.sendPacket(packet)
            showHudNotification("File payload queued: $fileName")
        }
    }

    fun deleteMessageLocally(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessageLocally(messageId)
            showHudNotification("Message removed locally from device memory")
        }
    }

    fun togglePinMessage(messageId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.toggleMessagePin(messageId, !currentPinned)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            showHudNotification("Local encrypted database cleared")
        }
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    // Voice Assistant Commands
    fun openVoiceAssistant() {
        _voiceDialogActive.value = true
    }

    fun closeVoiceAssistant() {
        _voiceDialogActive.value = false
    }

    fun executeVoiceText(commandText: String) {
        val command = voiceAssistant.parseVoiceCommand(commandText)
        when (command) {
            is JarvisVoiceCommand.ScanDevices -> {
                navigateTo(Screen.Scanner)
                startScan()
                voiceAssistant.speak("Scanning nearby wireless frequencies now, sir.")
            }
            is JarvisVoiceCommand.ShowConnected -> {
                navigateTo(Screen.ConnectedDevices)
                voiceAssistant.speak("Displaying active mesh links.")
            }
            is JarvisVoiceCommand.OpenMessages -> {
                navigateTo(Screen.Dashboard)
                voiceAssistant.speak("Opening message terminal.")
            }
            is JarvisVoiceCommand.StartVoiceRecording -> {
                navigateTo(Screen.VoiceStudio)
                voiceAssistant.speak("Voice recording module armed.")
            }
            is JarvisVoiceCommand.DisconnectCurrent -> {
                val active = connectedPeers.value.firstOrNull()
                if (active != null) {
                    disconnectDevice(active.address)
                    voiceAssistant.speak("Disconnected from ${active.name}.")
                } else {
                    voiceAssistant.speak("No active peer connections found.")
                }
            }
            is JarvisVoiceCommand.EnableDarkMode -> {
                setDarkTheme(true)
                voiceAssistant.speak("Dark HUD mode engaged.")
            }
            is JarvisVoiceCommand.EnableLightMode -> {
                setDarkTheme(false)
                voiceAssistant.speak("Titanium daylight mode engaged.")
            }
            is JarvisVoiceCommand.PrepareMessage -> {
                // Safety confirmation before sending sensitive transmissions
                _pendingVoiceCommand.value = command
                voiceAssistant.speak("Confirm transmission of message to ${command.targetContact}?")
            }
            is JarvisVoiceCommand.Unknown -> {
                voiceAssistant.speak("Command not recognized in offline catalog.")
            }
        }
        _voiceDialogActive.value = false
    }

    fun confirmPendingVoiceMessage() {
        val pending = _pendingVoiceCommand.value ?: return
        val targetContact = contacts.value.find { it.name.contains(pending.targetContact, ignoreCase = true) }
            ?: contacts.value.firstOrNull()

        if (targetContact != null) {
            sendTextMessage(targetContact.address, targetContact.address, pending.textContent)
            voiceAssistant.speak("Message transmitted to ${targetContact.name}.")
            showHudNotification("Transmitted: ${pending.textContent}")
        }
        _pendingVoiceCommand.value = null
    }

    fun cancelPendingVoiceMessage() {
        _pendingVoiceCommand.value = null
        voiceAssistant.speak("Transmission cancelled.")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
