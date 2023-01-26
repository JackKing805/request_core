package com.jerry.request_core.factory

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.annotations.RequestMethod
import com.jerry.request_base.interfaces.IConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.request_core.extensions.IsIConfigResult
import com.jerry.request_core.extensions.isIConfig
import com.jerry.request_core.additation.DefaultAuthConfigRegister
import com.jerry.request_core.additation.DefaultResourcesDispatcherConfigRegister
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.additation.DefaultRtInitConfigRegister
import com.jerry.rt.core.http.pojo.s.IResponse
import java.lang.reflect.Method

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    private val controllerMap = mutableMapOf<String, ControllerMapper>()
    private val configRegisterList = mutableListOf<ConfigRegisterMapper>()


    private var defaultIsInit = false
    private val defaultInjects = mutableListOf<Class<*>>(
        DefaultAuthConfigRegister::class.java,
        DefaultResourcesDispatcherConfigRegister::class.java,
        DefaultRtConfigRegister::class.java,
        DefaultRtInitConfigRegister::class.java
    )

    fun init(injects:MutableList<Class<*>>){
        if (!defaultIsInit){
            defaultIsInit = true
            injects.addAll(defaultInjects)
        }

        val registers = mutableListOf<ConfigRegisterMapper>()
        injects.forEach {
            val isIConfig = it.isIConfig()
            if (isIConfig is IsIConfigResult.Is){
                registers.add(ConfigRegisterMapper(it,isIConfig.annotation,isIConfig.instance) )
            }
        }
        injects.removeAll(registers.map { it.clazz })


        //按优先级排序，priority数字越大优先级越大
        registers.sortByDescending{
            it.annotation.priority
        }
        registers.forEach {
            registerConfigRegister(it)
        }


        injects.forEach {
            registerController(it)
            initConfiguration(it)
        }
    }

    private fun registerController(clazz:Class<*>){
        clazz.getAnnotation(Controller::class.java)?.let { cc ->
            val isClassJson = cc.isRest
            val clazzPath = cc.value
            val clazzEndIsLine = clazzPath.endsWith("/")
            clazz.methods.forEach { m ->
                m.getAnnotation(Controller::class.java)?.let { mc ->
                    val isMethodJson = mc.isRest
                    val methodPath = if (clazzEndIsLine){
                        if (mc.value.startsWith("/")) {
                            mc.value.substring(1)
                        } else {
                            mc.value
                        }
                    }else{
                        if (mc.value.startsWith("/")) {
                            mc.value
                        } else {
                            "/"+mc.value
                        }
                    }
                    val fullPath = clazzPath + methodPath
                    controllerMap[fullPath] = ControllerMapper(clazz.newInstance(),clazz, m, mc.requestMethod, isClassJson or isMethodJson)
                }
            }
        }
    }

    //注册配置注册器，需要有ConfigRegister注解并且继承子IConfig
    private fun registerConfigRegister(iConfig: ConfigRegisterMapper){
        configRegisterList.add(iConfig)
    }

    //根据已有的配置注册器注册配置，如若没有对应的配置注册器，就抛弃配置
    private fun initConfiguration(clazz: Class<*>){
        clazz.getAnnotation(Configuration::class.java)?.let { con->
            configRegisterList.forEach {
                if (it.annotation.registerClass.java.isAssignableFrom(clazz)){
                    it.instance.init(con,clazz)
                }
            }
        }
    }

    fun onRequest(context: Context,request: Request,response: IResponse,controllerMapper: ControllerMapper?):Boolean{
        val mapper = if (controllerMapper!=null){
            IConfig.ControllerMapper(controllerMapper.instance,controllerMapper.method)
        }else{
            null
        }
        configRegisterList.forEach {
            if (!it.instance.onRequest(context,request,response,mapper)){
                return false
            }
        }
        return true
    }

    fun matchController(path:String): ControllerMapper?{
        return controllerMap[path]
    }

    fun <T:IConfig> getConfigRegister(clazz: Class<T>) = configRegisterList.find { it.clazz == clazz }?.instance as? T
}

data class ConfigRegisterMapper(
    val clazz:Class<*>,
    val annotation: ConfigRegister,
    val instance: IConfig
)

data class ControllerMapper(
    val instance: Any,
    val classClazz:Class<*>,
    val method: Method,
    val requestMethod: RequestMethod,
    val isRestController: Boolean
)