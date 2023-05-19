package com.jerry.request_core.additation

import android.content.Context
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

    override fun onRtIn(context: Context,client: RtClient,request: Request, response: Response): Boolean {
        rtClient?.onRtIn(context,client,request,response)
        return false
    }

    override fun onRtMessage(context: Context,client: RtClient,request: Request, response: Response): Boolean {
        rtClient?.onRtMessage(context,client,request,response)
        return false
    }
    override fun onRtOut(context: Context,client: RtClient): Boolean {
        rtClient?.onRtOut(context,client)
        return false
    }

    interface RtClientHandler{
        fun handUrl():String

        fun onRtIn(context: Context,client: RtClient,request: Request,response: Response)

        fun onRtMessage(context: Context,client: RtClient,request: Request,response: Response)
        fun onRtOut(context: Context,client: RtClient)
    }
}
