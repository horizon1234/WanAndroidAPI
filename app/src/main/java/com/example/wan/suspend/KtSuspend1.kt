//package com.example.wan.suspend
//
//import kotlin.coroutines.Continuation
//
///**
// * CPS后的等价代码
// * @param completion [Continuation]类型的参数
// * */
//fun testCoroutine(completion: Continuation<Any?>): Any? {
//
//    /**
//     * 本质是匿名内部类，这里给取了一个名字[TestContinuation]， 构造
//     * 参数还是传递一个[Continuation]类型的字段
//     * */
//    class TestContinuation(completion: Continuation<Any?>) : ContinuationImpl(completion){
//        //表示状态机的状态
//        var label: Int = 0
//        //当前挂起函数执行的结果
//        var result: Any? = null
//
//        //用于保存挂起计算的结果，中间值
//        var mUser: Any? = null
//        var mFriendList: Any? = null
//
//        /**
//         * 状态机入口，该类是ContinuationImpl中的抽象方法，同时ContinuationImpl
//         * 又是继承至[Continuation]，所以[Continuation]中的resumeWith方法会回调
//         * 该invokeSuspend方法。
//         *
//         * 即每当调用挂起函数返回时，该方法都会被调用，在方法内部，先通过result记录
//         * 挂起函数的执行结果，再切换labeal，最后再调用testCoroutine方法
//         * */
//        override fun invokeSuspend(_result: Result<Any?>): Any?{
//            result = _result
//            label = label or Int.Companion.MIN_VALUE
//            return testCoroutine(this)
//        }
//    }
//
//    val continuation = if (completion is TestContinuation){
//        completion
//    }else{
//        //作为参数
//        TestContinuation(completion)
//    }
//
//    //3个变量，对应原函数的3个变量
//    lateinit var user: String
//    lateinit var friendList: String
//    lateinit var feedList: String
//
//    //result接收挂起函数的运行结果
//    var result = continuation.result
//
//    //suspendReturn表示乖巧函数的返回值
//    var suspendReturn: Any? = null
//
//    //该flag表示当前函数被挂起了
//    val sFlag = CoroutineSingles.CORTOUINE_SUSPEND
//
//    when(continuation.label){
//        0 -> {
//            //检测异常
//            throwOnFailure(result)
//            //将label设置为1，准备进入下一个状态
//            continuation.label = 1
//            //执行getUserInfo
//            suspendReturn = getUserInfo(continuation)
//            //判断是否挂起
//            if (suspendReturn == sFlag){
//                return suspendReturn
//            }else{
//                result = suspendReturn
//            }
//        }
//
//        1 -> {
//            throwOnFailure(result)
//            // 获取 user 值
//             user = result as String
//            // 将协程结果存到 continuation 里
//            continuation.mUser = user
//            // 准备进入下一个状态
//            continuation.label = 2
//            // 执行 getFriendList
//            suspendReturn = getFriendList(user, continuation)
//            // 判断是否挂起
//            if (suspendReturn == sFlag) {
//                return suspendReturn
//            }  else {
//                result = suspendReturn
//            }
//        }
//
//        2 -> {
//            throwOnFailure(result)
//            user = continuation.mUser as String
//            //获取friendList的值
//            friendList = result as String
//            //将挂起函数结果保存到continuation中
//            continuation.mUser = user
//            continuation.mFriendList = friendList
//            //准备进入下一个阶段
//            continuation.label = 3
//            //执行获取feedList
//            suspendReturn = getFeedList(user,friendList,continuation)
//            //判断是否挂起
//            if (suspendReturn == sFlag){
//                return suspendReturn
//            }else{
//                result = suspendReturn
//            }
//        }
//
//
//        3 -> {
//            throwOnFailure(result)
//            user = continuation.mUser as String
//            friendList = continuation.mFriendList as String
//            feedList = continuation.result as String
//            loop = false
//        }
//    }
//}