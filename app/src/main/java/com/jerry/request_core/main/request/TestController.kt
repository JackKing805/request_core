package com.jerry.request_core.main.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.main.request.B
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.pojo.Request

@Controller("/")
class TestController {
    @Controller("/")
    fun onRoot(request: Request,context: Context,@Inject b: B,@Inject rtContext: RtContext,@Inject sessionManager: SessionManager,parameterBean: ParameterBean):String{
        Log.e("ADSAD","onRoot:${request.getPackage().getSession().getId()},rtContext:$rtContext,sessioManager:$sessionManager,pb:$parameterBean")
        return "onRoot:${request.getPackage().getSession().getId()},rtContext:$rtContext,sessioManager:$sessionManager,pb:$parameterBean"
    }

    @Controller("/2")
    fun onRoot2(request: Request,context: Context,@Inject b: B):String{
        Log.e("ADSAD","onRoot2:${request.getPackage().getSession().getId()}")
        throw IllegalArgumentException("haha")
        return b.name + context.cacheDir.name
    }
}