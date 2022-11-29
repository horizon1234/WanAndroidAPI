package com.example.wan.launch

import kotlinx.coroutines.delay
import kotlin.coroutines.*

fun main(){
    Thread.sleep(2000L)
    testCreateCoroutine()
}

/**
 * 这里的block类型是"suspend () -> String"
 *
 * 这里我们秉承 单方法接口 <--> 高阶函数 <--> lambda这种关系
 * */
val block = suspend {
    println("Hello")
    delay(1000L)
    println("Kotlin")
    "Result"
}

/**
 * 这里调用了[startCoroutine]扩展函数，这个扩展函数是 suspend () -> T 的
 * 扩展函数。
 *
 * [Continuation]有2个作用，一个是实现挂起函数时用来向外传递数据；一个是以匿名
 * 内部类的方式来接收一个挂起函数的值。
 * */
private fun testCreateCoroutine(){
    val continuation = object : Continuation<String>{
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<String>) {
            println("Result is ${result.getOrNull()}")
        }
    }

    val c = block.createCoroutine(continuation)
    c.resume(Unit)
}