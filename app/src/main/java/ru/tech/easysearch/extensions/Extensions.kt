package ru.tech.easysearch.extensions

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceError
import android.webkit.WebViewClient.*
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat


object Extensions {

    fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)

    @ColorInt
    fun Context.getAttrColor(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun Context.adjustFontSize(fontScale: Float = 1.0f): Context {
        val configuration = resources.configuration
        configuration.fontScale = fontScale
        return createConfigurationContext(configuration)
    }

    fun ImageView.setTint(@ColorRes colorRes: Int?) {
        if (colorRes != null) {
            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP)
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        } else ImageViewCompat.setImageTintList(this, null)
    }

    fun Drawable.getBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth,
            intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun WebResourceError.errorMessage(): String {
        return when (errorCode) {
            ERROR_AUTHENTICATION -> "User authentication failed on server"
            ERROR_TIMEOUT -> "The server is taking too much time to communicate. Try again later."
            ERROR_TOO_MANY_REQUESTS -> "Too many requests during this load"
            ERROR_BAD_URL -> "Check entered URL.."
            ERROR_CONNECT -> "Failed to connect to the server"
            ERROR_FAILED_SSL_HANDSHAKE -> "Failed to perform SSL handshake"
            ERROR_HOST_LOOKUP -> "Server or proxy hostname lookup failed"
            ERROR_PROXY_AUTHENTICATION -> "User authentication failed on proxy"
            ERROR_REDIRECT_LOOP -> "Too many redirects"
            ERROR_UNSUPPORTED_AUTH_SCHEME -> "Unsupported authentication scheme (not basic or digest)"
            ERROR_UNSUPPORTED_SCHEME -> "Unsupported scheme"
            ERROR_FILE -> "Generic file error"
            ERROR_FILE_NOT_FOUND -> "File not found"
            ERROR_IO -> "The server failed to communicate. Try again later."
            else -> "Generic error"
        }
    }

    fun Class<*>.getResId(resName: String): Int {
        return try {
            getDeclaredField(resName).let { it.getInt(it) }
        } catch (e: Exception) {
            -1
        }
    }

    fun View.hideKeyboard(context: Context) {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            windowToken,
            0
        )
    }

}