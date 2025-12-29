package com.safesms.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.safesms.data.local.database.SafeSmsDatabase
import com.safesms.data.local.system.ContactsSystemProvider
import com.safesms.data.local.system.SmsSystemProvider
import com.safesms.data.repository.BlockedSenderRepositoryImpl
import com.safesms.data.repository.ChatRepositoryImpl
import com.safesms.data.repository.ContactRepositoryImpl
import com.safesms.data.repository.MessageRepositoryImpl
import com.safesms.domain.usecase.chat.ClassifyChatUseCase
import com.safesms.domain.usecase.message.ReceiveMessageUseCase
import com.safesms.domain.usecase.message.SendMessageUseCase
import com.safesms.presentation.util.NotificationManager
import com.safesms.util.NormalizedResult
import com.safesms.util.PhoneNormalizer
import com.safesms.util.ThreadIdHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
 * Tests de integración para el flujo de SMS.
 * 
 * Verifica que la lógica de "Crear chat -> Guardar mensaje" funciona correctamente
 * y que la información del contacto se vincula bien.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SmsFlowIntegrationTest {

    private lateinit var database: SafeSmsDatabase
    private lateinit var chatRepository: ChatRepositoryImpl
    private lateinit var contactRepository: ContactRepositoryImpl
    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var receiveMessageUseCase: ReceiveMessageUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var contactsSystemProvider: ContactsSystemProvider
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SafeSmsDatabase::class.java
        ).allowMainThreadQueries().build()
        
        val smsSystemProvider = mockk<SmsSystemProvider>(relaxed = true)
        contactsSystemProvider = mockk<ContactsSystemProvider>(relaxed = true)
        val phoneNormalizer = mockk<PhoneNormalizer>()
        
        every { phoneNormalizer.normalizePhone(any()) } answers {
            val address = firstArg<String>()
            val cleaned = address.replace(Regex("[^0-9+]"), "")
            if (cleaned.startsWith("+") || cleaned.length > 7) {
                NormalizedResult("E164", if(cleaned.startsWith("+")) cleaned else "+34$cleaned", address, cleaned, "ES")
            } else {
                NormalizedResult("SHORT_OR_NONPHONE", cleaned, address, cleaned, null)
            }
        }
        
        messageRepository = MessageRepositoryImpl(database.messageDao(), smsSystemProvider)
        chatRepository = ChatRepositoryImpl(context, database.chatDao(), database.contactDao())
        contactRepository = ContactRepositoryImpl(database.contactDao(), contactsSystemProvider, phoneNormalizer)
        
        val classifyChatUseCase = ClassifyChatUseCase(contactRepository, phoneNormalizer)
        val configurationRepository = mockk<com.safesms.domain.repository.ConfigurationRepository>(relaxed = true)
        every { configurationRepository.getQuarantineNotificationsEnabled() } returns kotlinx.coroutines.flow.flowOf(true)

        receiveMessageUseCase = ReceiveMessageUseCase(
            context,
            messageRepository,
            chatRepository,
            classifyChatUseCase,
            BlockedSenderRepositoryImpl(database.blockedSenderDao()),
            configurationRepository,
            mockk(relaxed = true)
        )
        
        sendMessageUseCase = SendMessageUseCase(context, messageRepository, chatRepository)

        mockkObject(ThreadIdHelper)
        every { ThreadIdHelper.getOrCreateThreadId(any(), any()) } answers {
            val addr = secondArg<String>()
            addr.hashCode().toLong().let { if (it < 0) -it else it }
        }
    }

    @After
    fun tearDown() {
        database.close()
        io.mockk.unmockkAll()
    }

    @Test
    fun `recibir mensaje de un contacto guardado crea chat de Inbox con nombre`() = runTest {
        // Given
        val address = "+34611223344"
        coEvery { contactsSystemProvider.getAllContacts() } returns listOf(
            ContactsSystemProvider.SystemContact(id = 1, phoneNumber = address, displayName = "Ricardo")
        )
        contactRepository.syncContacts()

        // When
        receiveMessageUseCase(address, "Hola amigo", System.currentTimeMillis())

        // Then
        val threadId = ThreadIdHelper.getOrCreateThreadId(context, address)
        val chat = database.chatDao().getChatByThreadId(threadId)
        
        assertNotNull("El chat debería haberse creado", chat)
        assertEquals("Ricardo", chat?.contactName)
        assertTrue("Debería ser un chat de Inbox", chat?.isInboxChat == true)
        
        val messages = database.messageDao().getMessagesByThreadId(threadId).first()
        assertEquals(1, messages.size)
        assertEquals("Hola amigo", messages[0].body)
    }

    @Test
    fun `recibir mensaje de desconocido crea chat de Cuarentena`() = runTest {
        // Given
        val address = "+34699001122"
        coEvery { contactsSystemProvider.getAllContacts() } returns emptyList()
        contactRepository.syncContacts()

        // When
        receiveMessageUseCase(address, "Spam peligroso", System.currentTimeMillis())

        // Then
        val threadId = ThreadIdHelper.getOrCreateThreadId(context, address)
        val chat = database.chatDao().getChatByThreadId(threadId)
        
        assertNotNull(chat)
        assertNull(chat?.contactName)
        assertFalse("Debería ser de Cuarentena", chat?.isInboxChat == true)
    }

    @Test
    fun `enviar mensaje crea el chat antes de insertar el mensaje`() = runTest {
        // Given
        val address = "+34655443322"
        val threadId = ThreadIdHelper.getOrCreateThreadId(context, address)

        // When
        sendMessageUseCase(threadId, address, "Test outgoing")

        // Then
        val chat = database.chatDao().getChatByThreadId(threadId)
        assertNotNull(chat)
        
        val messages = database.messageDao().getMessagesByThreadId(threadId).first()
        assertEquals(1, messages.size)
        assertEquals("Test outgoing", messages[0].body)
    }

    @Test
    fun `un chat existente se actualiza con el nombre si el contacto se guarda despues`() = runTest {
        // 1. Recibe mensaje de desconocido
        val address = "+34611111111"
        receiveMessageUseCase(address, "Msg 1", 1000L)
        
        val threadId = ThreadIdHelper.getOrCreateThreadId(context, address)
        var chat = database.chatDao().getChatByThreadId(threadId)
        assertNull(chat?.contactName)
        assertFalse(chat?.isInboxChat == true)

        // 2. Guardamos al contacto en la agenda y sincronizamos
        coEvery { contactsSystemProvider.getAllContacts() } returns listOf(
            ContactsSystemProvider.SystemContact(id = 2, phoneNumber = address, displayName = "Nuevo Amigo")
        )
        contactRepository.syncContacts()

        // 3. Recibe segundo mensaje
        receiveMessageUseCase(address, "Msg 2", 2000L)

        // 4. El chat debería haberse actualizado con el nombre
        chat = database.chatDao().getChatByThreadId(threadId)
        assertEquals("Nuevo Amigo", chat?.contactName)
        assertTrue(chat?.isInboxChat == true)
        assertEquals("Msg 2", chat?.lastMessageBody)
    }
}
