package com.jerry.request_core

import android.app.Application
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.config.Config
import com.jerry.request_core.delegator.RequestDelegator
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.interfaces.IRequestListener
import com.jerry.request_core.service.ServerService
import com.jerry.rt.bean.RtConfig

object RequestUtils {
    private lateinit var application: Application
    private lateinit var config: Config
    private var iRequestListener: IRequestListener?=null
    private var rtConfig = RtConfig()

    fun init(application: Application, config: Config, controllers:MutableList<Class<*>>){
        RequestUtils.application = application
        RequestUtils.config = config
        RequestFactory.init(controllers)
    }

    fun setRtConfig(rtConfig: RtConfig){
        this.rtConfig = rtConfig
    }

    fun getRtConfig() = rtConfig

    fun startServer(){
        ServerService.run(application,true)
    }

    fun stopServer(){
        ServerService.run(application,false)
    }

    fun listen(iRequestListener: IRequestListener){
        RequestUtils.iRequestListener = iRequestListener
    }

    fun <T: IConfig> getConfigRegister(clazz: Class<T>) = RequestFactory.getConfigRegister(clazz)

    internal fun getIRequestListener() = iRequestListener

    internal fun getConfig() = config

    internal fun  getApplication() = application
}