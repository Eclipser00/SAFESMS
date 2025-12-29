package com.safesms.data.local.system

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safesms.domain.usecase.message.ReceiveMessageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para procesar SMS recibidos en background
 */
@HiltWorker
class SmsProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val receiveMessageUseCase: ReceiveMessageUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SmsProcessingWorker", "Iniciando procesamiento de SMS...")
            
            val address = inputData.getString(KEY_ADDRESS)
            val body = inputData.getString(KEY_BODY)
            val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

            if (address == null || body == null) {
                Log.e("SmsProcessingWorker", "Datos incompletos - address: $address, body: ${body?.take(20)}")
                return Result.failure()
            }

            Log.d("SmsProcessingWorker", "Procesando SMS de: $address")
            val result = receiveMessageUseCase(address, body, timestamp)
            
            when (result) {
                is com.safesms.util.Result.Success -> {
                    Log.d("SmsProcessingWorker", "SMS procesado exitosamente")
                    Result.success()
                }
                is com.safesms.util.Result.Error -> {
                    Log.e("SmsProcessingWorker", "Error al procesar SMS: ${result.exception.message}", result.exception)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("SmsProcessingWorker", "Excepción no manejada al procesar SMS", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_ADDRESS = "address"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
    }
}

