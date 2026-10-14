package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        FileTransferEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun fileTransferDao(): FileTransferDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_mesh_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial contacts and welcome transmissions
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: JarvisDatabase) {
            val contactDao = database.contactDao()
            val convDao = database.conversationDao()
            val msgDao = database.messageDao()

            val now = System.currentTimeMillis()

            val contacts = listOf(
                ContactEntity(
                    address = "MK85:AA:BB:CC:DD:01",
                    name = "MARK-85 HUD Uplink",
                    transport = TransportType.BLUETOOTH_CLASSIC,
                    isTrusted = true,
                    encryptionFingerprint = "7F:3A:9C:12:DE:01",
                    lastSeenTimestamp = now,
                    rssi = -42,
                    isConnected = true,
                    avatarTag = "MK85"
                ),
                ContactEntity(
                    address = "FRID:11:22:33:44:02",
                    name = "FRIDAY Tactical AI",
                    transport = TransportType.BLUETOOTH_LE,
                    isTrusted = true,
                    encryptionFingerprint = "C4:88:E2:B0:19:02",
                    lastSeenTimestamp = now - 60_000,
                    rssi = -68,
                    isConnected = false,
                    avatarTag = "FRID"
                ),
                ContactEntity(
                    address = "STRK:55:66:77:88:03",
                    name = "STARK-PAD Alpha",
                    transport = TransportType.WIFI_DIRECT,
                    isTrusted = true,
                    encryptionFingerprint = "90:4B:EE:71:34:03",
                    lastSeenTimestamp = now - 180_000,
                    rssi = -35,
                    isConnected = false,
                    avatarTag = "PAD"
                )
            )
            contactDao.insertAll(contacts)

            // Conversations
            val conv1 = ConversationEntity(
                id = "MK85:AA:BB:CC:DD:01",
                title = "MARK-85 HUD Uplink",
                isGroup = false,
                lastMessageText = "Telemetry linked. Quantum cipher active on RFCOMM channel 1.",
                lastMessageTimestamp = now - 30_000,
                unreadCount = 0,
                isPinned = true,
                preferredTransport = TransportType.BLUETOOTH_CLASSIC
            )

            val groupConv = ConversationEntity(
                id = "GROUP_AVENGERS_GRID",
                title = "Avengers Tactical Mesh",
                isGroup = true,
                participantAddresses = "MK85:AA:BB:CC:DD:01,FRID:11:22:33:44:02,STRK:55:66:77:88:03",
                lastMessageText = "JARVIS: All nodes connected to offline frequency band.",
                lastMessageTimestamp = now - 120_000,
                unreadCount = 1,
                isPinned = true,
                preferredTransport = TransportType.BLUETOOTH_CLASSIC
            )

            convDao.insertOrUpdate(conv1)
            convDao.insertOrUpdate(groupConv)

            // Seed introductory messages
            val msg1 = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "MK85:AA:BB:CC:DD:01",
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS Terminal",
                receiverAddress = "MK85:AA:BB:CC:DD:01",
                content = "Initiating RFCOMM handshake with Mark-85 armor suit...",
                type = MessageType.SYSTEM,
                timestamp = now - 120_000,
                status = MessageStatus.READ,
                isEncrypted = true,
                securityVerified = true
            )
            val msg2 = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "MK85:AA:BB:CC:DD:01",
                senderAddress = "MK85:AA:BB:CC:DD:01",
                senderName = "MARK-85 HUD Uplink",
                receiverAddress = "LOCAL_NODE",
                content = "Handshake acknowledged. As salaam alaikum, sir. Telemetry linked. Quantum cipher active on RFCOMM channel 1.",
                type = MessageType.TEXT,
                timestamp = now - 30_000,
                status = MessageStatus.READ,
                isEncrypted = true,
                securityVerified = true
            )
            msgDao.insertAll(listOf(msg1, msg2))

            val groupMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "GROUP_AVENGERS_GRID",
                senderAddress = "LOCAL_NODE",
                senderName = "JARVIS System",
                receiverAddress = "GROUP_AVENGERS_GRID",
                content = "Mesh network operational across Bluetooth Classic & Wi-Fi Direct. Ready for mission payload distribution.",
                type = MessageType.TEXT,
                timestamp = now - 120_000,
                status = MessageStatus.DELIVERED,
                isEncrypted = true,
                securityVerified = true
            )
            msgDao.insertMessage(groupMsg)
        }
    }
}
