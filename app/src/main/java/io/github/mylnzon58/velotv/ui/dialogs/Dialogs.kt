package io.github.mylnzon58.velotv.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.core.AppContainer
import io.github.mylnzon58.velotv.data.system.OfflineQrGenerator
import io.github.mylnzon58.velotv.databinding.DialogOnboardingBinding
import io.github.mylnzon58.velotv.databinding.DialogPinBinding
import io.github.mylnzon58.velotv.databinding.DialogSupportCreatorBinding
import io.github.mylnzon58.velotv.databinding.DrawerSidePanelBinding
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.AppSourceType
import io.github.mylnzon58.velotv.domain.model.DonationChannel
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.SettingShortcutGroup
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.domain.model.WorldRegion
import io.github.mylnzon58.velotv.domain.usecase.BuildDonationPlanUseCase
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory
import io.github.mylnzon58.velotv.ui.home.HomeViewModel
import io.github.mylnzon58.velotv.ui.tools.AmbientClockActivity
import io.github.mylnzon58.velotv.ui.tools.CalibrationActivity
import java.util.Locale

object VeloDialogs {

    private var onboardingShowing = false

    private fun createRightDrawer(
        context: Context,
        theme: VisualTheme,
        widthDp: Int = 540
    ): Pair<Dialog, DrawerSidePanelBinding> {
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        val b = DrawerSidePanelBinding.inflate(LayoutInflater.from(context))
        b.drawerRoot.layoutParams = LinearLayout.LayoutParams(
            HallmarkSurfaceFactory.dp(context, widthDp),
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        b.drawerSurface.setBackgroundColor(theme.paper2)
        b.drawerLeftRule.setBackgroundColor(theme.rule)
        b.drawerHeaderRule.setBackgroundColor(theme.rule)
        b.drawerFooterRule.setBackgroundColor(theme.rule)
        b.tvDrawerTitle.setTextColor(theme.ink)
        b.tvDrawerSubtitle.setTextColor(theme.muted)
        b.tvDrawerFooter.setTextColor(theme.muted)
        b.tvDrawerFooter.text = context.getString(R.string.creator_settings_footer)

        dialog.setContentView(b.root)
        dialog.window?.apply {
            setGravity(Gravity.END)
            setLayout(
                HallmarkSurfaceFactory.dp(context, widthDp),
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        return dialog to b
    }

    fun showSearchDrawer(context: Context, vm: HomeViewModel, container: AppContainer) {
        val s = vm.state.value.settings
        val theme = s.theme
        val (dialog, b) = createRightDrawer(context, theme, 540)
        b.tvDrawerTitle.text = context.getString(R.string.search_title)
        b.tvDrawerSubtitle.text = context.getString(R.string.search_subtitle)
        b.drawerTopSlot.visibility = View.VISIBLE

        val input = EditText(context).apply {
            hint = context.getString(R.string.search_hint)
            setHintTextColor(theme.muted)
            setTextColor(theme.ink)
            textSize = 15f
            isSingleLine = true
            typeface = ResourcesCompat.getFont(context, R.font.barlow_regular)
            val padH = HallmarkSurfaceFactory.dp(context, 14)
            val padV = HallmarkSurfaceFactory.dp(context, 10)
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(theme.paper)
                setStroke(HallmarkSurfaceFactory.dp(context, 1), theme.rule)
                cornerRadius = HallmarkSurfaceFactory.dpFloat(context, s.cornerStyle.radiusDp.coerceAtMost(4).toFloat())
            }
        }
        b.drawerTopSlot.addView(input)

        fun renderResults(q: String) {
            b.drawerContent.removeAllViews()
            val res = vm.search(q)
            if (res.inputs.isNotEmpty()) {
                b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.cat_tv_inputs)))
                res.inputs.forEach { inp ->
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme, inp.label,
                            if (inp.isConnected) context.getString(R.string.input_connected) else context.getString(R.string.input_ready),
                            "HDMI/TV", true
                        ) {
                            container.tvInputRepository.launchTvInput(inp)
                            dialog.dismiss()
                        }
                    )
                }
            }
            if (res.apps.isNotEmpty()) {
                b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.cat_all_apps)))
                res.apps.forEach { app ->
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme, app.label, app.packageName,
                            if (app.isFavorite) "FAV" else null, app.isFavorite
                        ) {
                            container.appRepository.launchApp(app.packageName)
                            dialog.dismiss()
                        }
                    )
                }
            }
            if (res.apps.isEmpty() && res.inputs.isEmpty()) {
                b.drawerContent.addView(TextView(context).apply {
                    text = context.getString(R.string.search_empty)
                    setTextColor(theme.muted)
                    textSize = 14f
                    setPadding(0, HallmarkSurfaceFactory.dp(context, 24), 0, 0)
                })
            }
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderResults(s?.toString().orEmpty())
            override fun afterTextChanged(s: Editable?) = Unit
        })

        renderResults("")
        dialog.show()
        input.requestFocus()
    }

    fun showAppActions(context: Context, app: TvApp, vm: HomeViewModel, container: AppContainer) {
        val s = vm.state.value.settings
        val theme = s.theme
        val (dialog, b) = createRightDrawer(context, theme, 480)
        b.tvDrawerTitle.text = app.label
        b.tvDrawerSubtitle.text = app.packageName

        fun addRow(title: String, sub: String? = null, trail: String? = null, closeAfter: Boolean = true, act: () -> Unit) {
            b.drawerContent.addView(
                HallmarkSurfaceFactory.createDrawerRow(context, theme, title, sub, trail, true) {
                    act()
                    if (closeAfter) dialog.dismiss()
                }
            )
        }

        addRow(context.getString(R.string.action_open)) { container.appRepository.launchApp(app.packageName) }
        addRow(context.getString(if (app.isFavorite) R.string.action_remove_fav else R.string.action_add_fav)) {
            vm.toggleFavorite(app.packageName)
        }
        if (app.isFavorite) {
            addRow(context.getString(R.string.action_move_left), closeAfter = false) { vm.moveFavorite(app.packageName, -1) }
            addRow(context.getString(R.string.action_move_right), closeAfter = false) { vm.moveFavorite(app.packageName, +1) }
        }
        addRow(context.getString(R.string.action_rename), app.defaultLabel) {
            showTextInputDialog(context, theme, s.cornerStyle, context.getString(R.string.action_rename), app.label) { text ->
                vm.renameApp(app.packageName, text)
            }
        }

        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_category)))
        val builtinTargets = listOf(
            AppCategory.STREAMING.id to context.getString(R.string.cat_streaming),
            AppCategory.MEDIA_WEB.id to context.getString(R.string.cat_media_web),
            AppCategory.GAMES.id to context.getString(R.string.cat_games),
            AppCategory.TOOLS.id to context.getString(R.string.cat_tools)
        )
        (builtinTargets + s.customCategories.map { it.id to it.title }).forEach { (catId, catName) ->
            val currentMark = if (app.categoryId == catId) context.getString(R.string.state_active) else null
            addRow(catName, null, currentMark) { vm.setAppCategory(app.packageName, catId) }
        }

        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_control)))
        if (vm.hasPin()) {
            addRow(context.getString(if (app.isLocked) R.string.action_unlock_pin else R.string.action_lock_pin)) {
                vm.toggleLockApp(app.packageName)
            }
        }
        addRow(context.getString(if (app.isHidden) R.string.action_unhide else R.string.action_hide)) {
            vm.toggleHideApp(app.packageName)
        }
        val autoKey = "pkg:${app.packageName}"
        val isAuto = s.autoLaunchTarget == autoKey
        addRow(context.getString(if (isAuto) R.string.action_auto_launch_off else R.string.action_auto_launch_on)) {
            vm.toggleAutoLaunch(autoKey)
        }
        addRow(context.getString(R.string.action_app_info), context.getString(R.string.action_app_info_sub)) {
            container.appRepository.openAppInfo(app.packageName)
        }
        if (!app.isSystemApp) {
            addRow(context.getString(R.string.action_uninstall)) {
                container.appRepository.requestUninstall(app.packageName)
            }
        }

        dialog.show()
        b.drawerContent.getChildAt(0)?.requestFocus()
    }

    fun showInputActions(context: Context, input: TvInputSource, vm: HomeViewModel, container: AppContainer) {
        val s = vm.state.value.settings
        val theme = s.theme
        val (dialog, b) = createRightDrawer(context, theme, 460)
        b.tvDrawerTitle.text = input.label
        b.tvDrawerSubtitle.text = input.id

        fun addRow(title: String, sub: String? = null, act: () -> Unit) {
            b.drawerContent.addView(
                HallmarkSurfaceFactory.createDrawerRow(context, theme, title, sub, null, false) {
                    act()
                    dialog.dismiss()
                }
            )
        }

        addRow(context.getString(R.string.action_open)) { container.tvInputRepository.launchTvInput(input) }
        addRow(context.getString(R.string.action_rename), input.defaultLabel) {
            showTextInputDialog(context, theme, s.cornerStyle, context.getString(R.string.action_rename), input.label) { text ->
                vm.renameInput(input.id, text)
            }
        }
        if (vm.hasPin()) {
            addRow(context.getString(if (input.isLocked) R.string.action_unlock_pin else R.string.action_lock_pin)) {
                vm.toggleLockInput(input.id)
            }
        }
        addRow(context.getString(if (input.isHidden) R.string.action_unhide else R.string.action_hide)) {
            vm.toggleHideInput(input.id)
        }
        val autoKey = "input:${input.id}"
        val isAuto = s.autoLaunchTarget == autoKey
        addRow(context.getString(if (isAuto) R.string.action_auto_launch_off else R.string.action_auto_launch_on)) {
            vm.toggleAutoLaunch(autoKey)
        }

        dialog.show()
        b.drawerContent.getChildAt(0)?.requestFocus()
    }

    /**
     * Complete System, Native Developer Options & Engineering Diagnostics Drawer.
     */
    fun showSystemDeveloperDrawer(context: Context, vm: HomeViewModel) {
        vm.refreshDiagnostics()
        val s = vm.state.value.settings
        val theme = s.theme
        val (dialog, b) = createRightDrawer(context, theme, 600)
        b.tvDrawerTitle.text = context.getString(R.string.sys_drawer_title)
        b.tvDrawerSubtitle.text = context.getString(R.string.sys_drawer_subtitle)
        b.drawerTopSlot.visibility = View.VISIBLE

        var activeTab = 0 // 0=Developer, 1=System & Tools, 2=Engineering Specs
        val shortcuts = vm.getAvailableNativeShortcuts()

        fun renderTab(tab: Int) {
            activeTab = tab
            b.drawerTopSlot.removeAllViews()
            val tabRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            val tabs = listOf(
                0 to context.getString(R.string.tab_developer),
                1 to context.getString(R.string.tab_system_tools),
                2 to context.getString(R.string.tab_engineering)
            )
            tabs.forEach { (idx, label) ->
                tabRow.addView(HallmarkSurfaceFactory.createTabButton(context, theme, s.cornerStyle, label, idx == activeTab) {
                    renderTab(idx)
                })
            }
            b.drawerTopSlot.addView(tabRow)

            b.drawerContent.removeAllViews()
            val d = vm.state.value.diagnostics
            val yes = context.getString(R.string.state_on)
            val no = context.getString(R.string.state_off)

            when (activeTab) {
                0 -> {
                    if (d != null) {
                        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_dev_live)))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_adb), if (d.adbEnabled) "$yes (5555)" else no))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_dev_mode), if (d.developmentEnabled) yes else no))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_unknown_sources), if (d.unknownSourcesEnabled) yes else no))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_animations), "${d.animWindowScale} / ${d.animTransitionScale} / ${d.animDurationScale}"))
                    }

                    val devShortcuts = shortcuts.filter { it.group == SettingShortcutGroup.DEVELOPER }
                    if (devShortcuts.isNotEmpty()) {
                        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_native_dev)))
                        devShortcuts.forEach { sc ->
                            b.drawerContent.addView(
                                HallmarkSurfaceFactory.createDrawerRow(
                                    context, theme,
                                    context.getString(sc.titleRes),
                                    context.getString(sc.subtitleRes),
                                    context.getString(R.string.action_open_short),
                                    true
                                ) { vm.openNativeShortcut(sc.id) }
                            )
                        }
                    }

                    val hwShortcuts = shortcuts.filter { it.group == SettingShortcutGroup.HARDWARE }
                    if (hwShortcuts.isNotEmpty()) {
                        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_oem_hw)))
                        hwShortcuts.forEach { sc ->
                            b.drawerContent.addView(
                                HallmarkSurfaceFactory.createDrawerRow(
                                    context, theme,
                                    context.getString(sc.titleRes),
                                    context.getString(sc.subtitleRes),
                                    if (sc.requiresCaution) "OEM" else context.getString(R.string.action_open_short),
                                    sc.requiresCaution
                                ) { vm.openNativeShortcut(sc.id) }
                            )
                        }
                    }
                }

                1 -> {
                    b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_pro_tools)))
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.tool_calibration),
                            context.getString(R.string.tool_calibration_sub),
                            "4K / HDR", true
                        ) {
                            dialog.dismiss()
                            context.startActivity(Intent(context, CalibrationActivity::class.java))
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.tool_ambient_clock),
                            context.getString(R.string.tool_ambient_clock_sub),
                            "OLED", true
                        ) {
                            dialog.dismiss()
                            context.startActivity(Intent(context, AmbientClockActivity::class.java))
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.health_clear_cache),
                            context.getString(R.string.health_clear_cache_sub),
                            null, false
                        ) {
                            vm.clearIconCache()
                            Toast.makeText(context, R.string.toast_cache_cleared, Toast.LENGTH_SHORT).show()
                        }
                    )

                    b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_native_system)))
                    shortcuts.filter { it.group == SettingShortcutGroup.SYSTEM }.forEach { sc ->
                        b.drawerContent.addView(
                            HallmarkSurfaceFactory.createDrawerRow(
                                context, theme,
                                context.getString(sc.titleRes),
                                context.getString(sc.subtitleRes),
                                context.getString(R.string.action_open_short),
                                false
                            ) { vm.openNativeShortcut(sc.id) }
                        )
                    }
                }

                2 -> {
                    if (d != null) {
                        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_hw_profile)))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_device), "${d.manufacturer} ${d.model}"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_platform), "${d.platform} · ${d.board}"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_android), "Android ${d.androidVersion} (API ${d.apiLevel})"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_build), d.buildId))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_patch), d.securityPatch))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_kernel), "${d.kernelVersion} (${d.cpuAbi})"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_webview), d.webviewVersion))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_display), "${d.displayResolution} @ ${d.refreshRateHz} Hz"))

                        b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_resources)))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, "RAM", "${d.availableRamMb} MB libre / ${d.totalRamMb} MB (${d.usedRamPercent}%)"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, "/data", String.format(Locale.US, "%.1f GB libre / %.1f GB", d.dataFreeGb, d.dataTotalGb)))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, "/system", "${d.systemFreeMb} MB libre / ${d.systemTotalMb} MB"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_network), "${d.networkLabel} · ${d.localIp}"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_uptime), d.uptimeLabel))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, "Bootloader / Treble", "lock=${d.bootloaderLocked} · treble=${d.trebleEnabled}"))
                        b.drawerContent.addView(HallmarkSurfaceFactory.createKeyValueRow(context, theme, context.getString(R.string.diag_privacy), context.getString(R.string.privacy_zero_net_short)))
                    }
                }
            }
        }

        renderTab(0)
        dialog.show()
        b.drawerContent.getChildAt(1)?.requestFocus() ?: b.drawerTopSlot.requestFocus()
    }

    /**
     * Full Tabbed Settings Drawer (Aspecto · Filas · Parental · General).
     */
    fun showSettingsDrawer(
        context: Context,
        vm: HomeViewModel,
        onLanguageChanged: () -> Unit,
        onOpenSupport: () -> Unit
    ) {
        var activeTab = 0
        val initTheme = vm.state.value.settings.theme
        val (dialog, b) = createRightDrawer(context, initTheme, 580)
        b.tvDrawerTitle.text = context.getString(R.string.settings_title)
        b.tvDrawerSubtitle.text = context.getString(R.string.settings_subtitle)
        b.drawerTopSlot.visibility = View.VISIBLE

        fun rethemeDrawer(t: VisualTheme) {
            b.drawerSurface.setBackgroundColor(t.paper2)
            b.drawerLeftRule.setBackgroundColor(t.rule)
            b.drawerHeaderRule.setBackgroundColor(t.rule)
            b.drawerFooterRule.setBackgroundColor(t.rule)
            b.tvDrawerTitle.setTextColor(t.ink)
            b.tvDrawerSubtitle.setTextColor(t.muted)
            b.tvDrawerFooter.setTextColor(t.muted)
        }

        fun render(tab: Int, focusIdx: Int = 0) {
            activeTab = tab
            val s = vm.state.value.settings
            val theme = s.theme
            rethemeDrawer(theme)

            b.drawerTopSlot.removeAllViews()
            val tabRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            val tabs = listOf(
                0 to context.getString(R.string.tab_appearance),
                1 to context.getString(R.string.tab_rows),
                2 to context.getString(R.string.tab_parental),
                3 to context.getString(R.string.tab_general)
            )
            tabs.forEach { (idx, label) ->
                tabRow.addView(HallmarkSurfaceFactory.createTabButton(context, theme, s.cornerStyle, label, idx == activeTab) {
                    render(idx, 0)
                })
            }
            b.drawerTopSlot.addView(tabRow)
            b.drawerContent.removeAllViews()

            val on = context.getString(R.string.state_on)
            val off = context.getString(R.string.state_off)

            when (activeTab) {
                0 -> {
                    val items = listOf<Triple<String, String, () -> Unit>>(
                        Triple(context.getString(R.string.setting_theme), themeDisplayName(context, s.theme)) {
                            vm.cycleTheme(); render(0, 0)
                        },
                        Triple(context.getString(R.string.setting_corners), cornerDisplayName(context, s.cornerStyle.id)) {
                            vm.cycleCornerStyle(); render(0, 1)
                        },
                        Triple(context.getString(R.string.setting_density), densityDisplayName(context, s.cardDensity.id)) {
                            vm.cycleCardDensity(); render(0, 2)
                        },
                        Triple(context.getString(R.string.setting_banner_labels), if (s.showBannerLabels) on else off) {
                            vm.updateSettings { it.copy(showBannerLabels = !it.showBannerLabels) }; render(0, 3)
                        },
                        Triple(context.getString(R.string.setting_hero_bar), if (s.showHeroFocusBar) on else off) {
                            vm.updateSettings { it.copy(showHeroFocusBar = !it.showHeroFocusBar) }; render(0, 4)
                        },
                        Triple(context.getString(R.string.setting_clock_show), if (s.showClock) on else off) {
                            vm.updateSettings { it.copy(showClock = !it.showClock) }; render(0, 5)
                        },
                        Triple(context.getString(R.string.setting_clock_24h), if (s.use24HourClock) on else off) {
                            vm.updateSettings { it.copy(use24HourClock = !it.use24HourClock) }; render(0, 6)
                        },
                        Triple(context.getString(R.string.setting_clock_seconds), if (s.showSeconds) on else off) {
                            vm.updateSettings { it.copy(showSeconds = !it.showSeconds) }; render(0, 7)
                        },
                        Triple(context.getString(R.string.setting_show_stats), if (s.showSystemStats) on else off) {
                            vm.updateSettings { it.copy(showSystemStats = !it.showSystemStats) }; render(0, 8)
                        },
                        Triple(context.getString(R.string.setting_show_hints), if (s.showBottomHints) on else off) {
                            vm.updateSettings { it.copy(showBottomHints = !it.showBottomHints) }; render(0, 9)
                        }
                    )
                    items.forEach { (title, trail, click) ->
                        b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, title, null, trail, true, onClick = click))
                    }
                }

                1 -> {
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.row_create_custom),
                            context.getString(R.string.row_create_custom_sub),
                            "+", true
                        ) {
                            showTextInputDialog(context, theme, s.cornerStyle, context.getString(R.string.row_create_custom), "") { name ->
                                vm.addCustomCategory(name)
                                render(1, 0)
                            }
                        }
                    )

                    val customMap = s.customCategories.associate { it.id to it.title }
                    val orderedIds = LinkedHashSet<String>().apply {
                        addAll(s.rowOrder)
                        addAll(LauncherSettings.DEFAULT_ROW_ORDER)
                        addAll(s.customCategories.map { it.id })
                    }.toList()

                    b.drawerContent.addView(HallmarkSurfaceFactory.createSectionLabel(context, theme, context.getString(R.string.section_row_order)))
                    orderedIds.forEachIndexed { idx, rowId ->
                        val title = customMap[rowId] ?: builtInCategoryName(context, rowId)
                        val hidden = rowId in s.hiddenRows
                        b.drawerContent.addView(
                            HallmarkSurfaceFactory.createDrawerRow(
                                context, theme, title,
                                context.getString(R.string.row_actions_hint),
                                if (hidden) context.getString(R.string.state_hidden) else context.getString(R.string.state_visible),
                                !hidden
                            ) {
                                showRowItemMenu(context, theme, s.cornerStyle, title, rowId, customMap.containsKey(rowId), hidden, vm) {
                                    render(1, idx + 2)
                                }
                            }
                        )
                    }
                }

                2 -> {
                    val items = listOf<Triple<String, String, () -> Unit>>(
                        Triple(context.getString(if (vm.hasPin()) R.string.setting_pin_clear else R.string.setting_pin_set), if (vm.hasPin()) on else off) {
                            if (vm.hasPin()) {
                                vm.clearParentalPin(); render(2, 0)
                            } else {
                                showPinDialog(context, true, vm) {
                                    Toast.makeText(context, R.string.pin_saved, Toast.LENGTH_SHORT).show()
                                    render(2, 0)
                                }
                            }
                        },
                        Triple(context.getString(R.string.setting_lock_settings), if (s.lockSettingsWithPin) on else off) {
                            if (vm.hasPin()) {
                                vm.updateSettings { it.copy(lockSettingsWithPin = !it.lockSettingsWithPin) }; render(2, 1)
                            } else {
                                showPinDialog(context, true, vm) {
                                    vm.updateSettings { it.copy(lockSettingsWithPin = true) }; render(2, 1)
                                }
                            }
                        },
                        Triple(context.getString(R.string.setting_lock_dev), if (s.lockDeveloperWithPin) on else off) {
                            if (vm.hasPin()) {
                                vm.updateSettings { it.copy(lockDeveloperWithPin = !it.lockDeveloperWithPin) }; render(2, 2)
                            } else {
                                showPinDialog(context, true, vm) {
                                    vm.updateSettings { it.copy(lockDeveloperWithPin = true) }; render(2, 2)
                                }
                            }
                        },
                        Triple(context.getString(R.string.setting_parental_schedule), if (s.parentalScheduleEnabled) on else off) {
                            if (vm.hasPin()) {
                                vm.updateSettings { it.copy(parentalScheduleEnabled = !it.parentalScheduleEnabled) }; render(2, 3)
                            } else {
                                showPinDialog(context, true, vm) {
                                    vm.updateSettings { it.copy(parentalScheduleEnabled = true) }; render(2, 3)
                                }
                            }
                        },
                        Triple(context.getString(R.string.setting_parental_start), String.format(Locale.US, "%02d:00", s.parentalStartHour)) {
                            vm.updateSettings { it.copy(parentalStartHour = (it.parentalStartHour + 1) % 24) }; render(2, 4)
                        },
                        Triple(context.getString(R.string.setting_parental_end), String.format(Locale.US, "%02d:00", s.parentalEndHour)) {
                            vm.updateSettings { it.copy(parentalEndHour = (it.parentalEndHour + 1) % 24) }; render(2, 5)
                        }
                    )
                    items.forEach { (title, trail, click) ->
                        b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, title, null, trail, true, onClick = click))
                    }
                }

                3 -> {
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_language),
                            context.getString(R.string.setting_language_sub),
                            s.language.displayName, true
                        ) {
                            vm.cycleLanguage()
                            dialog.dismiss()
                            onLanguageChanged()
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_show_hidden),
                            null, if (s.showHiddenApps) on else off, true
                        ) {
                            vm.updateSettings { it.copy(showHiddenApps = !it.showHiddenApps) }; render(3, 1)
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_show_inputs),
                            null, if (s.showTvInputsRow) on else off, true
                        ) {
                            vm.updateSettings { it.copy(showTvInputsRow = !it.showTvInputsRow) }; render(3, 2)
                        }
                    )
                    if (!s.autoLaunchTarget.isNullOrBlank()) {
                        b.drawerContent.addView(
                            HallmarkSurfaceFactory.createDrawerRow(
                                context, theme,
                                context.getString(R.string.setting_clear_autolaunch),
                                s.autoLaunchTarget, context.getString(R.string.action_clear), true
                            ) {
                                vm.updateSettings { it.copy(autoLaunchTarget = null) }; render(3, 3)
                            }
                        )
                    }
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_backup_export),
                            null, "JSON", false
                        ) {
                            val ok = vm.exportBackup(context)
                            Toast.makeText(context, if (ok) R.string.toast_backup_ok else R.string.toast_no_backup, Toast.LENGTH_SHORT).show()
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_backup_restore),
                            null, "JSON", false
                        ) {
                            val ok = vm.restoreBackup(context)
                            Toast.makeText(context, if (ok) R.string.toast_restore_ok else R.string.toast_no_backup, Toast.LENGTH_SHORT).show()
                            if (ok) render(3, 0)
                        }
                    )
                    b.drawerContent.addView(
                        HallmarkSurfaceFactory.createDrawerRow(
                            context, theme,
                            context.getString(R.string.setting_support),
                            context.getString(R.string.creator_full_credit),
                            "QR", true
                        ) {
                            dialog.dismiss()
                            onOpenSupport()
                        }
                    )
                }
            }
            if (b.drawerContent.childCount > 0) {
                b.drawerContent.getChildAt(focusIdx.coerceIn(0, b.drawerContent.childCount - 1))?.requestFocus()
            }
        }

        render(0, 0)
        dialog.show()
    }

    private fun showRowItemMenu(
        context: Context,
        theme: VisualTheme,
        corner: io.github.mylnzon58.velotv.domain.model.CornerStyle,
        title: String,
        rowId: String,
        isCustom: Boolean,
        isHidden: Boolean,
        vm: HomeViewModel,
        onDone: () -> Unit
    ) {
        val (dialog, b) = createRightDrawer(context, theme, 420)
        b.tvDrawerTitle.text = title
        b.tvDrawerSubtitle.text = context.getString(R.string.section_row_order)

        b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, context.getString(R.string.row_move_up)) {
            vm.moveRow(rowId, -1); dialog.dismiss(); onDone()
        })
        b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, context.getString(R.string.row_move_down)) {
            vm.moveRow(rowId, +1); dialog.dismiss(); onDone()
        })
        b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, context.getString(if (isHidden) R.string.row_show else R.string.row_hide)) {
            vm.toggleRowHidden(rowId); dialog.dismiss(); onDone()
        })
        if (isCustom) {
            b.drawerContent.addView(HallmarkSurfaceFactory.createDrawerRow(context, theme, context.getString(R.string.row_delete_custom)) {
                vm.removeCustomCategory(rowId); dialog.dismiss(); onDone()
            })
        }
        dialog.show()
        b.drawerContent.getChildAt(0)?.requestFocus()
    }

    fun showAppHubDrawer(context: Context, vm: HomeViewModel, container: AppContainer) {
        val s = vm.state.value.settings
        val theme = s.theme
        val (dialog, b) = createRightDrawer(context, theme, 600)
        b.tvDrawerTitle.text = context.getString(R.string.hub_title)
        b.tvDrawerSubtitle.text = context.getString(R.string.hub_subtitle)
        b.drawerTopSlot.visibility = View.VISIBLE

        var selectedRegion = vm.state.value.detectedRegion

        fun renderRegion(region: WorldRegion) {
            selectedRegion = region
            b.drawerTopSlot.removeAllViews()
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            val regions = listOf(
                WorldRegion.ARGENTINA to "AR",
                WorldRegion.LATAM to "LATAM",
                WorldRegion.USA to "USA",
                WorldRegion.EUROPE to "EU",
                WorldRegion.ASIA to "ASIA"
            )
            regions.forEach { (r, label) ->
                row.addView(HallmarkSurfaceFactory.createTabButton(context, theme, s.cornerStyle, label, r == selectedRegion) {
                    renderRegion(r)
                })
            }
            b.drawerTopSlot.addView(row)

            b.drawerContent.removeAllViews()
            val allItems = vm.getRegionalHubApps(region)
            val playStoreEntry = allItems.firstOrNull { it.packageName == "com.android.vending" && it.isInstalled }
            if (playStoreEntry != null) {
                b.drawerContent.addView(
                    HallmarkSurfaceFactory.createDrawerRow(
                        context = context,
                        theme = theme,
                        title = playStoreEntry.title,
                        subtitle = playStoreEntry.packageName,
                        trailing = context.getString(R.string.action_open_short),
                        accentTrailing = true
                    ) {
                        container.appRepository.launchApp(playStoreEntry.packageName)
                        dialog.dismiss()
                    }
                )
            }

            val items = allItems.filterNot { playStoreEntry != null && it.packageName == "com.android.vending" }
            items.forEach { item ->
                val status = when {
                    item.isInstalled -> context.getString(R.string.hub_installed)
                    !item.isCompatibleWithDevice -> context.getString(R.string.hub_needs_newer)
                    item.sourceType == AppSourceType.OPEN_SOURCE -> context.getString(R.string.hub_open_source_qr)
                    else -> context.getString(R.string.hub_install_play)
                }
                b.drawerContent.addView(
                    HallmarkSurfaceFactory.createDrawerRow(
                        context = context,
                        theme = theme,
                        title = item.title,
                        subtitle = item.packageName,
                        trailing = status,
                        accentTrailing = !item.isInstalled && item.isCompatibleWithDevice
                    ) {
                        when {
                            item.isInstalled -> {
                                container.appRepository.launchApp(item.packageName)
                                dialog.dismiss()
                            }
                            item.sourceType == AppSourceType.OPEN_SOURCE && !item.openSourceUrl.isNullOrBlank() -> {
                                showSingleQrDialog(context, theme, s.cornerStyle, item.title, item.openSourceUrl)
                            }
                            else -> {
                                container.appRepository.openPlayStoreListing(item.packageName)
                            }
                        }
                    }
                )
            }
            b.drawerContent.getChildAt(0)?.requestFocus()
        }

        renderRegion(selectedRegion)
        dialog.show()
    }

    fun showSupportCreator(context: Context, vm: HomeViewModel) {
        val s = vm.state.value.settings
        val theme = s.theme
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        val b = DialogSupportCreatorBinding.inflate(LayoutInflater.from(context))
        b.supportRoot.background = HallmarkSurfaceFactory.studioFrameBackground(context, theme, s.cornerStyle)
        b.supportVerticalRule.setBackgroundColor(theme.rule)
        b.tvSupportTitle.setTextColor(theme.ink)
        b.tvSupportBody.setTextColor(theme.ink2)
        b.tvCreatorCredit.setTextColor(theme.muted)
        b.tvQrTitle.setTextColor(theme.ink)
        b.tvQrSubtitle.setTextColor(theme.accent)
        b.tvQrUrl.setTextColor(theme.muted)
        dialog.setContentView(b.root)

        val channels = vm.getDonationChannels() + DonationChannel(
            id = "github_repo",
            title = context.getString(R.string.support_repo_title),
            subtitle = context.getString(R.string.support_repo_sub),
            url = BuildDonationPlanUseCase.REPO_URL,
            badge = "MIT",
            isPrimaryForRegion = false
        )

        fun selectChannel(ch: DonationChannel) {
            b.tvQrTitle.text = ch.title
            b.tvQrSubtitle.text = ch.subtitle
            b.tvQrUrl.text = ch.url
            b.ivQrCode.setImageBitmap(OfflineQrGenerator.generate(ch.url, 400))
        }

        channels.forEach { ch ->
            b.channelsContainer.addView(
                HallmarkSurfaceFactory.createDrawerRow(
                    context = context,
                    theme = theme,
                    title = ch.title,
                    subtitle = null,
                    trailing = ch.badge,
                    accentTrailing = ch.isPrimaryForRegion,
                    onFocus = { selectChannel(ch) }
                ) { selectChannel(ch) }
            )
        }

        selectChannel(channels.first())
        dialog.show()
        dialog.window?.setLayout(
            HallmarkSurfaceFactory.dp(context, 780),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        b.channelsContainer.getChildAt(0)?.requestFocus()
    }

    fun showOnboardingIfNeeded(context: Context, vm: HomeViewModel) {
        if (onboardingShowing || vm.state.value.settings.onboardingCompleted) return
        onboardingShowing = true
        val s = vm.state.value.settings
        val theme = s.theme
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        dialog.setCancelable(true)
        val b = DialogOnboardingBinding.inflate(LayoutInflater.from(context))
        b.onboardingRoot.background = HallmarkSurfaceFactory.studioFrameBackground(context, theme, s.cornerStyle)
        b.onboardingRule.setBackgroundColor(theme.rule)
        b.tvOnboardingTitle.setTextColor(theme.ink)
        b.tvOnboardingBody.setTextColor(theme.ink2)
        b.tvOnboardingCredit.setTextColor(theme.muted)
        b.tvOnboardingQrCaption.setTextColor(theme.muted)
        b.btnStartVelo.background = HallmarkSurfaceFactory.primaryButtonBackground(context, theme, s.cornerStyle)
        b.btnStartVelo.setTextColor(theme.accentInk)
        dialog.setContentView(b.root)

        val primaryChannel = vm.getDonationChannels().first()
        b.ivOnboardingQr.setImageBitmap(OfflineQrGenerator.generate(primaryChannel.url, 320))
        b.tvOnboardingQrCaption.text = "${primaryChannel.title}\n${context.getString(R.string.onboarding_qr_caption)}"

        b.btnStartVelo.setOnClickListener {
            onboardingShowing = false
            vm.updateSettings { it.copy(onboardingCompleted = true) }
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            onboardingShowing = false
            vm.updateSettings { it.copy(onboardingCompleted = true) }
        }
        dialog.setOnDismissListener { onboardingShowing = false }
        dialog.show()
        dialog.window?.setLayout(
            HallmarkSurfaceFactory.dp(context, 720),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        b.btnStartVelo.requestFocus()
    }

    fun showPinDialog(context: Context, createMode: Boolean, vm: HomeViewModel, onVerified: () -> Unit) {
        val s = vm.state.value.settings
        val theme = s.theme
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        val b = DialogPinBinding.inflate(LayoutInflater.from(context))
        b.pinRoot.background = HallmarkSurfaceFactory.studioFrameBackground(context, theme, s.cornerStyle)
        b.tvPinTitle.setTextColor(theme.ink)
        b.tvPinDots.setTextColor(theme.accent)
        dialog.setContentView(b.root)
        b.tvPinTitle.text = context.getString(if (createMode) R.string.pin_create_title else R.string.pin_enter_title)

        val digits = StringBuilder()
        fun renderDots() {
            b.tvPinDots.text = (0 until 4).joinToString(" ") { idx -> if (idx < digits.length) "●" else "○" }
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
        val btnW = HallmarkSurfaceFactory.dp(context, 78)
        val btnH = HallmarkSurfaceFactory.dp(context, 52)
        val margin = HallmarkSurfaceFactory.dp(context, 5)

        keys.forEach { key ->
            val btn = HallmarkSurfaceFactory.createTabButton(context, theme, s.cornerStyle, key, false) {
                when (key) {
                    "⌫" -> if (digits.isNotEmpty()) digits.deleteCharAt(digits.length - 1)
                    "OK" -> { /* auto-submits on 4 digits */ }
                    else -> {
                        if (digits.length < 4) digits.append(key)
                        if (digits.length == 4) {
                            val code = digits.toString()
                            if (createMode) {
                                vm.setParentalPin(code)
                                dialog.dismiss()
                                onVerified()
                            } else if (vm.verifyPin(code)) {
                                dialog.dismiss()
                                onVerified()
                            } else {
                                digits.clear()
                                Toast.makeText(context, R.string.pin_wrong, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                renderDots()
            }.apply {
                textSize = 18f
                background = HallmarkSurfaceFactory.cardBackground(context, theme, s.cornerStyle)
                setTextColor(theme.ink)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = btnW
                    height = btnH
                    setMargins(margin, margin, margin, margin)
                }
            }
            b.pinKeypad.addView(btn)
        }

        renderDots()
        dialog.show()
        b.pinKeypad.getChildAt(0)?.requestFocus()
    }

    private fun showTextInputDialog(
        context: Context,
        theme: VisualTheme,
        corner: io.github.mylnzon58.velotv.domain.model.CornerStyle,
        title: String,
        initial: String,
        onSave: (String) -> Unit
    ) {
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        val pad = HallmarkSurfaceFactory.dp(context, 28)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = HallmarkSurfaceFactory.studioFrameBackground(context, theme, corner)
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(HallmarkSurfaceFactory.dp(context, 460), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        root.addView(TextView(context).apply {
            text = title
            textSize = 22f
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_semibold)
            setTextColor(theme.ink)
        })
        val edit = EditText(context).apply {
            setText(initial)
            setSelection(initial.length)
            setTextColor(theme.ink)
            setHintTextColor(theme.muted)
            isSingleLine = true
            textSize = 15f
            val ph = HallmarkSurfaceFactory.dp(context, 12)
            val pv = HallmarkSurfaceFactory.dp(context, 10)
            setPadding(ph, pv, ph, pv)
            background = GradientDrawable().apply {
                setColor(theme.paper)
                setStroke(HallmarkSurfaceFactory.dp(context, 1), theme.rule)
                cornerRadius = HallmarkSurfaceFactory.dpFloat(context, corner.radiusDp.coerceAtMost(4).toFloat())
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = HallmarkSurfaceFactory.dp(context, 16)
                bottomMargin = HallmarkSurfaceFactory.dp(context, 18)
            }
        }
        root.addView(edit)

        val saveBtn = TextView(context).apply {
            text = context.getString(R.string.action_save)
            textSize = 15f
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_semibold)
            background = HallmarkSurfaceFactory.primaryButtonBackground(context, theme, corner)
            setTextColor(theme.accentInk)
            setPadding(HallmarkSurfaceFactory.dp(context, 24), HallmarkSurfaceFactory.dp(context, 10), HallmarkSurfaceFactory.dp(context, 24), HallmarkSurfaceFactory.dp(context, 10))
            setOnClickListener {
                onSave(edit.text.toString())
                dialog.dismiss()
            }
        }
        root.addView(saveBtn)
        dialog.setContentView(root)
        dialog.show()
        edit.requestFocus()
    }

    private fun showSingleQrDialog(
        context: Context,
        theme: VisualTheme,
        corner: io.github.mylnzon58.velotv.domain.model.CornerStyle,
        title: String,
        url: String
    ) {
        val dialog = Dialog(context, R.style.VeloDrawerDialog)
        val b = DialogSupportCreatorBinding.inflate(LayoutInflater.from(context))
        b.supportRoot.background = HallmarkSurfaceFactory.studioFrameBackground(context, theme, corner)
        b.supportVerticalRule.setBackgroundColor(theme.rule)
        b.tvSupportTitle.text = title
        b.tvSupportTitle.setTextColor(theme.ink)
        b.tvSupportBody.text = context.getString(R.string.hub_qr_instruction)
        b.tvSupportBody.setTextColor(theme.ink2)
        b.tvCreatorCredit.setTextColor(theme.muted)
        b.tvQrTitle.text = title
        b.tvQrTitle.setTextColor(theme.ink)
        b.tvQrSubtitle.text = context.getString(R.string.support_scan_qr)
        b.tvQrSubtitle.setTextColor(theme.accent)
        b.tvQrUrl.text = url
        b.tvQrUrl.setTextColor(theme.muted)
        b.ivQrCode.setImageBitmap(OfflineQrGenerator.generate(url, 400))
        b.channelsContainer.addView(
            HallmarkSurfaceFactory.createDrawerRow(context, theme, context.getString(R.string.action_close), null, "ESC", true) {
                dialog.dismiss()
            }
        )
        dialog.setContentView(b.root)
        dialog.show()
        b.channelsContainer.getChildAt(0)?.requestFocus()
    }

    private fun themeDisplayName(ctx: Context, theme: VisualTheme): String = when (theme) {
        VisualTheme.DARKROOM_AMBER -> ctx.getString(R.string.theme_darkroom_amber)
        VisualTheme.DARKROOM_RED -> ctx.getString(R.string.theme_darkroom_red)
        VisualTheme.MIDNIGHT -> ctx.getString(R.string.theme_midnight)
        VisualTheme.STUDIO_LIGHT -> ctx.getString(R.string.theme_studio_light)
        VisualTheme.FOREST -> ctx.getString(R.string.theme_forest)
        VisualTheme.GRAPHITE -> ctx.getString(R.string.theme_graphite)
    }

    private fun cornerDisplayName(ctx: Context, id: String): String = when (id) {
        "sharp" -> ctx.getString(R.string.corner_sharp)
        "soft" -> ctx.getString(R.string.corner_soft)
        else -> ctx.getString(R.string.corner_editorial)
    }

    private fun densityDisplayName(ctx: Context, id: String): String = when (id) {
        "compact" -> ctx.getString(R.string.density_compact)
        "large" -> ctx.getString(R.string.density_large)
        else -> ctx.getString(R.string.density_normal)
    }

    private fun builtInCategoryName(ctx: Context, rowId: String): String = when (rowId) {
        AppCategory.FAVORITES.id -> ctx.getString(R.string.cat_favorites)
        AppCategory.TV_INPUTS.id -> ctx.getString(R.string.cat_tv_inputs)
        AppCategory.STREAMING.id -> ctx.getString(R.string.cat_streaming)
        AppCategory.MEDIA_WEB.id -> ctx.getString(R.string.cat_media_web)
        AppCategory.GAMES.id -> ctx.getString(R.string.cat_games)
        AppCategory.TOOLS.id -> ctx.getString(R.string.cat_tools)
        else -> ctx.getString(R.string.cat_all_apps)
    }
}
