package com.jerry.request_core.service

import android.content.Context
import com.jerry.request_core.Core
import com.jerry.request_core.constants.Status
import com.jerry.request_core.delegator.RequestDelegator
import com.jerry.request_core.extensions.log
import com.jerry.request_core.factory.InjectFactory
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.RtCore
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.interfaces.RtCoreListener

internal object RtCoreService {
    private var defaultStatus:RtCoreListener.Status? = null
        set(value) {
            value?.let {
                Core.getIRequestListener()?.onStatusChange(Status.rtStatusToStats(it))
            }?:run {
                Core.getIRequestListener()?.onStatusChange(Status.rtStatusToStats(null))
            }
            onStatus?.invoke(value)
            field = value
        }

    private var onStatus:((RtCoreListener.Status?)->Unit)?=null

    fun startRtCore(context: Context,onStatus:((RtCoreListener.Status?)->Unit)?=null){
        this.onStatus = onStatus

        RtCore.instance.run(Core.getRtConfig(), statusListener = object : RtCoreListener {
            override fun onRtCoreException(exception: Exception) {
                exception.printStackTrace()
            }

            override fun onClientIn(client: Client) {
                "onClientIn".log()
                client.listen(object : ClientListener {
                    override fun onException(exception: Exception) {
                        exception.printStackTrace()
                        "onException:${exception.toString()}".log()
                    }

                    override suspend fun onMessage(
                        client: Client,
                        request: Request,
                        response: Response
                    ) {
                        "onMessage".log()
                        RequestDelegator.dispatcher(context,request,response)
                    }

                    override fun onRtClientIn(client: Client, response: Response) {
                        "onRtClientIn".log()
                        RequestDelegator.onRtIn(context,client,response)
                    }

                    override fun onRtClientOut(client: Client, response: Response) {
                        "onRtClientOut".log()
                        RequestDelegator.onRtOut(context,client,response)
                    }

                    override suspend fun onRtHeartbeat(client: Client) {
                        "onRtHeartbeat".log()
                    }

                    override suspend fun onRtMessage(request: Request, response: Response) {
                        "onRtMessage".log()
                        RequestDelegator.onRtMessage(context,request,response)
                    }
                })
            }

            override fun onClientOut(client: Client) {
                "onClientOut".log()

            }

            override fun onStatusChange(status: RtCoreListener.Status) {
                "onStatusChange:$status".log()
                defaultStatus = status

                if (defaultStatus==RtCoreListener.Status.STOPPED){
                    defaultStatus = null
                }
            }

            override fun onCreateContext(rtContext: RtContext) {
                InjectFactory.insertBean(rtContext)
                InjectFactory.insertBean(rtContext.getSessionManager())
            }

            override fun onDestroyContext(rtContext: RtContext) {
                InjectFactory.removeBean(rtContext.getSessionManager())
                InjectFactory.removeBean(rtContext)
            }
        })
    }

    fun stopRtCore(){
        RtCore.instance.stop()
        this.onStatus = null
    }
}