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
    private var iRequestListener: IRequestListener?=null
    private var rtConfig = RtConfig()
    private var config = Config(R.raw.favicon)

    fun init(application: Application,  more:MutableList<Class<*>>){
        RequestUtils.application = application
        inject(more)
    }

    fun inject(more:MutableList<Class<*>>){
        RequestFactory.init(more)
    }

    fun setRtConfig(rtConfig: RtConfig){
        this.rtConfig = rtConfig
    }

    fun setConfig(config: Config){
        this.config = config
    }

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

    fun getRtConfig() = rtConfig

    fun getConfig() = config

    internal fun getIRequestListener() = iRequestListener

    internal fun  getApplication() = application
}