package com.jerry.request_core.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.annotations.Inject
import com.jerry.rt.core.http.pojo.Request

@Controller("/")
class TestController {
    @Controller("/")
    fun onRoot(request: Request,context: Context,@Inject b:B):String{
        Log.e("ADSAD","onRoot:${request.getPackage().getSession().getId()}")
        return b.name + context.cacheDir.name
    }
}