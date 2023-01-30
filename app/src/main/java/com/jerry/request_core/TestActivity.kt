package com.jerry.request_core

import android.os.Bundle
import androidx.activity.ComponentActivity

class TestActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Core.startServer()
    }
}