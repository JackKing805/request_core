package com.jerry.request_core.main.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Controller
import com.jerry.request_base.bean.RequestMethod
import com.jerry.request_core.anno.ParamsQuery
import com.jerry.request_core.constants.FileType
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.request.model.MultipartFile

@Controller("/")
class TestController {
    @Controller("/page/{page}")
    suspend fun onRootRequest(context: Context, request: Request, response: Response, @ParamsQuery("page") page:String?):String {
        Log.e("AA",page?:"")
        if (page==null){
            return FileType.ASSETS.content + "index.html"
        }else{
            return FileType.ASSETS.content + page + ".html"
        }
    }

    @Controller("/file", requestMethod = RequestMethod.POST)
    fun upFile(@ParamsQuery("file1") multipartFiles: List<MultipartFile>):String{
        multipartFiles.forEach {
            Log.e("AA","file:${it?.getHeader()?.getFileName()}")
            it.save()
        }
        return "redirect:/page/index"
    }

    @Controller("/")
    fun onRootRequest2(context: Context, request: Request, response: Response):String {
        return "redirect:https://www.baidu.com/s?wd=http%E6%80%8E%E4%B9%88%E8%AE%BE%E7%BD%AE%E9%87%8D%E5%AE%9A%E5%90%91&pn=10&oq=http%E6%80%8E%E4%B9%88%E8%AE%BE%E7%BD%AE%E9%87%8D%E5%AE%9A%E5%90%91&tn=baiduhome_pg&ie=utf-8&rsv_idx=2&rsv_pq=fc9c905200160bef&rsv_t=a910jdaxLpHe2Z%2BlgnjCF6W51k5%2BDBJKuOQsAXyheyWvw2ZgOvIlJYUhpdcMrHBy%2BzOW"
    }
}