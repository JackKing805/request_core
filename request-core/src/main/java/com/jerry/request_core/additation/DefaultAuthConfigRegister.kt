package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.bean.ControllerReferrer
import com.jerry.request_base.bean.ResourceReferrer
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.Core
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient

@ConfigRegister(registerClass = Any::class)
class DefaultAuthConfigRegister : IConfig() {
    private  val requestInterceptorList: MutableList<RequestInterceptor> = mutableListOf()

    override fun init(annotation: Configuration, clazz: Any) {
        clazz::class.java.methods.forEach {
            val parameters = it.parameters
            if (parameters.size==1 && ReflectUtils.isSameClass(RequestInterceptor::class.java,parameters[0].type)){
                val requestInterceptor = RequestInterceptor()
                InjectUtils.invokeMethod(clazz,it, arrayOf(requestInterceptor))
                if (!requestInterceptor.isBuild()){
                    throw IllegalStateException("please add request handler")
                }
                requestInterceptorList.add(requestInterceptor)
            }
        }
    }

    override fun onCreate() {
        val bean = Core.getBean(RequestInterceptor::class.java)
        if (bean!=null){
            val ss = bean as RequestInterceptor
            requestInterceptorList.add(ss)
        }
    }

    override fun onResourceRequest(
        context: Context,
        request: Request,
        response: Response,
        resourceReferrer: ResourceReferrer
    ): Boolean {
        requestInterceptorList.forEach {
            val pass = it.hand(request, response)
            if (!pass){
                return false
            }
        }
        return super.onResourceRequest(context, request, response, resourceReferrer)
    }

    override fun onRequestPre(
        context: Context,
        request: Request,
        response: Response,
        controllerReferrer: ControllerReferrer
    ): Boolean {
        requestInterceptorList.forEach {
            val pass = it.hand(request, response)
            if (!pass){
                return false
            }
        }
        return  true
    }

    override fun onRtIn(context: Context,client: RtClient, request: Request, response: Response): Boolean {
        requestInterceptorList.forEach {
            val pass = it.hand(request, response)
            if (!pass){
                return false
            }
        }
        return super.onRtIn(context,client, request, response)
    }

    override fun onRtMessage(context: Context,client: RtClient,request: Request, response: Response): Boolean {
        requestInterceptorList.forEach {
            val pass = it.hand(request, response)
            if (!pass){
                return false
            }
        }
        return super.onRtMessage(context,client,request, response)
    }

    class RequestInterceptor(
        private val interceptor:MutableList<String> = mutableListOf(),
    ){
        private var requestHandler: IRequestHandler?=null

        internal fun isBuild() = requestHandler != null

        fun interceptor(url:String): RequestInterceptor {
            interceptor.add(url)
            return this
        }

        fun build(requestHandler: IRequestHandler){
            this.requestHandler = requestHandler
        }

        internal fun hand(request: Request,response: Response):Boolean{
            val requestURI = request.getPackage().getRequestURI()
            val path = requestURI.path?:""
            interceptor.filter {
                path == it || path.startsWith(it)
            }.forEach { _ ->
                val pass = requestHandler!!.handle(request, response)
                if (!pass){
                    return  false
                }
            }
            return true
        }
    }

    interface IRequestHandler{
        fun handle(request: Request,response: Response):Boolean
    }
}
