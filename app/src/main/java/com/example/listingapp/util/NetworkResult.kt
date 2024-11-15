package com.example.listingapp.util

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null,
    val headers: Map<String, String>? = null
) {

    class Success<T>(data: T) : NetworkResult<T>(data)

    class Error<T>(message: String?, headers: Map<String, String>? = null, data: T? = null) :
        NetworkResult<T>(data, message, headers)

}