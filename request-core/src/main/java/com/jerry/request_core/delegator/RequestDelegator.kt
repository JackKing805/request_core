package com.jerry.request_core.delegator

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.request_core.Core
import com.jerry.request_core.additation.DefaultRtConfigRegister
import com.jerry.request_core.anno.ExceptionHandler
import com.jerry.request_core.anno.ExceptionRule
import com.jerry.request_core.anno.ParamsQuery
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.exception.IllParamsQueryException
import com.jerry.request_core.exception.InvokeMethodException
import com.jerry.request_core.exception.NotSupportPathParamsTypeException
import com.jerry.request_core.exception.PathParamsConvertErrorException
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.extensions.parameterToArray
import com.jerry.request_core.extensions.pathParams
import com.jerry.request_core.extensions.toObject
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ISessionManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * 请求分发
 */
internal object RequestDelegator {
    internal fun dispatcher(context: Context, request: Request, response: Response) {
        val requestURI = request.getPackage().getRequestURI()
        val controllerMapper = RequestFactory.matchController(requestURI.path)
        Core.getIRequestListener()?.onRequest(requestURI.path ?: "")


        try {
            if (!RequestFactory.onRequestPre(context, request, response, controllerMapper)) {
                return
            }
        } catch (e: Exception) {
            onException(response, e)
            return
        }


        if (controllerMapper != null) {
            if (controllerMapper.requestMethod.content.equals(request.getPackage().method, true)) {
                try {
                    val pbBean = ParameterBean(
                        request.getPackage()
                            .getRequestURI().parameterToArray()
                    )

                    val invoke = try {
                        InjectUtils.invokeMethod(
                            controllerMapper.instance,
                            controllerMapper.method
                        ) {
                            val paramQuery = ReflectUtils.getAnnotation(it, ParamsQuery::class.java)
                            if (paramQuery != null) {
                                if (controllerMapper.pathParam!=null){
                                    if (paramQuery.name == controllerMapper.pathParam.name){
                                        val pathParams = controllerMapper.pathParams(requestURI.path)
                                        if (pathParams!=null){
                                            try {
                                                when(it.type){
                                                    Int::class.javaObjectType,
                                                    Int::class.java->pathParams.toInt()
                                                    Long::class.javaObjectType,
                                                    Long::class.java->pathParams.toLong()
                                                    String::class.javaObjectType,
                                                    String::class.java-> pathParams
                                                    Boolean::class.javaObjectType,
                                                    Boolean::class.java->pathParams.toBoolean()
                                                    Float::class.javaObjectType,
                                                    Float::class.java->pathParams.toFloat()
                                                    Double::class.javaObjectType,
                                                    Double::class.java->pathParams.toDouble()
                                                    else-> throw NotSupportPathParamsTypeException(it.type)
                                                }
                                            }catch (e:NumberFormatException){
                                                e.printStackTrace()
                                                throw PathParamsConvertErrorException(pathParams,it.type)
                                            }
                                        }else{
                                            //如果path里面没有携带参数
                                            pbBean.find(paramQuery.name,it.type)
                                        }
                                    }else{
                                        //如果path里面没有查找到参数
                                        pbBean.find(paramQuery.name,it.type)
                                    }
                                }else{
                                    pbBean.find(paramQuery.name,it.type)
                                }
                            } else {
                                when (val clazz = it.type) {
                                    Context::class.java -> {
                                        context
                                    }
                                    Request::class.java -> {
                                        request
                                    }
                                    Response::class.java -> {
                                        response
                                    }
                                    ParameterBean::class.java -> {
                                        pbBean
                                    }
                                    else -> {
                                        val objects = request.getBody().toObject(clazz)
                                        objects
                                    }
                                }
                            }
                        }
                    } catch (e: InvokeMethodException) {
                        e.printStackTrace()
                        ResponseUtils.dispatcherError(response, 500)
                        return
                    } catch (e: Exception) {
                        onException(response, e)
                        return
                    }
                    if (invoke == null) {
                        ResponseUtils.dispatcherError(response, 500)
                    } else {
                        if (RequestFactory.onRequestEnd(context, request, response)) {
                            ResponseUtils.dispatcherReturn(
                                controllerMapper.isRestController,
                                response,
                                invoke
                            )
                        }
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


    internal fun onRtIn(context: Context, client: Client, response: Response) {
        try {
            (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtIn(
                context,
                client,
                response
            )
        } catch (e: Exception) {
            onException(response, e)
        }
    }

    internal fun onRtMessage(context: Context, request: Request, response: Response) {
        dispatcher(context, request, response)
    }

    internal fun onRtOut(context: Context, client: Client, response: Response) {
        try {
            (InjectFactory.getBeanBy { it.bean is DefaultRtConfigRegister }?.bean as? DefaultRtConfigRegister)?.onRtOut(
                context,
                client,
                response
            )
        } catch (e: Exception) {
            onException(response, e)
        }
    }


    private fun onException(response: Response, e: Exception) {
        val realException = if (e is InvocationTargetException) {
            e.targetException
        } else {
            e
        }

        if (requestExceptionHandler == null) {
            val annotationBean = Core.getAnnotationBean(ExceptionRule::class.java)
            if (annotationBean != null) {
                requestExceptionHandler = RequestExceptionHandler(annotationBean)
            }
        }


        if (requestExceptionHandler != null) {
            if (requestExceptionHandler!!.dealException(response, realException)) {
                return
            }
        }
        ResponseUtils.dispatcherError(response, 500)
    }

    private var requestExceptionHandler: RequestExceptionHandler? = null


    private class RequestExceptionHandler(private val ins: Any) {
        private class ExceptionMethod(
            val method: Method,
            val exceptionClass: KClass<out Throwable>
        )

        private val exceptionClasses = mutableListOf<ExceptionMethod>()

        init {
            val annotationBean = Core.getAnnotationBean(ExceptionRule::class.java)
            if (annotationBean != null) {
                annotationBean::class.java.declaredMethods.forEach { m ->
                    val handlerAnno = ReflectUtils.getAnnotation(m, ExceptionHandler::class.java)
                    if (handlerAnno != null) {
                        exceptionClasses.add(ExceptionMethod(m, handlerAnno.exceptionClasses))
                    }
                }
            }
        }


        fun dealException(response: Response, e: Throwable): Boolean {
            e.printStackTrace()
            val AllExce = exceptionClasses.find { it.exceptionClass == Exception::class }
            if (AllExce != null) {
                val invokeMethod = try {
                    InjectUtils.invokeMethod(ins, AllExce.method, provider = arrayOf(e))
                } catch (e: Exception) {
                    return false
                }
                if (invokeMethod != null) {
                    ResponseUtils.dispatcherReturn(true, response, invokeMethod)
                    return true
                }
            } else {
                val exce = exceptionClasses.find { it.exceptionClass == e::class }
                if (exce != null) {
                    val invokeMethod = try {
                        InjectUtils.invokeMethod(ins, exce.method, provider = arrayOf(e))
                    } catch (e: Exception) {
                        return false
                    }
                    if (invokeMethod != null) {
                        ResponseUtils.dispatcherReturn(true, response, invokeMethod)
                        return true
                    }
                }
            }
            return false
        }
    }
}

