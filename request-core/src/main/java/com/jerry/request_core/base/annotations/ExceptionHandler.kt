package com.jerry.request_core.base.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExceptionHandler(
    val exceptionClasses: KClass<out Throwable>
)
