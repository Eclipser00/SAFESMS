package com.safesms.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para contactos sincronizados
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phoneNumber"], unique = true) // Índice único para búsquedas por teléfono
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val displayName: String,
    val syncTimestamp: Long = System.currentTimeMillis()
)

