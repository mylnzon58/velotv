package io.github.mylnzon58.velotv.ui.home

import android.graphics.Bitmap
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
    private var theme: VisualTheme,
    private var cornerStyle: CornerStyle,
    private var density: CardDensity,
    private var showBannerLabels: Boolean,
    private val onAppFocused: (TvApp) -> Unit,
    private val onAppClick: (TvApp) -> Unit,
    private val onAppLongClick: (TvApp) -> Unit
) : ListAdapter<TvApp, AppCardAdapter.AppVH>(DIFF) {

    fun updateStyle(
        newTheme: VisualTheme,
        newCorner: CornerStyle,
        newDensity: CardDensity,
        newShowBannerLabels: Boolean
    ) {
        if (theme != newTheme || cornerStyle != newCorner || density != newDensity || showBannerLabels != newShowBannerLabels) {
            theme = newTheme
            cornerStyle = newCorner
            density = newDensity
            showBannerLabels = newShowBannerLabels
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppVH {
        val binding = ItemAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppVH(binding)
    }

    override fun onBindViewHolder(holder: AppVH, position: Int) = holder.bind(getItem(position))

    override fun onViewRecycled(holder: AppVH) {
        holder.cancelLoad()
        super.onViewRecycled(holder)
    }

    inner class AppVH(private val b: ItemAppCardBinding) : RecyclerView.ViewHolder(b.root) {
        private var loadJob: Job? = null
        private var appliedTheme: VisualTheme? = null
        private var appliedCorner: CornerStyle? = null
        private var appliedDensity: CardDensity? = null

        private fun applyCardChrome() {
            if (appliedTheme == theme && appliedCorner == cornerStyle && appliedDensity == density) return
            appliedTheme = theme
            appliedCorner = cornerStyle
            appliedDensity = density

            val ctx = b.cardRoot.context
            val lp = b.cardRoot.layoutParams
            val w = HallmarkSurfaceFactory.dp(ctx, density.widthDp)
            val h = HallmarkSurfaceFactory.dp(ctx, density.heightDp)
            if (lp.width != w || lp.height != h) {
                lp.width = w
                lp.height = h
                b.cardRoot.layoutParams = lp
            }
            b.cardRoot.background = HallmarkSurfaceFactory.cardSurfaceBackground(ctx, theme, cornerStyle)
            b.cardRoot.foreground = HallmarkSurfaceFactory.cardForegroundRing(ctx, theme, cornerStyle)
            b.cardRoot.clipToOutline = true
            b.tvIconLabel.setTextColor(theme.ink)
            b.tvBannerLabel.setTextColor(theme.ink)
            b.tvBannerLabel.setBackgroundColor(HallmarkSurfaceFactory.withAlpha(theme.paper, 224))
            b.tvBadge.setBackgroundColor(HallmarkSurfaceFactory.withAlpha(theme.paper, 232))
            b.tvBadge.setTextColor(theme.accent)
        }

        fun bind(app: TvApp) {
            cancelLoad()
            applyCardChrome()

            b.cardRoot.alpha = if (app.isHidden) 0.45f else 1.0f
            b.tvIconLabel.text = app.label
            b.tvBannerLabel.text = app.label

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

            // Synchronous cache fast-path: zero flicker, zero coroutine allocation on cached bitmaps
            val cached = appRepository.getCachedBannerOrIcon(app.packageName)
            if (cached != null) {
                renderGraphic(cached.first, cached.second)
            } else {
                b.ivBanner.visibility = View.GONE
                b.tvBannerLabel.visibility = View.GONE
                b.iconContainer.visibility = View.VISIBLE
                b.ivIcon.setImageDrawable(null)
                loadJob = scope.launch {
                    val (bmp, isBanner) = appRepository.loadBannerOrIcon(app.packageName)
                    if (bmp != null) renderGraphic(bmp, isBanner)
                }
            }

            b.cardRoot.setOnFocusChangeListener { v, hasFocus ->
                FocusAnimator.apply(v, hasFocus, 1.08f, 14f)
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

        private fun renderGraphic(bmp: Bitmap, isBanner: Boolean) {
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
