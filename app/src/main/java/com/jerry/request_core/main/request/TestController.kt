package com.jerry.request_core.main.request

import android.content.Context
import com.jerry.request_core.base.annotations.Controller
import com.jerry.request_core.base.annotations.ParamsQuery
import com.jerry.request_core.constants.FileType
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

@Controller("/")
class TestController {
    @Controller("/page/{page}")
    fun onRootRequest(context: Context, request: Request, response: Response, @ParamsQuery("page") page:String?):String {
        if (page==null){
            return FileType.ASSETS.content + "index.html"
        }else{
            return FileType.ASSETS.content + page + ".html"
        }
    }

    @Controller("/")
    fun onRootRequest2(context: Context, request: Request, response: Response, @ParamsQuery("page") page:String?):String {
        return FileType.ASSETS.content + "assets/index.html"
    }
}