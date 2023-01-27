package com.jerry.request_core

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jerry.request_core.RequestUtils

class TestActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RequestUtils.startServer()
    }
}