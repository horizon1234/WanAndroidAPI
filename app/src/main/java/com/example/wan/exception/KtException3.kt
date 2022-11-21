package com.example.wan.exception

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 利用[CoroutineExceptionHandler]创建异常处理器，lambda中
 * 就是发生异常的[CoroutineContext]和[Throwable]
 * */
val myExceptionHandler = CoroutineExceptionHandler{_, throwable ->
    println("Catch exception: $throwable")
}

fun main() = runBlocking{
    //操作符重载操作，支持这样
    val scope = CoroutineScope(coroutineContext)
    //复杂的协程嵌套
    scope.launch {
        async {
            delay(1000)
        }

        launch {
            delay(2000)
            //仅对该协程进行设置Handler
            launch(myExceptionHandler) {
                delay(1000)
                //制作异常
                1 / 0
            }
        }
    }

    delay(10000)
    println("End")

/**
 * 运行结果：
 * 崩溃: Exception in thread "main" java.lang.ArithmeticException: / by zero
 * */
}