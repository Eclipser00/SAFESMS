package com.safesms.presentation.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilidades para formateo de fechas en la UI
 */
object DateFormatter {
    
    private val today = Calendar.getInstance()
    private val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    
    /**
     * Formatea timestamp para lista de chats
     * Formato: "Hoy", "Ayer", o "dd/MM"
     */
    fun formatChatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val calendar = Calendar.getInstance().apply {
            time = date
        }
        
        return when {
            isSameDay(calendar, today) -> "Hoy"
            isSameDay(calendar, yesterday) -> "Ayer"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    }
    
    /**
     * Formatea timestamp para mensaje individual
     * Formato: "HH:mm"
     */
    fun formatMessageTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

