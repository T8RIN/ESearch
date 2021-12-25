package ru.tech.easysearch.extensions

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.ImageViewCompat
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection


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

    fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
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

    fun Context.fetchFavicon(url: String): Bitmap {
        val uri = Uri.parse(url)
        val iconUri: Uri = uri.buildUpon().path("favicon.ico").build()
        val inputStream: InputStream?
        val buffer: BufferedInputStream?
        return try {
            val urlConnection: URLConnection = URL(iconUri.toString()).openConnection()
            urlConnection.connect()
            inputStream = urlConnection.getInputStream()
            buffer = BufferedInputStream(inputStream, 8192)
            BitmapFactory.decodeStream(buffer)
        } catch (e: Exception) {
            ContextCompat.getDrawable(this, R.drawable.ic_earth)!!.toBitmap()
        }
    }

    fun Context.setCoeff() {
        val displayMetrics = resources.displayMetrics
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val dpWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        ESearchApplication.coeff = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> (dpWidth - 210 - 35) / 5
            else -> (dpHeight - 210 - 35) / 5
        }
    }

}