package com.example.wan.select

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

fun main() = runBlocking {
    val startTime = System.currentTimeMillis()
    //开启一个协程，往channel1中发送数据，这里发送完 ABC需要450ms，
    val channel1 = produce {
        delay(50L)
        send("A")
        delay(150)
        send("B")
        delay(250)
        send("C")
        //延迟1000ms是为了这个Channel不那么快close
        //因为produce高阶函数开启协程，当执行完时，会自动close
        delay(1000)
    }
    //开启一个协程，往channel2中发送数据，发送完abc需要500ms
    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
        delay(1000)
    }

    //选择Channel，接收2个Channel
    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String =
        select {
            //这里同样使用类onXXX的API
            channel1.onReceiveCatching {
                it.getOrNull() ?: "channel1 is closed!"
            }
            channel2.onReceiveCatching {
                it.getOrNull() ?: "channel2 is closed!"
            }
        }

    //连续选择6次
    repeat(6) {
        val result = selectChannel(channel1, channel2)
        println(result)
    }

    //最后再把协程取消，因为前面设置的有1000ms延迟
    channel1.cancel()
    channel2.cancel()

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}
