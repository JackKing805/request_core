package com.jerry.request_core

import android.app.Application
import com.jerry.request_core.config.Config
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.interfaces.IRequestListener
import com.jerry.request_core.service.RtCoreService
import com.jerry.request_core.service.ServerService
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.bean.RtFileConfig
import java.io.File

object Core {
    private lateinit var application: Application
    private var iRequestListener: IRequestListener?=null
    private var rtConfig: RtConfig = RtConfig(
        rtFileConfig = RtFileConfig(
            tempFileDir = "",
            saveFileDir = ""
        )
    )

    private var config = Config(R.raw.favicon)


    //先保存，调用init的时再初始化
    private val injects = mutableListOf<Class<*>>()
    private var isInit = false

    fun init(application: Application,  more:MutableList<Class<*>>){
        if (isInit){
            return
        }
        isInit = true
        Core.application = application
        if (rtConfig.rtFileConfig.tempFileDir == "" && rtConfig.rtFileConfig.saveFileDir == ""){
            setRtConfig(rtConfig.copy(
                rtFileConfig = RtFileConfig(
                    tempFileDir = application.filesDir.absolutePath + File.separatorChar + "temp",
                    saveFileDir = application.filesDir.absolutePath + File.separatorChar + "save"
                )
            ))
        }
        more.addAll(injects)
        injects.clear()
        InjectFactory.inject(more)
    }

    fun inject(more:MutableList<Class<*>>){
        injects.addAll(more)
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

    fun <T : Annotation> getAnnotationBean(annotationClass:Class<T>) = InjectFactory.getAnnotationBean(annotationClass)
}