package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.s.IResponse
import java.lang.reflect.Method
import kotlin.reflect.KClass

@ConfigRegister(registerClass = DefaultRtConfigRegister.RtClient::class)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient:RtClient?=null

    override fun init(annotation: Configuration, clazz: Any) {
        rtClient = clazz as RtClient
    }

    override fun onRequest(
        context: Context,
        request: Request,
        response: IResponse,
        controllerMapper: ControllerMapper?
    ): Boolean {
        return true
    }

    fun onRtIn(context: Context,client: Client,response: IResponse){
        rtClient?.onRtIn(client,response)
    }

    fun onRtOut(context: Context,client: Client,response: IResponse){
        rtClient?.onRtOut(client,response)
    }

    interface RtClient{
        fun onRtIn(client: Client,response: IResponse)

        fun onRtOut(client: Client,response: IResponse)
    }
}