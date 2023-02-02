package com.jerry.request_core.factory

import android.content.Context
import com.jerry.request_base.bean.IConfigControllerMapper
import com.jerry.request_base.interfaces.IConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    fun init(injects:MutableList<Class<*>>){
        InjectFactory.inject(injects)
    }

    fun onRequestPre(context: Context,request: Request,response: Response,controllerMapper: ControllerMapper?):Boolean{
        val mapper = if (controllerMapper!=null){
            IConfigControllerMapper(controllerMapper.instance,controllerMapper.method)
        }else{
            null
        }
        InjectFactory.getConfigRegisters().forEach {
            if (!it.instance.onRequestPre(context,request,response,mapper)){
                return false
            }
        }
        return true
    }

    fun onRequestEnd(context: Context,request: Request,response: Response):Boolean{
        InjectFactory.getConfigRegisters().forEach {
            if (!it.instance.onRequestEnd(context,request,response)){
                return false
            }
        }
        return true
    }

    fun matchController(path:String): ControllerMapper?{
        return InjectFactory.getController(path)
    }
}


