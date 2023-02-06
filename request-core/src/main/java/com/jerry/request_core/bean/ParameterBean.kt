package com.jerry.request_core.bean

import com.jerry.request_core.exception.NotSupportPathParamsTypeException

//将url中的参数转化喂bean
data class ParameterBean(
    val parameters:Map<String,String>
){
    fun find(id:String,clazz: Class<*>):Any?{
        val s = parameters[id]
        if (s!=null){
            return when(clazz){
                Int::class.javaObjectType,
                Int::class.java->s.toInt()
                Long::class.javaObjectType,
                Long::class.java->s.toLong()
                String::class.javaObjectType,
                String::class.java-> s
                Boolean::class.javaObjectType,
                Boolean::class.java->s.toBoolean()
                Float::class.javaObjectType,
                Float::class.java->s.toFloat()
                Double::class.javaObjectType,
                Double::class.java->s.toDouble()
                else-> throw NotSupportPathParamsTypeException(clazz)
            }
        }
        return null
    }
}
