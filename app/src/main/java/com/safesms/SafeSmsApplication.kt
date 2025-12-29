package com.safesms

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Punto de entrada de la aplicación
 * Inicializa Hilt, WorkManager y configura AdMob
 */
@HiltAndroidApp
class SafeSmsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        Log.d("SafeSmsApplication", "onCreate iniciado")
        
        // CRÍTICO: Inicializar WorkManager ANTES de cualquier otra cosa
        // para asegurar que use nuestra configuración con HiltWorkerFactory
        try {
            // Esperar un momento para que Hilt termine de inyectar
            // En realidad, Hilt debería estar listo en este punto porque @HiltAndroidApp
            // se procesa antes de onCreate()
            
            if (::workerFactory.isInitialized) {
                Log.d("SafeSmsApplication", "workerFactory está inicializado, configurando WorkManager")
                
                val config = Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
                
                // Intentar inicializar WorkManager con nuestra configuración
                try {
                    WorkManager.initialize(this, config)
                    Log.d("SafeSmsApplication", "✓ WorkManager inicializado exitosamente con HiltWorkerFactory")
                } catch (e: IllegalStateException) {
                    Log.w("SafeSmsApplication", "WorkManager ya estaba inicializado: ${e.message}")
                    // Si ya está inicializado, necesitamos reiniciar la app o limpiar WorkManager
                    Log.e("SafeSmsApplication", "ERROR: WorkManager se inicializó antes de nuestra configuración. Reinicia la app.")
                }
            } else {
                Log.e("SafeSmsApplication", "ERROR: workerFactory NO está inicializado en onCreate()")
                Log.e("SafeSmsApplication", "Esto puede causar que WorkManager use el factory por defecto")
            }
        } catch (e: Exception) {
            Log.e("SafeSmsApplication", "Excepción al inicializar WorkManager", e)
            e.printStackTrace()
        }
        
        // Inicializar AdMob de forma segura
        try {
            MobileAds.initialize(this) { initializationStatus ->
                // AdMob inicializado
            }
        } catch (e: Exception) {
            // Si AdMob falla, la app puede continuar funcionando
            // Solo se perderá la monetización
            e.printStackTrace()
        }
        
        Log.d("SafeSmsApplication", "onCreate completado")
    }

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("SafeSmsApplication", "getWorkManagerConfiguration() llamado")
            Log.d("SafeSmsApplication", "workerFactory inicializado: ${::workerFactory.isInitialized}")
            
            if (!::workerFactory.isInitialized) {
                Log.e("SafeSmsApplication", "ERROR CRÍTICO: HiltWorkerFactory no está disponible")
                throw IllegalStateException(
                    "HiltWorkerFactory no ha sido inyectado. " +
                    "Esto puede ocurrir si WorkManager se inicializa antes de que Hilt esté listo."
                )
            }
            
            val config = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
            
            Log.d("SafeSmsApplication", "✓ Configuración de WorkManager creada con HiltWorkerFactory")
            return config
        }
}
