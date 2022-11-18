package com.example.wan.select

import androidx.appcompat.widget.AppCompatButton
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * 模拟从缓存中获取物品信息
 * @param productId 商品Id
 * @return 返回物品信息[Product]
 * */
suspend fun getCacheInfo(productId: String): Product {
    delay(100L)
    return Product(productId, 9.9)
}

/**
 * 模拟从网络中获取物品信息
 * @param productId 商品Id
 * @return 返回物品信息[Product]
 * */
suspend fun getNetworkInfo(productId: String): Product {
    delay(200L)
    return Product(productId, 9.8)
}

/**
 * 模拟更新UI
 * @param product 商品信息
 * */
fun updateUI(product: Product) {
    println("${product.productId}==${product.price}")
}

/**
 * 数据类，来表示一个商品
 * @param isCache 是否是缓存数据
 * */
data class Product(val productId: String,val price: Double,val isCache: Boolean = false)

fun main(){
    runBlocking {
        val startTime = System.currentTimeMillis()
        val productId = "11211"
        val cacheDeferred = async { getCacheInfo(productId) }
        val networkDeferred = async { getNetworkInfo(productId) }
        val product = select {
            cacheDeferred.onAwait{
                    it.copy(isCache = true)
                }

            networkDeferred.onAwait{
                    it.copy(isCache = false)
                }
        }
        updateUI(product)
        println("total time : ${System.currentTimeMillis() - startTime}")
        //如果当前是缓存信息，则再去获取网络信息
        if (product.isCache){
            val latest = networkDeferred.await()
            updateUI(latest)
            println("all total time : ${System.currentTimeMillis() - startTime}")
        }
    }
}

/**
 * var finished = false
var product: Product? = null
val cacheDeferred = async { getCacheInfo(productId) }
val networkDeferred = async { getNetworkInfo(productId) }

launch {
product = cacheDeferred.await()
finished = true
}

launch {
product = networkDeferred.await()
finished = true
}

while (!finished){
delay(1)
}

 * */

/**
 * val productId = "11211"
val cacheProduct = getCacheInfo(productId)
updateUI(cacheProduct)
val networkProduct = getNetworkInfo(productId)
updateUI(networkProduct)
 * */

/*
* val product = select {
            async { getCacheInfo(productId) }
                .onAwait{
                    it
                }

            async { getNetworkInfo(productId) }
                .onAwait{
                    it
                }
        }
* */