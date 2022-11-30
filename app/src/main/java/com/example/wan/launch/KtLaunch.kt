package com.example.wan.launch

import kotlinx.coroutines.*

fun main() {
    testCoroutine()
    Thread.sleep(2000)
}

private fun testCoroutine(){
    val scope = CoroutineScope(Job())
    val b: suspend CoroutineScope.() -> Unit = {
        println("Hello")
        delay(1000)
        println("Kotlin")
    }
    scope.launch(block = b)
}