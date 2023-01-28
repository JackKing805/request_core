package com.jerry.request_core.request

import android.content.Context
import android.util.Log
import com.jerry.request_base.annotations.Bean
import com.jerry.request_base.annotations.Configuration
import com.jerry.request_base.annotations.Inject
import com.jerry.request_core.R
import com.jerry.request_core.TestApp
import com.jerry.request_core.config.Config

@Configuration
class A {
    @Bean()
    fun getFun(@Inject("fuckC") c:C):B{
        Log.e("ADSAD","getFun")
        return B("${c.name}:from b")
    }

    @Bean("fuckC")
    fun getFun2():C{
        Log.e("ADSAD","getFun2")
        return C("from c")
    }
}

data class C(
    val name: String
)

data class B(
    val name:String
)