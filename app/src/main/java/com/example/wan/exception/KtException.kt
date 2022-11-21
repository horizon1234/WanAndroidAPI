package com.example.wan.exception

import kotlinx.coroutines.*

/**
 * Kotlin的挂起函数，是可以自动响应协程的取消的
 * */
fun main() = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 0
        while (true){
            try {
                delay(500)
                //业务代码
            }catch (e: Exception){
                //捕获到业务异常，进行处理
                logX("catch $e")
            }
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