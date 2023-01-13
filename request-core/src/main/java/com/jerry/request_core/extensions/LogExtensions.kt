package com.jerry.request_core.extensions

import android.util.Log
import kotlin.reflect.KProperty

internal fun String.log(tag:String="Server"){
    Log.e(tag,this)
}