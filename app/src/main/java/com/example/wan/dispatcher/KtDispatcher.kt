package com.example.wan.dispatcher

import com.example.wan.exception.logX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main(){
    testLaunch()
    Thread.sleep(10000)
}

private fun testLaunch(){
    val scope = CoroutineScope(Job())
    scope.launch {
        logX("Hello")
        delay(1000)
        logX("Kotlin")
    }
}

/**
 * 输出结果：
 * ================================
Hello
Thread:DefaultDispatcher-worker-2
================================
================================
Kotlin
Thread:DefaultDispatcher-worker-2
================================
 * */