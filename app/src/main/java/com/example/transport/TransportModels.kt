package com.example.transport

import com.example.data.TransportType
import org.json.JSONObject

enum class PacketType {
    HANDSHAKE,
    HANDSHAKE_ACK,
    TEXT,
    DELIVERY_ACK,
    READ_ACK,
    FILE_START,
    FILE_CHUNK,
    FILE_END,
    VOICE_CHUNK,
    PING,
    PONG,
    GROUP_BROADCAST,
    TYPING
}

data class JarvisPacket(
    val id: String,
    val sequenceId: Long,
    val type: PacketType,
    val senderAddress: String,
    val senderName: String,
    val receiverAddress: String,
    val conversationId: String,
    val timestamp: Long,
    val encryptedPayload: String = "",
    val chunkIndex: Int = 0,
    val totalChunks: Int = 1,
    val mimeType: String = "",
    val fileName: String = "",
    val fileSize: Long = 0L,
    val sha256: String = ""
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("seq", sequenceId)
        json.put("type", type.name)
        json.put("senderAddr", senderAddress)
        json.put("senderName", senderName)
        json.put("receiverAddr", receiverAddress)
        json.put("convId", conversationId)
        json.put("ts", timestamp)
        json.put("payload", encryptedPayload)
        json.put("chunkIdx", chunkIndex)
        json.put("totalChunks", totalChunks)
        json.put("mime", mimeType)
        json.put("fn", fileName)
        json.put("fs", fileSize)
        json.put("sha256", sha256)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): JarvisPacket? {
            return try {
                val json = JSONObject(jsonStr)
                JarvisPacket(
                    id = json.getString("id"),
                    sequenceId = json.optLong("seq", 0L),
                    type = PacketType.valueOf(json.getString("type")),
                    senderAddress = json.getString("senderAddr"),
                    senderName = json.optString("senderName", "Unknown Peer"),
                    receiverAddress = json.getString("receiverAddr"),
                    conversationId = json.optString("convId", json.getString("senderAddr")),
                    timestamp = json.getLong("ts"),
                    encryptedPayload = json.optString("payload", ""),
                    chunkIndex = json.optInt("chunkIdx", 0),
                    totalChunks = json.optInt("totalChunks", 1),
                    mimeType = json.optString("mime", ""),
                    fileName = json.optString("fn", ""),
                    fileSize = json.optLong("fs", 0L),
                    sha256 = json.optString("sha256", "")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class DiscoveredDevice(
    val address: String,
    val name: String,
    val transport: TransportType,
    val rssi: Int = -55,
    val isPaired: Boolean = false,
    val isConnected: Boolean = false,
    val lastDiscovered: Long = System.currentTimeMillis()
)

data class TransportDiagnostics(
    val activeTransport: TransportType = TransportType.BLUETOOTH_CLASSIC,
    val linkSpeedMbps: Float = 2.1f,
    val packetLossRate: Float = 0.0f,
    val latencyMs: Long = 18L,
    val bytesSent: Long = 12480L,
    val bytesReceived: Long = 34920L,
    val isEncrypted: Boolean = true,
    val cipherSuite: String = "AES-256-GCM (Hardware Keystore)"
)
