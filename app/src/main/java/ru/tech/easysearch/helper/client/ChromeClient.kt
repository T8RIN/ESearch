package ru.tech.easysearch.helper.client

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.progressindicator.LinearProgressIndicator

class ChromeClient(
    private val activity: AppCompatActivity,
    private val progressBar: LinearProgressIndicator,
    var iconView: ImageView? = null
) :
    WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar.progress = newProgress
        if (newProgress == 100) {
            progressBar.visibility = View.GONE
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageBitmap(icon)
        super.onReceivedIcon(view, icon)
    }

    private var videoPlayer: View? = null
    override fun onHideCustomView() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        WindowInsetsControllerCompat(
            activity.window,
            videoPlayer!!
        ).show(WindowInsetsCompat.Type.systemBars())
        (activity.window.decorView as FrameLayout).removeView(videoPlayer)
        videoPlayer = null
    }

    override fun onShowCustomView(paramView: View?, paramCustomViewCallback: CustomViewCallback?) {
        if (videoPlayer != null) {
            onHideCustomView()
            return
        }
        videoPlayer = paramView
        (activity.window
            .decorView as FrameLayout)
            .addView(
                videoPlayer,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        updateControls()
    }

    private fun updateControls() {
        (videoPlayer?.layoutParams as FrameLayout.LayoutParams).let {
            it.setMargins(0, 0, 0, 0)
            it.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowInsetsControllerCompat(activity.window, videoPlayer!!).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
