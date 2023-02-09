package com.jerry.request_core.base.interfaces

import android.content.Context
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.base.annotations.Configuration
import com.jerry.request_core.base.bean.ControllerReferrer
import com.jerry.request_core.base.bean.ControllerResult

/**
 * 配置注册类，需要搭配ConfigRegister 注解同时使用
 */
abstract class IConfig {
    abstract fun onCreate()

    abstract fun init(annotation: Configuration, clazz: Any)

    abstract fun onRequestPre(context: Context, request: Request, response: Response, controllerReferrer: ControllerReferrer?):Boolean

    abstract fun onRequestEnd(context: Context, request: Request, response: Response, controllerResult: ControllerResult) :Boolean
}