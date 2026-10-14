package com.example

import com.example.audio.JarvisVoiceAssistant
import com.example.audio.JarvisVoiceCommand
import com.example.data.TransportType
import com.example.security.CryptoManager
import com.example.transport.JarvisPacket
import com.example.transport.PacketType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testAes256GcmEncryptionAndDecryption() {
        val peerAddress = "MK85:AA:BB:CC:DD:01"
        val secretKey = CryptoManager.deriveSessionKey(peerAddress)
        val originalMessage = "Tactical payload: All systems 100%. Offline frequency armed."

        val cipherText = CryptoManager.encrypt(originalMessage, secretKey)
        assertNotEquals(originalMessage, cipherText)

        val decrypted = CryptoManager.decrypt(cipherText, secretKey)
        assertEquals(originalMessage, decrypted)
    }

    @Test
    fun testReplayAttackProtection() {
        val peerAddress = "MK85:AA:BB:CC:DD:01"
        val now = System.currentTimeMillis()

        // First transmission with sequence 100 should succeed
        val valid1 = CryptoManager.validateReplayProtection(peerAddress, sequenceId = 100L, timestamp = now)
        assertTrue(valid1)

        // Immediate duplicate transmission with identical sequence 100 must be rejected (replay attack)
        val replayDuplicate = CryptoManager.validateReplayProtection(peerAddress, sequenceId = 100L, timestamp = now)
        assertFalse(replayDuplicate)

        // Transmission with stale timestamp (older than 10 minutes) must be rejected
        val stalePacket = CryptoManager.validateReplayProtection(peerAddress, sequenceId = 101L, timestamp = now - 600_000L)
        assertFalse(stalePacket)
    }

    @Test
    fun testJarvisPacketSerialization() {
        val packet = JarvisPacket(
            id = UUID.randomUUID().toString(),
            sequenceId = 42L,
            type = PacketType.TEXT,
            senderAddress = "LOCAL_NODE",
            senderName = "JARVIS Terminal",
            receiverAddress = "FRID:11:22:33:44:02",
            conversationId = "FRID:11:22:33:44:02",
            timestamp = 1700000000000L,
            encryptedPayload = "ENCRYPTED_GCM_BLOB"
        )

        val json = packet.toJson()
        val restored = JarvisPacket.fromJson(json)

        assertNotNull(restored)
        assertEquals(packet.id, restored?.id)
        assertEquals(packet.sequenceId, restored?.sequenceId)
        assertEquals(packet.type, restored?.type)
        assertEquals(packet.senderAddress, restored?.senderAddress)
        assertEquals(packet.encryptedPayload, restored?.encryptedPayload)
    }

    @Test
    fun testBluetoothPermissionHelper() {
        val requiredPerms = com.example.transport.BluetoothPermissionHelper.getRequiredPermissions()
        assertTrue(requiredPerms.isNotEmpty())

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val missing = com.example.transport.BluetoothPermissionHelper.getMissingPermissions(context)
        assertNotNull(missing)
    }

    @Test
    fun testBluetoothDiscoveryReadiness() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // When Bluetooth is disabled
        val disabledResult = com.example.transport.BluetoothPermissionHelper.checkDiscoveryReadiness(context, isBluetoothEnabled = false)
        assertNotNull(disabledResult)

        // When permissions granted and Bluetooth enabled
        val isReadyOrMissing = com.example.transport.BluetoothPermissionHelper.checkDiscoveryReadiness(context, isBluetoothEnabled = true)
        assertTrue(
            isReadyOrMissing is com.example.transport.DiscoveryPermissionResult.Ready ||
            isReadyOrMissing is com.example.transport.DiscoveryPermissionResult.MissingPermissions
        )
    }
}
