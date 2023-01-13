package com.jerry.request_core.constants

import com.jerry.rt.interfaces.RtCoreListener

enum class Status {
    STOPPED,
    RUNNING;
    companion object{
        fun rtStatusToStats(rt:RtCoreListener.Status?): Status {
            return if (rt==null){
                STOPPED
            }else{
                RUNNING
            }
        }
    }
}