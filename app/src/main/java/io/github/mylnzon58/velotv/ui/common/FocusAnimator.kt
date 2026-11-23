package io.github.mylnzon58.velotv.ui.common

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.PathInterpolator

/**
 * Material 3 Emphasized Decelerate GPU-only motion helper tuned for 60 fps Android TV GPUs.
 * Animates only scale, alpha, translationX/Y/Z — zero layout invalidation during focus moves.
 */
object FocusAnimator {
    private val emphasizedDecelerate = PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f)

    fun apply(view: View, hasFocus: Boolean, scaleFocused: Float = 1.075f, liftDp: Float = 14f) {
        val density = view.resources.displayMetrics.density
        val targetScale = if (hasFocus) scaleFocused else 1.0f
        val targetZ = if (hasFocus) liftDp * density else 0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .translationZ(targetZ)
            .setDuration(if (hasFocus) 185L else 140L)
            .setInterpolator(emphasizedDecelerate)
            .start()
    }

    fun applyNavFocus(view: View, hasFocus: Boolean) {
        val targetScale = if (hasFocus) 1.045f else 1.0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(150L)
            .setInterpolator(emphasizedDecelerate)
            .start()
    }

    fun animateHeroChange(titleView: View, metaView: View) {
        val shift = 6f * titleView.resources.displayMetrics.density
        titleView.animate().cancel()
        metaView.animate().cancel()

        titleView.alpha = 0.35f
        titleView.translationY = shift
        titleView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(190L)
            .setInterpolator(emphasizedDecelerate)
            .start()

        metaView.alpha = 0.25f
        metaView.translationY = shift * 0.7f
        metaView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220L)
            .setInterpolator(emphasizedDecelerate)
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
