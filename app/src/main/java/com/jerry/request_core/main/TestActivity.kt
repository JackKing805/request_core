package com.jerry.request_core.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jerry.request_core.Core

class TestActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Core.startServer()
    }
}