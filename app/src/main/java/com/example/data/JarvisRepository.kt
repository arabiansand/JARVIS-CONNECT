package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class JarvisRepository(private val database: JarvisDatabase) {

    private val contactDao = database.contactDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val fileTransferDao = database.fileTransferDao()

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val allTransfers: Flow<List<FileTransferEntity>> = fileTransferDao.getAllTransfers()
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    fun getConversationFlow(id: String): Flow<ConversationEntity?> {
        return conversationDao.getConversationFlow(id)
    }

    fun searchMessages(query: String): Flow<List<MessageEntity>> {
        return messageDao.searchMessages(query)
    }

    suspend fun getContact(address: String): ContactEntity? {
        return contactDao.getContact(address)
    }

    suspend fun saveContact(contact: ContactEntity) {
        contactDao.insertOrUpdate(contact)
    }

    suspend fun updateConnectionStatus(address: String, connected: Boolean) {
        contactDao.updateConnectionStatus(address, connected)
    }

    suspend fun markAllDisconnected() {
        contactDao.markAllDisconnected()
    }

    suspend fun deleteContact(address: String) {
        contactDao.deleteContact(address)
    }

    suspend fun getOrCreateConversation(
        peerAddress: String,
        peerName: String,
        transport: TransportType = TransportType.BLUETOOTH_CLASSIC
    ): ConversationEntity {
        var conv = conversationDao.getConversationById(peerAddress)
        if (conv == null) {
            conv = ConversationEntity(
                id = peerAddress,
                title = peerName,
                isGroup = false,
                participantAddresses = peerAddress,
                lastMessageText = "Secure link initialized",
                lastMessageTimestamp = System.currentTimeMillis(),
                preferredTransport = transport
            )
            conversationDao.insertOrUpdate(conv)
        }
        return conv
    }

    suspend fun sendMessage(
        conversationId: String,
        senderAddress: String,
        senderName: String,
        receiverAddress: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        attachmentPath: String = "",
        attachmentSize: Long = 0L,
        attachmentMimeType: String = "",
        replyToId: String? = null
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderAddress = senderAddress,
            senderName = senderName,
            receiverAddress = receiverAddress,
            content = content,
            type = type,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            attachmentPath = attachmentPath,
            attachmentSize = attachmentSize,
            attachmentMimeType = attachmentMimeType,
            isEncrypted = true,
            securityVerified = true,
            replyToMessageId = replyToId
        )

        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(
            id = conversationId,
            text = when (type) {
                MessageType.VOICE -> "[Voice Transmission]"
                MessageType.IMAGE -> "[Image Transmission]"
                MessageType.FILE -> "[File Attachment]"
                else -> content
            },
            timestamp = message.timestamp,
            unreadDelta = 0
        )
        return message
    }

    suspend fun receiveMessage(message: MessageEntity) {
        // Prevent duplicate messages
        val existing = messageDao.getMessageById(message.id)
        if (existing != null) return

        messageDao.insertMessage(message)

        // Ensure conversation exists
        val conv = conversationDao.getConversationById(message.conversationId)
        if (conv == null) {
            val newConv = ConversationEntity(
                id = message.conversationId,
                title = message.senderName,
                isGroup = false,
                participantAddresses = message.senderAddress,
                lastMessageText = message.content,
                lastMessageTimestamp = message.timestamp,
                unreadCount = 1
            )
            conversationDao.insertOrUpdate(newConv)
        } else {
            conversationDao.updateLastMessage(
                id = message.conversationId,
                text = when (message.type) {
                    MessageType.VOICE -> "[Voice Transmission]"
                    MessageType.IMAGE -> "[Image Transmission]"
                    MessageType.FILE -> "[File Attachment]"
                    else -> message.content
                },
                timestamp = message.timestamp,
                unreadDelta = 1
            )
        }
    }

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status)
    }

    suspend fun toggleMessagePin(messageId: String, pinned: Boolean) {
        messageDao.setMessagePinned(messageId, pinned)
    }

    suspend fun deleteMessageLocally(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun clearConversation(conversationId: String) {
        messageDao.deleteMessagesForConversation(conversationId)
        conversationDao.updateLastMessage(conversationId, "Chat history cleared", System.currentTimeMillis(), 0)
    }

    suspend fun clearAllHistory() {
        messageDao.deleteAllMessages()
        conversationDao.deleteAllConversations()
        fileTransferDao.deleteAllTransfers()
    }

    suspend fun markConversationRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun setConversationPinned(conversationId: String, pinned: Boolean) {
        conversationDao.setPinned(conversationId, pinned)
    }

    suspend fun recordFileTransfer(transfer: FileTransferEntity) {
        fileTransferDao.insertOrUpdate(transfer)
    }

    suspend fun updateTransferProgress(id: String, bytes: Long, status: TransferStatus) {
        fileTransferDao.updateProgress(id, bytes, status)
    }

    suspend fun deleteTransfer(id: String) {
        fileTransferDao.deleteTransfer(id)
    }
}
