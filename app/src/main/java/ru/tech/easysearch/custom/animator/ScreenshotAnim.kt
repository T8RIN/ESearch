package ru.tech.easysearch.custom.animator

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY

class ScreenshotAnim(rootGroup: ViewGroup, bitmap: Bitmap, context: Context) {

    private val duration = 1000L

    init {
        val tint = LayoutInflater.from(context).inflate(R.layout.screenshot_tint, rootGroup, false)
        val image =
            LayoutInflater.from(context).inflate(R.layout.screenshot_image, rootGroup, false)
        rootGroup.addView(tint)
        rootGroup.addView(image)
        tint.animate().alpha(0.8f).setDuration(duration / 2)
            .withStartAction {
                Glide.with(context.applicationContext).load(bitmap).into(image as ImageView)
                image.animate()
                    .y(-displayOffsetY)
                    .setDuration(duration)
                    .start()
                image.animate()
                    .scaleX(0.1f)
                    .scaleY(0.1f)
                    .setDuration((duration / 1.5).toLong())
                    .start()
            }
            .withEndAction {
                tint.animate().alpha(0f).setDuration(duration / 2).withEndAction {
                    rootGroup.removeView(tint)
                    rootGroup.removeView(image)
                }.start()
            }.start()
    }

}