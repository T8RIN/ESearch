package ru.tech.easysearch.helper.utils.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object AnimUtils {

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

}