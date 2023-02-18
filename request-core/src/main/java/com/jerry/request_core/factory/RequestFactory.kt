package com.jerry.request_core.factory

import android.content.Context
import com.jerry.request_base.bean.ControllerReferrer
import com.jerry.request_base.bean.ControllerResult
import com.jerry.request_base.bean.ResourceReferrer
import com.jerry.request_core.extensions.ifIsResourcesName
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

/**
 * configRegister 会提前注册
 */
internal object RequestFactory {
    fun onRequestPre(
        context: Context,
        request: Request,
        response: Response,
        controllerMapper: ControllerMapper?
    ): Boolean {
        val referer = request.getPackage().getHeader().getHeaderValue("Referer", "")
        val toController = if (controllerMapper != null) {
            if (request.getPackage().getRelativePath() == controllerMapper.path) {
                true
            } else {
                referer.isEmpty()
            }
        } else {
            false
        }
        if (toController) {
            val controllerReferrer = ControllerReferrer(
                controllerMapper!!.path,
                controllerMapper.instance,
                controllerMapper.method
            )
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onRequestPre(context, request, response, controllerReferrer)) {
                    return false
                }
            }
        } else {
            val resourcesPath = ifIsResourcesName(request)
            val resourceReferrer = ResourceReferrer(referer, resourcesPath)
            InjectFactory.getConfigRegisters().forEach {
                if (!it.instance.onResourceRequest(context, request, response, resourceReferrer)) {
                    return false
                }
            }
        }
        return true
    }

    fun onRequestEnd(
        context: Context,
        request: Request,
        response: Response,
        controllerMapper: ControllerMapper,
        result: Any
    ): Boolean {
        InjectFactory.getConfigRegisters().forEach {
            val controllerReferrer = ControllerReferrer(
                controllerMapper.path,
                controllerMapper.instance,
                controllerMapper.method
            )
            val controllerResult = ControllerResult(controllerReferrer, result)
            if (!it.instance.onRequestEnd(context, request, response, controllerResult)) {
                return false
            }
        }
        return true
    }

    fun matchController(path: String): ControllerMapper? {
        return InjectFactory.getController(path)
    }
}


