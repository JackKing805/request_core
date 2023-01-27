package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.R
import com.jerry.request_core.constants.FileType
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.extensions.isResources
import com.jerry.request_core.extensions.resourcesName
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InvokeUtils
import com.jerry.rt.core.http.pojo.s.IResponse

@ConfigRegister(-1, registerClass = Any::class)
class DefaultResourcesDispatcherConfigRegister : IConfig() {
    private  val resourcesDispatchers: MutableList<ResourcesDeal> = mutableListOf()
    override fun init(annotation: Configuration, clazz:Any) {
        clazz::class.java.methods.forEach {
            val parameters = it.parameters
            if (parameters.size==1 && ResourcesDeal::class.java.isAssignableFrom(parameters[0].type)){
                val resourcesDispatcher = ResourcesDeal()
                InvokeUtils.invokeMethod(clazz,it, arrayOf(resourcesDispatcher))
                if (!resourcesDispatcher.isBuild()){
                    throw IllegalStateException("please add resources handler")
                }
                resourcesDispatchers.add(resourcesDispatcher)
            }
        }
    }

    override fun onRequest(
        context: Context,
        request: Request,
        response: IResponse,
        controllerMapper: ControllerMapper?
    ): Boolean {
        val requestURI = request.getPackage().getRequestURI()
        if (requestURI.isResources()){
            if (resourcesDispatchers.isNotEmpty()){
                for (i in resourcesDispatchers){
                    if (i.dealResources(context, request, response)){
                        return false
                    }
                }
            }
            dealDefault(context,request,response,request.getPackage().getRequestURI().resourcesName())
            return false
        }
        return true
    }

    private fun dealDefault(context: Context,request: Request,response: IResponse,resourcesPath:String){
        fun path():String{
            if (resourcesPath=="favicon.ico"){
                return FileType.RAW.content + R.raw.favicon
            }
            return FileType.ASSETS.content + resourcesPath
        }
        ResponseUtils.dispatcherReturn(context,false,request,response,path())
    }

    class ResourcesDeal(
        private var url:String = "/"
    ){
        private var resourcesDispatcher: ResourcesDispatcher?=null

        internal fun isBuild() = resourcesDispatcher != null

        fun interceptor(url:String): ResourcesDeal {
            this.url = url
            return this
        }

        fun build(requestHandler: ResourcesDispatcher){
            this.resourcesDispatcher = requestHandler
        }

        internal fun dealResources(context: Context,request: Request,response: IResponse):Boolean{
            val requestURI = request.getPackage().getRequestURI()
            val path = requestURI.path?:""
            val responsePath = requestURI.resourcesName()
            if (path == url || path.startsWith(url)){
                val resourcesPath = resourcesDispatcher!!.onResourcesRequest(context, request, response,responsePath)
                ResponseUtils.dispatcherReturn(context,false,request,response,resourcesPath)
                return true
            }else{
                return false
            }
        }
    }

    interface ResourcesDispatcher{
        fun onResourcesRequest(context: Context,request: Request,response: IResponse,resourcesPath:String):String
    }
}