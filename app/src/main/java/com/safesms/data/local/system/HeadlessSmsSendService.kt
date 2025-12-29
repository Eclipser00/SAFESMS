package com.safesms.data.local.system

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service para respuestas rápidas a SMS desde notificaciones
 * Componente requerido para ser app SMS por defecto en Android
 * 
 * Este servicio se invoca cuando el usuario responde a un SMS
 * directamente desde una notificación sin abrir la app.
 */
@AndroidEntryPoint
class HeadlessSmsSendService : Service() {
    
    @Inject
    lateinit var smsSystemProvider: SmsSystemProvider
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == TelephonyManager.ACTION_RESPOND_VIA_MESSAGE) {
            handleQuickResponse(intent)
        }
        return START_NOT_STICKY
    }
    
    private fun handleQuickResponse(intent: Intent) {
        val recipients = intent.getStringArrayExtra("recipients")
        val message = intent.getStringExtra(Intent.EXTRA_TEXT)
        
        if (!recipients.isNullOrEmpty() && !message.isNullOrEmpty()) {
            serviceScope.launch {
                try {
                    val phoneNumber = recipients[0]
                    smsSystemProvider.sendSms(phoneNumber, message)
                    Log.d("HeadlessSmsSendService", "Respuesta rápida enviada a: $phoneNumber")
                } catch (e: Exception) {
                    Log.e("HeadlessSmsSendService", "Error al enviar respuesta rápida", e)
                } finally {
                    stopSelf()
                }
            }
        } else {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

