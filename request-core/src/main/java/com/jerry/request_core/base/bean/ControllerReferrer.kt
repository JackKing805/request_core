package com.jerry.request_core.base.bean

import java.lang.reflect.Method

data class ControllerReferrer(
    val instance:Any,
    val method: Method,
    val path:String
)