package com.jerry.request_core.constants

/**
 * @className: FileType
 * @author: Jack
 * @date: 1/9/23
 **/
enum class FileType(val content:String) {
    SD_CARD("sd://"),//example: sd://path
    ASSETS("assets://"),//example: assets://path
    RAW("raw://"),//example: raw://id
    APP_FILE("file://");//example: file://path

    companion object{
        fun matchFileType(str:String): ResultFileType?{
            return if (str.startsWith(SD_CARD.content)){
                return ResultFileType(SD_CARD, getFilePath(SD_CARD,str))
            }else if(str.startsWith(ASSETS.content)){
                return ResultFileType(ASSETS, getFilePath(ASSETS,str))
            }else if(str.startsWith(APP_FILE.content)){
                return ResultFileType(APP_FILE, getFilePath(APP_FILE,str))
            }else if(str.startsWith(RAW.content)){
                return ResultFileType(RAW, getFilePath(RAW,str))
            }else{
                null
            }
        }

        private fun getFilePath(fileType: FileType, str:String):String{
            return str.substring(fileType.content.length)
        }
    }
}

data class ResultFileType(
    val fileType: FileType,
    val str:String
)