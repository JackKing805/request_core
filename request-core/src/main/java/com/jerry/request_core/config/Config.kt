package com.jerry.request_core.config

data class Config(
    val appIcon:Int,
    val showStatusService:Boolean = true,
    val resourcesPrefix:String = "assets",//静态文件文件地址
    val pageSuffix:String = ".html"//布局文件后缀名，response会根据后缀名判断是不是布局文件
)
