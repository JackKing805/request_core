package com.jerry.request_core.delegator

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.jerry.request_core.Core
import com.jerry.request_core.anno.ExceptionHandler
import com.jerry.request_core.anno.ExceptionRule
import com.jerry.request_core.anno.ParamsQuery
import com.jerry.request_core.anno.PathQuery
import com.jerry.request_core.bean.ParameterBean
import com.jerry.request_core.bean.ParametersBeanCreator
import com.jerry.request_core.exception.InvokeMethodException
import com.jerry.request_core.extensions.pathParams
import com.jerry.request_core.extensions.toBasicTargetType
import com.jerry.request_core.extensions.toObject
import com.jerry.request_core.factory.InjectFactory
import com.jerry.request_core.factory.RequestFactory
import com.jerry.request_core.utils.ResponseUtils
import com.jerry.request_core.utils.reflect.InjectUtils
import com.jerry.request_core.utils.reflect.ReflectUtils
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.request.model.MultipartFile
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
            if (controllerMapper.requestMethod.content.equals(request.getPackage().getRequestMethod(), true)) {
                try {
                    val multipartFormData = request.getMultipartFormData()
                    val pbBean = ParametersBeanCreator(request).create()
                    multipartFormData?.getParameters()?.let {
                        pbBean.add(it)
                    }
                    val body = request.getByteBody()

                    val invoke = try {
                        InjectUtils.invokeMethod(
                            controllerMapper.instance,
                            controllerMapper.method
                        ) {
                            val pathQuery = ReflectUtils.getAnnotation(it,PathQuery::class.java)
                            if (pathQuery!=null) {
                                if (controllerMapper.pathParam==null){
                                    throw NullPointerException("this ${request.getPackage().getRelativePath()} not support path request")
                                }else{
                                    val pathParams = controllerMapper.pathParams(requestURI.path)
                                    if (pathParams!=null && controllerMapper.pathParam.name == pathQuery.name){
                                        pathParams.toBasicTargetType(it.type)
                                    }else{
                                        null
                                    }
                                }
                            }else {
                                val paramQuery = ReflectUtils.getAnnotation(it, ParamsQuery::class.java)
                                (if (paramQuery != null) {
                                    pbBean.find(paramQuery.name, it.type)
                                } else {
                                    null
                                }) ?: when (it.parameterizedType) {
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
                                        GsonUtils.getListType(MultipartFile::class.java) -> multipartFormData?.getFiles()?.map { it.value }
                                        GsonUtils.getMapType(String::class.java, MultipartFile::class.java) -> multipartFormData?.getFiles()?.map { it.value }
                                        MultipartFile::class.java -> {
                                            if (paramQuery != null) {
                                                multipartFormData?.getFile(paramQuery.name)
                                            } else {
                                                multipartFormData?.getFiles()?.map { it.value }
                                                    ?.first()
                                            }
                                        }
                                        else -> {
                                            body?.toObject(it.type)
                                        }
                                    }
                            }
                        }
                    } catch (e: InvokeMethodException) {
                        e.printStackTrace()
                        ResponseUtils.dispatcherError(response,  RtCode._500.code)
                        return
                    } catch (e: Exception) {
                        onException(response, e)
                        return
                    }
                    if (invoke == null) {
                        ResponseUtils.dispatcherError(response, RtCode._500.code)
                    } else {
                        if (RequestFactory.onRequestEnd(context, request, response,controllerMapper,invoke)) {
                            ResponseUtils.dispatcherReturn(
                                controllerMapper.isRestController,
                                response,
                                invoke
                            )
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    ResponseUtils.dispatcherError(response, RtCode._502.code)
                }
                return
            } else {
                ResponseUtils.dispatcherError(response, RtCode._405.code)
                return
            }
        }
        ResponseUtils.dispatcherError(response, RtCode._404.code)
    }


    internal fun onRtIn(context: Context, client: Client,request: Request, response: Response) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtIn(client,request,response)){
                    return
                }
            }
        } catch (e: Exception) {
            onException(response, e)
        }
    }

    internal fun onRtMessage(context: Context, request: Request, response: Response) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtMessage(request,response)){
                    return
                }
            }
        } catch (e: Exception) {
            onException(response, e)
        }
    }

    internal fun onRtOut(context: Context, client: Client) {
        try {
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRtOut(client)){
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

