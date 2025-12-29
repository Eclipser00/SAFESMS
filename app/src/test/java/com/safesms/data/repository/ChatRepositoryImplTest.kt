package com.safesms.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.safesms.data.local.database.SafeSmsDatabase
import com.safesms.data.local.database.dao.ChatDao
import com.safesms.data.local.database.dao.ContactDao
import com.safesms.data.local.database.entities.ContactEntity
import com.safesms.domain.model.Message
import com.safesms.domain.model.MessageType
import com.safesms.domain.model.MessageStatus
import com.safesms.util.ThreadIdHelper
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests para ChatRepositoryImpl usando Room in-memory database
 * 
 * ACTUALIZADO: Usa threadId del sistema Android
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ChatRepositoryImplTest {

    private lateinit var database: SafeSmsDatabase
    private lateinit var chatDao: ChatDao
    private lateinit var contactDao: ContactDao
    private lateinit var context: Context
    private lateinit var chatRepository: ChatRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SafeSmsDatabase::class.java
        ).allowMainThreadQueries().build()
        
        chatDao = database.chatDao()
        contactDao = database.contactDao()
        
        chatRepository = ChatRepositoryImpl(context, chatDao, contactDao)
    }

    @After
    fun tearDown() {
        database.close()
        io.mockk.unmockkAll()
    }

    @Test
    fun `crea nuevo chat de Inbox correctamente`() = runTest {
        // Given
        val address = "+34612345678"
        val messageBody = "Hola, soy Juan"
        val timestamp = System.currentTimeMillis()
        val threadId = 1L
        
        // Agregar contacto para que sea Inbox
        contactDao.insertContacts(listOf(
            ContactEntity(
                id = 0,
                phoneNumber = "34612345678",
                displayName = "Juan",
                syncTimestamp = System.currentTimeMillis()
            )
        ))

        // Crear mensaje
        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = messageBody,
            timestamp = timestamp,
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )

        // When
        val result = chatRepository.createOrUpdateChat(
            threadId = threadId,
            address = address,
            lastMessage = message
        )

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        val returnedThreadId = (result as com.safesms.util.Result.Success).data
        assertEquals(threadId, returnedThreadId)
        
        // Verificar que se creó el chat
        val inboxChats = chatRepository.getInboxChats().first()
        assertEquals(1, inboxChats.size)
        assertEquals("Juan", inboxChats[0].contactName)
        assertEquals(messageBody, inboxChats[0].lastMessageBody)
        assertEquals(threadId, inboxChats[0].threadId)
    }

    @Test
    fun `crea nuevo chat de Cuarentena correctamente`() = runTest {
        // Given
        val address = "+34999999999"
        val messageBody = "Mensaje sospechoso"
        val timestamp = System.currentTimeMillis()
        val threadId = 2L
        
        // Crear mensaje
        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = messageBody,
            timestamp = timestamp,
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )

        // When
        val result = chatRepository.createOrUpdateChat(
            threadId = threadId,
            address = address,
            lastMessage = message
        )

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        
        // Verificar que se creó en Cuarentena
        val quarantineChats = chatRepository.getQuarantineChats().first()
        assertEquals(1, quarantineChats.size)
        assertNull(quarantineChats[0].contactName)
        assertEquals(messageBody, quarantineChats[0].lastMessageBody)
        assertEquals(threadId, quarantineChats[0].threadId)
    }

    @Test
    fun `actualiza chat existente con nuevo mensaje`() = runTest {
        // Given
        val address = "+34612345678"
        val threadId = 3L
        val firstMessage = "Primer mensaje"
        val firstTimestamp = System.currentTimeMillis()
        
        // Agregar contacto para que sea Inbox
        contactDao.insertContacts(listOf(
            ContactEntity(id = 0, phoneNumber = "34612345678", displayName = "Juan", syncTimestamp = System.currentTimeMillis())
        ))
        
        // Crear mensaje inicial
        val firstMsg = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = firstMessage,
            timestamp = firstTimestamp,
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        // Crear chat inicial
        chatRepository.createOrUpdateChat(
            threadId = threadId,
            address = address,
            lastMessage = firstMsg
        )

        // When - Enviar segundo mensaje al mismo chat
        val secondMessage = "Segundo mensaje"
        val secondTimestamp = System.currentTimeMillis() + 1000
        val secondMsg = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = secondMessage,
            timestamp = secondTimestamp,
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        val result = chatRepository.createOrUpdateChat(
            threadId = threadId,
            address = address,
            lastMessage = secondMsg
        )

        // Then
        assertTrue(result is com.safesms.util.Result.Success)
        
        // Verificar que solo hay un chat y se actualizó
        val inboxChats = chatRepository.getInboxChats().first()
        assertEquals(1, inboxChats.size)
        assertEquals(secondMessage, inboxChats[0].lastMessageBody)
        assertEquals(secondTimestamp, inboxChats[0].lastMessageTimestamp)
    }

    @Test
    fun `obtiene chats de Inbox correctamente`() = runTest {
        // Given
        val address1 = "+34612345678"
        val address2 = "+34612345679"
        val threadId1 = 4L
        val threadId2 = 5L
        
        // Agregar contactos
        contactDao.insertContacts(listOf(
            ContactEntity(id = 0, phoneNumber = "34612345678", displayName = "Juan", syncTimestamp = System.currentTimeMillis()),
            ContactEntity(id = 0, phoneNumber = "34612345679", displayName = "María", syncTimestamp = System.currentTimeMillis())
        ))
        
        val message1 = Message(
            id = 0,
            chatThreadId = threadId1,
            address = address1,
            body = "Mensaje 1",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        val message2 = Message(
            id = 0,
            chatThreadId = threadId2,
            address = address2,
            body = "Mensaje 2",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        chatRepository.createOrUpdateChat(threadId1, address1, message1)
        chatRepository.createOrUpdateChat(threadId2, address2, message2)

        // When
        val inboxChats = chatRepository.getInboxChats().first()

        // Then
        assertEquals(2, inboxChats.size)
        assertTrue(inboxChats.all { it.chatType == com.safesms.domain.model.ChatType.INBOX })
    }

    @Test
    fun `obtiene chats de Cuarentena correctamente`() = runTest {
        // Given
        val address1 = "+34999999999"
        val address2 = "BANCO123"
        val threadId1 = 6L
        val threadId2 = 7L
        
        val message1 = Message(
            id = 0,
            chatThreadId = threadId1,
            address = address1,
            body = "Mensaje sospechoso 1",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        val message2 = Message(
            id = 0,
            chatThreadId = threadId2,
            address = address2,
            body = "Mensaje sospechoso 2",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        chatRepository.createOrUpdateChat(threadId1, address1, message1)
        chatRepository.createOrUpdateChat(threadId2, address2, message2)

        // When
        val quarantineChats = chatRepository.getQuarantineChats().first()

        // Then
        assertEquals(2, quarantineChats.size)
        assertTrue(quarantineChats.all { it.chatType == com.safesms.domain.model.ChatType.QUARANTINE })
    }

    @Test
    fun `obtiene chat por threadId correctamente`() = runTest {
        // Given
        val address = "+34612345678"
        val threadId = 8L
        
        
        // Agregar contacto
        contactDao.insertContacts(listOf(
            ContactEntity(id = 0, phoneNumber = "34612345678", displayName = "Juan", syncTimestamp = System.currentTimeMillis())
        ))
        
        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = "Mensaje",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        chatRepository.createOrUpdateChat(threadId, address, message)

        // When
        val chat = chatRepository.getChatByThreadId(threadId)

        // Then
        assertNotNull(chat)
        assertEquals(threadId, chat?.threadId)
        assertEquals(address, chat?.address)
        assertEquals("Juan", chat?.contactName)
    }

    @Test
    fun `retorna null cuando chat no existe por threadId`() = runTest {
        // Given
        val threadId = 999L

        // When
        val chat = chatRepository.getChatByThreadId(threadId)

        // Then
        assertNull(chat)
    }

    @Test
    fun `obtiene chat por threadId Flow correctamente`() = runTest {
        // Given
        val address = "+34612345678"
        val threadId = 9L
        
        
        // Agregar contacto
        contactDao.insertContacts(listOf(
            ContactEntity(id = 0, phoneNumber = "34612345678", displayName = "Juan", syncTimestamp = System.currentTimeMillis())
        ))
        
        val message = Message(
            id = 0,
            chatThreadId = threadId,
            address = address,
            body = "Mensaje",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        chatRepository.createOrUpdateChat(threadId, address, message)

        // When
        val chat = chatRepository.getChatByThreadIdFlow(threadId).first()

        // Then
        assertNotNull(chat)
        assertEquals(threadId, chat?.threadId)
        assertEquals("Juan", chat?.contactName)
    }

    @Test
    fun `separa chats de Inbox y Cuarentena correctamente`() = runTest {
        // Given
        val threadId1 = 10L
        val threadId2 = 11L
        val threadId3 = 12L
        
        // Agregar contactos para los dos primeros
        contactDao.insertContacts(listOf(
            ContactEntity(id = 0, phoneNumber = "34612345678", displayName = "Juan", syncTimestamp = System.currentTimeMillis()),
            ContactEntity(id = 0, phoneNumber = "34612345679", displayName = "María", syncTimestamp = System.currentTimeMillis())
        ))
        
        val message1 = Message(
            id = 0,
            chatThreadId = threadId1,
            address = "+34612345678",
            body = "Inbox 1",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        val message2 = Message(
            id = 0,
            chatThreadId = threadId2,
            address = "+34999999999",
            body = "Cuarentena 1",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        val message3 = Message(
            id = 0,
            chatThreadId = threadId3,
            address = "+34612345679",
            body = "Inbox 2",
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED,
            status = MessageStatus.RECEIVED,
            isRead = false
        )
        
        chatRepository.createOrUpdateChat(threadId1, "+34612345678", message1)
        chatRepository.createOrUpdateChat(threadId2, "+34999999999", message2)
        chatRepository.createOrUpdateChat(threadId3, "+34612345679", message3)

        // When
        val inboxChats = chatRepository.getInboxChats().first()
        val quarantineChats = chatRepository.getQuarantineChats().first()

        // Then
        assertEquals(2, inboxChats.size)
        assertEquals(1, quarantineChats.size)
        assertTrue(inboxChats.all { it.contactName != null })
        assertTrue(quarantineChats.all { it.contactName == null })
    }
}

