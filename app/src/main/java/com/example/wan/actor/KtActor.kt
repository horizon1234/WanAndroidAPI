package com.example.wan.actor

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor

/**
 * 密封类，用于定义向[actor]中发送的消息
 * */
sealed class Msg

/**
 * 增加消息，单例，用于在[actor]被处理
 * */
object AddMsg : Msg()

/**
 * 获取结果的消息，用于获取[actor]中
 * 的结果
 * */
class ResultMsg(
    val result: CompletableDeferred<Int>
) : Msg()

fun main() = runBlocking {

    /**
     * 这里涉及Actor模型，在一个Actor系统中，用的是消息[Msg]来通信，
     * 比如这里可以往[actor]中发送2种消息
     * */
    suspend fun addActor() = actor<Msg> {
        var counter = 0
        //这里的for循环其实在[Channel]中说过，是用来获取[channel]中的
        //数据，是一种简写。
        for (msg in channel) {
            when (msg) {
                is AddMsg -> counter++
                is ResultMsg -> msg.result.complete(counter)
            }
        }
    }

    //函数类型引用
    val actor = addActor()
    val jobs = mutableListOf<Job>()

    repeat(10) {
        //启动10个协程
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                //在每个协程中，往[actor]发送1000次消息
                actor.send(AddMsg)
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()
    //发送获取结果的消息
    val deferred = CompletableDeferred<Int>()
    actor.send(ResultMsg(deferred))
    //挂起函数，等结果
    val result = deferred.await()
    actor.close()

    println("i = $result")
}