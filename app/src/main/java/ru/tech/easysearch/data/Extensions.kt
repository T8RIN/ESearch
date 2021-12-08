package ru.tech.easysearch.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.TypedValue
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

}