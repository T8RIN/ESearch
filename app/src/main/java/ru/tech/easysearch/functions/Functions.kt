package ru.tech.easysearch.functions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*

object Functions {

    fun doInBackground(function: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            runAsync(function)
        }
    }

    fun waitForDoInBackground(time: Long, function: () -> Unit) {
        tempJob?.cancel()
        tempJob = CoroutineScope(Dispatchers.Main).launch {
            waitForAsync(time, function)
        }
    }

    private var tempJob: Job? = null

    private suspend fun runAsync(function: () -> Unit) = withContext(Dispatchers.IO) {
        function()
    }

    private suspend fun waitForAsync(time: Long, function: () -> Unit) =
        withContext(Dispatchers.IO) {
            delay(time)
            function()
        }

    fun byteArrayToBitmap(array: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(array, 0, array.size);
    }

}