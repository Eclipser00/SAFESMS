package com.safesms.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo Hilt para Use Cases
 * Los Use Cases se inyectan automáticamente mediante @Inject constructor
 * Este módulo está aquí por si necesitamos proveer dependencias adicionales en el futuro
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Los Use Cases se inyectan automáticamente con Hilt
    // No necesitan providers explícitos ya que tienen @Inject constructor
}
