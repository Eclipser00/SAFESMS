package com.safesms.domain.model

/**
 * Entidad de dominio para contactos
 */
data class Contact(
    val id: Long,
    val phoneNumber: String,
    val displayName: String
)

