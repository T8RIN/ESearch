package ru.tech.easysearch.functions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import ru.tech.easysearch.data.DataArrays.sizeSuffixes

object Functions {

    fun doInBackground(function: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            runAsync(function)
        }
    }

    fun delayedDoInBackground(time: Long, function: () -> Unit) {
        tempJob?.cancel()
        tempJob = CoroutineScope(Dispatchers.Main).launch {
            waitForAsync(time, function)
        }
    }

    fun delayedDoInIoThreadWithObservingOnMain(
        delay: Long,
        backgroundTask: () -> Any,
        mainTask: (it: Any) -> Unit
    ) {
        tempJobSecond?.cancel()
        tempJobSecond = CoroutineScope(Dispatchers.Main).launch {
            val b = runAsyncWithDelayedResult(delay, backgroundTask)
            mainTask(b)
        }
    }

    fun doInIoThreadWithObservingOnMain(backgroundTask: () -> Any, mainTask: (it: Any) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val b = runAsyncWithResult(backgroundTask)
            mainTask(b)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun delayedDoInForeground(time: Long, function: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            waitForSync(time)
            function()
        }
    }

    private var tempJob: Job? = null
    private var tempJobSecond: Job? = null

    private suspend fun runAsyncWithDelayedResult(delay: Long, backgroundTask: () -> Any) =
        withContext(Dispatchers.IO) {
            delay(delay)
            return@withContext backgroundTask()
        }

    private suspend fun runAsyncWithResult(backgroundTask: () -> Any) =
        withContext(Dispatchers.IO) {
            return@withContext backgroundTask()
        }

    private suspend fun runAsync(function: () -> Unit) = withContext(Dispatchers.IO) {
        function()
    }

    private suspend fun waitForAsync(time: Long, function: () -> Unit) =
        withContext(Dispatchers.IO) {
            delay(time)
            function()
        }

    private suspend fun waitForSync(time: Long) =
        withContext(Dispatchers.IO) {
            delay(time)
        }

    fun byteArrayToBitmap(array: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }

    fun getNearestFileSize(bytes: Long): String {
        var size = bytes
        var a = 0
        while (size > 1024) {
            size /= 1024
            a += 1
        }
        return "$size ${sizeSuffixes[a]}"
    }

}