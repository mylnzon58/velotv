package io.github.mylnzon58.velotv.ui.common

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.PathInterpolator

/**
 * Silky 60 fps GPU-only cinema motion helper tuned for 1-2 GB RAM Android TV GPUs.
 * Animates exclusively scale, alpha, and translationZ — zero layout passes on D-Pad focus.
 */
object FocusAnimator {
    private val cinemaGlide = PathInterpolator(0.16f, 1.0f, 0.3f, 1.0f)

    fun apply(view: View, hasFocus: Boolean, scaleFocused: Float = 1.085f, liftDp: Float = 16f) {
        val density = view.resources.displayMetrics.density
        val targetScale = if (hasFocus) scaleFocused else 1.0f
        val targetZ = if (hasFocus) liftDp * density else 0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .translationZ(targetZ)
            .setDuration(if (hasFocus) 205L else 155L)
            .setInterpolator(cinemaGlide)
            .start()
    }

    fun applyNavFocus(view: View, hasFocus: Boolean) {
        val targetScale = if (hasFocus) 1.08f else 1.0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(165L)
            .setInterpolator(cinemaGlide)
            .start()
    }

    fun applySubtleRowFocus(view: View, hasFocus: Boolean) {
        val targetScale = if (hasFocus) 1.015f else 1.0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(140L)
            .setInterpolator(cinemaGlide)
            .start()
    }

    fun animateHeroChange(titleView: View, metaView: View) {
        val shift = 5f * titleView.resources.displayMetrics.density
        titleView.animate().cancel()
        metaView.animate().cancel()

        titleView.alpha = 0.42f
        titleView.translationY = shift
        titleView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(210L)
            .setInterpolator(cinemaGlide)
            .start()

        metaView.alpha = 0.30f
        metaView.translationY = shift * 0.65f
        metaView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(235L)
            .setInterpolator(cinemaGlide)
            .start()
    }

    fun shakeError(view: View) {
        val d = 10f * view.resources.displayMetrics.density
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -d, d, -d * 0.7f, d * 0.7f, -d * 0.3f, 0f).apply {
            duration = 320L
            start()
        }
    }
}
