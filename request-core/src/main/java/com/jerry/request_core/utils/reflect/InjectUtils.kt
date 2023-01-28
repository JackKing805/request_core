package com.jerry.request_core.utils.reflect

import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.factory.InjectFactory
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.Parameter

object InvokeUtils {
    @Throws(NullPointerException::class)
    fun invokeMethod(any: Any,method: Method,provider:Array<Any> = arrayOf()):Any?{
        val args = mutableListOf<Any>()
        val parameters = method.parameters
        parameters.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it,Inject::class.java)
            val injectBean = if (haveInject){
                getInjectBean(
                    it,
                    it.type
                )
            }else{
                provider.find { a-> it.type.isAssignableFrom(a::class.java) }?:throw NullPointerException("please provider resources :${it.type}")
            }
            args.add(injectBean)
        }
        return method.invoke(any,*args.toTypedArray())
    }


    @Throws(NullPointerException::class)
    fun invokeMethod(any: Any,method: Method,provider:(pa:Parameter)->Any?):Any?{
        val args = mutableListOf<Any>()
        val parameters = method.parameters
        parameters.forEach {
            val haveInject = ReflectUtils.haveAnnotation(it,Inject::class.java)
            val injectBean = if (haveInject){
                getInjectBean(
                    it,
                    it.type
                )
            }else{
                provider(it)?:throw NullPointerException("please provider resources :${it.type}")
            }
            args.add(injectBean)
        }
        return method.invoke(any,*args.toTypedArray())
    }

    private fun getInjectBean(any: AnnotatedElement,clazz: Class<*>):Any{
        val inject = ReflectUtils.haveAnnotation(any,Inject::class.java)
        if (!inject){
            throw NullPointerException("please use inject annotation to find bean in bean factory")
        }

        return InjectFactory.getBeanByInjectOrClass(
            any,
            clazz
        )?.bean?:throw NullPointerException("please provider bean:${clazz}")
    }
}