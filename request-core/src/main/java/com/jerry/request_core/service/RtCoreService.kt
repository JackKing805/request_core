package com.jerry.request_core.service

import android.content.Context
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.constants.Status
import com.jerry.request_core.delegator.RequestDelegator
import com.jerry.request_core.extensions.log
import com.jerry.rt.core.RtCore
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtResponse
import com.jerry.rt.interfaces.RtCoreListener
import java.io.InputStream

object RtCoreService {
    private var defaultStatus:RtCoreListener.Status? = null
        set(value) {
            value?.let {
                RequestUtils.getIRequestListener()?.onStatusChange(Status.rtStatusToStats(it))
            }?:run {
                RequestUtils.getIRequestListener()?.onStatusChange(Status.rtStatusToStats(null))
            }
            onStatus?.invoke(value)
            field = value
        }

    private var onStatus:((RtCoreListener.Status?)->Unit)?=null

    fun startRtCore(context: Context,onStatus:((RtCoreListener.Status?)->Unit)?=null){
        this.onStatus = onStatus

        RtCore.instance.run(RequestUtils.getRtConfig(), statusListener = object : RtCoreListener {
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

                    override suspend fun onInputStreamIn(client: Client, inputStream: InputStream) {

                    }

                    override suspend fun onMessage(
                        client: Client,
                        request: Request,
                        response: Response
                    ) {
                        "onMessage".log()
                        RequestDelegator.dispatcher(context,request,response)
                    }

                    override fun onRtClientIn(client: Client, response: RtResponse) {
                        RequestDelegator.onRtIn(context,client,response)
                    }

                    override fun onRtClientOut(client: Client, rtResponse: RtResponse) {
                        RequestDelegator.onRtOut(context,client,rtResponse)
                    }

                    override suspend fun onRtHeartbeat(client: Client) {

                    }

                    override suspend fun onRtMessage(request: Request, rtResponse: RtResponse) {
                        RequestDelegator.onRtMessage(context,request,rtResponse)
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
        })
    }

    fun stopRtCore(){
        RtCore.instance.stop()
        this.onStatus = null
    }
}