package com.jerry.request_core.additation

import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.factory.InjectFactory
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient

@ConfigRegister(priority = -1)
class DefaultRtConfigRegister : IConfig() {
    private var rtClient:RtClientHandler?=null

    override fun init(annotation: Configuration, clazz: Any) {
        val bean2 = InjectFactory.getBean(RtClientHandler::class.java)
        if (bean2!=null){
            rtClient = bean2 as RtClientHandler
        }
    }

    override fun onRtIn(client: RtClient,request: Request, response: Response): Boolean {
        rtClient?.onRtIn(client,request,response)
        return false
    }

    override fun onRtMessage(request: Request, response: Response): Boolean {
        rtClient?.onRtMessage(request,response)
        return false
    }
    override fun onRtOut(client: RtClient): Boolean {
        rtClient?.onRtOut(client)
        return false
    }

    interface RtClientHandler{
        fun handUrl():String

        fun onRtIn(client: RtClient,request: Request,response: Response)

        fun onRtMessage(request: Request,response: Response)
        fun onRtOut(client: RtClient)
    }
}