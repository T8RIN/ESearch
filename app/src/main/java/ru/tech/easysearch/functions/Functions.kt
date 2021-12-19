package ru.tech.easysearch.functions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Functions {

    fun doInBackground(function: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            runAsync(function)
        }
    }

    private suspend fun runAsync(function: () -> Unit) = withContext(Dispatchers.IO) {
        function()
    }

}