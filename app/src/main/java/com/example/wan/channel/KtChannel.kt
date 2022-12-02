package com.example.wan.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

fun main()  {
    val scope = CoroutineScope(Job())
    //创建管道，都使用默认参数
    val channel = Channel<Int>()
    scope.launch {
        //在一个单独的协程当中发送管道消息
        repeat(3)  {
            channel.send(it)
            println("Send: $it")
        }
        channel.close()
    }
    scope.launch {
        //在一个单独的协程当中接收管道消息
        repeat(3) {
            val result = channel.receive()
            println("Receive $result")
        }
    }

    println("end")
    Thread.sleep(2000000L)
}

/*
输出结果：
end
Receive 0
Send: 0
Send: 1
Receive 1
Receive 2
Send: 2
*/