package com.jerry.request_core

import android.app.Application
import com.jerry.request_core.config.Config
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.interfaces.IRequestListener
import com.jerry.request_core.service.RtCoreService
import com.jerry.request_core.service.ServerService
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.bean.RtConfig

object Core {
    private lateinit var application: Application
    private var iRequestListener: IRequestListener?=null
    private var rtConfig = RtConfig()
    private var config = Config(R.raw.favicon)


    fun init(application: Application,  more:MutableList<Class<*>>){
        Core.application = application
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
        if (config.showStatusService){
            ServerService.run(application,true)
        }else{
            RtCoreService.startRtCore(application)
        }
    }

    fun stopServer(){
        if (config.showStatusService){
            ServerService.run(application,false)
        }else{
            RtCoreService.stopRtCore()
        }
    }

    fun listen(iRequestListener: IRequestListener){
        Core.iRequestListener = iRequestListener
    }

    fun getRtConfig() = rtConfig

    fun getConfig() = config

    internal fun getIRequestListener() = iRequestListener

    internal fun  getApplication() = application


    fun getBean(clazz: Class<*>) = InjectFactory.getBean(clazz)

    fun getBean(beanName: String) = InjectFactory.getBean(beanName)
}