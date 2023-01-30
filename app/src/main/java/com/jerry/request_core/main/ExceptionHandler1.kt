package com.jerry.request_core.main

import android.util.Log
import com.jerry.request_core.anno.ExceptionHandler
import com.jerry.request_core.anno.ExceptionRule

@ExceptionRule
class ExceptionHandler1 {

    @ExceptionHandler(NullPointerException::class)
    fun onNull(e:NullPointerException){
        Log.e("ADSAD","onNull:$e")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIll(e:IllegalArgumentException){
        Log.e("ADSAD","onNull:$e")
    }
}