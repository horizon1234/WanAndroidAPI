package com.example.wan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.wan.exception.logX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.syncCall).setOnClickListener {
            sync()
        }
        findViewById<TextView>(R.id.asyncCall).setOnClickListener {
            async()
        }
        findViewById<TextView>(R.id.coroutineCall).setOnClickListener {
            lifecycleScope.launch {
                val data = KtHttp.create(ApiService::class.java).reposAsync(language = "Kotlin", since = "weekly").await()
                findViewById<TextView>(R.id.result).text = data.toString()
            }
        }
        findViewById<TextView>(R.id.flowCall).setOnClickListener {
            val dataFlow = KtHttp.create(ApiService::class.java).reposAsync(language = "Kotlin", since = "weekly").asFlow()
            dataFlow
                .onStart {
                    Toast.makeText(this@MainActivity, "开始请求", Toast.LENGTH_SHORT).show()
                }
                .onCompletion {
                    Toast.makeText(this@MainActivity, "请求完成", Toast.LENGTH_SHORT).show()
                }
                .onEach {
                    findViewById<TextView>(R.id.result).text = it.toString()
                }
                .catch {
                    Log.i("Flow", "catch exception: $it")
                }
                .launchIn(lifecycleScope)
        }

        findViewById<TextView>(R.id.flowReturnCall).setOnClickListener {
            KtHttp.create(ApiService::class.java).reposFlow(language = "Kotlin", since = "weekly")
                .flowOn(Dispatchers.IO)
                .onStart {
                    Toast.makeText(this@MainActivity, "开始请求", Toast.LENGTH_SHORT).show()
                }
                .onCompletion {
                    Toast.makeText(this@MainActivity, "请求完成", Toast.LENGTH_SHORT).show()
                }
                .catch {
                    Log.i("Flow", "catch exception: $it")
                }
                .onEach {
                    logX("Display UI")
                    findViewById<TextView>(R.id.result).text = it.toString()
                }
                .launchIn(lifecycleScope)
        }

        findViewById<TextView>(R.id.suspendCall).setOnClickListener {
            lifecycleScope.launch {
                val data = KtHttp.create(ApiService::class.java).reposSuspend(language = "Kotlin", since = "weekly")
                findViewById<TextView>(R.id.result).text = data.toString()
            }

        }

    }

    private fun sync(){
        thread {
            val apiService: ApiService = KtHttp.create(ApiService::class.java)
            val data = apiService.reposSync(language = "Kotlin", since = "weekly")
            runOnUiThread {
                findViewById<TextView>(R.id.result).text = data.toString()
                Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun async(){
        KtHttp.create(ApiService::class.java).reposAsync(language = "Java", since = "weekly").call(object : CallBack<RepoList>{
            override fun onSuccess(data: RepoList) {
                runOnUiThread {
                    findViewById<TextView>(R.id.result).text = data.toString()
                    Toast.makeText(this@MainActivity, "$data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFail(throwable: Throwable) {
                runOnUiThread {
                    findViewById<TextView>(R.id.result).text = throwable.toString()
                    Toast.makeText(this@MainActivity, "$throwable", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}