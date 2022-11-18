package com.example.wan.ktHttp.annotations

/**
 * [GET]注解用于标记该方法的调用是HTTP的GET方式
 * */

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(val value: String)
