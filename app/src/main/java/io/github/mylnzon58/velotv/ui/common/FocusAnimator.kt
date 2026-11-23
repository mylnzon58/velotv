package io.github.mylnzon58.velotv.ui.common

import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Restrained GPU-only scale transition (Hallmark motion discipline: transform only, zero overshoot, 60 fps on Mali-T860).
 */
object FocusAnimator {
    private val interpolator = DecelerateInterpolator()

    fun apply(view: View, hasFocus: Boolean, scaleFocused: Float = 1.055f) {
        val targetScale = if (hasFocus) scaleFocused else 1.0f
        view.animate().cancel()
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(120L)
            .setInterpolator(interpolator)
            .start()
    }
}
