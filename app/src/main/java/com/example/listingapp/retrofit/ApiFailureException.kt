package com.example.listingapp.retrofit

import android.text.TextUtils

/**
 * Exception to manage the Network call failures
 * @param message failure msg
 * @param cause throwable
 * @param code is error code of the exception
 * */
open class ApiFailureException(message: String? = null, cause: Throwable? = null, code: Int?) :
    Exception(message) {
    private val _message: String? by lazy {
        if (!TextUtils.isEmpty(message)) {
            cause?.toString()
        } else {
            null
        }
    }

    private val _code: Int? by lazy {
        code
    }

    val code get() = _code

    override val message get() = _message ?: super.message

    init {
        if (cause != null) {
            super.initCause(cause)
        }
    }
}
