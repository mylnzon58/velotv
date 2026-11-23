package io.github.mylnzon58.velotv.ui.home

import android.content.Context
import android.os.Build
import io.github.mylnzon58.velotv.core.AppContainer
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.AppLanguage
import io.github.mylnzon58.velotv.domain.model.CardDensity
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.CustomCategory
import io.github.mylnzon58.velotv.domain.model.DonationChannel
import io.github.mylnzon58.velotv.domain.model.HomeRow
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.NativeSettingShortcut
import io.github.mylnzon58.velotv.domain.model.RegionalAppEntry
import io.github.mylnzon58.velotv.domain.model.SystemDiagnostics
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.domain.model.WorldRegion
import io.github.mylnzon58.velotv.domain.usecase.SearchLauncherUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class HomeUiState(
    val rows: List<HomeRow> = emptyList(),
    val allApps: List<TvApp> = emptyList(),
    val allInputs: List<TvInputSource> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val diagnostics: SystemDiagnostics? = null,
    val detectedCountry: String = "AR",
    val detectedRegion: WorldRegion = WorldRegion.ARGENTINA
)

class HomeViewModel(private val container: AppContainer) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var cachedApps: List<TvApp> = emptyList()
    private var cachedInputs: List<TvInputSource> = emptyList()

    private val _state = MutableStateFlow(
        HomeUiState(
            settings = container.preferencesRepository.loadSettings(),
            detectedCountry = detectCountryCode(),
            detectedRegion = WorldRegion.fromIsoCountry(detectCountryCode())
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refreshAll() {
        scope.launch {
            val apps = container.appRepository.loadInstalledApps()
            val inputs = container.tvInputRepository.loadTvInputs()
            val diag = container.systemDiagnosticsRepository.readDiagnostics()
            val settings = container.preferencesRepository.loadSettings()

            cachedApps = apps
            cachedInputs = inputs

            val effectiveSettings = if (!settings.onboardingCompleted && settings.favoritePackages.isEmpty()) {
                val preferredOrder = listOf(
                    "com.android.vending",
                    "com.phlox.tvwebbrowser",
                    "com.brave.browser",
                    "org.smarttube.stable",
                    "com.netflix.ninja",
                    "com.disney.disneyplus",
                    "com.stremio.one",
                    "com.spotify.tv.android",
                    "org.videolan.vlc",
                    "com.google.android.youtube.tv"
                )
                val installedSet = apps.map { it.packageName }.toSet()
                val starterFavs = preferredOrder.filter { it in installedSet }.ifEmpty {
                    apps.take(6).map { it.packageName }
                }
                if (starterFavs.isNotEmpty()) {
                    settings.copy(favoritePackages = starterFavs).also {
                        container.preferencesRepository.saveSettings(it)
                    }
                } else settings
            } else settings

            emitState(effectiveSettings, diag)
        }
    }

    fun refreshDiagnostics() {
        val diag = container.systemDiagnosticsRepository.readDiagnostics()
        _state.value = _state.value.copy(diagnostics = diag)
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        val updated = transform(_state.value.settings)
        container.preferencesRepository.saveSettings(updated)
        emitState(updated, _state.value.diagnostics)
    }

    fun toggleFavorite(packageName: String) {
        updateSettings { s ->
            val current = s.favoritePackages.toMutableList()
            if (packageName in current) current.remove(packageName) else current.add(packageName)
            s.copy(favoritePackages = current)
        }
    }

    fun moveFavorite(packageName: String, delta: Int) {
        updateSettings { s ->
            val list = s.favoritePackages.toMutableList()
            val idx = list.indexOf(packageName)
            val newIdx = idx + delta
            if (idx >= 0 && newIdx in list.indices) {
                list.removeAt(idx)
                list.add(newIdx, packageName)
            }
            s.copy(favoritePackages = list)
        }
    }

    fun setAppCategory(packageName: String, categoryId: String) {
        updateSettings { s ->
            s.copy(categoryOverrides = s.categoryOverrides + (packageName to categoryId))
        }
    }

    fun renameApp(packageName: String, newLabel: String) {
        updateSettings { s ->
            val clean = newLabel.trim()
            val next = if (clean.isEmpty()) {
                s.customAppLabels - packageName
            } else {
                s.customAppLabels + (packageName to clean)
            }
            s.copy(customAppLabels = next)
        }
    }

    fun renameInput(inputId: String, newLabel: String) {
        updateSettings { s ->
            val clean = newLabel.trim()
            val next = if (clean.isEmpty()) {
                s.customInputLabels - inputId
            } else {
                s.customInputLabels + (inputId to clean)
            }
            s.copy(customInputLabels = next)
        }
    }

    fun toggleHideApp(packageName: String) {
        updateSettings { s ->
            val next = s.hiddenPackages.toMutableSet()
            if (packageName in next) next.remove(packageName) else next.add(packageName)
            s.copy(hiddenPackages = next)
        }
    }

    fun toggleHideInput(inputId: String) {
        updateSettings { s ->
            val next = s.hiddenInputIds.toMutableSet()
            if (inputId in next) next.remove(inputId) else next.add(inputId)
            s.copy(hiddenInputIds = next)
        }
    }

    fun toggleLockApp(packageName: String) {
        updateSettings { s ->
            val next = s.lockedPackages.toMutableSet()
            if (packageName in next) next.remove(packageName) else next.add(packageName)
            s.copy(lockedPackages = next)
        }
    }

    fun toggleLockInput(inputId: String) {
        updateSettings { s ->
            val next = s.lockedInputIds.toMutableSet()
            if (inputId in next) next.remove(inputId) else next.add(inputId)
            s.copy(lockedInputIds = next)
        }
    }

    fun toggleAutoLaunch(targetKey: String) {
        updateSettings { s ->
            val next = if (s.autoLaunchTarget == targetKey) null else targetKey
            s.copy(autoLaunchTarget = next)
        }
    }

    fun addCustomCategory(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val id = "custom_${System.currentTimeMillis()}"
        updateSettings { s ->
            val updatedCats = s.customCategories + CustomCategory(id, clean)
            val updatedOrder = s.rowOrder.toMutableList().apply {
                val allIdx = indexOf(AppCategory.ALL_APPS.id)
                if (allIdx >= 0) add(allIdx, id) else add(id)
            }
            s.copy(customCategories = updatedCats, rowOrder = updatedOrder)
        }
    }

    fun removeCustomCategory(categoryId: String) {
        updateSettings { s ->
            s.copy(
                customCategories = s.customCategories.filterNot { it.id == categoryId },
                rowOrder = s.rowOrder.filterNot { it == categoryId },
                hiddenRows = s.hiddenRows - categoryId,
                categoryOverrides = s.categoryOverrides.filterValues { it != categoryId }
            )
        }
    }

    fun moveRow(rowId: String, delta: Int) {
        updateSettings { s ->
            val list = LinkedHashSet<String>().apply {
                addAll(s.rowOrder)
                addAll(LauncherSettings.DEFAULT_ROW_ORDER)
                addAll(s.customCategories.map { it.id })
            }.toMutableList()
            val idx = list.indexOf(rowId)
            val target = idx + delta
            if (idx >= 0 && target in list.indices) {
                list.removeAt(idx)
                list.add(target, rowId)
            }
            s.copy(rowOrder = list)
        }
    }

    fun toggleRowHidden(rowId: String) {
        updateSettings { s ->
            val next = s.hiddenRows.toMutableSet()
            if (rowId in next) next.remove(rowId) else next.add(rowId)
            s.copy(hiddenRows = next)
        }
    }

    fun cycleTheme() {
        updateSettings { s ->
            val entries = VisualTheme.entries
            s.copy(theme = entries[(entries.indexOf(s.theme) + 1) % entries.size])
        }
    }

    fun cycleCornerStyle() {
        updateSettings { s ->
            val entries = CornerStyle.entries
            s.copy(cornerStyle = entries[(entries.indexOf(s.cornerStyle) + 1) % entries.size])
        }
    }

    fun cycleCardDensity() {
        updateSettings { s ->
            val entries = CardDensity.entries
            s.copy(cardDensity = entries[(entries.indexOf(s.cardDensity) + 1) % entries.size])
        }
    }

    fun cycleLanguage(): AppLanguage {
        val entries = AppLanguage.entries
        val next = entries[(entries.indexOf(_state.value.settings.language) + 1) % entries.size]
        updateSettings { it.copy(language = next) }
        return next
    }

    fun setParentalPin(pin: String) {
        val salt = container.pinSecurityUseCase.generateSalt()
        val hash = container.pinSecurityUseCase.hashPin(pin, salt)
        updateSettings { it.copy(parentalPinHash = hash, parentalPinSalt = salt) }
    }

    fun clearParentalPin() {
        updateSettings {
            it.copy(
                parentalPinHash = null,
                parentalPinSalt = null,
                lockSettingsWithPin = false,
                lockDeveloperWithPin = false,
                lockedPackages = emptySet(),
                lockedInputIds = emptySet(),
                parentalScheduleEnabled = false
            )
        }
    }

    fun verifyPin(candidate: String): Boolean {
        val s = _state.value.settings
        return container.pinSecurityUseCase.verifyPin(candidate, s.parentalPinHash, s.parentalPinSalt)
    }

    fun hasPin(): Boolean = !_state.value.settings.parentalPinHash.isNullOrBlank()

    fun isAppPinProtectedNow(app: TvApp): Boolean {
        if (!hasPin()) return false
        val s = _state.value.settings
        if (app.packageName in s.lockedPackages) return true
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return container.parentalScheduleUseCase.isRestrictedNow(s, hour) && app.categoryId == AppCategory.GAMES.id
    }

    fun isInputPinProtectedNow(input: TvInputSource): Boolean {
        return hasPin() && input.id in _state.value.settings.lockedInputIds
    }

    fun search(query: String): SearchLauncherUseCase.SearchResult {
        val rows = _state.value.rows
        val allEnrichedApps = rows.filterIsInstance<HomeRow.AppsRow>()
            .firstOrNull { it.rowId == AppCategory.ALL_APPS.id }?.apps ?: cachedApps
        val allEnrichedInputs = rows.filterIsInstance<HomeRow.InputsRow>().firstOrNull()?.inputs ?: cachedInputs
        return container.searchLauncherUseCase.search(query, allEnrichedApps, allEnrichedInputs)
    }

    fun getAvailableNativeShortcuts(): List<NativeSettingShortcut> =
        container.nativeSettingsRepository.getAvailableShortcuts()

    fun openNativeShortcut(id: String): Boolean =
        container.nativeSettingsRepository.openShortcut(id)

    fun getRegionalHubApps(region: WorldRegion): List<RegionalAppEntry> {
        val installed = cachedApps.map { it.packageName }.toSet()
        return container.filterRegionalHubUseCase.filter(
            catalog = container.regionalCatalogRepository.getCatalog(),
            region = region,
            installedPackages = installed,
            deviceSdkInt = Build.VERSION.SDK_INT
        )
    }

    fun getDonationChannels(): List<DonationChannel> =
        container.buildDonationPlanUseCase.buildForCountry(_state.value.detectedCountry)

    fun exportBackup(context: Context): Boolean = runCatching {
        File(context.filesDir, BACKUP_FILENAME).writeText(container.preferencesRepository.exportJson(), Charsets.UTF_8)
        true
    }.getOrDefault(false)

    fun restoreBackup(context: Context): Boolean {
        val file = File(context.filesDir, BACKUP_FILENAME)
        if (!file.exists()) return false
        return runCatching {
            val raw = file.readText(Charsets.UTF_8)
            container.preferencesRepository.importJson(raw).onSuccess {
                emitState(it, _state.value.diagnostics)
            }.isSuccess
        }.getOrDefault(false)
    }

    fun clearIconCache() {
        container.iconCache.clear()
        refreshDiagnostics()
    }

    private fun emitState(settings: LauncherSettings, diag: SystemDiagnostics?) {
        val rows = container.buildHomeRowsUseCase.build(cachedApps, cachedInputs, settings)
        _state.value = _state.value.copy(
            rows = rows,
            allApps = cachedApps,
            allInputs = cachedInputs,
            settings = settings,
            diagnostics = diag
        )
    }

    private fun detectCountryCode(): String {
        val tz = TimeZone.getDefault().id.lowercase()
        return when {
            tz.contains("argentina") || tz.contains("buenos_aires") -> "AR"
            tz.contains("montevideo") -> "UY"
            tz.contains("santiago") -> "CL"
            tz.contains("sao_paulo") -> "BR"
            tz.contains("mexico") -> "MX"
            tz.contains("bogota") -> "CO"
            tz.contains("lima") -> "PE"
            tz.contains("tokyo") -> "JP"
            tz.contains("seoul") -> "KR"
            tz.contains("shanghai") || tz.contains("taipei") -> "CN"
            else -> Locale.getDefault().country.takeIf { it.length == 2 } ?: "AR"
        }
    }

    fun onDestroy() = scope.cancel()

    companion object {
        private const val BACKUP_FILENAME = "velotv_backup_v2.json"
    }
}
