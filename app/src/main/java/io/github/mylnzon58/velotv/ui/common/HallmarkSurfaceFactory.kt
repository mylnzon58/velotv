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
 * Dynamic Hallmark design-system surface factory.
 * Enforces solid stepped surfaces (paper -> paper2 -> paper3), 1dp structural hairlines,
 * restrained accent signals, and zero translucent glass or neon glow.
 */
object HallmarkSurfaceFactory {

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun dpFloat(context: Context, value: Float): Float =
        value * context.resources.displayMetrics.density

    fun cardBackground(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, corner.radiusDp.toFloat())
        val strokeIdle = dp(context, 1)
        val strokeFocused = dpFloat(context, 2.5f).toInt().coerceAtLeast(2)

        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.paper3)
            setStroke(strokeFocused, theme.accent)
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.paper2)
            setStroke(strokeIdle, theme.rule)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(android.R.attr.state_selected), focused)
            addState(intArrayOf(), idle)
        }
    }

    fun navTabBackground(context: Context, theme: VisualTheme, corner: CornerStyle): StateListDrawable {
        val radius = dpFloat(context, corner.radiusDp.coerceAtMost(4).toFloat())
        val focused = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(theme.accent)
        }
        val selected = LayerDrawable(
            arrayOf(
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(theme.paper2)
                },
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(theme.accent)
                }
            )
        ).apply {
            setLayerGravity(1, Gravity.BOTTOM)
            setLayerHeight(1, dp(context, 2))
        }
        val idle = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
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
        val radius = dpFloat(context, corner.radiusDp.coerceAtMost(4).toFloat())
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

    fun studioFrameBackground(context: Context, theme: VisualTheme, corner: CornerStyle): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(context, corner.radiusDp.coerceAtMost(6).toFloat())
            setColor(theme.paper2)
            setStroke(dp(context, 1), theme.rule)
        }
    }

    fun createSectionLabel(context: Context, theme: VisualTheme, title: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(context, 14)
                bottomMargin = dp(context, 6)
            }
            addView(TextView(context).apply {
                text = title.uppercase()
                textSize = 13f
                letterSpacing = 0.08f
                typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_semibold)
                setTextColor(theme.muted)
                setPadding(0, 0, 0, dp(context, 4))
            })
            addView(View(context).apply {
                setBackgroundColor(theme.rule)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(context, 1)
                )
            })
        }
    }

    /**
     * Clean editorial list row: flat hairline separation, solid paper3 step + 3dp left accent rail on focus.
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
        val accentBar = View(context).apply {
            setBackgroundColor(theme.accent)
            visibility = View.INVISIBLE
            layoutParams = LinearLayout.LayoutParams(dp(context, 3), LinearLayout.LayoutParams.MATCH_PARENT)
        }

        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = dp(context, 14)
            val padV = dp(context, 11)
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val textCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        textCol.addView(TextView(context).apply {
            text = title
            textSize = 14f
            typeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
            setTextColor(theme.ink)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })

        if (!subtitle.isNullOrBlank()) {
            textCol.addView(TextView(context).apply {
                text = subtitle
                textSize = 11.5f
                typeface = ResourcesCompat.getFont(context, R.font.barlow_regular)
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
                typeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
                setTextColor(if (accentTrailing) theme.accent else theme.ink2)
                setPadding(dp(context, 12), 0, 0, 0)
            })
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isFocusable = true
            isClickable = true
            setBackgroundColor(theme.paper2)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val rowH = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                addView(accentBar)
                addView(inner)
            }
            addView(rowH)
            addView(View(context).apply {
                setBackgroundColor(theme.rule)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(context, 1)
                )
            })

            setOnFocusChangeListener { v, hasFocus ->
                v.setBackgroundColor(if (hasFocus) theme.paper3 else theme.paper2)
                accentBar.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                if (hasFocus) onFocus?.invoke()
            }
            setOnClickListener { onClick() }
        }
        return container
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
            typeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
            isFocusable = true
            isClickable = true
            isSelected = selected
            background = navTabBackground(context, theme, corner)
            setTextColor(navTabTextColors(theme))
            val padH = dp(context, 12)
            val padV = dp(context, 8)
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(context, 4)
            }
            setOnClickListener { onClick() }
        }
    }

    fun createKeyValueRow(context: Context, theme: VisualTheme, key: String, value: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padV = dp(context, 7)
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(context, 4), padV, dp(context, 4), padV)
                addView(TextView(context).apply {
                    text = key
                    textSize = 12.5f
                    typeface = ResourcesCompat.getFont(context, R.font.barlow_regular)
                    setTextColor(theme.muted)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.45f)
                })
                addView(TextView(context).apply {
                    text = value
                    textSize = 12.5f
                    gravity = Gravity.END
                    typeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
                    setTextColor(theme.ink)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.55f)
                })
            }
            addView(row)
            addView(View(context).apply {
                setBackgroundColor(theme.rule)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1))
            })
        }
    }
}

