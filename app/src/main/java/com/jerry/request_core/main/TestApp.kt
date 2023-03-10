package com.jerry.request_core.main

import android.app.Application
import com.jerry.request_core.Core
import com.jerry.request_core.main.request.A
import com.jerry.request_core.main.request.ExceptionHandler1
import com.jerry.request_core.main.request.TestController

class TestApp : Application() {
    companion object{
        lateinit var app: TestApp
    }

    init {
        app = this
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(
            this,
            mutableListOf(A::class.java, TestController::class.java, ExceptionHandler1::class.java)
        )
    }
}