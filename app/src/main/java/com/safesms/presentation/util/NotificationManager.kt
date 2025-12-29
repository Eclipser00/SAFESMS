package com.safesms.presentation.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.safesms.R
import com.safesms.presentation.MainActivity
import com.safesms.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper para sistema de notificaciones
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Verifica si tenemos permiso para mostrar notificaciones (Android 13+)
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Crea canales de notificación
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para Inbox
            val inboxChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_INBOX,
                context.getString(R.string.notification_channel_inbox),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_inbox_description)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(inboxChannel)

            // Canal para Cuarentena
            val quarantineChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_QUARANTINE,
                context.getString(R.string.notification_channel_quarantine),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_quarantine_description)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(quarantineChannel)
        }
    }

    /**
     * Muestra notificación normal con texto del mensaje (Inbox).
     * 
     * ACTUALIZADO: Usa threadId en lugar de chatId.
     */
    fun showInboxNotification(
        messageId: Int,
        address: String,
        body: String,
        chatId: Long // threadId
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("threadId", chatId) // Ahora es threadId
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            messageId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_INBOX)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(address)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission()) {
            notificationManager.notify(messageId, notification)
        }
    }

    /**
     * Muestra notificación genérica para Cuarentena.
     * 
     * ACTUALIZADO: Usa threadId en lugar de chatId.
     */
    fun showQuarantineNotification(messageId: Int, chatId: Long) { // threadId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("threadId", chatId) // Ahora es threadId
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            messageId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_QUARANTINE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notification_quarantine_title))
            .setContentText(context.getString(R.string.notification_quarantine_message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission()) {
            notificationManager.notify(messageId, notification)
        }
    }
}

