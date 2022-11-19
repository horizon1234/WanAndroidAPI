package com.example.wan.exception

import kotlinx.coroutines.*

/**
 * Kotlin的挂起函数，是可以自动响应协程的取消的
 * */
fun main() = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 0
        while (true){
            //这里改成delay
            delay(500)
            i ++
            println("i = $i")
        }
    }
    delay(2000)
    //取消job
    job.cancel()
    //等待job执行完成
    job.join()

    println("End")
}