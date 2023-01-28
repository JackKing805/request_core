package com.jerry.request_core.delegator

import android.content.Context
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.extensions.parameterToArray
import com.jerry.request_core.extensions.toObject
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InvokeUtils
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.RtResponse
import com.jerry.rt.core.http.pojo.s.IResponse

/**
 * 请求分发
 */
internal object RequestDelegator {
    internal fun dispatcher(context: Context, request: Request, response: IResponse) {
        val requestURI = request.getPackage().getRequestURI()
        val controllerMapper = RequestFactory.matchController(requestURI.path)
        RequestUtils.getIRequestListener()?.onRequest(requestURI.path?:"")
        if (!RequestFactory.onRequest(context, request,response,controllerMapper)){
            return
        }
        if (controllerMapper != null) {
            if (controllerMapper.requestMethod.content.equals(request.getPackage().method, true)) {
                val newInstance = controllerMapper.instance
                try {
                    val invoke = try {
                        InvokeUtils.invokeMethod(newInstance, controllerMapper.method){
                            when (val clazz = it.type) {
                                Context::class.java -> {
                                    context
                                }
                                Request::class.java -> {
                                    request
                                }
                                IResponse::class.java -> {
                                    response
                                }
                                Response::class.java->{
                                    response
                                }
                                RtResponse::class.java->{
                                    response
                                }
                                ParameterBean::class.java -> {
                                    ParameterBean(
                                        request.getPackage()
                                            .getRequestURI().parameterToArray()
                                    )
                                }
                                else -> {
                                    val objects = request.getBody().toObject(clazz)
                                    objects
                                }
                            }
                        }
                    }catch (e:NullPointerException){
                        ResponseUtils.dispatcherError(response, 500)
                        return
                    }
                    if (invoke==null){
                        ResponseUtils.dispatcherError(response, 500)
                    }else{
                        ResponseUtils.dispatcherReturn(controllerMapper.isRestController, response, invoke)
                    }
                } catch (e: NullPointerException) {
                    ResponseUtils.dispatcherError(response, 502)
                }
                return
            } else {
                ResponseUtils.dispatcherError(response, 405)
                return
            }
        }
        ResponseUtils.dispatcherError(response, 404)
    }


    internal fun onRtIn(context: Context,client:Client,response: RtResponse){
        (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtIn(context, client, response)
    }

    internal fun onRtMessage(context: Context,request: Request,response: RtResponse){
        dispatcher(context,request,response)
    }

    internal fun onRtOut(context: Context,client: Client,response: RtResponse){
        (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtOut(context, client, response)
    }
}

