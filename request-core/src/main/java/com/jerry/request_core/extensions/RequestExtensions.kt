package com.jerry.request_core.extensions

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.jerry.request_core.Core
import com.jerry.request_core.constants.FileType
import com.jerry.request_core.exception.NotSupportPathParamsTypeException
import com.jerry.request_core.exception.PathParamsConvertErrorException
import com.jerry.request_core.factory.ControllerMapper
import com.jerry.request_core.utils.JavaUtils
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.protocol.RtMimeType
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset

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

fun String.isAssetsExists():Boolean{
    return try {
        Core.getApplication().assets.open(this).close()
        true
    } catch (e: Exception) {
        false
    }
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

fun Int.rawExists(): Boolean {
    return  try {
        Core.getApplication().resources.openRawResource(this).close()
        true
    } catch (e: Exception) {
        false
    }

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

//获取相对路径，获取任何除路径以外的字符串
fun String.getJustPath():String{
    val uri = URI.create(this)

    var relPath = if (uri.path.length==1){
        uri.path
    }else{
        if (uri.path.endsWith("/")){
            uri.path.substring(0,uri.path.length-1)
        }else{
            uri.path
        }
    }

    if (uri.query!=null){
        relPath = relPath.replace("?${uri.query}","")
    }
    return relPath
}

fun URI?.parameterToArray(charset: Charset): Map<String, String> {
    return if (this == null) {
        emptyMap()
    } else {
        val map = mutableMapOf<String, String>()
        if (this.query != null) {
            if (this.query.isNotEmpty()){
                this.query.split("&").forEach {
                    val split = it.split("=")
                    map[split[0]] = URLDecoder.decode(split[1], charset.name())
                }
            }
        }
        map
    }
}

fun String.parameterToArray(charset: Charset):Map<String,String>{
    val map = mutableMapOf<String, String>()
    this.split("&").forEach {
        val split = it.split("=")
        map[split[0]] = URLDecoder.decode(split[1], charset.name())
    }
    return map
}


//fun Request.isResources(): Boolean {
//    val contentType = getPackage().getHeader().getContentType()
//    if (contentType.isEmpty()){
//        return false
//    }else{
//        if (
//            contentType == RtMimeType.matchContentType()
//        )
//
//    }
//}

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
            FileType.SD_CARD -> File(matchFileType.fileName).exists()
            FileType.ASSETS -> try {
                val open = context.assets.open(matchFileType.fileName)
                open.close()
                true
            }catch (e:Exception){
                false
            }
            FileType.RAW -> try {
                val openRawResource = context.resources.openRawResource(matchFileType.fileName.toInt())
                openRawResource.close()
                true
            }catch (e:Exception){
                false
            }
            FileType.APP_FILE -> File(matchFileType.fileName).exists()
        }
    }else{
        false
    }
}

//找出两个元素的交集
infix fun String.samePath(string:String):String{
    return JavaUtils.getSamePath(this,string)
}


fun String.toBasicTargetType(type:Class<*>):Any{
    return try {
        when(type){
            Int::class.javaObjectType,
            Int::class.java->this.toInt()
            Long::class.javaObjectType,
            Long::class.java->this.toLong()
            String::class.javaObjectType,
            String::class.java-> this
            Boolean::class.javaObjectType,
            Boolean::class.java->this.toBoolean()
            Float::class.javaObjectType,
            Float::class.java->this.toFloat()
            Double::class.javaObjectType,
            Double::class.java->this.toDouble()
            else-> throw NotSupportPathParamsTypeException(type)
        }
    }catch (e:NumberFormatException){
        e.printStackTrace()
        throw PathParamsConvertErrorException(this,type)
    }
}

fun ifIsResourcesName(request: Request):String{
    val rpackage = request.getPackage()
    val referer = rpackage.getHeader().getHeaderValue("Referer","")
    val query = rpackage.getRequestURI().query
    val fullPath = rpackage.getRequestAbsolutePath()
    val path = rpackage.getRelativePath()
    val root = rpackage.getRootAbsolutePath()
    var resourcesPath = if (referer.isEmpty() || referer==root){
        path
    }else{
        val same = fullPath samePath referer
        fullPath.replace(same,"")
    }
    if (resourcesPath.startsWith("/")){
        resourcesPath = resourcesPath.substring(1)
    }

    if (query!=null){
        resourcesPath = resourcesPath.replace("?$query","")
    }
    return resourcesPath
}