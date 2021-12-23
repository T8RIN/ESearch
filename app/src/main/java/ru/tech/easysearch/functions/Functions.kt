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

    fun doInIoThreadWithObservingOnMain(backgroundTask: () -> Any, mainTask: (it: Any) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val b = runAsyncWithResult(backgroundTask)
            mainTask(b)
        }
    }

    private var tempJob: Job? = null

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