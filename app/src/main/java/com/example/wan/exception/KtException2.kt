package com.example.wan.exception

import kotlinx.coroutines.*

/**
 * 这里使用一个特殊的[SupervisorJob]，来构建
 * [CoroutineScope]
 * */
fun main() = runBlocking {
    //创建一个scope
    val scope = CoroutineScope(SupervisorJob())
    val deferred = scope.async {
        delay(1000L)
        1 / 0
    }
    //捕获异常
    try {
        deferred.await()
    }catch (e: ArithmeticException){
        println("Catch $e")
    }

    delay(5000L)
    println("End")
}

/**
 * 运行结果：
 * Catch java.lang.ArithmeticException: / by zero
 *  End
 * */