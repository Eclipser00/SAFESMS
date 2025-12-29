package com.safesms.data.local.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver para recepción de MMS
 * Componente requerido para ser app SMS por defecto en Android
 * 
 * Nota: SafeSMS v1.0 se enfoca en SMS. Este receiver es un stub
 * necesario para cumplir con los requisitos del sistema.
 */
class MmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.provider.Telephony.WAP_PUSH_DELIVER" -> {
                Log.d("MmsReceiver", "MMS recibido (no procesado en esta versión)")
                // TODO: Implementar procesamiento de MMS en versión futura
            }
        }
    }
}

