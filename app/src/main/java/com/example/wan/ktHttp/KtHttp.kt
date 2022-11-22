package com.example.wan

import android.util.Log
import com.example.wan.exception.logX
import com.example.wan.ktHttp.annotations.Field
import com.example.wan.ktHttp.annotations.GET
import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`.getRawType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

/**
 * [ApiService]类定义了整个项目需要调用的接口
 * */
interface ApiService{

    /**
     * [reposAsync]用于异步获取仓库信息
     *
     * @param language 要查询的语言，http真实调用的是[Field]中的lang
     * @param since 要查询的周期
     *
     * @return
     * */
    @GET("/repo")
    fun reposAsync(
        @Field("lang") language: String,
        @Field("since") since: String
    ): KtCall<RepoList>

    /**
     * [reposSync]用于同步调用
     * @see [reposSync]
     * */
    @GET("/repo")
    fun reposSync(
        @Field("lang") language: String,
        @Field("since") since: String
    ): RepoList

    /**
     * [reposFlow]用于异步调用，同时返回类型是[Flow]
     * */
    @GET("/repo")
    fun reposFlow(
        @Field("lang") language: String,
        @Field("since") since: String
    ): Flow<RepoList>
}

data class RepoList(
    var count: Int?,
    var items: List<Repo>?,
    var msg: String?
)

data class Repo(
    var added_stars: String?,
    var avatars: List<String>?,
    var desc: String?,
    var forks: String?,
    var lang: String?,
    var repo: String?,
    var repo_link: String?,
    var stars: String?
)

/**
 * 业务使用的接口，表示返回的数据
 * */
interface CallBack<T: Any>{
    fun onSuccess(data: T)
    fun onFail(throwable:Throwable)
}

/**
 * 该类用于异步请求承载，主要是用来把[OkHttp]中返回的请求值给转换
 * 一下
 *
 * @param call [OkHttp]框架中的[Call]，用来进行网络请求
 * @param gson [Gson]的实例，用来反序列化
 * @param type [Type]类型实例，用来反序列化
 * */
class KtCall<T: Any>(
    private val call: Call,
    private val gson: Gson,
    private val type: Type
){

    fun call(callback: CallBack<T>): Call{
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback.onFail(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = gson.fromJson<T>(response.body?.string(),type)
                    callback.onSuccess(data)
                }catch (e: java.lang.Exception){
                    callback.onFail(e)
                }
            }
        })
        return call
    }
}

/**
 * 单例类
 *
 * */
object KtHttp{

    private val okHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()
    private val gson = Gson()
    val baseUrl = "https://trendings.herokuapp.com"

    /**
     * 利用Java的动态代理，传递[T]类型[Class]对象，可以返回[T]的
     * 对象。
     * 其中在lambda中，一共有3个参数，当调用[T]对象的方法时，会动态
     * 代理到该lambda中执行。[method]就是对象中的方法，[args]是该
     * 方法的参数。
     * */
    fun <T: Any> create(service: Class<T>): T {
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf(service)
        ){ _,method,args ->
            val annotations = method.annotations
            for (annotation in annotations){
                if (annotation is GET){
                    val url = baseUrl + annotation.value
                    return@newProxyInstance invoke<T>(url, method, args!!)
                }
            }
            return@newProxyInstance null
        } as T
    }

    /**
     * 调用[OkHttp]功能进行网络请求，这里根据方法的返回值类型选择不同的策略。
     * @param path 这个是HTTP请求的url
     * @param method 定义在[ApiService]中的方法，在里面实现中，假如方法的返回值类型是[KtCall]带
     * 泛型参数的类型，则认为需要进行异步调用，进行封装，让调用者传入[CallBack]。假如返回类型是普通的
     * 类型，则直接进行同步调用。
     * @param args 方法的参数。
     * */
    private fun <T: Any> invoke(path: String, method: Method, args: Array<Any>): Any?{
        if (method.parameterAnnotations.size != args.size) return null

        var url = path
        val paramAnnotations = method.parameterAnnotations
        for (i in paramAnnotations.indices){
            for (paramAnnotation in paramAnnotations[i]){
                if (paramAnnotation is Field){
                    val key = paramAnnotation.value
                    val value = args[i].toString()
                    if (!url.contains("?")){
                        url += "?$key=$value"
                    }else{
                        url += "&$key=$value"
                    }
                }
            }
        }

        val request = Request.Builder()
            .url(url)
            .build()
        val call = okHttpClient.newCall(request)
        //泛型判断
        return when{
            isKtCallReturn(method) -> {
                val genericReturnType = getTypeArgument(method)
                KtCall<T>(call, gson, genericReturnType)
            }
            isFlowReturn(method) -> {
                logX("Start Out")
                flow<T> {
                    logX("Start In")
                    val genericReturnType = getTypeArgument(method)
                    val response = okHttpClient.newCall(request).execute()
                    val json = response.body?.string()
                    val result = gson.fromJson<T>(json, genericReturnType)
                    // 传出结果
                    logX("Start Emit")
                    emit(result)
                    logX("End Emit")
                }
            }
            else -> {
                val response = okHttpClient.newCall(request).execute()

                val genericReturnType = method.genericReturnType
                val json = response.body?.string()
                Log.i("zyh", "invoke: json = $json")
                //这里这个调用，必须要传入泛型参数
                gson.fromJson<Any?>(json, genericReturnType)
            }
        }
    }

    /**
     * 判断方法返回类型是否是[KtCall]类型。这里调用了[Gson]中的方法，具体可以研究一下，
     * 对于类型来说，RawType和Type有什么区别
    */
    private fun isKtCallReturn(method: Method) =
        getRawType(method.genericReturnType) == KtCall::class.java

    /**
     * 判断方法返回值类型是否是[Flow]类型
     * */
    private fun isFlowReturn(method: Method) =
        getRawType(method.genericReturnType) == Flow::class.java


    /**
     * 获取[Method]的返回值类型中的泛型参数
     * */
    private fun getTypeArgument(method: Method) =
        (method.genericReturnType as ParameterizedType).actualTypeArguments[0]
}

/**
 * 把原来的[CallBack]形式的代码，改成协程样式的，即消除回调，使用挂起函数来完成，以同步的方式来
 * 完成异步的代码调用。
 *
 * 这里的[suspendCancellableCoroutine] 翻译过来就是挂起可取消的协程，因为我们需要结果，所以
 * 需要在合适的时机恢复，而恢复就是通过[Continuation]的[resumeWith]方法来完成。
 * */
suspend fun <T: Any> KtCall<T>.await() : T =
    suspendCancellableCoroutine { continuation ->
        //开始网络请求
        val c = call(object : CallBack<T>{
            override fun onSuccess(data: T) {
                //这里扩展函数也是奇葩，容易重名
                continuation.resume(data)
            }

            override fun onFail(throwable: Throwable) {
                continuation.resumeWithException(throwable)
            }
        })
        //当收到cancel信号时
        continuation.invokeOnCancellation {
            c.cancel()
        }
}

/**
 * 把原来[CallBack]形式的代码，改成[Flow]样式的，即消除回调。其实和扩展挂起函数一样，大致有如下步骤：
 * * 调用一个高阶函数，对于成功数据进行返回，即[trySendBlocking]方法
 * * 对于失败的数据进行返回异常，即[close]方法
 * * 同时要可以响应取消，即[awaitClose]方法
 * */
fun <T: Any> KtCall<T>.asFlow(): Flow<T> =
    callbackFlow {
        //开始网络请求
        val c = call(object : CallBack<T>{
            override fun onSuccess(data: T) {
                //返回正确的数据，但是要调用close()
                trySendBlocking(data)
                    .onSuccess { close() }
                    .onFailure { close(it) }
            }

            override fun onFail(throwable: Throwable) {
                //返回异常信息
                close(throwable)
            }
        })

        awaitClose {
            //响应外部取消请求
            c.cancel()
        }
    }


