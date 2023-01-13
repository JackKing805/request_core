package com.jerry.request_core

import android.app.Application
import com.jerry.request_core.config.Config
import com.jerry.request_core.delegator.RequestDelegator
import com.jerry.request_core.interfaces.IRequestListener
import com.jerry.request_core.service.ServerService

object RequestUtils {
    private lateinit var application: Application
    private lateinit var config: Config
    private var iRequestListener: IRequestListener?=null

    fun init(application: Application, config: Config, controllers:MutableList<Class<*>>){
        RequestUtils.application = application
        RequestUtils.config = config
        RequestDelegator.init(controllers)
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

    internal fun getIRequestListener() = iRequestListener

    internal fun getConfig() = config

    internal fun  getApplication() = application
}