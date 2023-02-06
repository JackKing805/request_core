package com.jerry.request_core.additation

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.bean.IConfigControllerMapper
import com.jerry.request_base.interfaces.IConfig
import com.jerry.request_core.Core
import com.jerry.request_core.R
import com.jerry.request_core.constants.FileType
import com.jerry.rt.core.http.pojo.Request
import com.jerry.request_core.extensions.isResources
import com.jerry.request_core.extensions.resourcesPath
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.core.http.pojo.Response

@ConfigRegister(-999999999, registerClass = Any::class)
class DefaultResourcesDispatcherConfigRegister : IConfig() {
    private  val resourcesDispatchers: MutableList<ResourcesDeal> = mutableListOf()
    override fun init(annotation: Configuration, clazz:Any) {
        clazz::class.java.methods.forEach {
            val parameters = it.parameters
            if (parameters.size==1 && ReflectUtils.isSameClass(ResourcesDeal::class.java,parameters[0].type)){
                val resourcesDispatcher = ResourcesDeal()
                InjectUtils.invokeMethod(clazz,it, arrayOf(resourcesDispatcher))
                if (!resourcesDispatcher.isBuild()){
                    throw IllegalStateException("please add resources handler")
                }
                resourcesDispatchers.add(resourcesDispatcher)
            }
        }
    }

    override fun onCreate() {
        val bean = Core.getBean(ResourcesDeal::class.java)
        if (bean!=null){
            val ss = bean as ResourcesDeal
            if (!resourcesDispatchers.contains(ss)){
                resourcesDispatchers.add(bean)
            }
        }
    }

    override fun onRequestEnd(context: Context, request: Request, response: Response): Boolean {
        return true
    }

    override fun onRequestPre(
        context: Context,
        request: Request,
        response: Response,
        IConfigControllerMapper: IConfigControllerMapper?
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
            dealDefault(context,request,response,request.getPackage().getRequestURI().resourcesPath())
            return false
        }
        return true
    }

    private fun dealDefault(context: Context,request: Request,response: Response,resourcesPath:String){
        fun path():String{
            if (resourcesPath=="favicon.ico"){
                return FileType.RAW.content + R.raw.favicon
            }
            return FileType.ASSETS.content + resourcesPath
        }
        ResponseUtils.dispatcherReturn(false,response,path())
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

        internal fun dealResources(context: Context,request: Request,response: Response):Boolean{
            val requestURI = request.getPackage().getRequestURI()
            val path = requestURI.path?:""
            val responsePath = requestURI.resourcesPath()
            if (path == url || path.startsWith(url)){
                val resourcesPath = resourcesDispatcher!!.onResourcesRequest(context, request, response,responsePath)
                ResponseUtils.dispatcherReturn(false,response,resourcesPath)
                return true
            }else{
                return false
            }
        }
    }

    interface ResourcesDispatcher{
        fun onResourcesRequest(context: Context,request: Request,response: Response,resourcesPath:String):String
    }
}
