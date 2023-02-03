package com.jerry.request_core.main.request

import android.util.Log
import com.jerry.request_core.anno.ExceptionHandler
import com.jerry.request_core.anno.ExceptionRule

@ExceptionRule
class ExceptionHandler1 {

    @ExceptionHandler(NullPointerException::class)
    fun onNull(e:NullPointerException):String{
        e.printStackTrace()
        Log.e("ADSAD","onNull:$e")
        return "onNull"
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIll(e:IllegalArgumentException):String{
        Log.e("ADSAD","onNull:$e")
        return "onIll"
    }
}