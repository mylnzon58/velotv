package io.github.mylnzon58.velotv.ui.home

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mylnzon58.velotv.data.apps.PackageManagerAppRepository
import io.github.mylnzon58.velotv.databinding.ItemAppCardBinding
import io.github.mylnzon58.velotv.domain.model.CardDensity
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.ui.common.FocusAnimator
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppCardAdapter(
    private val appRepository: PackageManagerAppRepository,
    private val scope: CoroutineScope,
    private val theme: VisualTheme,
    private val cornerStyle: CornerStyle,
    private val density: CardDensity,
    private val showBannerLabels: Boolean,
    private val onAppFocused: (TvApp) -> Unit,
    private val onAppClick: (TvApp) -> Unit,
    private val onAppLongClick: (TvApp) -> Unit
) : ListAdapter<TvApp, AppCardAdapter.AppVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppVH {
        val binding = ItemAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val lp = binding.cardRoot.layoutParams
        lp.width = HallmarkSurfaceFactory.dp(parent.context, density.widthDp)
        lp.height = HallmarkSurfaceFactory.dp(parent.context, density.heightDp)
        binding.cardRoot.layoutParams = lp
        binding.cardRoot.background = HallmarkSurfaceFactory.cardBackground(parent.context, theme, cornerStyle)
        binding.cardRoot.clipToOutline = true
        binding.tvIconLabel.setTextColor(theme.ink)
        binding.tvBannerLabel.setTextColor(theme.ink)
        binding.tvBannerLabel.setBackgroundColor(theme.paper)
        binding.tvBadge.setBackgroundColor(theme.paper)
        binding.tvBadge.setTextColor(theme.accent)
        return AppVH(binding)
    }

    override fun onBindViewHolder(holder: AppVH, position: Int) = holder.bind(getItem(position))

    override fun onViewRecycled(holder: AppVH) {
        holder.cancelLoad()
        super.onViewRecycled(holder)
    }

    inner class AppVH(private val b: ItemAppCardBinding) : RecyclerView.ViewHolder(b.root) {
        private var loadJob: Job? = null

        fun bind(app: TvApp) {
            cancelLoad()
            b.cardRoot.alpha = if (app.isHidden) 0.45f else 1.0f
            b.tvIconLabel.text = app.label
            b.tvBannerLabel.text = app.label
            b.ivBanner.visibility = View.GONE
            b.tvBannerLabel.visibility = View.GONE
            b.iconContainer.visibility = View.VISIBLE
            b.ivIcon.setImageDrawable(null)

            val badge = when {
                app.isLocked -> "PIN"
                app.isHidden -> "OCULTA"
                else -> ""
            }
            if (badge.isNotEmpty()) {
                b.tvBadge.text = badge
                b.tvBadge.visibility = View.VISIBLE
            } else {
                b.tvBadge.visibility = View.GONE
            }

            loadJob = scope.launch {
                val (bmp, isBanner) = appRepository.loadBannerOrIcon(app.packageName)
                if (bmp != null) {
                    if (isBanner) {
                        b.ivBanner.setImageBitmap(bmp)
                        b.ivBanner.visibility = View.VISIBLE
                        b.tvBannerLabel.visibility = if (showBannerLabels) View.VISIBLE else View.GONE
                        b.iconContainer.visibility = View.GONE
                    } else {
                        b.ivIcon.setImageBitmap(bmp)
                        b.iconContainer.visibility = View.VISIBLE
                        b.ivBanner.visibility = View.GONE
                        b.tvBannerLabel.visibility = View.GONE
                    }
                }
            }

            b.cardRoot.setOnFocusChangeListener { v, hasFocus ->
                FocusAnimator.apply(v, hasFocus, 1.06f)
                if (hasFocus) onAppFocused(app)
            }
            b.cardRoot.setOnClickListener { onAppClick(app) }
            b.cardRoot.setOnLongClickListener {
                onAppLongClick(app)
                true
            }
            b.cardRoot.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
                    onAppLongClick(app)
                    true
                } else false
            }
        }

        fun cancelLoad() {
            loadJob?.cancel()
            loadJob = null
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TvApp>() {
            override fun areItemsTheSame(a: TvApp, b: TvApp): Boolean = a.packageName == b.packageName
            override fun areContentsTheSame(a: TvApp, b: TvApp): Boolean = a == b
        }
    }
}
