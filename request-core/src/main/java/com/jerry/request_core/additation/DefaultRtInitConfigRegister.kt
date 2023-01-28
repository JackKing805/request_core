package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.config.Config
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.s.IResponse

@ConfigRegister(registerClass = Any::class)
class DefaultRtInitConfigRegister : IConfig() {

    override fun init(annotation: Configuration, clazz: Any) {
        clazz::class.java.methods.forEach {
            val parameters = it.parameters
            if (parameters.isEmpty() && RtConfig::class.java.isAssignableFrom(it.returnType)){
                val invoke = InjectUtils.invokeMethod(clazz,it, arrayOf()) as? RtConfig
                if (invoke!=null){
                    RequestUtils.setRtConfig(invoke)
                }
            }else if (parameters.isEmpty() && Config::class.java.isAssignableFrom(it.returnType)){
                val invoke = InjectUtils.invokeMethod(clazz,it, arrayOf()) as? Config
                if (invoke!=null){
                    RequestUtils.setConfig(invoke)
                }
            }
        }
    }

    override fun onRequest(
        context: Context,
        request: Request,
        response: IResponse,
        controllerMapper: ControllerMapper?
    ): Boolean {
        return true
    }
}