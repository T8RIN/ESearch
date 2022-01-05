package ru.tech.easysearch.helper.adblock

import android.content.Context
import ru.tech.easysearch.functions.Functions.doInBackground
import java.io.*
import java.net.URI
import java.net.URL
import java.util.*

object AdBlocker {

    private const val AD_DIRECTORY = "adblocker"
    private const val AD_LIST = "adList.txt"
    private const val noTrackUrl =
        "https://raw.githubusercontent.com/notracking/hosts-blocklists/master/adblock/adblock.txt"
    private const val adListUrl = "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"

    private val adList: HashSet<String> = HashSet()

    fun String.areAD(): Boolean {
        val domain = this.getDomain().lowercase()
        return adList.contains(domain)
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

    fun createAdList(context: Context) {
        val listAsset = File("${context.getDir(AD_DIRECTORY, Context.MODE_PRIVATE)}/$AD_LIST")
        if (adList.isEmpty()) {
            try {
                val time = Calendar.getInstance()
                time.add(Calendar.DAY_OF_YEAR, -1)
                if (Date(listAsset.lastModified()).before(time.time))
                    listAsset.delete()

                val reader = BufferedReader(FileReader(listAsset))
                while (reader.readLine() != null) {
                    adList.add(reader.readLine())
                }
                adList.add("mc.yandex.ru")
            } catch (e: Exception) {
                downloadAdList(context)
            }
        }
    }

    private fun downloadAdList(context: Context) {
        doInBackground {
            val tempPath = "${context.getDir(AD_DIRECTORY, Context.MODE_PRIVATE)}/temp.txt"
            val path = "${context.getDir(AD_DIRECTORY, Context.MODE_PRIVATE)}/$AD_LIST"

            val urlConnection = URL(adListUrl).openConnection()
            val urlConnection2 = URL(noTrackUrl).openConnection()

            val inputStream = BufferedInputStream(urlConnection.getInputStream(), 1024 * 10)

            val tempFile = File(tempPath)

            if (tempFile.exists()) tempFile.delete()
            tempFile.createNewFile()

            val outStream = FileOutputStream(tempFile)
            val buff = ByteArray(10 * 1024)

            var len: Int
            while (inputStream.read(buff).also { len = it } != -1) {
                outStream.write(buff, 0, len)
            }

            inputStream.close()

            val inputStream2 = BufferedInputStream(urlConnection2.getInputStream(), 1024 * 10)

            val buff2 = ByteArray(10 * 1024)

            var len2: Int
            while (inputStream2.read(buff2).also { len2 = it } != -1) {
                outStream.write(buff2, 0, len2)
            }

            outStream.flush()
            outStream.close()
            inputStream2.close()

            val reader = BufferedReader(FileReader(tempFile))
            val out = FileWriter(File(path))
            while (reader.readLine() != null) {
                val line = reader.readLine()
                if (line.startsWith("||"))
                    out.write("${line.removePrefix("||").removeSuffix("^").lowercase()}\n")
                else if (line.startsWith("0.0.0.0 "))
                    out.write("${line.removePrefix("0.0.0.0 ").lowercase()}\n")
            }
            out.close()
            tempFile.delete()

            adList.clear()
            createAdList(context)
        }
    }

}