package ru.tech.easysearch.helper.utils.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import ru.tech.easysearch.R

object AnimUtils {

    var fadeIn: Animation? = null
    var fadeOut: Animation? = null

    fun Context.assignAnimations() {
        fadeIn = AnimationUtils.loadAnimation(
            this,
            R.anim.fade_in
        )
        fadeOut = AnimationUtils.loadAnimation(
            this,
            R.anim.fade_out
        )
    }

    fun Context.getAnimInstance(`in`: Boolean): Animation {
        if (fadeIn == null) assignAnimations()
        return if (`in`) fadeIn!!
        else fadeOut!!
    }

    fun View.slideViewVertically(
        from: Int,
        to: Int,
        startAction: () -> Unit,
        endAction: () -> Unit
    ) {

        val slideAnimator = ValueAnimator
            .ofInt(from, to)
            .setDuration(500)

        slideAnimator.addUpdateListener {
            val animVal = it.animatedValue as Int
            layoutParams.height = animVal
            requestLayout()
        }

        slideAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                startAction()
            }

            override fun onAnimationEnd(animation: Animator?) {
                endAction()
            }

            override fun onAnimationCancel(animation: Animator?) {
                endAction()
            }

            override fun onAnimationRepeat(animation: Animator?) {
                startAction()
            }

        })

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(slideAnimator)
            start()
        }
    }

    fun View.slideViewHorizontally(
        from: Int,
        to: Int,
        startAction: () -> Unit,
        endAction: () -> Unit
    ) {

        val slideAnimator = ValueAnimator
            .ofInt(from, to)
            .setDuration(500)

        slideAnimator.addUpdateListener {
            val animVal = it.animatedValue as Int
            layoutParams.width = animVal
            requestLayout()
        }
        slideAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                startAction()
            }

            override fun onAnimationEnd(animation: Animator?) {
                endAction()
            }

            override fun onAnimationCancel(animation: Animator?) {
                endAction()
            }

            override fun onAnimationRepeat(animation: Animator?) {
                startAction()
            }

        })

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(slideAnimator)
            start()
        }
    }

    fun Animation.setAnimListener(
        startAction: (() -> Unit)? = null,
        endAction: (() -> Unit)? = null,
        repeatAction: (() -> Unit)? = null
    ) {
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                startAction?.invoke()
            }

            override fun onAnimationEnd(animation: Animation?) {
                endAction?.invoke()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                repeatAction?.invoke()
            }
        })
    }

}