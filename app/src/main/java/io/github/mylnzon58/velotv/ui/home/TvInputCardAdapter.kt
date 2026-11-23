package io.github.mylnzon58.velotv.ui.home

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.databinding.ItemTvInputCardBinding
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.ui.common.FocusAnimator
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory

class TvInputCardAdapter(
    private val theme: VisualTheme,
    private val cornerStyle: CornerStyle,
    private val onInputFocused: (TvInputSource) -> Unit,
    private val onInputClick: (TvInputSource) -> Unit,
    private val onInputLongClick: (TvInputSource) -> Unit
) : ListAdapter<TvInputSource, TvInputCardAdapter.InputVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputVH {
        val binding = ItemTvInputCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.inputRoot.background = HallmarkSurfaceFactory.cardBackground(parent.context, theme, cornerStyle)
        binding.inputRoot.clipToOutline = true
        binding.tvInputTitle.setTextColor(theme.ink)
        binding.tvInputSub.setTextColor(theme.muted)
        return InputVH(binding)
    }

    override fun onBindViewHolder(holder: InputVH, position: Int) = holder.bind(getItem(position))

    inner class InputVH(private val b: ItemTvInputCardBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(input: TvInputSource) {
            val ctx = b.root.context
            b.inputRoot.alpha = if (input.isHidden) 0.45f else 1.0f
            b.tvInputTitle.text = input.label
            val statusText = ctx.getString(if (input.isConnected) R.string.input_connected else R.string.input_ready)
            b.tvInputSub.text = if (input.isLocked) "$statusText · PIN" else statusText
            b.statusBar.setBackgroundColor(if (input.isConnected) 0xFF7CB68A.toInt() else theme.rule)

            b.inputRoot.setOnFocusChangeListener { v, hasFocus ->
                FocusAnimator.apply(v, hasFocus, 1.055f)
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
