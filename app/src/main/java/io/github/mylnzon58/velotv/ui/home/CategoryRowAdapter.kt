package io.github.mylnzon58.velotv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.data.apps.PackageManagerAppRepository
import io.github.mylnzon58.velotv.databinding.ItemCategoryRowBinding
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.CardDensity
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.HomeRow
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.ui.common.SmoothTvLayoutManager
import kotlinx.coroutines.CoroutineScope

class CategoryRowAdapter(
    private val appRepository: PackageManagerAppRepository,
    private val scope: CoroutineScope,
    private val onAppFocused: (TvApp) -> Unit,
    private val onAppClick: (TvApp) -> Unit,
    private val onAppLongClick: (TvApp) -> Unit,
    private val onInputFocused: (TvInputSource) -> Unit,
    private val onInputClick: (TvInputSource) -> Unit,
    private val onInputLongClick: (TvInputSource) -> Unit
) : ListAdapter<HomeRow, CategoryRowAdapter.RowVH>(DIFF) {

    // Shared RecycledViewPool across all horizontal app rows minimizes GC & inflation lag on 2 GB TVs
    private val sharedAppPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(0, 24)
    }

    private var theme: VisualTheme = VisualTheme.DARKROOM_AMBER
    private var corner: CornerStyle = CornerStyle.EDITORIAL
    private var density: CardDensity = CardDensity.NORMAL
    private var showBannerLabels: Boolean = false

    fun updateStyle(
        newTheme: VisualTheme,
        newCorner: CornerStyle,
        newDensity: CardDensity,
        newShowBannerLabels: Boolean
    ) {
        if (theme != newTheme || corner != newCorner || density != newDensity || showBannerLabels != newShowBannerLabels) {
            theme = newTheme
            corner = newCorner
            density = newDensity
            showBannerLabels = newShowBannerLabels
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowVH {
        val binding = ItemCategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.itemsRecyclerView.layoutManager =
            SmoothTvLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, 88)
        binding.itemsRecyclerView.itemAnimator = null
        binding.itemsRecyclerView.setHasFixedSize(true)
        binding.itemsRecyclerView.setItemViewCacheSize(6)
        return RowVH(binding)
    }

    override fun onBindViewHolder(holder: RowVH, position: Int) = holder.bind(getItem(position))

    inner class RowVH(private val b: ItemCategoryRowBinding) : RecyclerView.ViewHolder(b.root) {
        private var appAdapter: AppCardAdapter? = null
        private var inputAdapter: TvInputCardAdapter? = null

        fun bind(row: HomeRow) {
            val ctx = b.root.context
            b.tvCategoryTitle.setTextColor(theme.ink)
            b.tvCategoryCount.setTextColor(theme.muted)
            b.tvCategoryTitle.text = row.customTitle ?: ctx.getString(categoryTitleRes(row.rowId))

            when (row) {
                is HomeRow.AppsRow -> {
                    b.tvCategoryCount.text = row.apps.size.toString()
                    b.itemsRecyclerView.setRecycledViewPool(sharedAppPool)
                    val existing = appAdapter
                    val adapter = if (existing != null) {
                        existing.updateStyle(theme, corner, density, showBannerLabels)
                        existing
                    } else {
                        AppCardAdapter(
                            appRepository = appRepository,
                            scope = scope,
                            theme = theme,
                            cornerStyle = corner,
                            density = density,
                            showBannerLabels = showBannerLabels,
                            onAppFocused = onAppFocused,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick
                        ).also { appAdapter = it }
                    }
                    if (b.itemsRecyclerView.adapter !== adapter) {
                        b.itemsRecyclerView.adapter = adapter
                    }
                    adapter.submitList(row.apps)
                }

                is HomeRow.InputsRow -> {
                    b.tvCategoryCount.text = row.inputs.size.toString()
                    val existing = inputAdapter
                    val adapter = if (existing != null) {
                        existing.updateStyle(theme, corner)
                        existing
                    } else {
                        TvInputCardAdapter(
                            theme = theme,
                            cornerStyle = corner,
                            onInputFocused = onInputFocused,
                            onInputClick = onInputClick,
                            onInputLongClick = onInputLongClick
                        ).also { inputAdapter = it }
                    }
                    if (b.itemsRecyclerView.adapter !== adapter) {
                        b.itemsRecyclerView.adapter = adapter
                    }
                    adapter.submitList(row.inputs)
                }
            }
        }
    }

    private fun categoryTitleRes(rowId: String): Int = when (rowId) {
        AppCategory.FAVORITES.id -> R.string.cat_favorites
        AppCategory.TV_INPUTS.id -> R.string.cat_tv_inputs
        AppCategory.STREAMING.id -> R.string.cat_streaming
        AppCategory.MEDIA_WEB.id -> R.string.cat_media_web
        AppCategory.GAMES.id -> R.string.cat_games
        AppCategory.TOOLS.id -> R.string.cat_tools
        else -> R.string.cat_all_apps
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HomeRow>() {
            override fun areItemsTheSame(a: HomeRow, b: HomeRow): Boolean = a.rowId == b.rowId
            override fun areContentsTheSame(a: HomeRow, b: HomeRow): Boolean = a == b
        }
    }
}
