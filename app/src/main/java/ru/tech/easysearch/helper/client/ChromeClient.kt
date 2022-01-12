package ru.tech.easysearch.helper.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.data.SharedPreferencesAccess.CAMERA_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.MIC_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.helper.utils.permissions.PermissionUtils.grantPermissionsCamera
import ru.tech.easysearch.helper.utils.permissions.PermissionUtils.grantPermissionsLoc
import ru.tech.easysearch.helper.utils.permissions.PermissionUtils.grantPermissionsMic

class ChromeClient(
    private val activity: Activity,
    private val progressBar: LinearProgressIndicator?,
    private val browser: WebView
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar?.progress = newProgress
        if (newProgress == 100) {
            progressBar?.visibility = View.GONE
            (activity as? BrowserActivity)?.apply {
                if (reloadButton?.currentView == cancelReload) reloadButton?.showPrevious()
            }
        }
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

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (activity is BrowserActivity) {
            activity.tempFileCallback = filePathCallback
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)

            activity.fileChooserResultLauncher.launch(chooserIntent)
        }
        return true
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback
    ) {
        grantPermissionsLoc(activity)
        callback.invoke(origin, true, false)
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val resources = request.resources
        for (resource in resources) {
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE == resource) {
                grantPermissionsCamera(activity)
                if (browser.settings.mediaPlaybackRequiresUserGesture) {
                    browser.settings.mediaPlaybackRequiresUserGesture = false
                    browser.reload()
                }
                if (getSetting(activity, CAMERA_ACCESS)) request.grant(request.resources)
                else request.deny()
            } else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE == resource) {
                grantPermissionsMic(activity)
                if (getSetting(activity, MIC_ACCESS)) request.grant(request.resources)
                else request.deny()
            } else if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resource) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.permissionRequest)
                    .setMessage(R.string.permDRM)
                    .setPositiveButton(R.string.ok_ok) { _, _ ->
                        request.grant(request.resources)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        request.deny()
                    }
                    .show()
            }
        }
    }

}
