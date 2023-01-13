package com.jerry.request_core.additation.interfaces

import android.content.Context
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

abstract class IAuthDispatcher {
    abstract fun onAuth(context: Context,request: Request,response: Response): Boolean
}