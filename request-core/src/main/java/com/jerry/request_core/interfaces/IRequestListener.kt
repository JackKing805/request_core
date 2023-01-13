package com.jerry.request_core.interfaces

import com.jerry.request_core.constants.Status

interface IRequestListener {
    fun onStatusChange(status: Status)

    fun onRequest(url:String){}

    sealed class AuthResult{
        object Grant : AuthResult()
        data class Denied(val result: Any): AuthResult()
    }
}