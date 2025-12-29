package com.safesms.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log

/**
 * Helper para obtener thread_id del sistema Android.
 * 
 * Esta es la función CENTRAL que reemplaza toda normalización manual.
 * Delegamos al sistema Android la tarea de determinar qué números
 * pertenecen al mismo chat/thread.
 * 
 * BENEFICIOS:
 * - Consistencia con el comportamiento nativo de Android
 * - No necesitamos mantener lógica compleja de normalización
 * - Android maneja automáticamente variaciones del mismo número
 * - Funciona con números normales, short codes y alfanuméricos
 */
object ThreadIdHelper {
    
    private const val TAG = "ThreadIdHelper"
    
    /**
     * Obtiene o crea el thread_id para una dirección.
     * 
     * Esta función utiliza el sistema de threads de Android (Telephony Provider)
     * para obtener un identificador único que agrupa mensajes de la misma conversación.
     * 
     * Android automáticamente considera que diferentes formatos del mismo número
     * pertenecen al mismo thread:
     * - "+34600123456" y "600123456" -> mismo thread_id
     * - "600 123 456" y "600-123-456" -> mismo thread_id
     * 
     * @param context Contexto de Android
     * @param address Dirección del SMS (número de teléfono, short code o alfanumérico)
     * @return thread_id único para esta conversación
     * @throws IllegalStateException si no se puede crear el thread_id
     */
    fun getOrCreateThreadId(context: Context, address: String): Long {
        return try {
            // Limpiar prefijos internos si existen (de sistema antiguo)
            val cleanAddress = address
                .removePrefix("short:")
                .removePrefix("alpha:")
                .removePrefix("raw:")
                .trim()
            
            // Usar API oficial de Android para obtener/crear thread_id
            // Esta es la misma función que usa la app de SMS nativa
            Telephony.Threads.getOrCreateThreadId(
                context,
                setOf(cleanAddress)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating thread_id for address: $address", e)
            throw IllegalStateException("Cannot create thread_id for address: $address", e)
        }
    }
    
    /**
     * Obtiene thread_id si existe, null si no existe.
     * 
     * A diferencia de getOrCreateThreadId, esta función NO crea un nuevo thread
     * si no existe. Útil para verificar si ya hay una conversación existente.
     * 
     * @param context Contexto de Android
     * @param address Dirección del SMS
     * @return thread_id si existe, null si no existe
     */
    fun getThreadIdIfExists(context: Context, address: String): Long? {
        val cleanAddress = address
            .removePrefix("short:")
            .removePrefix("alpha:")
            .removePrefix("raw:")
            .trim()
        
        return try {
            // Consultar el ContentProvider de threads
            val uri = Uri.parse("content://mms-sms/threadID").buildUpon()
                .appendQueryParameter("recipient", cleanAddress)
                .build()
            
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(0)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying thread_id for address: $address", e)
            null
        }
    }
    
    /**
     * Obtiene todas las direcciones/participantes de un thread.
     * 
     * Útil para mostrar todos los participantes en la UI o para debug.
     * Normalmente un thread SMS tiene un solo participante, pero MMS
     * puede tener múltiples.
     * 
     * @param context Contexto de Android
     * @param threadId ID del thread
     * @return Lista de direcciones participantes en el thread
     */
    fun getAddressesForThread(context: Context, threadId: Long): List<String> {
        val addresses = mutableListOf<String>()
        
        return try {
            // Consultar los participantes del thread
            val uri = Uri.parse("content://mms-sms/conversations/$threadId/recipients")
            
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val addressColumn = cursor.getColumnIndex("address")
                if (addressColumn >= 0) {
                    while (cursor.moveToNext()) {
                        val address = cursor.getString(addressColumn)
                        if (!address.isNullOrBlank()) {
                            addresses.add(address)
                        }
                    }
                }
            }
            addresses
        } catch (e: Exception) {
            Log.e(TAG, "Error getting addresses for thread: $threadId", e)
            addresses
        }
    }
    
    /**
     * Obtiene la dirección principal de un thread.
     * 
     * Para SMS (1 participante), retorna esa dirección.
     * Para MMS (múltiples), retorna la primera o una representación.
     * 
     * @param context Contexto de Android
     * @param threadId ID del thread
     * @return Dirección principal o "Unknown" si no se encuentra
     */
    fun getPrimaryAddressForThread(context: Context, threadId: Long): String {
        val addresses = getAddressesForThread(context, threadId)
        return when {
            addresses.isEmpty() -> "Unknown"
            addresses.size == 1 -> addresses[0]
            else -> addresses.joinToString(", ")
        }
    }
    
    /**
     * Limpia una dirección eliminando prefijos del sistema antiguo.
     * 
     * Útil durante la migración para limpiar direcciones que puedan
     * tener los prefijos antiguos (short:, alpha:, raw:).
     * 
     * @param address Dirección posiblemente con prefijo
     * @return Dirección limpia sin prefijos
     */
    fun cleanAddress(address: String): String {
        return address
            .removePrefix("short:")
            .removePrefix("alpha:")
            .removePrefix("raw:")
            .trim()
    }
}

