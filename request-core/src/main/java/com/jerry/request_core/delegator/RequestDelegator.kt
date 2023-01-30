package com.jerry.request_core.delegator

import android.content.Context
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.Core
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.anno.ExceptionHandler
import com.jerry.request_core.anno.ExceptionRule
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.exception.InvokeMethodException
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.extensions.parameterToArray
import com.jerry.request_core.extensions.toObject
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.RtResponse
import com.jerry.rt.core.http.pojo.s.IResponse
import java.lang.reflect.InvocationTargetException

/**
 * 请求分发
 */
internal object RequestDelegator {
    internal fun dispatcher(context: Context, request: Request, response: IResponse) {
        val requestURI = request.getPackage().getRequestURI()
        val controllerMapper = RequestFactory.matchController(requestURI.path)
        Core.getIRequestListener()?.onRequest(requestURI.path?:"")


        try {
            if (!RequestFactory.onRequest(context, request,response,controllerMapper)){
                if (response is Response){
                    ResponseUtils.dispatcherError(response,400)
                }
                return
            }
        }catch (e:Exception){
            onException(response,e)
            return
        }


        if (controllerMapper != null) {
            if (controllerMapper.requestMethod.content.equals(request.getPackage().method, true)) {
                try {
                    val invoke = try { InjectUtils.invokeMethod(controllerMapper.instance, controllerMapper.method){
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
                    }catch (e: InvokeMethodException){
                        e.printStackTrace()
                        ResponseUtils.dispatcherError(response, 500)
                        return
                    }catch (e:Exception){
                        onException(response,e)
                        return
                    }
                    if (invoke==null){
                        ResponseUtils.dispatcherError(response, 500)
                    }else{
                        ResponseUtils.dispatcherReturn(controllerMapper.isRestController, response, invoke)
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
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
        try {
            (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtIn(context, client, response)
        }catch (e:Exception){
            onException(response,e)
        }
    }

    internal fun onRtMessage(context: Context,request: Request,response: RtResponse){
        dispatcher(context,request,response)
    }

    internal fun onRtOut(context: Context,client: Client,response: RtResponse){
        try {
            (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtOut(context, client, response)
        }catch (e:Exception){
            onException(response,e)
        }
    }


    private fun onException(response: IResponse,e:Exception){
        val realException = if (e is InvocationTargetException){
            e.targetException
        }else{
            e
        }
        val annotationBean = Core.getAnnotationBean(ExceptionRule::class.java)
        if (annotationBean!=null){
            annotationBean::class.java.declaredMethods.forEach { m->
                val handlerAnno = ReflectUtils.getAnnotation(m,ExceptionHandler::class.java)
                if (handlerAnno!=null){
                    if (handlerAnno.exceptionClasses==realException::class){
                        val invokeMethod = try {
                            InjectUtils.invokeMethod(annotationBean, m, provider = arrayOf(realException))
                        }catch (e:Exception){
                            ResponseUtils.dispatcherError(response,500)
                            return
                        }
                        if (invokeMethod!=null){
                            ResponseUtils.dispatcherReturn(true,response,invokeMethod)
                        }else{
                            ResponseUtils.dispatcherError(response,500)
                        }
                        return
                    }
                }
            }
        }
        ResponseUtils.dispatcherError(response,500)
    }
}

