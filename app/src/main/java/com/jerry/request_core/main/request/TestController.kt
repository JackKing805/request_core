package com.jerry.request_core.main.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.main.request.B
import com.jerry.rt.core.http.pojo.Request

@Controller("/")
class TestController {
    @Controller("/")
    @kotlin.jvm.Throws(NullPointerException::class)
    fun onRoot(request: Request,context: Context,@Inject b: B):String{
        Log.e("ADSAD","onRoot:${request.getPackage().getSession().getId()}")
        throw NullPointerException("haha")
        return b.name + context.cacheDir.name
    }

    @Controller("/2")
    @kotlin.jvm.Throws(NullPointerException::class)
    fun onRoot2(request: Request,context: Context,@Inject b: B):String{
        Log.e("ADSAD","onRoot2:${request.getPackage().getSession().getId()}")
        throw IllegalArgumentException("haha")
        return b.name + context.cacheDir.name
    }
}