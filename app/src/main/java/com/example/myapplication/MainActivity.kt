package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {

    val ceh = CoroutineExceptionHandler { _, throwable ->
        Log.e("MainActivity", "Глобальне перехоплення крашу: ${throwable.message}")
    }

    var calculationJob: Job? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            calculationJob?.cancel()
            calculationJob = lifecycleScope.launch(Dispatchers.Default) {

                try{
                    for(i in 1..100){

                        Thread.sleep(50)
                        ensureActive()
                        Log.d("MainActivity","Working i = ${i}")
                    }
                } catch(e: CancellationException){
                    Log.d("MainActivity","Обробку скасовано користувачем!")
                    throw(e)
                }
                Log.d("MainActivity","Роботу успішно завершено!")
            }
        }

        binding.btnCancel.setOnClickListener {
            calculationJob?.cancel()
            Log.d("MainActivity","Cancel натиснуто!")

        }

        binding.btnTestCrash.setOnClickListener {
            lifecycleScope.launch {
                supervisorScope {
                    launch {
                        try {
                            delay(1000)
                            throw RuntimeException("Банер не завантажився!")
                        } catch (e: Exception) {
                            Log.e("MainActivity", e.message.toString())
                        }
                    }

                    launch {
                        delay(2000)
                        Log.d("MainActivity", "Стрічка новин завантажена")
                    }
                }

            }
        }

        binding.btnGlobalCrash.setOnClickListener {
//            val safeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + ceh)
//            safeScope.launch { delay(500)
//                throw IllegalArgumentException("Ой!") }

            val safeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            safeScope.launch { launch(ceh){delay(500) //падає бо помилки передаються каскадно нагору
                throw IllegalArgumentException("Ой!")} }
        }
    }
}
