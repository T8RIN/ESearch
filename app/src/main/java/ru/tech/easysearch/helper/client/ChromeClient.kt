package ru.tech.easysearch.helper.client

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity

class ChromeClient(
    private val context: Context,
    private val progressBar: LinearProgressIndicator,
    private var iconView: ImageView? = null
) :
    WebChromeClient() {

    val backStack: ArrayList<String> = ArrayList()

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar.progress = newProgress
        if (newProgress == 100) {
            progressBar.visibility = View.GONE
        }
        view?.url?.let {
            backStack.add(it)
            if(context is BrowserActivity){
                context.findViewById<TextInputEditText>(R.id.searchView).setText(it)
            }
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        iconView?.setImageBitmap(icon)
        super.onReceivedIcon(view, icon)
    }

}
