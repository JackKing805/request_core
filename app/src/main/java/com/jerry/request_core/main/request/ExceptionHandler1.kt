package com.jerry.request_core.main.request

import android.util.Log
import com.jerry.request_core.base.annotations.ExceptionHandler
import com.jerry.request_core.base.annotations.ExceptionRule
import com.jerry.request_core.exception.PathParamsConvertErrorException

@ExceptionRule
class ExceptionHandler1 {

    @ExceptionHandler(NullPointerException::class)
    fun onNull(e:NullPointerException):String{
        e.printStackTrace()
        Log.e("ADSAD","onNull:$e")
        return "onNull"
    }

    @ExceptionHandler(PathParamsConvertErrorException::class)
    fun onIll(e:PathParamsConvertErrorException):String{
        Log.e("ADSAD","onNull:$e")
        return "NotSupportPathParamsTypeException"
    }
}