package com.jerry.request_core.extensions

import com.blankj.utilcode.util.GsonUtils
import com.jerry.rt.core.http.protocol.RtMimeType
import com.jerry.request_core.Core
import com.jerry.request_core.exception.NotSupportPathParamsTypeException
import com.jerry.request_core.factory.ControllerMapper
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
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
        path.contains(".") && !path.endsWith(".") && !path.startsWith(".")
    }
}

fun URI.resourcesName(): String {
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