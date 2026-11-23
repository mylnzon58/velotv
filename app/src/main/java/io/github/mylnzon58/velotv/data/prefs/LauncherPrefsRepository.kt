package io.github.mylnzon58.velotv.data.prefs

import android.content.Context
import android.content.SharedPreferences
import io.github.mylnzon58.velotv.domain.model.AppLanguage
import io.github.mylnzon58.velotv.domain.model.AppUsageStat
import io.github.mylnzon58.velotv.domain.model.CardDensity
import io.github.mylnzon58.velotv.domain.model.CornerStyle
import io.github.mylnzon58.velotv.domain.model.CustomCategory
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.VisualTheme
import io.github.mylnzon58.velotv.domain.model.WallpaperInterval
import io.github.mylnzon58.velotv.domain.model.WallpaperSource
import io.github.mylnzon58.velotv.domain.repository.PreferencesRepository
import org.json.JSONArray
import org.json.JSONObject

class LauncherPrefsRepository(context: Context) : PreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadSettings(): LauncherSettings {
        val raw = prefs.getString(KEY_SETTINGS_JSON, null) ?: return LauncherSettings()
        return parseJson(raw).getOrDefault(LauncherSettings())
    }

    override fun saveSettings(settings: LauncherSettings) {
        prefs.edit().putString(KEY_SETTINGS_JSON, serializeJson(settings)).apply()
    }

    override fun loadUsageStats(): Map<String, AppUsageStat> {
        val raw = prefs.getString(KEY_USAGE_JSON, null) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            buildMap {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val pkg = keys.next()
                    val o = root.optJSONObject(pkg) ?: continue
                    put(
                        pkg,
                        AppUsageStat(
                            packageName = pkg,
                            lastUsedMs = o.optLong("last", 0L),
                            totalLaunches = o.optInt("total", 0),
                            morningLaunches = o.optInt("am", 0),
                            afternoonLaunches = o.optInt("pm", 0),
                            eveningLaunches = o.optInt("eve", 0)
                        )
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    override fun recordAppLaunch(packageName: String, hour24: Int, nowMs: Long) {
        val current = loadUsageStats().toMutableMap()
        val prev = current[packageName] ?: AppUsageStat(packageName)
        val h = hour24.coerceIn(0, 23)
        current[packageName] = prev.copy(
            lastUsedMs = nowMs,
            totalLaunches = prev.totalLaunches + 1,
            morningLaunches = prev.morningLaunches + if (h in 5..11) 1 else 0,
            afternoonLaunches = prev.afternoonLaunches + if (h in 12..18) 1 else 0,
            eveningLaunches = prev.eveningLaunches + if (h !in 5..18) 1 else 0
        )
        val root = JSONObject()
        current.values
            .sortedByDescending { it.lastUsedMs }
            .take(40)
            .forEach { st ->
                root.put(
                    st.packageName,
                    JSONObject().apply {
                        put("last", st.lastUsedMs)
                        put("total", st.totalLaunches)
                        put("am", st.morningLaunches)
                        put("pm", st.afternoonLaunches)
                        put("eve", st.eveningLaunches)
                    }
                )
            }
        prefs.edit().putString(KEY_USAGE_JSON, root.toString()).apply()
    }

    override fun exportJson(): String = serializeJson(loadSettings())

    override fun importJson(rawJson: String): Result<LauncherSettings> {
        return parseJson(rawJson).onSuccess { saveSettings(it) }
    }

    private fun serializeJson(s: LauncherSettings): String {
        return JSONObject().apply {
            put("version", 3)
            put("theme", s.theme.id)
            put("cornerStyle", s.cornerStyle.id)
            put("cardDensity", s.cardDensity.id)
            put("language", s.language.code)
            put("use24HourClock", s.use24HourClock)
            put("showSeconds", s.showSeconds)
            put("showClock", s.showClock)
            put("showTopBar", s.showTopBar)
            put("showHeroFocusBar", s.showHeroFocusBar)
            put("showSystemStats", s.showSystemStats)
            put("showBannerLabels", s.showBannerLabels)
            put("showTvInputsRow", s.showTvInputsRow)
            put("showBottomHints", s.showBottomHints)
            put("showHiddenApps", s.showHiddenApps)
            put("showSmartShelf", s.showSmartShelf)
            put("nightModeEnabled", s.nightModeEnabled)
            put("nightModeAutoSchedule", s.nightModeAutoSchedule)
            put("autoCleanLowRam", s.autoCleanLowRam)
            put("remoteSpeedDialEnabled", s.remoteSpeedDialEnabled)
            put("includeAlternativeLaunchers", s.includeAlternativeLaunchers)
            put("onboardingCompleted", s.onboardingCompleted)
            put("favorites", JSONArray(s.favoritePackages))
            put("hidden", JSONArray(s.hiddenPackages.toList()))
            put("hiddenInputs", JSONArray(s.hiddenInputIds.toList()))
            put("customAppLabels", JSONObject(s.customAppLabels))
            put("customInputLabels", JSONObject(s.customInputLabels))
            put("customCategories", JSONArray().apply {
                s.customCategories.forEach { cat ->
                    put(JSONObject().put("id", cat.id).put("title", cat.title))
                }
            })
            put("categoryOverrides", JSONObject(s.categoryOverrides))
            put("rowOrder", JSONArray(s.rowOrder))
            put("hiddenRows", JSONArray(s.hiddenRows.toList()))
            if (s.autoLaunchTarget != null) put("autoLaunchTarget", s.autoLaunchTarget)
            if (s.parentalPinHash != null) put("parentalPinHash", s.parentalPinHash)
            if (s.parentalPinSalt != null) put("parentalPinSalt", s.parentalPinSalt)
            put("lockSettingsWithPin", s.lockSettingsWithPin)
            put("lockDeveloperWithPin", s.lockDeveloperWithPin)
            put("lockedPackages", JSONArray(s.lockedPackages.toList()))
            put("lockedInputs", JSONArray(s.lockedInputIds.toList()))
            put("parentalScheduleEnabled", s.parentalScheduleEnabled)
            put("parentalStartHour", s.parentalStartHour)
            put("parentalEndHour", s.parentalEndHour)
            put("wallpaperSource", s.wallpaperSource.id)
            put("wallpaperInterval", s.wallpaperInterval.id)
            if (s.pinnedWallpaperId != null) put("pinnedWallpaperId", s.pinnedWallpaperId)
            put("wallpaperDimPercent", s.wallpaperDimPercent)
            put("wallpaperKenBurns", s.wallpaperKenBurns)
            put("wallpaperOfflineOnly", s.wallpaperOfflineOnly)
            put("ambientAppColorGlow", s.ambientAppColorGlow)
        }.toString(2)
    }

    private fun parseJson(raw: String): Result<LauncherSettings> = runCatching {
        val o = JSONObject(raw)
        val storedVersion = o.optInt("version", 2)

        fun readList(key: String): List<String> = buildList {
            val arr = o.optJSONArray(key) ?: return@buildList
            for (i in 0 until arr.length()) add(arr.getString(i))
        }

        fun readMap(key: String): Map<String, String> = buildMap {
            val obj = o.optJSONObject(key) ?: return@buildMap
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                put(k, obj.optString(k))
            }
        }

        val customCats = buildList {
            val arr = o.optJSONArray("customCategories") ?: return@buildList
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id")
                val title = item.optString("title")
                if (id.isNotBlank() && title.isNotBlank()) add(CustomCategory(id, title))
            }
        }

        val order = readList("rowOrder").ifEmpty { LauncherSettings.DEFAULT_ROW_ORDER }

        // Smooth one-time visual upgrade from v2 (harsh darkroom/hints) to v3 Cinema Lounge aesthetic
        val resolvedTheme = if (storedVersion < 3 && o.optString("theme") == "darkroom_amber") {
            VisualTheme.CINEMA_LOUNGE
        } else {
            VisualTheme.fromId(o.optString("theme"))
        }
        val resolvedCorner = if (storedVersion < 3 && o.optString("cornerStyle") == "editorial") {
            CornerStyle.SOFT
        } else {
            CornerStyle.fromId(o.optString("cornerStyle"))
        }
        val resolvedBottomHints = if (storedVersion < 3) false else o.optBoolean("showBottomHints", false)

        LauncherSettings(
            theme = resolvedTheme,
            cornerStyle = resolvedCorner,
            cardDensity = CardDensity.fromId(o.optString("cardDensity")),
            language = AppLanguage.fromCode(o.optString("language")),
            use24HourClock = o.optBoolean("use24HourClock", true),
            showSeconds = o.optBoolean("showSeconds", false),
            showClock = o.optBoolean("showClock", true),
            showTopBar = o.optBoolean("showTopBar", true),
            showHeroFocusBar = o.optBoolean("showHeroFocusBar", true),
            showSystemStats = o.optBoolean("showSystemStats", true),
            showBannerLabels = o.optBoolean("showBannerLabels", false),
            showTvInputsRow = o.optBoolean("showTvInputsRow", true),
            showBottomHints = resolvedBottomHints,
            showHiddenApps = o.optBoolean("showHiddenApps", false),
            showSmartShelf = o.optBoolean("showSmartShelf", true),
            nightModeEnabled = o.optBoolean("nightModeEnabled", false),
            nightModeAutoSchedule = o.optBoolean("nightModeAutoSchedule", true),
            autoCleanLowRam = o.optBoolean("autoCleanLowRam", true),
            remoteSpeedDialEnabled = o.optBoolean("remoteSpeedDialEnabled", true),
            includeAlternativeLaunchers = o.optBoolean("includeAlternativeLaunchers", true),
            onboardingCompleted = o.optBoolean("onboardingCompleted", false),
            favoritePackages = readList("favorites"),
            hiddenPackages = readList("hidden").toSet(),
            hiddenInputIds = readList("hiddenInputs").toSet(),
            customAppLabels = readMap("customAppLabels"),
            customInputLabels = readMap("customInputLabels"),
            customCategories = customCats,
            categoryOverrides = readMap("categoryOverrides"),
            rowOrder = order,
            hiddenRows = readList("hiddenRows").toSet(),
            autoLaunchTarget = o.optString("autoLaunchTarget").takeIf { it.isNotBlank() },
            parentalPinHash = o.optString("parentalPinHash").takeIf { it.isNotBlank() },
            parentalPinSalt = o.optString("parentalPinSalt").takeIf { it.isNotBlank() },
            lockSettingsWithPin = o.optBoolean("lockSettingsWithPin", false),
            lockDeveloperWithPin = o.optBoolean("lockDeveloperWithPin", false),
            lockedPackages = readList("lockedPackages").toSet(),
            lockedInputIds = readList("lockedInputs").toSet(),
            parentalScheduleEnabled = o.optBoolean("parentalScheduleEnabled", false),
            parentalStartHour = o.optInt("parentalStartHour", 22),
            parentalEndHour = o.optInt("parentalEndHour", 7),
            wallpaperSource = WallpaperSource.fromId(o.optString("wallpaperSource")),
            wallpaperInterval = WallpaperInterval.fromId(o.optString("wallpaperInterval")),
            pinnedWallpaperId = o.optString("pinnedWallpaperId").takeIf { it.isNotBlank() },
            wallpaperDimPercent = o.optInt("wallpaperDimPercent", 56).coerceIn(25, 90),
            wallpaperKenBurns = o.optBoolean("wallpaperKenBurns", true),
            wallpaperOfflineOnly = o.optBoolean("wallpaperOfflineOnly", false),
            ambientAppColorGlow = o.optBoolean("ambientAppColorGlow", true)
        )
    }

    companion object {
        private const val PREFS_NAME = "velotv_prefs_v2"
        private const val KEY_SETTINGS_JSON = "settings_json"
        private const val KEY_USAGE_JSON = "usage_stats_json"
    }
}
