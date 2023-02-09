package com.jerry.request_core.factory

import android.content.Context
import com.jerry.request_core.base.bean.ControllerReferrer
import com.jerry.request_core.base.bean.ControllerResult
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    fun init(injects:MutableList<Class<*>>){
        InjectFactory.inject(injects)
    }

    fun onRequestPre(context: Context,request: Request,response: Response,controllerMapper: CoreControllerMapper?):Boolean{
        val mapper = if (controllerMapper!=null){
            ControllerReferrer(controllerMapper.instance,controllerMapper.method,controllerMapper.path)
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

    fun onRequestEnd(context: Context,request: Request,response: Response,controllerResult: ControllerResult):Boolean{
        InjectFactory.getConfigRegisters().forEach {
            if (!it.instance.onRequestEnd(context,request,response,controllerResult)){
                return false
            }
        }
        return true
    }

    fun matchController(path:String): CoreControllerMapper?{
        return InjectFactory.getController(path)
    }
}


