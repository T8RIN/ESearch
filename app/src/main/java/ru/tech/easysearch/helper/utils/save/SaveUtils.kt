package ru.tech.easysearch.helper.utils.save

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import ru.tech.easysearch.R
import java.text.SimpleDateFormat
import java.util.*

object SaveUtils {

    private fun String.pdfName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        val domain =
            Uri.parse(this).host?.replace("www.", "")!!.trim { it <= ' ' }
        return domain.replace(".", "_").trim { it <= ' ' } + "_" + currentTime.trim { it <= ' ' }
    }

    fun WebView.saveAsPDF(context: Context) {
        val title: String = url!!.pdfName()
        val printManager = context.getSystemService(AppCompatActivity.PRINT_SERVICE) as PrintManager
        val printAdapter: PrintDocumentAdapter = createPrintDocumentAdapter(title)
        printManager.print(title, printAdapter, PrintAttributes.Builder().build())
    }

    fun WebView.addToHomeScreen(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(
                ShortcutManager::class.java
            )!!
            val pinShortcutInfo = ShortcutInfo.Builder(context, url)
                .setShortLabel(title!!)
                .setLongLabel(title!!)
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                .build()
            shortcutManager.requestPinShortcut(pinShortcutInfo, null)
        } else {
            val saveIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val installer = Intent()
            installer.putExtra("android.intent.extra.shortcut.INTENT", saveIntent)
            installer.putExtra("android.intent.extra.shortcut.NAME", title)
            @Suppress("DEPRECATION")
            installer.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                    context.applicationContext,
                    R.mipmap.ic_launcher
                )
            )
            installer.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            context.sendBroadcast(installer)
        }
    }

}