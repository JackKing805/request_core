package com.jerry.request_core.anno

import com.jerry.request_base.annotations.Bean
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExceptionHandler(
    val exceptionClasses: KClass<out Throwable>
)
