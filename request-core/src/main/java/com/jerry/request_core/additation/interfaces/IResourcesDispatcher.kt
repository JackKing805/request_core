package com.jerry.request_core.additation.interfaces

import android.content.Context
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

abstract class IResourcesDispatcher {
    abstract fun dealResources(context: Context,request: Request,response: Response,resourcesName:String):String
}