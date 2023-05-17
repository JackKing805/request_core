package com.jerry.request_core.main.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Bean
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.additation.DefaultResourcesDispatcherConfigRegister
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.config.Config
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.main.R
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient
import com.jerry.rt.core.http.protocol.RtContentType

@Configuration
class A {
    @Bean
    fun setConfig() = Config(appIcon = R.raw.a)

    @Bean
    fun rtClient() = object : DefaultRtConfigRegister.RtClientHandler {
        override fun handUrl(): String {
            return "/rt/aa"
        }

        override fun onRtIn(client: RtClient, request: Request, response: Response) {
            Log.e("AAWWDA", "onRtIn")
        }

        override fun onRtMessage(request: Request, response: Response) {
            Log.e("AAWWDA", "onRtMessage:${request.getBody()}")
            response.setContentType(RtContentType.TEXT_PLAIN.content)
            response.write("hallo：${request.getPackage().getSession().getId()}")
        }

        override fun onRtOut(client: RtClient) {
            Log.e("AAWWDA", "onRtOut")
        }
    }


    fun customR(r: DefaultResourcesDispatcherConfigRegister.ResourcesDeal) {
        r.interceptor("/favicon.ico")
            .build(object : DefaultResourcesDispatcherConfigRegister.ResourcesDispatcher {
                override fun onResourcesRequest(
                    context: Context,
                    request: Request,
                    response: Response,
                    resourcesPath: String
                ): String {
                    return FileType.RAW.content + R.raw.a
                }
            })
    }

    @Bean()
    fun getFun(@Inject("fuckC") c: C): B {
        Log.e("ADSAD", "getFun")
        return B("${c.name}:from b")
    }

    @Bean("fuckC")
    fun getFun2(): C {
        Log.e("ADSAD", "getFun2")
        return C("from c")
    }
}

data class C(
    val name: String
)

data class B(
    val name: String
)