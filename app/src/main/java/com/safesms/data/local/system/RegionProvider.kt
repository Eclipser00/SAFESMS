package com.safesms.data.local.system

import android.content.Context
import android.telephony.TelephonyManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provee la región por defecto para normalización de números de teléfono.
 * 
 * La región se obtiene desde:
 * 1. La SIM activa del dispositivo (si hay múltiples SIM, detecta por cuál llegó el SMS)
 * 2. La red móvil del dispositivo
 * 3. El locale del sistema como fallback
 * 
 * Por qué usar la región de la SIM:
 * - Los números sin prefijo internacional (+) necesitan contexto regional para parsearse correctamente
 * - Ejemplo: "600123456" en España es "+34600123456", pero en otro país sería diferente
 * - La SIM del dispositivo indica la región del usuario, no el locale del sistema
 */
@Singleton
class RegionProvider @Inject constructor(
    private val context: Context
) {
    
    /**
     * Obtiene el código de región ISO 3166-1 alpha-2 (ej: "ES", "US", "MX")
     * desde la SIM activa o la red del dispositivo.
     * 
     * @return Código de región de 2 letras, o "ES" como fallback si no se puede determinar
     */
    fun getDefaultRegion(): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return getFallbackRegion()
            
            // Intentar obtener desde la SIM activa
            val simCountryIso = telephonyManager.simCountryIso
            
            if (!simCountryIso.isNullOrBlank()) {
                return simCountryIso.uppercase()
            }
            
            // Si no hay SIM o no se puede obtener, intentar desde la red
            val networkCountryIso = telephonyManager.networkCountryIso
            
            if (!networkCountryIso.isNullOrBlank()) {
                return networkCountryIso.uppercase()
            }
            
            // Fallback: usar locale del sistema
            getFallbackRegion()
            
        } catch (e: Exception) {
            // Si hay cualquier error (permisos, etc.), usar fallback
            getFallbackRegion()
        }
    }
    
    /**
     * Obtiene la región desde el locale del sistema como fallback
     */
    private fun getFallbackRegion(): String {
        val locale = context.resources.configuration.locales[0]
        val country = locale.country
        
        return if (country.isNotBlank()) {
            country.uppercase()
        } else {
            // Fallback final: España (común en el mercado objetivo)
            "ES"
        }
    }
    
    /**
     * Obtiene la región para una SIM específica (útil cuando hay múltiples SIM)
     * 
     * @param slotIndex Índice de la ranura SIM (0 para primera SIM, 1 para segunda)
     * @return Código de región o fallback
     */
    fun getRegionForSimSlot(slotIndex: Int): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return getFallbackRegion()
            
            // Android API 26+ soporta múltiples SIM
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val subscriptionManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    // API 33+: usar getSystemService directamente
                    context.getSystemService(android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
                } else {
                    // API 26-32: usar método deprecated pero funcional
                    @Suppress("DEPRECATION")
                    android.telephony.SubscriptionManager.from(context)
                }
                
                val subscriptionInfoList = subscriptionManager?.activeSubscriptionInfoList
                
                if (subscriptionInfoList != null && slotIndex < subscriptionInfoList.size) {
                    val countryIso = subscriptionInfoList[slotIndex].countryIso
                    if (!countryIso.isNullOrBlank()) {
                        return countryIso.uppercase()
                    }
                }
            }
            
            // Si no se puede obtener para slot específico, usar método general
            getDefaultRegion()
            
        } catch (e: Exception) {
            getFallbackRegion()
        }
    }
}

