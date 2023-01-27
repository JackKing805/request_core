package com.jerry.request_core

import android.app.Application
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.request.A
import com.jerry.request_core.request.TestController

class TestApp : Application() {
    companion object{
        lateinit var app:TestApp
    }

    init {
        app = this
    }

    override fun onCreate() {
        super.onCreate()
        RequestUtils.init(this, mutableListOf(A::class.java,TestController::class.java))
    }
}