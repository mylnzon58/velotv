package io.github.mylnzon58.velotv.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.VeloTvApplication
import io.github.mylnzon58.velotv.core.AppContainer
import io.github.mylnzon58.velotv.data.prefs.LauncherPrefsRepository
import io.github.mylnzon58.velotv.databinding.ActivityHomeBinding
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.AppLanguage
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.domain.model.WallpaperInterval
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.domain.model.WallpaperSource
import io.github.mylnzon58.velotv.ui.common.FocusAnimator
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory
import io.github.mylnzon58.velotv.ui.common.SmoothTvLayoutManager
import io.github.mylnzon58.velotv.ui.dialogs.VeloDialogs
import io.github.mylnzon58.velotv.ui.tools.AmbientClockActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding
    private lateinit var vm: HomeViewModel
    private lateinit var rowAdapter: CategoryRowAdapter
    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoLaunchedOnce = false
    private var resumedOnce = false

    private var lastAppliedTheme: VisualTheme? = null
    private var lastAppliedCorner: CornerStyle? = null
    private var lastAppliedNightActive: Boolean? = null
    private var lastBackdropConfigKey: String? = null
    private var lastDisplayedWallpaperId: String? = null
    private var lastFocusedHeroKey: String? = null
    private var wallpaperLoadJob: Job? = null
    private var cleaningInProgress = false
    private var initialRowFocused = false

    private val hideFeedbackRunnable = Runnable {
        b.tvFeedbackPill.animate()
            .alpha(0f)
            .translationY(HallmarkSurfaceFactory.dpFloat(this, 8f))
            .setDuration(220L)
            .withEndAction { b.tvFeedbackPill.visibility = View.GONE }
            .start()
    }

    private val clockTicker = object : Runnable {
        override fun run() {
            updateClock()
            applyNightComfortState()
            vm.refreshDiagnostics()
            val delay = if (vm.state.value.settings.showSeconds) 1_000L else 20_000L
            mainHandler.postDelayed(this, delay)
        }
    }

    private val wallpaperTicker = object : Runnable {
        override fun run() {
            val s = vm.state.value.settings
            val interval = s.wallpaperInterval.durationMs
            if (s.pinnedWallpaperId == null && interval > 0L &&
                s.wallpaperSource != WallpaperSource.SOLID_MINIMAL &&
                s.wallpaperSource != WallpaperSource.DYNAMIC_AMBIENT
            ) {
                vm.advanceWallpaper()
                mainHandler.postDelayed(this, interval)
            }
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vm.refreshAll()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val savedLang = runCatching { LauncherPrefsRepository(newBase).loadSettings().language }
            .getOrDefault(AppLanguage.SYSTEM)
        if (savedLang == AppLanguage.SYSTEM) {
            super.attachBaseContext(newBase)
            return
        }
        val locale = Locale.forLanguageTag(savedLang.code)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        val container = (application as VeloTvApplication).container
        vm = HomeViewModel(container)

        rowAdapter = CategoryRowAdapter(
            appRepository = container.appRepository,
            scope = lifecycleScope,
            onAppFocused = { app -> onAppCardFocused(app, container) },
            onAppClick = { app -> handleAppLaunch(app) },
            onAppLongClick = { app -> VeloDialogs.showAppActions(this, app, vm, container) },
            onInputFocused = { input -> onInputCardFocused(input) },
            onInputClick = { input -> handleInputLaunch(input) },
            onInputLongClick = { input -> VeloDialogs.showInputActions(this, input, vm, container) }
        )

        b.rowsRecyclerView.layoutManager = SmoothTvLayoutManager(this, LinearLayoutManager.VERTICAL, 44)
        b.rowsRecyclerView.adapter = rowAdapter
        b.rowsRecyclerView.itemAnimator = null
        b.rowsRecyclerView.setItemViewCacheSize(4)

        setupTopBar(container)
        observeState(container)
        registerPackageWatcher()
        vm.refreshAll()
    }

    override fun onResume() {
        super.onResume()
        b.wallpaperBackdrop.setPaused(false)
        updateClock()
        applyNightComfortState()
        mainHandler.removeCallbacks(clockTicker)
        mainHandler.postDelayed(clockTicker, 1_000L)

        val s = vm.state.value.settings
        if (resumedOnce && s.pinnedWallpaperId == null && s.wallpaperInterval == WallpaperInterval.ON_RESUME) {
            vm.advanceWallpaper()
        }
        if (resumedOnce) {
            lifecycleScope.launch {
                val autoRes = vm.runAutoCleanIfNeeded()
                if (autoRes != null && autoRes.reclaimedMb > 0L) {
                    showFeedbackPill(getString(R.string.auto_clean_pill, autoRes.reclaimedMb, autoRes.ramAfterMb))
                }
            }
        }
        resumedOnce = true
        scheduleWallpaperRotation()
        vm.refreshAll()
    }

    override fun onPause() {
        super.onPause()
        b.wallpaperBackdrop.setPaused(true)
        mainHandler.removeCallbacks(clockTicker)
        mainHandler.removeCallbacks(wallpaperTicker)
    }

    override fun onDestroy() {
        wallpaperLoadJob?.cancel()
        mainHandler.removeCallbacks(hideFeedbackRunnable)
        runCatching { unregisterReceiver(packageReceiver) }
        vm.onDestroy()
        super.onDestroy()
    }

    @Deprecated("Launcher must remain active on Back press")
    override fun onBackPressed() {
        b.rowsRecyclerView.smoothScrollToPosition(0)
        b.rowsRecyclerView.requestFocus()
    }

    /**
     * Physical TV remote superpowers (Zero menus needed):
     * - Keys 1..9: Speed Dial Favorites #1..#9
     * - GREEN: Instant Turbo RAM Clean
     * - YELLOW: Next artistic wallpaper
     * - BLUE: Ambient Art & OLED Clock
     * - RED: Multi-Launcher & HDMI Switcher
     * - SEARCH / INFO: Instant Search
     * - PLAY_PAUSE: Quick Resume most recent app
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val container = (application as VeloTvApplication).container
        val s = vm.state.value.settings

        if (s.remoteSpeedDialEnabled && keyCode in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_9) {
            val slot = keyCode - KeyEvent.KEYCODE_1 + 1
            val targetApp = vm.getFavoriteAtSlot(slot)
            if (targetApp != null) {
                showFeedbackPill(getString(R.string.speed_dial_pill, slot, targetApp.label))
                handleAppLaunch(targetApp)
                return true
            }
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_PROG_GREEN -> {
                triggerInstantQuickClean()
                true
            }
            KeyEvent.KEYCODE_PROG_YELLOW -> {
                vm.advanceWallpaper(forceUnpin = true)
                showFeedbackPill(getString(R.string.wp_switched_pill))
                true
            }
            KeyEvent.KEYCODE_PROG_BLUE -> {
                openAmbientClock()
                true
            }
            KeyEvent.KEYCODE_PROG_RED -> {
                VeloDialogs.showLauncherAndInputsDrawer(this, vm, container)
                true
            }
            KeyEvent.KEYCODE_SEARCH, KeyEvent.KEYCODE_INFO -> {
                VeloDialogs.showSearchDrawer(this, vm, container)
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                val recent = vm.getMostRecentApp()
                if (recent != null) {
                    showFeedbackPill(getString(R.string.quick_resume_pill, recent.label))
                    handleAppLaunch(recent)
                    true
                } else super.onKeyDown(keyCode, event)
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun setHeroContext(key: String, title: String, subtitle: String) {
        if (lastFocusedHeroKey == key) return
        lastFocusedHeroKey = key
        b.tvFocusTitle.text = title
        b.tvFocusMeta.text = subtitle
        FocusAnimator.animateHeroChange(b.tvFocusTitle, b.tvFocusMeta)
    }

    private fun onAppCardFocused(app: TvApp, container: AppContainer) {
        val catLabel = when {
            app.isHomeLauncher -> getString(R.string.hero_meta_alt_launcher)
            app.categoryId == AppCategory.STREAMING.id -> getString(R.string.cat_streaming)
            app.categoryId == AppCategory.MEDIA_WEB.id -> getString(R.string.cat_media_web)
            app.categoryId == AppCategory.GAMES.id -> getString(R.string.cat_games)
            app.categoryId == AppCategory.TOOLS.id -> getString(R.string.cat_tools)
            else -> vm.state.value.settings.customCategories.firstOrNull { it.id == app.categoryId }?.title
                ?: getString(R.string.hero_meta_app)
        }
        val favSlot = vm.state.value.settings.favoritePackages.indexOf(app.packageName)
        val suffix = when {
            app.isHomeLauncher -> getString(R.string.hero_meta_keep_default)
            favSlot in 0..8 && vm.state.value.settings.remoteSpeedDialEnabled ->
                "${getString(R.string.cat_favorites)} · #${favSlot + 1}"
            app.isFavorite -> getString(R.string.cat_favorites)
            vm.getAppLaunchCount(app.packageName) > 0 -> getString(R.string.cat_smart_shelf)
            else -> null
        }
        val humanMeta = if (suffix != null) "$catLabel · $suffix" else catLabel
        setHeroContext("app:${app.packageName}", app.label, humanMeta)

        val s = vm.state.value.settings
        if (s.ambientAppColorGlow || s.wallpaperSource == WallpaperSource.DYNAMIC_AMBIENT) {
            val hue = container.appRepository.getAmbientColorForApp(
                app.packageName,
                container.wallpaperPolicyUseCase,
                s.theme.accent
            )
            b.wallpaperBackdrop.setAmbientAccentColor(hue, animate = true)
        }
    }

    private fun onInputCardFocused(input: TvInputSource) {
        val stateLabel = getString(if (input.isConnected) R.string.input_connected else R.string.input_ready)
        setHeroContext("input:${input.id}", input.label, "${input.type.name} · $stateLabel")

        val s = vm.state.value.settings
        if (s.ambientAppColorGlow || s.wallpaperSource == WallpaperSource.DYNAMIC_AMBIENT) {
            val hue = if (input.isConnected) ContextCompat.getColor(this, R.color.hm_signal_ok) else s.theme.accent
            b.wallpaperBackdrop.setAmbientAccentColor(hue, animate = true)
        }
    }

    private fun setupTopBar(container: AppContainer) {
        val iconButtons = listOf(
            b.btnSearch to Pair(R.string.nav_search, R.string.hero_sub_search),
            b.btnLaunchers to Pair(R.string.nav_launchers, R.string.hero_sub_launchers),
            b.btnNightMode to Pair(R.string.nav_night_mode, R.string.hero_sub_night),
            b.btnWallpapers to Pair(R.string.nav_wallpapers, R.string.hero_sub_wallpapers),
            b.btnAppHub to Pair(R.string.nav_app_hub, R.string.hero_sub_catalog),
            b.btnSystem to Pair(R.string.nav_system, R.string.hero_sub_system),
            b.btnSettings to Pair(R.string.nav_settings, R.string.hero_sub_settings)
        )

        iconButtons.forEach { (btn, copy) ->
            btn.setOnFocusChangeListener { v, hasFocus ->
                FocusAnimator.applyNavFocus(v, hasFocus)
                if (hasFocus) {
                    setHeroContext("nav:${v.id}", getString(copy.first), getString(copy.second))
                }
            }
        }

        b.btnClean.setOnFocusChangeListener { v, hasFocus ->
            FocusAnimator.applyNavFocus(v, hasFocus)
            val theme = vm.state.value.settings.theme
            b.tvStatsBadge.setTextColor(if (hasFocus) theme.accentInk else theme.ink2)
            b.ivBoostIcon.setColorFilter(if (hasFocus) theme.accentInk else theme.accent)
            if (hasFocus) {
                setHeroContext("nav:clean", getString(R.string.clean_hero_title), getString(R.string.clean_hero_sub_short))
            }
        }
        b.btnClean.setOnClickListener { triggerInstantQuickClean() }
        b.btnClean.setOnLongClickListener {
            VeloDialogs.showQuickCleanDrawer(this, vm, lifecycleScope)
            true
        }
        b.btnClean.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
                VeloDialogs.showQuickCleanDrawer(this, vm, lifecycleScope)
                true
            } else false
        }

        b.clockGroup.setOnFocusChangeListener { v, hasFocus ->
            FocusAnimator.applyNavFocus(v, hasFocus)
            if (hasFocus) {
                setHeroContext("nav:clock", getString(R.string.tool_ambient_clock), getString(R.string.hero_sub_ambient))
            }
        }
        b.clockGroup.setOnClickListener { openAmbientClock() }

        b.btnSearch.setOnClickListener { VeloDialogs.showSearchDrawer(this, vm, container) }
        b.btnLaunchers.setOnClickListener { VeloDialogs.showLauncherAndInputsDrawer(this, vm, container) }
        b.btnNightMode.setOnClickListener {
            val enabled = vm.toggleNightMode()
            applyNightComfortState()
            showFeedbackPill(getString(if (enabled) R.string.night_mode_on_pill else R.string.night_mode_off_pill))
        }
        b.btnWallpapers.setOnClickListener {
            VeloDialogs.showWallpaperDrawer(
                context = this,
                vm = vm,
                container = container,
                scope = lifecycleScope,
                onPreviewWallpaper = { item -> renderWallpaperItem(item, container, animate = true) }
            )
        }
        b.btnAppHub.setOnClickListener { VeloDialogs.showAppHubDrawer(this, vm, container) }

        b.btnSystem.setOnClickListener {
            val s = vm.state.value.settings
            if (s.lockDeveloperWithPin && vm.hasPin()) {
                VeloDialogs.showPinDialog(this, false, vm) {
                    VeloDialogs.showSystemDeveloperDrawer(this, vm, lifecycleScope)
                }
            } else {
                VeloDialogs.showSystemDeveloperDrawer(this, vm, lifecycleScope)
            }
        }

        b.btnSettings.setOnClickListener {
            val s = vm.state.value.settings
            val open = {
                VeloDialogs.showSettingsDrawer(
                    context = this,
                    vm = vm,
                    onLanguageChanged = { recreate() },
                    onOpenWallpapers = {
                        VeloDialogs.showWallpaperDrawer(
                            context = this,
                            vm = vm,
                            container = container,
                            scope = lifecycleScope,
                            onPreviewWallpaper = { item -> renderWallpaperItem(item, container, animate = true) }
                        )
                    },
                    onOpenSupport = { VeloDialogs.showSupportCreator(this, vm) }
                )
            }
            if (s.lockSettingsWithPin && vm.hasPin()) {
                VeloDialogs.showPinDialog(this, false, vm) { open() }
            } else {
                open()
            }
        }
    }

    private fun openAmbientClock() {
        startActivity(Intent(this, AmbientClockActivity::class.java))
    }

    private fun observeState(container: AppContainer) {
        lifecycleScope.launch {
            vm.state.collectLatest { state ->
                val s = state.settings
                val nightActive = vm.isNightComfortActiveNow()
                if (lastAppliedTheme != s.theme || lastAppliedCorner != s.cornerStyle || lastAppliedNightActive != nightActive) {
                    lastAppliedTheme = s.theme
                    lastAppliedCorner = s.cornerStyle
                    lastAppliedNightActive = nightActive
                    applyCinemaTheme(s.theme, s.cornerStyle, nightActive)
                }
                applyNightComfortState()

                val isSolidOrDynamic = s.wallpaperSource == WallpaperSource.SOLID_MINIMAL ||
                    s.wallpaperSource == WallpaperSource.DYNAMIC_AMBIENT
                val backdropKey = "${s.theme.id}|${s.wallpaperDimPercent}|${s.wallpaperKenBurns}|${s.ambientAppColorGlow}|$isSolidOrDynamic"
                if (lastBackdropConfigKey != backdropKey) {
                    lastBackdropConfigKey = backdropKey
                    b.wallpaperBackdrop.configureAppearance(
                        theme = s.theme,
                        dimPercent = s.wallpaperDimPercent,
                        kenBurns = s.wallpaperKenBurns,
                        ambientGlowEnabled = s.ambientAppColorGlow || s.wallpaperSource == WallpaperSource.DYNAMIC_AMBIENT,
                        solidOnly = isSolidOrDynamic
                    )
                    if (isSolidOrDynamic) {
                        lastDisplayedWallpaperId = null
                        b.tvWallpaperCredit.visibility = View.GONE
                    }
                    scheduleWallpaperRotation()
                }

                if (!isSolidOrDynamic) {
                    val activeWp = state.activeWallpaper
                    if (activeWp != null && activeWp.id != lastDisplayedWallpaperId) {
                        renderWallpaperItem(activeWp, container, animate = lastDisplayedWallpaperId != null)
                    }
                }

                b.topBar.visibility = if (s.showTopBar) View.VISIBLE else View.GONE
                b.heroFocusBar.visibility = if (s.showHeroFocusBar) View.VISIBLE else View.GONE
                b.clockGroup.visibility = if (s.showClock) View.VISIBLE else View.GONE
                b.clockDivider.visibility = if (s.showClock) View.VISIBLE else View.GONE
                b.btnClean.visibility = if (s.showSystemStats && state.diagnostics != null) View.VISIBLE else View.GONE
                b.tvBottomHint.visibility = if (s.showBottomHints) View.VISIBLE else View.INVISIBLE
                b.bottomBar.visibility = if (s.showBottomHints || !isSolidOrDynamic) View.VISIBLE else View.GONE

                state.diagnostics?.let { d ->
                    if (!cleaningInProgress) {
                        b.tvStatsBadge.text = "${d.availableRamMb} MB"
                    }
                    val dotColor = when {
                        d.availableRamMb >= 500L -> ContextCompat.getColor(this@HomeActivity, R.color.hm_signal_ok)
                        d.availableRamMb >= 300L -> ContextCompat.getColor(this@HomeActivity, R.color.hm_signal_warn)
                        else -> ContextCompat.getColor(this@HomeActivity, R.color.hm_signal_crit)
                    }
                    b.ramHealthDot.background = HallmarkSurfaceFactory.dotDrawable(dotColor)
                }

                rowAdapter.updateStyle(s.theme, s.cornerStyle, s.cardDensity, s.showBannerLabels)
                rowAdapter.submitList(state.rows) {
                    if (!initialRowFocused && state.rows.isNotEmpty() && s.onboardingCompleted) {
                        initialRowFocused = true
                        b.rowsRecyclerView.post { b.rowsRecyclerView.requestFocus() }
                    }
                }
                updateClock()

                if (!s.onboardingCompleted && state.rows.isNotEmpty()) {
                    VeloDialogs.showOnboardingIfNeeded(this@HomeActivity, vm)
                }

                if (!autoLaunchedOnce && s.onboardingCompleted && !s.autoLaunchTarget.isNullOrBlank()) {
                    autoLaunchedOnce = true
                    triggerAutoLaunch(s.autoLaunchTarget, state.allInputs)
                }
            }
        }
    }

    private fun applyNightComfortState() {
        val active = vm.isNightComfortActiveNow()
        val targetAlpha = if (active) 1f else 0f
        if (b.nightComfortOverlay.alpha != targetAlpha) {
            b.nightComfortOverlay.animate().cancel()
            b.nightComfortOverlay.animate().alpha(targetAlpha).setDuration(420L).start()
        }
    }

    private fun renderWallpaperItem(item: WallpaperItem, container: AppContainer, animate: Boolean) {
        lastDisplayedWallpaperId = item.id
        b.tvWallpaperCredit.text = item.title
        b.tvWallpaperCredit.visibility = View.VISIBLE
        wallpaperLoadJob?.cancel()
        wallpaperLoadJob = lifecycleScope.launch {
            val bmp = container.wallpaperRepository.loadFullBitmap(item, 1280, 720)
            if (bmp != null) {
                b.wallpaperBackdrop.transitionTo(bmp, animate = animate)
            }
        }
    }

    private fun scheduleWallpaperRotation() {
        mainHandler.removeCallbacks(wallpaperTicker)
        val s = vm.state.value.settings
        val interval = s.wallpaperInterval.durationMs
        if (s.pinnedWallpaperId == null && interval > 0L &&
            s.wallpaperSource != WallpaperSource.SOLID_MINIMAL &&
            s.wallpaperSource != WallpaperSource.DYNAMIC_AMBIENT
        ) {
            mainHandler.postDelayed(wallpaperTicker, interval)
        }
    }

    private fun applyCinemaTheme(theme: VisualTheme, corner: CornerStyle, nightActive: Boolean) {
        b.rootContainer.setBackgroundColor(theme.paper)
        b.tvWordmark.setTextColor(theme.ink)
        b.tvStatsBadge.setTextColor(if (b.btnClean.hasFocus()) theme.accentInk else theme.ink2)
        b.ivBoostIcon.setColorFilter(if (b.btnClean.hasFocus()) theme.accentInk else theme.accent)
        b.btnClean.background = HallmarkSurfaceFactory.pillCapsuleBackground(this, theme)

        b.clockDivider.setBackgroundColor(HallmarkSurfaceFactory.withAlpha(theme.ink, 40))
        b.clockGroup.background = HallmarkSurfaceFactory.clockFocusBackground(this, theme)
        b.tvClock.setTextColor(theme.ink)
        b.tvDate.setTextColor(theme.ink2)
        b.tvFocusTitle.setTextColor(theme.ink)
        b.tvFocusMeta.setTextColor(theme.ink2)
        b.tvBottomHint.setTextColor(theme.muted)
        b.tvWallpaperCredit.setTextColor(theme.muted)

        b.tvFeedbackPill.background = HallmarkSurfaceFactory.feedbackPillBackground(this, theme)
        b.tvFeedbackPill.setTextColor(theme.ink)

        val standardIcons: List<ImageButton> = listOf(
            b.btnSearch, b.btnLaunchers, b.btnWallpapers, b.btnAppHub, b.btnSystem, b.btnSettings
        )
        standardIcons.forEach { btn ->
            btn.background = HallmarkSurfaceFactory.iconButtonBackground(this, theme, active = false)
            ImageViewCompat.setImageTintList(btn, HallmarkSurfaceFactory.iconTintColors(theme, active = false))
        }
        b.btnNightMode.background = HallmarkSurfaceFactory.iconButtonBackground(this, theme, active = nightActive)
        ImageViewCompat.setImageTintList(b.btnNightMode, HallmarkSurfaceFactory.iconTintColors(theme, active = nightActive))
    }

    private fun showFeedbackPill(message: String) {
        mainHandler.removeCallbacks(hideFeedbackRunnable)
        b.tvFeedbackPill.animate().cancel()
        b.tvFeedbackPill.text = message
        b.tvFeedbackPill.alpha = 0f
        b.tvFeedbackPill.translationY = HallmarkSurfaceFactory.dpFloat(this, 10f)
        b.tvFeedbackPill.visibility = View.VISIBLE
        b.tvFeedbackPill.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(200L)
            .start()
        mainHandler.postDelayed(hideFeedbackRunnable, 2_800L)
    }

    private fun triggerInstantQuickClean() {
        if (cleaningInProgress) return
        cleaningInProgress = true
        b.tvStatsBadge.text = getString(R.string.clean_running_short)
        lifecycleScope.launch {
            val res = vm.runQuickClean()
            cleaningInProgress = false
            b.tvStatsBadge.text = "${res.ramAfterMb} MB"
            b.btnClean.animate().cancel()
            b.btnClean.scaleX = 1.1f
            b.btnClean.scaleY = 1.1f
            b.btnClean.animate().scaleX(1f).scaleY(1f).setDuration(240L).start()
            val msg = if (res.reclaimedMb > 0L) {
                getString(R.string.clean_toast_freed, res.ramAfterMb, res.reclaimedMb, res.processedPackages)
            } else {
                getString(R.string.clean_toast_ready, res.ramAfterMb, res.processedPackages)
            }
            showFeedbackPill(msg)
        }
    }

    private fun handleAppLaunch(app: TvApp) {
        val container = (application as VeloTvApplication).container
        val launch = {
            if (!app.isHomeLauncher) {
                vm.recordAppLaunch(app.packageName)
                container.appRepository.launchApp(app.packageName)
            } else {
                container.appRepository.launchHomeLauncher(app.packageName)
            }
            Unit
        }
        if (vm.isAppPinProtectedNow(app)) {
            VeloDialogs.showPinDialog(this, false, vm) { launch() }
        } else {
            launch()
        }
    }

    private fun handleInputLaunch(input: TvInputSource) {
        val container = (application as VeloTvApplication).container
        if (vm.isInputPinProtectedNow(input)) {
            VeloDialogs.showPinDialog(this, false, vm) {
                container.tvInputRepository.launchTvInput(input)
            }
        } else {
            container.tvInputRepository.launchTvInput(input)
        }
    }

    private fun triggerAutoLaunch(target: String, inputs: List<TvInputSource>) {
        val container = (application as VeloTvApplication).container
        when {
            target.startsWith("pkg:") -> {
                val pkg = target.removePrefix("pkg:")
                vm.recordAppLaunch(pkg)
                container.appRepository.launchApp(pkg)
            }
            target.startsWith("input:") -> {
                val id = target.removePrefix("input:")
                inputs.firstOrNull { it.id == id }?.let { container.tvInputRepository.launchTvInput(it) }
            }
        }
    }

    private fun updateClock() {
        val now = Date()
        val s = vm.state.value.settings
        val pattern = when {
            s.use24HourClock && s.showSeconds -> "HH:mm:ss"
            s.use24HourClock -> "HH:mm"
            s.showSeconds -> "hh:mm:ss a"
            else -> "hh:mm a"
        }
        b.tvClock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(now)
        b.tvDate.text = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(now)
    }

    private fun registerPackageWatcher() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)
    }
}
