package io.github.mylnzon58.velotv.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.VisualTheme

/**
 * Cinema Lounge surface & control factory tuned for 10-foot Android TV legibility.
 * - Soft rounded glass-tinted dark surfaces with crisp luminous focus rings.
 * - Circular minimal icon buttons and floating Control Center drawers.
 * - Outfit typography across all dynamic rows, labels, and tabs.
 */
object HallmarkSurfaceFactory {

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun dpFloat(context: Context, value: Float): Float =
        value * context.resources.displayMetrics.density

    fun withAlpha(color: Int, alpha: Int): Int =
        (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

    fun dotDrawable(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    /**
     * Soft circular/pill translucent icon button for the minimal cinema top bar.
     */
    fun iconButtonBackground(context: Context, theme: VisualTheme, active: Boolean = false): StateListDrawable {
        val radius = dpFloat(context, 99f)
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            val baseBg = if (active) withAlpha(theme.accent, 52) else withAlpha(theme.paper2, 168)
            val rim = if (active) withAlpha(theme.accent, 175) else withAlpha(theme.ink, 30)
            setColor(baseBg)
            setStroke(dp(context, 1).coerceAtLeast(1), rim)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(android.R.attr.state_selected), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun iconTintColors(theme: VisualTheme, active: Boolean = false): ColorStateList {
        val idleColor = if (active) theme.accent else theme.ink
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(theme.accentInk, theme.accentInk, idleColor)
        )
    }

    /**
     * Soft capsule for live RAM Turbo pill and focusable Ambient Clock.
     */
    fun pillCapsuleBackground(context: Context, theme: VisualTheme): StateListDrawable {
        val radius = dpFloat(context, 99f)
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper2, 174))
            setStroke(dp(context, 1).coerceAtLeast(1), withAlpha(theme.ink, 30))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun clockFocusBackground(context: Context, theme: VisualTheme): StateListDrawable {
        val radius = dpFloat(context, 14f)
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper3, 215))
            setStroke(dpFloat(context, 1.8f).toInt().coerceAtLeast(2), theme.accent)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0x00000000)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun feedbackPillBackground(context: Context, theme: VisualTheme): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(context, 99f)
            setColor(withAlpha(theme.paper2, 246))
            setStroke(dpFloat(context, 1.2f).toInt().coerceAtLeast(1), withAlpha(theme.accent, 185))
        }
    }

    fun badgePillBackground(context: Context, theme: VisualTheme): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(context, 8f)
            setColor(withAlpha(theme.paper, 224))
            setStroke(dp(context, 1).coerceAtLeast(1), withAlpha(theme.ink, 36))
        }
    }

    fun inputIconCircle(context: Context, theme: VisualTheme, connected: Boolean): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (connected) withAlpha(theme.accent, 38) else withAlpha(theme.paper3, 210))
            setStroke(dp(context, 1).coerceAtLeast(1), if (connected) withAlpha(theme.accent, 110) else withAlpha(theme.ink, 24))
        }
    }

    fun cardSurfaceBackground(context: Context, theme: VisualTheme, corner: CornerStyle): Drawable {
        val radius = dpFloat(context, corner.radiusDp.toFloat())
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.paper3)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper2, 222))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(android.R.attr.state_selected), focused)
            addState(intArrayOf(), idle)
        }
    }

    /**
     * Luminous crisp cinema focus ring drawn in foreground over edge-to-edge TV artwork.
     */
    fun cardForegroundRing(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, corner.radiusDp.toFloat())
        val strokeIdle = dp(context, 1).coerceAtLeast(1)
        val strokeFocused = dpFloat(context, 2.5f).toInt().coerceAtLeast(3)

        val focusedRing = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0x00000000)
            setStroke(strokeFocused, theme.accent)
        }
        val idleRim = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0x00000000)
            setStroke(strokeIdle, withAlpha(theme.ink, if (theme.isLight) 36 else 26))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focusedRing)
            addState(intArrayOf(android.R.attr.state_selected), focusedRing)
            addState(intArrayOf(), idleRim)
        }
    }

    fun cardBackground(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, corner.radiusDp.toFloat())
        val strokeIdle = dp(context, 1)
        val strokeFocused = dpFloat(context, 2.2f).toInt().coerceAtLeast(2)

        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.paper3)
            setStroke(strokeFocused, theme.accent)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper2, 220))
            setStroke(strokeIdle, withAlpha(theme.ink, 28))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(android.R.attr.state_selected), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun navTabBackground(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, if (corner == CornerStyle.SHARP) 2f else 99f)
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
        }
        val selected = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper3, 235))
            setStroke(dp(context, 1).coerceAtLeast(1), withAlpha(theme.accent, 140))
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0x00000000)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(android.R.attr.state_selected), selected)
            addState(intArrayOf(), idle)
        }
    }

    fun navTabTextColors(theme: VisualTheme): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(theme.accentInk, theme.ink, theme.ink2)
        )
    }

    fun primaryButtonBackground(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, if (corner == CornerStyle.SHARP) 2f else 99f)
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
            setStroke(dp(context, 2), theme.ink)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun floatingDrawerBackground(context: Context, theme: VisualTheme, corner: CornerStyle): Drawable {
        val radius = dpFloat(context, if (corner == CornerStyle.SHARP) 0f else 22f)
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper2, 248))
            setStroke(dp(context, 1).coerceAtLeast(1), withAlpha(theme.ink, 36))
        }
    }

    fun studioFrameBackground(context: Context, theme: VisualTheme, corner: CornerStyle): Drawable {
        val radius = dpFloat(context, if (corner == CornerStyle.SHARP) 0f else 22f)
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper2, 250))
            setStroke(dp(context, 1).coerceAtLeast(1), withAlpha(theme.ink, 40))
        }
    }

    fun createSectionLabel(context: Context, theme: VisualTheme, title: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(context, 16)
                bottomMargin = dp(context, 6)
            }
            addView(TextView(context).apply {
                text = title
                textSize = 12.5f
                letterSpacing = 0.02f
                typeface = ResourcesCompat.getFont(context, R.font.outfit_semibold)
                setTextColor(theme.muted)
                setPadding(dp(context, 4), 0, 0, dp(context, 4))
            })
        }
    }

    /**
     * Soft floating pill row inside right-docked Control Center drawers.
     */
    fun createDrawerRow(
        context: Context,
        theme: VisualTheme,
        title: String,
        subtitle: String? = null,
        trailing: String? = null,
        accentTrailing: Boolean = false,
        onFocus: (() -> Unit)? = null,
        onClick: () -> Unit
    ): LinearLayout {
        val radius = dpFloat(context, 12f)
        val idleBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0x00000000)
        }
        val focusedBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(withAlpha(theme.paper3, 245))
            setStroke(dpFloat(context, 1.5f).toInt().coerceAtLeast(2), withAlpha(theme.accent, 210))
        }

        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = dp(context, 14)
            val padV = dp(context, 11)
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val textCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        textCol.addView(TextView(context).apply {
            text = title
            textSize = 14.5f
            typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
            setTextColor(theme.ink)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })

        if (!subtitle.isNullOrBlank()) {
            textCol.addView(TextView(context).apply {
                text = subtitle
                textSize = 12f
                typeface = ResourcesCompat.getFont(context, R.font.outfit_regular)
                setTextColor(theme.muted)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, dp(context, 2), 0, 0)
            })
        }
        inner.addView(textCol)

        if (!trailing.isNullOrBlank()) {
            inner.addView(TextView(context).apply {
                text = trailing
                textSize = 12f
                typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
                setTextColor(if (accentTrailing) theme.accent else theme.ink2)
                setPadding(dp(context, 12), 0, 0, 0)
            })
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isFocusable = true
            isClickable = true
            background = idleBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(context, 4)
            }
            addView(inner)

            setOnFocusChangeListener { v, hasFocus ->
                v.background = if (hasFocus) focusedBg else idleBg
                FocusAnimator.applySubtleRowFocus(v, hasFocus)
                if (hasFocus) onFocus?.invoke()
            }
            setOnClickListener { onClick() }
        }
    }

    fun createTabButton(
        context: Context,
        theme: VisualTheme,
        corner: CornerStyle,
        label: String,
        selected: Boolean,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            text = label
            textSize = 12.5f
            maxLines = 1
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
            isFocusable = true
            isClickable = true
            isSelected = selected
            background = navTabBackground(context, theme, corner)
            setTextColor(navTabTextColors(theme))
            val padH = dp(context, 14)
            val padV = dp(context, 7)
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(context, 6)
            }
            setOnFocusChangeListener { v, hasFocus -> FocusAnimator.applyNavFocus(v, hasFocus) }
            setOnClickListener { onClick() }
        }
    }

    fun createKeyValueRow(context: Context, theme: VisualTheme, key: String, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padV = dp(context, 7)
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(context, 6), padV, dp(context, 6), padV)
                addView(TextView(context).apply {
                    text = key
                    textSize = 12.5f
                    typeface = ResourcesCompat.getFont(context, R.font.outfit_regular)
                    setTextColor(theme.muted)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.44f)
                })
                addView(TextView(context).apply {
                    text = value
                    textSize = 12.5f
                    gravity = Gravity.END
                    typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
                    setTextColor(theme.ink)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.56f)
                })
            }
            addView(row)
            addView(View(context).apply {
                setBackgroundColor(withAlpha(theme.rule, 110))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1))
            })
        }
    }
}
