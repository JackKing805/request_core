package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.bean.ControllerReferrer
import com.jerry.request_base.interfaces.IConfig
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(registerClass = DefaultRtConfigRegister.RtClient::class)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient:RtClient?=null

    override fun init(annotation: Configuration, clazz: Any) {
        rtClient = clazz as RtClient
    }



    override fun onRequestPre(
        context: Context,
        request: Request,
        response: Response,
        controllerReferrer: ControllerReferrer
    ): Boolean {
        return true
    }

    fun onRtIn(context: Context,client: Client,response: Response){
        rtClient?.onRtIn(client,response)
    }

    fun onRtOut(context: Context,client: Client,response: Response){
        rtClient?.onRtOut(client,response)
    }

    interface RtClient{
        fun onRtIn(client: Client,response: Response)

        fun onRtOut(client: Client,response: Response)
    }
}