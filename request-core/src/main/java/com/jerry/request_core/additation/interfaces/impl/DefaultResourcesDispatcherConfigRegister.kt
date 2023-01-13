package com.jerry.request_core.additation.interfaces.impl

import android.content.Context
import com.jerry.request_base.annotations.ConfigRegister
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.interfaces.IConfig
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.extensions.isResources
import com.jerry.request_core.extensions.resourcesName
import com.jerry.request_core.additation.interfaces.IResourcesDispatcher
import com.jerry.request_core.utils.ResponseUtils

@ConfigRegister(-1, registerClass = IResourcesDispatcher::class)
class DefaultResourcesDispatcherConfigRegister : IConfig() {
    private lateinit var iResourcesDispatcher: IResourcesDispatcher
    override fun init(annotation: Configuration, clazz: Class<*>) {
        iResourcesDispatcher = clazz.newInstance() as IResourcesDispatcher
    }


    override fun onRequest(context: Context, request: Request, response: Response): Boolean {
        val requestURI = request.getPackage().getRequestURI()
        if (requestURI.isResources()){
            val result = iResourcesDispatcher.dealResources(context,request,response,requestURI.resourcesName())
            ResponseUtils.dispatcherReturn(context,false,request,response,result)
            return false
        }
        return true
    }
}