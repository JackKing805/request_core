package com.jerry.request_core.additation

import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.Core
import com.jerry.request_core.config.Config
import com.jerry.request_core.factory.InjectFactory
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.bean.RtSessionConfig

@ConfigRegister(registerClass = Any::class)
class DefaultRtInitConfigRegister : IConfig() {


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

}