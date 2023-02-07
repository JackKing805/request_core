package com.jerry.request_core.main.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.anno.ParamsQuery
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.main.request.B
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@Controller("/")
class TestController {
    @Controller("/page/{page}")
    fun onRootRequest(context: Context, request: Request, response: Response, @ParamsQuery("page") page:String?):String {
        if (page==null){
            return FileType.ASSETS.content + "index.html"
        }else{
            if (page=="index"){
                return FileType.ASSETS.content + page + ".html"
            }else{
                return FileType.ASSETS.content + page
            }
        }
    }

    @Controller("/")
    fun onRootRequest2(context: Context, request: Request, response: Response, @ParamsQuery("page") page:String?):String {
        return FileType.ASSETS.content + "index.html"
    }
}