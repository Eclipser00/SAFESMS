package com.safesms.data.local.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager

/**
 * BroadcastReceiver para recepción de SMS en tiempo real
 * Dispara un Worker para procesar el SMS usando Hilt
 */
class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("SmsReceiver", "onReceive llamado con acción: $action")
        
        // Determinar si somos la app de SMS por defecto
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val currentPackage = context.packageName
        val isDefaultSmsApp = currentPackage == defaultSmsPackage
        Log.d("SmsReceiver", "Package actual: $currentPackage")
        Log.d("SmsReceiver", "Package SMS por defecto: $defaultSmsPackage")
        Log.d("SmsReceiver", "Es app por defecto: $isDefaultSmsApp")

        // Lógica para evitar duplicados:
        // 1. Si somos la app por defecto, Android nos envía SMS_DELIVER_ACTION de forma exclusiva.
        //    También se envía SMS_RECEIVED_ACTION a todas las apps con permiso, incluida la nuestra.
        //    Para evitar duplicados, si somos la app por defecto, ignoramos SMS_RECEIVED_ACTION.
        // 2. Si NO somos la app por defecto, solo recibiremos SMS_RECEIVED_ACTION.
        
        val shouldProcess = when (action) {
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> true
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> !isDefaultSmsApp
            else -> false
        }

        if (shouldProcess) {
            Log.d("SmsReceiver", "Procesando broadcast: $action")
            val smsData = extractSmsFromIntent(intent)
            
            if (smsData != null) {
                Log.d("SmsReceiver", "SMS extraído exitosamente - Remitente: ${smsData.address}, Cuerpo: ${smsData.body.take(50)}...")
                
                // Usar goAsync() para procesamiento asíncrono
                val pendingResult = goAsync()
                
                try {
                    // Crear WorkRequest para procesar SMS en background
                    val inputData = Data.Builder()
                        .putString(SmsProcessingWorker.KEY_ADDRESS, smsData.address)
                        .putString(SmsProcessingWorker.KEY_BODY, smsData.body)
                        .putLong(SmsProcessingWorker.KEY_TIMESTAMP, smsData.timestamp)
                        .build()
                    
                    // Generar nombre único para el Work basado en address + timestamp
                    // Esto evita que múltiples broadcasts (SMS_DELIVER + SMS_RECEIVED) 
                    // procesen el mismo SMS dos veces (común con números cortos)
                    val uniqueWorkName = "sms_${smsData.address}_${smsData.timestamp}"
                    
                    val workRequest = OneTimeWorkRequestBuilder<SmsProcessingWorker>()
                        .setInputData(inputData)
                        .build()
                    
                    Log.d("SmsReceiver", "Encolando WorkRequest para procesar SMS (nombre único: $uniqueWorkName)...")
                    val workManager = WorkManager.getInstance(context)
                    
                    // ExistingWorkPolicy.KEEP: Si ya existe un trabajo con este nombre,
                    // se mantiene el existente y se ignora el nuevo (evita duplicados)
                    workManager.enqueueUniqueWork(
                        uniqueWorkName,
                        androidx.work.ExistingWorkPolicy.KEEP,
                        workRequest
                    )
                    
                    pendingResult.finish()
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error al encolar WorkRequest", e)
                    e.printStackTrace()
                    pendingResult.finish()
                }
            } else {
                Log.w("SmsReceiver", "No se pudo extraer datos del SMS del Intent")
            }
        } else {
            Log.d("SmsReceiver", "Ignorando acción: $action (isDefaultSmsApp=$isDefaultSmsApp)")
        }
    }
    
    /**
     * Extrae datos del SMS del Intent
     */
    private fun extractSmsFromIntent(intent: Intent): SmsData? {
        return try {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (smsMessages.isNotEmpty()) {
                val smsMessage = smsMessages[0]
                SmsData(
                    address = smsMessage.displayOriginatingAddress ?: "",
                    body = smsMessage.messageBody ?: "",
                    timestamp = smsMessage.timestampMillis
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error al extraer SMS del Intent", e)
            null
        }
    }
    
    /**
     * Data class para datos de SMS extraídos
     */
    data class SmsData(
        val address: String,
        val body: String,
        val timestamp: Long
    )
}
