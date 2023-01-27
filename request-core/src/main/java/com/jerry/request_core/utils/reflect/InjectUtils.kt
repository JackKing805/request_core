package com.jerry.request_core.utils.reflect

import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.factory.InjectFactory
import java.lang.reflect.Method
import java.lang.reflect.Parameter

object InvokeUtils {
    @Throws(NullPointerException::class)
    fun invokeMethod(any: Any,method: Method,provider:Array<Any> = arrayOf()):Any?{
        val args = mutableListOf<Any>()
        val parameters = method.parameters
        parameters.forEach {
            val inject = ReflectUtils.getAnnotation(it,Inject::class.java)
            val injectBean = if (inject!=null){
                InjectFactory.getInjectBean(
                    it.type,
                    ReflectUtils.getAnnotation(it, Inject::class.java)
                )?.bean
            }else{
                provider.find { a-> it.type.isAssignableFrom(a::class.java) }?:InjectFactory.getInjectBean(
                    it.type,
                    ReflectUtils.getAnnotation(it, Inject::class.java)
                )?.bean
            }
            if (injectBean!=null){
                args.add(injectBean)
            }else{
                throw NullPointerException("inject failure not have bean ${it.type}")
            }
        }
        return method.invoke(any,*args.toTypedArray())
    }


    @Throws(NullPointerException::class)
    fun invokeMethod(any: Any,method: Method,provider:(pa:Parameter)->Any?):Any?{
        val args = mutableListOf<Any>()
        val parameters = method.parameters
        parameters.forEach {
            val inject = ReflectUtils.getAnnotation(it,Inject::class.java)
            val injectBean = if (inject!=null){
                InjectFactory.getInjectBean(
                    it.type,
                    ReflectUtils.getAnnotation(it, Inject::class.java)
                )?.bean
            }else{
                provider(it)?:InjectFactory.getInjectBean(
                    it.type,
                    ReflectUtils.getAnnotation(it, Inject::class.java)
                )?.bean
            }
            if (injectBean!=null){
                args.add(injectBean)
            }else{
                throw NullPointerException("inject failure not have bean ${it.type}")
            }
        }
        return method.invoke(any,*args.toTypedArray())
    }
}