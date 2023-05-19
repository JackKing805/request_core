package com.jerry.request_core.interfaces

import com.jerry.request_core.constants.Status
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

interface IRequestListener {
    fun onStatusChange(status: Status)

    fun onRequest(request:Request,response:Response,url:String){}

    sealed class AuthResult{
        object Grant : AuthResult()
        data class Denied(val result: Any): AuthResult()
    }
}
