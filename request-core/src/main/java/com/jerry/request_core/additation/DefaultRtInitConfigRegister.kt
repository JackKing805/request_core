package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.config.Config
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.s.IResponse

@ConfigRegister(registerClass = Any::class)
class DefaultRtInitConfigRegister : IConfig() {

    override fun init(annotation: Configuration, clazz: Any) {
        val bean1 = InjectFactory.getBeanClass(RtConfig::class.java)?.bean
        if (bean1!=null){
            RequestUtils.setRtConfig(bean1 as RtConfig)
        }

        val bean2 = InjectFactory.getBeanClass(RtSessionConfig::class.java)?.bean
        if (bean2!=null){
            RequestUtils.setRtConfig(RequestUtils.getRtConfig().copy(rtSessionConfig = bean2 as RtSessionConfig))
        }

        val bean3 = InjectFactory.getBeanClass(Config::class.java)?.bean
        if (bean3!=null){
            RequestUtils.setConfig(bean3 as Config)
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