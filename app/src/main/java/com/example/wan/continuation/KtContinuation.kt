package com.example.wan.continuation

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.intrinsics.*

//fun main(){
//    val func = ::getLengthSuspend as (String,Continuation<Int>) -> Any?
//    func("Hello",object : Continuation<Int>{
//        override val context: CoroutineContext
//            get() = EmptyCoroutineContext
//
//        override fun resumeWith(result: Result<Int>) {
//            println(result.getOrNull())
//        }
//    })
//    //防止程序退出
//    Thread.sleep(5000)
//}

/**
 * 使用[suspendCancellableCoroutine]实现挂起函数，模拟耗时后返回文本的长度
 * @param text 测试文本
 * */
suspend fun getLengthSuspend(text: String): Int =
    suspendCancellableCoroutine { continuation ->
        thread {
            //模拟耗时
            Thread.sleep(2000)
            continuation.resume(text.length)
        }
    }

/**
 * 测试实现伪挂起函数，这里值得注意的是[suspendCoroutineUninterceptedOrReturn]的函数类型
 * [block]是通过[crossinline]修饰的，通过该关键字修饰的高阶函数类型，在里面是不能直接使用[return]的，
 * 必须要return特定的作用域。
 * */
private suspend fun testNoSuspendCoroutine() =
    suspendCoroutineUninterceptedOrReturn<String> { continuation ->
        return@suspendCoroutineUninterceptedOrReturn "Hello"
    }

/**
 * 这里真正使用[Continuation]来往挂起函数外传递了值
 * 同时，函数返回值范围了挂起标志位
 * */
private suspend fun testSuspendCoroutine() =
    suspendCoroutineUninterceptedOrReturn<String> { continuation ->
        thread {
            Thread.sleep(1000)
            continuation.resume("Hello")
        }
        return@suspendCoroutineUninterceptedOrReturn kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
    }

fun main() = runBlocking {
    val length = testSuspendCoroutine()
    println(length)
}