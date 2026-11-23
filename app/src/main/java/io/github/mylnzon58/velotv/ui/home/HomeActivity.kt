package io.github.mylnzon58.velotv.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.VeloTvApplication
import io.github.mylnzon58.velotv.data.prefs.LauncherPrefsRepository
import io.github.mylnzon58.velotv.databinding.ActivityHomeBinding
import io.github.mylnzon58.velotv.domain.model.AppLanguage
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory
import io.github.mylnzon58.velotv.ui.dialogs.VeloDialogs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding
    private lateinit var vm: HomeViewModel
    private lateinit var rowAdapter: CategoryRowAdapter
    private val clockHandler = Handler(Looper.getMainLooper())
    private var autoLaunchedOnce = false

    private val clockTicker = object : Runnable {
        override fun run() {
            updateClock()
            vm.refreshDiagnostics()
            val delay = if (vm.state.value.settings.showSeconds) 1_000L else 20_000L
            clockHandler.postDelayed(this, delay)
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
            onAppFocused = { app ->
                b.tvFocusTitle.text = app.label
                b.tvFocusMeta.text = app.packageName
            },
            onAppClick = { app -> handleAppLaunch(app) },
            onAppLongClick = { app -> VeloDialogs.showAppActions(this, app, vm, container) },
            onInputFocused = { input ->
                b.tvFocusTitle.text = input.label
                val stateLabel = getString(if (input.isConnected) R.string.input_connected else R.string.input_ready)
                b.tvFocusMeta.text = "${input.type.name} · $stateLabel"
            },
            onInputClick = { input -> handleInputLaunch(input) },
            onInputLongClick = { input -> VeloDialogs.showInputActions(this, input, vm, container) }
        )

        b.rowsRecyclerView.layoutManager = LinearLayoutManager(this)
        b.rowsRecyclerView.adapter = rowAdapter
        b.rowsRecyclerView.itemAnimator = null

        setupTopBar(container)
        observeState()
        registerPackageWatcher()
        vm.refreshAll()
    }

    override fun onResume() {
        super.onResume()
        updateClock()
        clockHandler.removeCallbacks(clockTicker)
        clockHandler.postDelayed(clockTicker, 1_000L)
        vm.refreshAll()
    }

    override fun onPause() {
        super.onPause()
        clockHandler.removeCallbacks(clockTicker)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(packageReceiver) }
        vm.onDestroy()
        super.onDestroy()
    }

    @Deprecated("Launcher must remain active on Back press")
    override fun onBackPressed() {
        b.rowsRecyclerView.smoothScrollToPosition(0)
    }

    private fun setupTopBar(container: io.github.mylnzon58.velotv.core.AppContainer) {
        b.btnSearch.setOnClickListener { VeloDialogs.showSearchDrawer(this, vm, container) }
        b.btnAppHub.setOnClickListener { VeloDialogs.showAppHubDrawer(this, vm, container) }
        b.btnSupport.setOnClickListener { VeloDialogs.showSupportCreator(this, vm) }

        b.btnSystem.setOnClickListener {
            val s = vm.state.value.settings
            if (s.lockDeveloperWithPin && vm.hasPin()) {
                VeloDialogs.showPinDialog(this, false, vm) {
                    VeloDialogs.showSystemDeveloperDrawer(this, vm)
                }
            } else {
                VeloDialogs.showSystemDeveloperDrawer(this, vm)
            }
        }

        b.btnSettings.setOnClickListener {
            val s = vm.state.value.settings
            val open = {
                VeloDialogs.showSettingsDrawer(
                    context = this,
                    vm = vm,
                    onLanguageChanged = { recreate() },
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

    private fun observeState() {
        lifecycleScope.launch {
            vm.state.collectLatest { state ->
                val s = state.settings
                applyHallmarkTheme(s.theme, s.cornerStyle)

                b.topBar.visibility = if (s.showTopBar) View.VISIBLE else View.GONE
                b.topRule.visibility = if (s.showTopBar) View.VISIBLE else View.GONE
                b.heroFocusBar.visibility = if (s.showHeroFocusBar) View.VISIBLE else View.GONE
                b.clockGroup.visibility = if (s.showClock) View.VISIBLE else View.GONE
                b.clockDivider.visibility = if (s.showClock) View.VISIBLE else View.GONE
                b.tvStatsBadge.visibility = if (s.showSystemStats && state.diagnostics != null) View.VISIBLE else View.GONE
                b.brandDivider.visibility = b.tvStatsBadge.visibility
                b.bottomBar.visibility = if (s.showBottomHints) View.VISIBLE else View.GONE
                b.bottomRule.visibility = if (s.showBottomHints) View.VISIBLE else View.GONE

                state.diagnostics?.let { d ->
                    b.tvStatsBadge.text = "${d.availableRamMb} MB · ${d.networkLabel}"
                }

                rowAdapter.updateStyle(s.theme, s.cornerStyle, s.cardDensity, s.showBannerLabels)
                rowAdapter.submitList(state.rows)
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

    private fun applyHallmarkTheme(theme: VisualTheme, corner: CornerStyle) {
        b.rootContainer.setBackgroundColor(theme.paper)
        b.tvWordmark.setTextColor(theme.ink)
        b.tvStatsBadge.setTextColor(theme.muted)
        b.brandDivider.setBackgroundColor(theme.rule)
        b.clockDivider.setBackgroundColor(theme.rule)
        b.topRule.setBackgroundColor(theme.rule)
        b.bottomRule.setBackgroundColor(theme.rule)
        b.tvClock.setTextColor(theme.ink)
        b.tvDate.setTextColor(theme.muted)
        b.tvFocusTitle.setTextColor(theme.ink)
        b.tvFocusMeta.setTextColor(theme.muted)
        b.tvFocusCue.setTextColor(theme.muted)
        b.tvBottomHint.setTextColor(theme.muted)
        b.tvCreatorFooter.setTextColor(theme.muted)

        val tabColors = HallmarkSurfaceFactory.navTabTextColors(theme)
        listOf(b.btnSearch, b.btnAppHub, b.btnSystem, b.btnSettings, b.btnSupport).forEach { tab ->
            tab.background = HallmarkSurfaceFactory.navTabBackground(this, theme, corner)
            tab.setTextColor(tabColors)
        }
    }

    private fun handleAppLaunch(app: TvApp) {
        val container = (application as VeloTvApplication).container
        if (vm.isAppPinProtectedNow(app)) {
            VeloDialogs.showPinDialog(this, false, vm) {
                container.appRepository.launchApp(app.packageName)
            }
        } else {
            container.appRepository.launchApp(app.packageName)
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
            target.startsWith("pkg:") -> container.appRepository.launchApp(target.removePrefix("pkg:"))
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
