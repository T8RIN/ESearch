package ru.tech.easysearch.helper.adblock

import android.content.Context
import java.net.URI

class AdBlocker {

    companion object {
        const val AD_LIST = "adList.txt"
        val adList: HashSet<String> = HashSet()

        fun String.areAD(): Boolean {
            return adList.contains(this.getDomain().lowercase())
        }

        fun String.getDomain(): String {
            var url = this.lowercase()
            val index: Int = url.indexOf('/', 8)
            if (index != -1) {
                url = url.substring(0, index)
            }
            val uri = URI(url)
            val domain = uri.host ?: return url
            return if (domain.startsWith("www.")) domain.substring(4) else domain
        }
    }

    fun createAdList(context: Context) {
        if (adList.isEmpty()) {
            context.applicationContext.assets.open(AD_LIST).bufferedReader().use {
                while (it.readLine() != null) {
                    adList.add(it.readLine())
                }
            }
        }
    }

}