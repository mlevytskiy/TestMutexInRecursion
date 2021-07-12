package com.core.testmutexinrecursion

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.core.testmutexinrecursion.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var i = 0

    private val mutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->

            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            CoroutineScope(Dispatchers.Main).launch {
                Log.i("testr", "start work")
//                val result = increment()
                increment2()
                Log.i("testr", "finish work $i")
            }

        }
    }

    private suspend fun increment(doAgain: Boolean = true): Int {
        mutex.withLock {
            i++
            if (doAgain) {
                return increment(false)
            } else {
                return i
            }
        }
    }

    private suspend fun increment2(doAgain: Boolean = true) {
        mutex.withReentrantLock {
            i++
            if (doAgain) {
                increment2(false)
            } else {
                i
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    suspend fun <T> Mutex.withReentrantLock(block: suspend () -> T): T {
        val key = ReentrantMutexContextKey(this)
        // call block directly when this mutex is already locked in the context
        if (coroutineContext[key] != null) return block()
        // otherwise add it to the context and lock the mutex
        return withContext(ReentrantMutexContextElement(key)) {
            withLock { block() }
        }
    }

    class ReentrantMutexContextElement(
        override val key: ReentrantMutexContextKey
    ) : CoroutineContext.Element

    data class ReentrantMutexContextKey(
        val mutex: Mutex
    ) : CoroutineContext.Key<ReentrantMutexContextElement>


}