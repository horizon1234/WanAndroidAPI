package com.example.wan.suspend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 挂起函数，这里由于获取信息是后面依赖于前面，
 * 所以使用挂起函数来解决Callback
 * */
suspend fun testCoroutine(){
    val user = getUserInfo()
    val friendList = getFriendList(user)
    val feedList = getFeedList(user,friendList)
    println(feedList)
}

/**
 * 获取用户信息
 * */
suspend fun getUserInfo(): String{
    withContext(Dispatchers.IO){
        delay(1000)
    }
    return "Coder"
}

/**
 * 函数内部没有挂起
 * */
suspend fun noSuspendGetUserInfo(): String{
    return "zyh"
}

/**
 * 获取好友列表
 * */
suspend fun getFriendList(user: String): String{
    withContext(Dispatchers.IO){
        delay(1000)
    }
    return "Tom,Jack"
}

/**
 * 获取和好友的动态列表
 * */
suspend fun getFeedList(user: String, list: String): String{
    withContext(Dispatchers.IO){
        delay(1000)
    }
    return "[FeedList...]"
}