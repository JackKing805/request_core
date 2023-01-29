package com.jerry.request_core.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Bean
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.R
import com.jerry.request_core.TestApp
import com.jerry.request_core.additation.DefaultResourcesDispatcherConfigRegister
import com.jerry.request_core.config.Config
import com.jerry.request_core.constants.FileType
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.s.IResponse

@Configuration
class A {
    @Bean
    fun setConfig() = Config(appIcon = R.raw.a)

    @Bean
    fun setRtConfig() = RtConfig(port = 8081)


    fun customR(r:DefaultResourcesDispatcherConfigRegister.ResourcesDeal){
        r.interceptor("/favicon.ico")
            .build(object :DefaultResourcesDispatcherConfigRegister.ResourcesDispatcher{
                override fun onResourcesRequest(
                    context: Context,
                    request: Request,
                    response: IResponse,
                    resourcesPath: String
                ): String {
                    return FileType.RAW.content + R.raw.a
                }
            })
    }

    @Bean()
    fun getFun(@Inject("fuckC") c:C):B{
        Log.e("ADSAD","getFun")
        return B("${c.name}:from b")
    }

    @Bean("fuckC")
    fun getFun2():C{
        Log.e("ADSAD","getFun2")
        return C("from c")
    }
}

data class C(
    val name: String
)

data class B(
    val name:String
)