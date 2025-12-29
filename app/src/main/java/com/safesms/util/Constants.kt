package com.safesms.util

object Constants {
    const val MIN_COUNTDOWN_SECONDS = 3
    const val MAX_COUNTDOWN_SECONDS = 8
    const val DEFAULT_COUNTDOWN_SECONDS = 5
    
    const val SMS_TYPE_RECEIVED = 1
    const val SMS_TYPE_SENT = 2
    
    const val NOTIFICATION_CHANNEL_INBOX = "inbox_channel"
    const val NOTIFICATION_CHANNEL_QUARANTINE = "quarantine_channel"
    
    const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
    
    const val SHORT_CODE_MAX_DIGITS = 6 // Números cortos tienen menos de 6 dígitos
}
