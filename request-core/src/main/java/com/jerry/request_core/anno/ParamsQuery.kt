package com.jerry.request_core.anno

import com.jerry.request_base.annotations.Bean
import kotlin.reflect.KClass

/**
 * /q/b/{id}
 * 根据名字获取path中的参数
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamsQuery(
    val name: String
)
