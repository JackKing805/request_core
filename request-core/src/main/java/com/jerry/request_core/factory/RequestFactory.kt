package com.jerry.request_core.factory

import android.content.Context
import com.jerry.request_base.interfaces.IConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.s.IResponse

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    fun init(injects:MutableList<Class<*>>){
        InjectFactory.inject(injects)
    }

    fun onRequest(context: Context,request: Request,response: IResponse,controllerMapper: ControllerMapper?):Boolean{
        val mapper = if (controllerMapper!=null){
            IConfig.ControllerMapper(controllerMapper.instance,controllerMapper.method)
        }else{
            null
        }
        InjectFactory.getConfigRegisters().forEach {
            if (!it.instance.onRequest(context,request,response,mapper)){
                return false
            }
        }
        return true
    }

    fun matchController(path:String): ControllerMapper?{
        return InjectFactory.getController(path)
    }
}


