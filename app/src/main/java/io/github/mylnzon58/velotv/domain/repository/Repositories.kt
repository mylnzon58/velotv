package io.github.mylnzon58.velotv.domain.repository

import io.github.mylnzon58.velotv.domain.model.AppUsageStat
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.NativeSettingShortcut
import io.github.mylnzon58.velotv.domain.model.QuickCleanResult
import io.github.mylnzon58.velotv.domain.model.RegionalAppEntry
import io.github.mylnzon58.velotv.domain.model.SystemDiagnostics
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.domain.model.WallpaperSource

interface AppRepository {
    suspend fun loadInstalledApps(includeAlternativeLaunchers: Boolean = true): List<TvApp>
    suspend fun loadAlternativeLaunchers(): List<TvApp>
    fun launchApp(packageName: String): Boolean
    fun launchHomeLauncher(packageName: String): Boolean
    fun openAppInfo(packageName: String): Boolean
    fun requestUninstall(packageName: String): Boolean
    fun openPlayStoreListing(packageName: String): Boolean
}

interface TvInputRepository {
    suspend fun loadTvInputs(): List<TvInputSource>
    fun launchTvInput(input: TvInputSource): Boolean
}

interface PreferencesRepository {
    fun loadSettings(): LauncherSettings
    fun saveSettings(settings: LauncherSettings)
    fun loadUsageStats(): Map<String, AppUsageStat>
    fun recordAppLaunch(packageName: String, hour24: Int, nowMs: Long)
    fun exportJson(): String
    fun importJson(rawJson: String): Result<LauncherSettings>
}

interface SystemDiagnosticsRepository {
    fun readDiagnostics(): SystemDiagnostics
}

interface NativeSettingsRepository {
    fun getAvailableShortcuts(): List<NativeSettingShortcut>
    fun openShortcut(shortcutId: String): Boolean
}

interface RegionalCatalogRepository {
    fun getCatalog(): List<RegionalAppEntry>
}

interface WallpaperCatalogRepository {
    fun getBundledWallpapers(): List<WallpaperItem>
    fun getCachedOnlineWallpapers(): List<WallpaperItem>
    suspend fun refreshOnlineWallpapers(source: WallpaperSource, offlineOnly: Boolean): List<WallpaperItem>
}

interface SystemCleanerRepository {
    suspend fun runQuickClean(installedApps: List<TvApp>): QuickCleanResult
}
