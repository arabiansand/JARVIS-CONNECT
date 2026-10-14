package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransportType {
    BLUETOOTH_CLASSIC,
    BLUETOOTH_LE,
    WIFI_DIRECT,
    SIMULATED
}

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    VOICE,
    SYSTEM
}

enum class MessageStatus {
    PENDING,
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class TransferStatus {
    QUEUED,
    TRANSFERRING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class TransferDirection {
    OUTGOING,
    INCOMING
}

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val address: String, // MAC address, Wi-Fi P2P IP, or unique simulated ID
    val name: String,
    val transport: TransportType,
    val isTrusted: Boolean = true,
    val encryptionFingerprint: String = "",
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val rssi: Int = -55,
    val isConnected: Boolean = false,
    val avatarTag: String = "MARK"
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String, // Peer address or group UUID
    val title: String,
    val isGroup: Boolean = false,
    val participantAddresses: String = "", // Comma-separated addresses
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val preferredTransport: TransportType = TransportType.BLUETOOTH_CLASSIC
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String, // UUID
    val conversationId: String,
    val senderAddress: String,
    val senderName: String,
    val receiverAddress: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val attachmentPath: String = "",
    val attachmentSize: Long = 0L,
    val attachmentMimeType: String = "",
    val isEncrypted: Boolean = true,
    val securityVerified: Boolean = true,
    val replyToMessageId: String? = null,
    val isPinned: Boolean = false
)

@Entity(tableName = "file_transfers")
data class FileTransferEntity(
    @PrimaryKey val id: String, // UUID
    val messageId: String,
    val fileName: String,
    val localUriOrPath: String,
    val totalBytes: Long,
    val bytesTransferred: Long = 0L,
    val direction: TransferDirection,
    val status: TransferStatus = TransferStatus.QUEUED,
    val sha256Hash: String = "",
    val mimeType: String = "application/octet-stream",
    val peerAddress: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
