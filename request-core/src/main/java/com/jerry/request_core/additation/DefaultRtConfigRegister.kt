package com.jerry.request_core.additation

import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.factory.InjectFactory
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(priority = -1)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient:RtClient?=null

    override fun init(annotation: Configuration, clazz: Any) {
        val bean2 = InjectFactory.getBean(RtClient::class.java)
        rtClient = bean2 as RtClient
    }

    override fun onRtIn(client: Client, response: Response): Boolean {
        rtClient?.onRtIn(client,response)
        return false
    }

    override fun onRtMessage(request: Request, response: Response): Boolean {
        rtClient?.onRtMessage(request,response)
        return false
    }
    override fun onRtOut(client: Client, response: Response): Boolean {
        rtClient?.onRtOut(client,response)
        return false
    }

    interface RtClient{
        fun handUrl():String

        fun onRtIn(client: Client,response: Response)

        fun onRtMessage(request: Request,response: Response)
        fun onRtOut(client: Client,response: Response)
    }
}