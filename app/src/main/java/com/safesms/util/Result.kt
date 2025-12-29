package com.safesms.util

/**
 * Sealed class para manejo de resultados de operaciones
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error
    
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception)
    }
    
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }
}

fun <T> Result<T>.getOrElse(defaultValue: T): T = when (this) {
    is Result.Success -> data
    is Result.Error -> defaultValue
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * Ejecuta un bloque de código suspendible y captura cualquier excepción,
 * devolviendo un Result.Success o Result.Error
 */
suspend inline fun <T> runCatching(crossinline block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(e)
}