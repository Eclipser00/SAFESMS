package com.safesms.domain.model

/**
 * Información de progreso de importación de SMS
 */
data class ImportProgress(
    val totalMessages: Int,
    val importedMessages: Int,
    val totalChats: Int,
    val importedChats: Int,
    val isCompleted: Boolean,
    val error: Throwable? = null
) {
    /**
     * Porcentaje de progreso (0-100)
     */
    val progressPercentage: Int
        get() = if (totalMessages > 0) {
            (importedMessages * 100 / totalMessages).coerceIn(0, 100)
        } else {
            0
        }
}

