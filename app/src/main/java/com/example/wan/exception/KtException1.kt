package com.example.wan.exception

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 先创建一个固定数量线程的线程池，即[ExecutorService]，然后通过[asCoroutineDispatcher]
 * 转换为一个[CoroutineDispatcher]
 *
 * @param nThreads 线程池的线程数量
 * @param Runnable 当需要使用线程时，用该lambda中的方法创建，这里设置为守护线程，防止退出
 * */
val fixedDispatcher = Executors.newFixedThreadPool(2){
    Thread(it).apply { isDaemon = false }
}.asCoroutineDispatcher()

fun main() = runBlocking {
    //父协程
    val parentJob = launch(fixedDispatcher) {
        //开启一个子协程，但是传递的上下文是一个新的句柄
        launch(Job()) {
            var i = 0
            while (isActive){
                Thread.sleep(500L)
                i ++
                logX("First i = $i")
            }
        }

        //开启子协程，使用父协程的CoroutineScope
        launch {
            var i = 0
            while (isActive){
                Thread.sleep(500L)
                i ++
                logX("Second i = $i")
            }
        }
    }

    delay(2000)

    parentJob.cancel()
    parentJob.join()

    logX("End")
}


/**
 * * 打印[Job]的状态信息，这里使用```来控制文本格式不会变化
 * */
fun Job.log() {
    logX("""        
        isActive = $isActive        
        isCancelled = $isCancelled        
        isCompleted = $isCompleted    
        """.trimIndent())
}
/**
 * 控制台输出带协程信息的log，该方法得打印，在打印[Thread]的name
 * 时会携带协程信息
 *
 * 想实现这种效果，需要配合IDE的支持，操作如下：
 * * 点击Make Project小锤子标记右边的项目选择框。
 * * 选中 Edit Configurations
 * * 在VM options中填入 -Dkotlinx.coroutines.debug
 * */
fun logX(any: Any?) {
    println("""
        ================================
        $any
        Thread:${Thread.currentThread().name}
        ================================
        """.trimIndent())
}
