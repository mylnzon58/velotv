package io.github.mylnzon58.velotv.ui.home

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.databinding.ItemTvInputCardBinding
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.TvInputType
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.ui.common.FocusAnimator
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory

class TvInputCardAdapter(
    private var theme: VisualTheme,
    private var cornerStyle: CornerStyle,
    private val onInputFocused: (TvInputSource) -> Unit,
    private val onInputClick: (TvInputSource) -> Unit,
    private val onInputLongClick: (TvInputSource) -> Unit
) : ListAdapter<TvInputSource, TvInputCardAdapter.InputVH>(DIFF) {

    fun updateStyle(newTheme: VisualTheme, newCorner: CornerStyle) {
        if (theme != newTheme || cornerStyle != newCorner) {
            theme = newTheme
            cornerStyle = newCorner
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputVH {
        val binding = ItemTvInputCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InputVH(binding)
    }

    override fun onBindViewHolder(holder: InputVH, position: Int) = holder.bind(getItem(position))

    inner class InputVH(private val b: ItemTvInputCardBinding) : RecyclerView.ViewHolder(b.root) {
        private var appliedTheme: VisualTheme? = null
        private var appliedCorner: CornerStyle? = null

        fun bind(input: TvInputSource) {
            val ctx = b.root.context
            if (appliedTheme != theme || appliedCorner != cornerStyle) {
                appliedTheme = theme
                appliedCorner = cornerStyle
                b.inputRoot.background = HallmarkSurfaceFactory.cardSurfaceBackground(ctx, theme, cornerStyle)
                b.inputRoot.foreground = HallmarkSurfaceFactory.cardForegroundRing(ctx, theme, cornerStyle)
                b.inputRoot.clipToOutline = true
                b.tvInputTitle.setTextColor(theme.ink)
                b.tvInputSub.setTextColor(theme.muted)
            }

            b.inputRoot.alpha = if (input.isHidden) 0.45f else 1.0f
            b.tvInputTitle.text = input.label
            val statusText = ctx.getString(if (input.isConnected) R.string.input_connected else R.string.input_ready)
            b.tvInputSub.text = if (input.isLocked) "$statusText · PIN" else statusText

            val iconRes = if (input.type == TvInputType.HDMI) R.drawable.ic_input_hdmi else R.drawable.ic_input_tv
            b.ivInputIcon.setImageResource(iconRes)
            b.ivInputIcon.setColorFilter(if (input.isConnected) theme.accent else theme.ink2)
            b.inputIconWrap.background = HallmarkSurfaceFactory.inputIconCircle(ctx, theme, input.isConnected)

            val dotColor = if (input.isConnected) {
                ContextCompat.getColor(ctx, R.color.hm_signal_ok)
            } else {
                HallmarkSurfaceFactory.withAlpha(theme.muted, 150)
            }
            b.statusBar.background = HallmarkSurfaceFactory.dotDrawable(dotColor)

            b.inputRoot.setOnFocusChangeListener { v, hasFocus ->
                FocusAnimator.apply(v, hasFocus, 1.07f, 14f)
                if (hasFocus) onInputFocused(input)
            }
            b.inputRoot.setOnClickListener { onInputClick(input) }
            b.inputRoot.setOnLongClickListener {
                onInputLongClick(input)
                true
            }
            b.inputRoot.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
                    onInputLongClick(input)
                    true
                } else false
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TvInputSource>() {
            override fun areItemsTheSame(a: TvInputSource, b: TvInputSource): Boolean = a.id == b.id
            override fun areContentsTheSame(a: TvInputSource, b: TvInputSource): Boolean = a == b
        }
    }
}
