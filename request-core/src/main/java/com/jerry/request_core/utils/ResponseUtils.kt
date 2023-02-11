package com.jerry.request_core.utils

import android.os.Environment
import com.blankj.utilcode.util.GsonUtils
import com.jerry.request_core.Core
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.extensions.byteArrayFromAssets
import com.jerry.request_core.extensions.byteArrayFromRaw
import com.jerry.request_core.extensions.getFileMimeType
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtContentType
import java.io.File


internal object ResponseUtils{
    private val rootDir = Environment.getExternalStorageDirectory().absolutePath

    fun dispatcherError(response: Response, errorCode: Int) {
        response.setResponseStatusCode(errorCode)
        response.write(RtCode.match(errorCode).message, RtContentType.TEXT_HTML.content)
    }

    fun dispatcherReturn(
        isRestController: Boolean,
        response: Response,
        returnObject: Any?
    ) {
        if (returnObject == null) {
            dispatcherError(response,  RtCode._500.code)
            return
        }
        if (returnObject is Unit) {
            dispatcherError(response,  RtCode._500.code)
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
                        if (returnObject.startsWith("redirect:")){//重定向链接
                            val newPath = returnObject.replace("redirect:","")

                            val location = if (
                                newPath.startsWith("https:") ||
                                newPath.startsWith("http:") ||
                                newPath.startsWith("ftp:") ||
                                newPath.startsWith("ws:")
                            ){
                                newPath
                            }else{
                                response.getPackage().getRootAbsolutePath() + if (newPath.startsWith("/")){
                                    newPath.substring(1)
                                }else{
                                    newPath
                                }
                            }
                            response.setResponseStatusCode(RtCode._302.code)
                            response.setContentType(response.getPackage().getHeader().getContentType())
                            response.setHeader("Location",location)
                            response.sendHeader()
                        } else if(returnObject.startsWith("{")&& returnObject.endsWith("}")){
                            response.write(returnObject, RtContentType.JSON.content)
                        }else {
                            response.write(returnObject, RtContentType.TEXT_PLAIN.content)
                        }
                    }else{
                        when(fileType.fileType){
                            FileType.SD_CARD -> {
                                if (fileType.fileName.startsWith(rootDir)){
                                    response.writeFile(File(fileType.fileName))
                                }else{
                                    response.writeFile(File(rootDir,fileType.fileName))
                                }
                            }
                            FileType.ASSETS -> {
                                val byteArrayFromAssets = fileType.fileName.byteArrayFromAssets()
                                if (byteArrayFromAssets!=null){
                                    response.write(byteArrayFromAssets,fileType.fileName.getFileMimeType())
                                }else{
                                    dispatcherError(response,RtCode._404.code)
                                }
                            }
                            FileType.APP_FILE -> {
                                response.writeFile(File(Core.getApplication().filesDir,fileType.fileName))
                            }
                            FileType.RAW -> {
                                val raw = fileType.fileName.toInt().byteArrayFromRaw()
                                if (raw!=null){
                                    response.write(raw,fileType.fileName.getFileMimeType())
                                }else{
                                    dispatcherError(response, RtCode._404.code)
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
