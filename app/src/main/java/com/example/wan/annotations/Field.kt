package com.example.wan.annotations

/**
* [Field]注解用在API接口定义的方法的参数上
* */

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val value: String)
