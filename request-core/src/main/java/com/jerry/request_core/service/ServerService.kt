package com.jerry.request_core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.RtCore
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.interfaces.RtCoreListener
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.constants.Status
import com.jerry.request_core.delegator.RequestDelegator
import com.jerry.request_core.extensions.log
import com.jerry.rt.core.http.pojo.RtResponse
import java.io.InputStream

internal class ServerService: Service() {
    companion object{
        fun run(context: Context,run:Boolean){
            ContextCompat.startForegroundService(context,Intent(context, ServerService::class.java).apply {
                putExtra("open",run)
            })
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        updateNotification(RtCoreListener.Status.STOPPED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val open = it.getBooleanExtra("open",false)
            if (open){
                startServer()
            }else{
                stopServer()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startServer(){
        RtCoreService.startRtCore(this){
            updateNotification(it?:RtCoreListener.Status.STOPPED)
        }
    }

    private fun stopServer(){
        RtCoreService.stopRtCore()
    }

    private fun updateNotification(status:RtCoreListener.Status){
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("Server","Server",NotificationManager.IMPORTANCE_DEFAULT)
        channel.enableLights(false)
        channel.enableVibration(false)
        channel.setSound(null,null)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(channel)

        val config = RequestUtils.getConfig()
        val notification = NotificationCompat.Builder(this,"Server")
            .setSmallIcon(config.appIcon)
            .setLargeIcon(BitmapFactory.decodeResource(resources,config.appIcon))
            .setWhen(System.currentTimeMillis())
            .setContentTitle("Server Status")
            .setContentText(status.name)
            .build()
        startForeground(1,notification)
    }
}