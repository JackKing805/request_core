package com.jerry.request_core.exception

class PathParamsConvertErrorException(param:String,type:Class<*>):Exception("$param can't convert to $type")