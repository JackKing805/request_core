package com.jerry.request_core.exception

class NotSupportPathParamsTypeException(type:Class<*>):Exception("$type is ill,path params not support")