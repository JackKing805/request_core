package com.jerry.request_core.extensions

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.jerry.rt.core.http.protocol.RtMimeType
import com.jerry.request_core.Core
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.exception.NotSupportPathParamsTypeException
import com.jerry.request_core.factory.ControllerMapper
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.net.URI
import java.net.URL
import java.net.URLDecoder

fun String.fromAssets(): String {
    val stringBuilder = StringBuilder()
    try {
        BufferedReader(InputStreamReader(Core.getApplication().assets.open(this))).use {
            var line = ""
            while (it.readLine().also { r ->
                    r?.let {
                        line = it
                    }
                } != null) {
                stringBuilder.append(line)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return stringBuilder.toString()
}


fun String.byteArrayFromAssets(): ByteArray? {
    val open = Core.getApplication().assets.open(this)
    val readBytes = try {
        open.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    try {
        open.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return readBytes
}

fun String.byteArrayFromAppFile(): ByteArray? {
    val file = File(Core.getApplication().filesDir, this)
    val inputStream = file.inputStream()
    val readBytes = try {
        inputStream.readBytes()
    } catch (e: Exception) {
        null
    }
    try {
        inputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return readBytes
}

fun String.byteArrayFromSdCard(): ByteArray? {
    val file = File(this)
    val inputStream = file.inputStream()
    val readBytes = try {
        inputStream.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    try {
        inputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return readBytes
}

fun Int.byteArrayFromRaw(): ByteArray? {
    val inputStream = Core.getApplication().resources.openRawResource(this)
    val readBytes = try {
        inputStream.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    try {
        inputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return readBytes
}

fun String.getFileMimeType(): String = RtMimeType.matchContentType(this).mimeType


fun ByteArray.toKotlinString() = String(this)

inline fun <reified T> ByteArray.toObject() = try {
    GsonUtils.fromJson(toKotlinString(), T::class.java)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun <T> ByteArray.toObject(clazz: Class<T>) = try {
    GsonUtils.fromJson(toKotlinString(), clazz)
} catch (e: Exception) {
    e.printStackTrace()
    null
}


fun String.matchUrlPath(localRegisterPath: String): Boolean {
    val url = URL(this)
    return url.path == localRegisterPath
}

fun URI.matchUrlPath(localRegisterPath: String): Boolean {
    return path == localRegisterPath
}

fun String.getJustPath():String{
    val uri = URI.create(this)
    return if (uri.path.length==1){
        uri.path
    }else{
        if (uri.path.endsWith("/")){
            uri.path.substring(0,uri.path.length-1)
        }else{
            uri.path
        }
    }
}

fun URI?.parameterToArray(): Map<String, String> {
    return if (this == null) {
        emptyMap()
    } else {
        val map = mutableMapOf<String, String>()
        if (this.query != null) {
            if (this.query.isNotEmpty()){
                this.query.split("&").forEach {
                    val split = it.split("=")
                    map[split[0]] = URLDecoder.decode(split[1], "UTF-8")
                }
            }
        }
        map
    }
}


fun URI.isResources(): Boolean {
    return if (path == null) {
        return false
    } else {
        if (query==null){
            if (path.endsWith("/")){
                false
            }else{
                val indexOf = path.lastIndexOf("/")
                if (indexOf!=-1){
                    val name = path.substring(indexOf+1)
                    name.contains(".") && !name.endsWith(".") && !name.startsWith(".")
                }else{
                    path.contains(".") && !path.endsWith(".") && !path.startsWith(".")
                }
            }
        }else{
            false
        }
    }
}

/**
 * 只能获取去除域名加端口后的路径，不精确
 */
fun URI.resourcesPath(): String {
    return if (path == null) {
        ""
    } else {
        if (path.startsWith("/")) {
            path.substring(1)
        } else {
            path
        }
    }
}


internal fun ControllerMapper.pathParams(fullUrl: String): String? {
    if (fullUrl==path){
        return null
    }

    if (fullUrl.endsWith("/")){
        if (fullUrl.substring(0,fullUrl.length-1)==path){
            return null
        }
    }

    if (pathParam != null) {
        var p = fullUrl.replace(path, "")
        if (p.isEmpty()) {
            return null
        }
        if (p.startsWith("/")){
            p= p.substring(1)
        }

        return p
    } else {
        return null
    }
}

fun Class<*>.isBasicType(): Boolean {
    return when (this) {
        Int::class.javaObjectType,
        Int::class.java,
        Long::class.javaObjectType,
        Long::class.java,
        String::class.javaObjectType,
        String::class.java,
        Boolean::class.javaObjectType,
        Boolean::class.java ,
        Float::class.javaObjectType,
        Float::class.java ,
        Double::class.javaObjectType,
        Double::class.java -> true
        else ->false
    }
}

fun String.isFileExists(context: Context):Boolean{
    val matchFileType = FileType.matchFileType(this)
    return if (matchFileType!=null){
        when(matchFileType.fileType){
            FileType.SD_CARD -> File(this).exists()
            FileType.ASSETS -> try {
                val open = context.assets.open(this)
                open.close()
                true
            }catch (e:Exception){
                false
            }
            FileType.RAW -> try {
                val openRawResource = context.resources.openRawResource(this.toInt())
                openRawResource.close()
                true
            }catch (e:Exception){
                false
            }
            FileType.APP_FILE -> File(this).exists()
        }
    }else{
        File(this).exists()
    }
}

//找出两个元素的交集
infix fun String.samePath(string:String):String{
    return JavaUtils.getSamePath(this,string)
}



