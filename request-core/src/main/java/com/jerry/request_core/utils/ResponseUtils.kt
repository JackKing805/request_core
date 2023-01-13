package com.jerry.request_core.utils

import android.content.Context
import android.os.Environment
import com.blankj.utilcode.util.GsonUtils
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.request_core.RequestUtils
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.extensions.byteArrayFromAssets
import com.jerry.request_core.extensions.byteArrayFromRaw
import com.jerry.request_core.extensions.getFileMimeType
import java.io.File


object ResponseUtils{
    private val rootDir = Environment.getExternalStorageDirectory().absolutePath

    fun dispatcherError(context: Context, request: Request, response: Response, errorCode: Int) {
        response.setStatusCode(errorCode)
        response.write(RtCode.match(errorCode).message, RtContentType.TEXT_HTML.content)
    }

    fun dispatcherReturn(
        context: Context,
        isRestController: Boolean,
        request: Request,
        response: Response,
        returnObject: Any?
    ) {
        if (returnObject == null) {
            dispatcherError(context,request,response, 500)
            return
        }
        if (returnObject is Unit) {
            dispatcherError(context,request,response, 500)
        } else {
            if (isRestController) {
                if (returnObject is String) {
                    response.write(returnObject, RtContentType.JSON.content)
                } else {
                    response.write(GsonUtils.toJson(returnObject), RtContentType.JSON.content)
                }
            } else {
                if (returnObject is String) {
                    val fileType = FileType.matchFileType(returnObject)
                    if (fileType==null){
                        if(returnObject.startsWith("{")&& returnObject.endsWith("}")){
                            response.write(returnObject, RtContentType.JSON.content)
                        }else {
                            response.write(returnObject, returnObject.getFileMimeType())
                        }
                    }else{
                        when(fileType.fileType){
                            FileType.SD_CARD -> {
                                if (fileType.str.startsWith(rootDir)){
                                    response.writeFile(File(fileType.str))
                                }else{
                                    response.writeFile(File(rootDir,fileType.str))
                                }
                            }
                            FileType.ASSETS -> {
                                val byteArrayFromAssets = fileType.str.byteArrayFromAssets()
                                if (byteArrayFromAssets!=null){
                                    response.write(byteArrayFromAssets,fileType.str.getFileMimeType())
                                }else{
                                    dispatcherError(context,request,response,404)
                                }
                            }
                            FileType.APP_FILE -> {
                                response.writeFile(File(RequestUtils.getApplication().filesDir,fileType.str))
                            }
                            FileType.RAW -> {
                                val raw = fileType.str.toInt().byteArrayFromRaw()
                                if (raw!=null){
                                    response.write(raw,fileType.str.getFileMimeType())
                                }else{
                                    dispatcherError(context,request,response,404)
                                }
                            }
                        }
                    }
                } else if (returnObject is File) {
                    response.writeFile(returnObject)
                } else {
                    response.write(GsonUtils.toJson(returnObject), RtContentType.TEXT_PLAIN.content)
                }
            }
        }
    }
}