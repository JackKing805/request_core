package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_core.base.annotations.ConfigRegister
import com.jerry.request_core.base.annotations.Configuration
import com.jerry.request_core.base.interfaces.IConfig
import com.jerry.request_core.Core
import com.jerry.request_core.base.bean.ControllerReferrer
import com.jerry.request_core.base.bean.ControllerResult
import com.jerry.request_core.config.Config
import com.jerry.request_core.factory.InjectFactory
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(registerClass = Any::class)
class DefaultRtInitConfigRegister : IConfig() {

    override fun init(annotation: Configuration, clazz: Any) {

    }

    override fun onCreate() {
        val bean1 = InjectFactory.getBean(RtConfig::class.java)
        if (bean1!=null){
            Core.setRtConfig(bean1 as RtConfig)
        }

        val bean2 = InjectFactory.getBean(RtSessionConfig::class.java)
        if (bean2!=null){
            Core.setRtConfig(Core.getRtConfig().copy(rtSessionConfig = bean2 as RtSessionConfig))
        }

        val bean3 = InjectFactory.getBean(Config::class.java)
        if (bean3!=null){
            Core.setConfig(bean3 as Config)
        }
    }

    override fun onRequestEnd(
        context: Context,
        request: Request,
        response: Response,
        controllerResult: ControllerResult
    ): Boolean {
        return true
    }
    override fun onRequestPre(
        context: Context,
        request: Request,
        response: Response,
        controllerReferrer: ControllerReferrer?
    ): Boolean {
        return true
    }
}