package io.github.mylnzon58.velotv.domain.model

enum class AppCategory(val id: String) {
    FAVORITES("favorites"),
    TV_INPUTS("tv_inputs"),
    STREAMING("streaming"),
    MEDIA_WEB("media_web"),
    GAMES("games"),
    TOOLS("tools"),
    ALL_APPS("all_apps")
}

data class CustomCategory(
    val id: String,
    val title: String
)

data class TvApp(
    val packageName: String,
    val label: String,
    val defaultLabel: String = label,
    val categoryId: String,
    val isLeanback: Boolean,
    val isSystemApp: Boolean,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val customOrder: Int = Int.MAX_VALUE
)

enum class TvInputType {
    HDMI,
    TUNER,
    AV,
    OTHER
}

data class TvInputSource(
    val id: String,
    val label: String,
    val defaultLabel: String = label,
    val type: TvInputType,
    val isConnected: Boolean,
    val isPassthrough: Boolean,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * Calibrated solid-surface themes derived from Hallmark's OKLCH four-layer ladder.
 * Zero neon cyan-on-dark, zero translucent alpha fills, zero pure #000/#FFF.
 */
enum class VisualTheme(
    val id: String,
    val paper: Int,
    val paper2: Int,
    val paper3: Int,
    val ink: Int,
    val ink2: Int,
    val muted: Int,
    val rule: Int,
    val accent: Int,
    val accentInk: Int,
    val isLight: Boolean = false
) {
    DARKROOM_AMBER(
        id = "darkroom_amber",
        paper = 0xFF12100E.toInt(),
        paper2 = 0xFF1B1815.toInt(),
        paper3 = 0xFF26221E.toInt(),
        ink = 0xFFEDE8E1.toInt(),
        ink2 = 0xFFC9C1B7.toInt(),
        muted = 0xFF8F867C.toInt(),
        rule = 0xFF332E29.toInt(),
        accent = 0xFFDF9D3E.toInt(),
        accentInk = 0xFF12100E.toInt()
    ),
    DARKROOM_RED(
        id = "darkroom_red",
        paper = 0xFF12100E.toInt(),
        paper2 = 0xFF1B1815.toInt(),
        paper3 = 0xFF26221E.toInt(),
        ink = 0xFFEDE8E1.toInt(),
        ink2 = 0xFFC9C1B7.toInt(),
        muted = 0xFF8F867C.toInt(),
        rule = 0xFF352D2B.toInt(),
        accent = 0xFFD64F3C.toInt(),
        accentInk = 0xFFF7F2EB.toInt()
    ),
    MIDNIGHT(
        id = "midnight",
        paper = 0xFF111624.toInt(),
        paper2 = 0xFF182034.toInt(),
        paper3 = 0xFF212C45.toInt(),
        ink = 0xFFEBF0F7.toInt(),
        ink2 = 0xFFBAC6D8.toInt(),
        muted = 0xFF7D8DA6.toInt(),
        rule = 0xFF2D3A56.toInt(),
        accent = 0xFF78B7DC.toInt(),
        accentInk = 0xFF111624.toInt()
    ),
    STUDIO_LIGHT(
        id = "studio_light",
        paper = 0xFFF3EFE9.toInt(),
        paper2 = 0xFFE8E3DA.toInt(),
        paper3 = 0xFFDDD6CA.toInt(),
        ink = 0xFF191715.toInt(),
        ink2 = 0xFF46413B.toInt(),
        muted = 0xFF6E675E.toInt(),
        rule = 0xFFCBC3B6.toInt(),
        accent = 0xFFC95B1C.toInt(),
        accentInk = 0xFFF8F5F0.toInt(),
        isLight = true
    ),
    FOREST(
        id = "forest",
        paper = 0xFF111613.toInt(),
        paper2 = 0xFF19211C.toInt(),
        paper3 = 0xFF232E27.toInt(),
        ink = 0xFFEAECE7.toInt(),
        ink2 = 0xFFBCC3B8.toInt(),
        muted = 0xFF818B7E.toInt(),
        rule = 0xFF2E3A32.toInt(),
        accent = 0xFF7CB68A.toInt(),
        accentInk = 0xFF111613.toInt()
    ),
    GRAPHITE(
        id = "graphite",
        paper = 0xFF151514.toInt(),
        paper2 = 0xFF1E1E1C.toInt(),
        paper3 = 0xFF292926.toInt(),
        ink = 0xFFEFECE6.toInt(),
        ink2 = 0xFFC0BCB4.toInt(),
        muted = 0xFF85817A.toInt(),
        rule = 0xFF343430.toInt(),
        accent = 0xFFE6E1D8.toInt(),
        accentInk = 0xFF151514.toInt()
    );

    companion object {
        fun fromId(id: String?): VisualTheme = entries.firstOrNull { it.id == id } ?: DARKROOM_AMBER
    }
}

enum class CornerStyle(val id: String, val radiusDp: Int) {
    SHARP("sharp", 0),
    EDITORIAL("editorial", 6),
    SOFT("soft", 12);

    companion object {
        fun fromId(id: String?): CornerStyle = entries.firstOrNull { it.id == id } ?: EDITORIAL
    }
}

enum class CardDensity(val id: String, val widthDp: Int, val heightDp: Int) {
    COMPACT("compact", 164, 94),
    NORMAL("normal", 196, 112),
    LARGE("large", 232, 132);

    companion object {
        fun fromId(id: String?): CardDensity = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "Sistema"),
    ES_AR("es-AR", "Español (Argentina)"),
    ES("es", "Español"),
    EN("en", "English"),
    PT_BR("pt-BR", "Português (Brasil)"),
    FR("fr", "Français"),
    DE("de", "Deutsch"),
    IT("it", "Italiano"),
    JA("ja", "日本語"),
    KO("ko", "한국어"),
    ZH_CN("zh-CN", "简体中文");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

data class LauncherSettings(
    val theme: VisualTheme = VisualTheme.DARKROOM_AMBER,
    val cornerStyle: CornerStyle = CornerStyle.EDITORIAL,
    val cardDensity: CardDensity = CardDensity.NORMAL,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val use24HourClock: Boolean = true,
    val showSeconds: Boolean = false,
    val showClock: Boolean = true,
    val showTopBar: Boolean = true,
    val showHeroFocusBar: Boolean = true,
    val showSystemStats: Boolean = true,
    val showBannerLabels: Boolean = false,
    val showTvInputsRow: Boolean = true,
    val showBottomHints: Boolean = true,
    val showHiddenApps: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val favoritePackages: List<String> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val hiddenInputIds: Set<String> = emptySet(),
    val customAppLabels: Map<String, String> = emptyMap(),
    val customInputLabels: Map<String, String> = emptyMap(),
    val customCategories: List<CustomCategory> = emptyList(),
    val categoryOverrides: Map<String, String> = emptyMap(),
    val rowOrder: List<String> = DEFAULT_ROW_ORDER,
    val hiddenRows: Set<String> = emptySet(),
    val autoLaunchTarget: String? = null,
    val parentalPinHash: String? = null,
    val parentalPinSalt: String? = null,
    val lockSettingsWithPin: Boolean = false,
    val lockDeveloperWithPin: Boolean = false,
    val lockedPackages: Set<String> = emptySet(),
    val lockedInputIds: Set<String> = emptySet(),
    val parentalScheduleEnabled: Boolean = false,
    val parentalStartHour: Int = 22,
    val parentalEndHour: Int = 7
) {
    companion object {
        val DEFAULT_ROW_ORDER = listOf(
            AppCategory.FAVORITES.id,
            AppCategory.TV_INPUTS.id,
            AppCategory.STREAMING.id,
            AppCategory.MEDIA_WEB.id,
            AppCategory.GAMES.id,
            AppCategory.TOOLS.id,
            AppCategory.ALL_APPS.id
        )
    }
}

sealed class HomeRow {
    abstract val rowId: String
    abstract val customTitle: String?

    data class AppsRow(
        override val rowId: String,
        override val customTitle: String? = null,
        val apps: List<TvApp>
    ) : HomeRow()

    data class InputsRow(
        override val rowId: String = AppCategory.TV_INPUTS.id,
        override val customTitle: String? = null,
        val inputs: List<TvInputSource>
    ) : HomeRow()
}

data class SystemDiagnostics(
    val manufacturer: String,
    val model: String,
    val board: String,
    val platform: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildId: String,
    val kernelVersion: String,
    val cpuAbi: String,
    val webviewVersion: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedRamPercent: Int,
    val dataFreeGb: Float,
    val dataTotalGb: Float,
    val systemFreeMb: Long,
    val systemTotalMb: Long,
    val displayResolution: String,
    val refreshRateHz: Int,
    val networkLabel: String,
    val isConnected: Boolean,
    val localIp: String,
    val uptimeLabel: String,
    val adbEnabled: Boolean,
    val developmentEnabled: Boolean,
    val unknownSourcesEnabled: Boolean,
    val bootloaderLocked: String,
    val trebleEnabled: String,
    val animWindowScale: String,
    val animTransitionScale: String,
    val animDurationScale: String
)

enum class SettingShortcutGroup {
    DEVELOPER,
    SYSTEM,
    HARDWARE
}

data class NativeSettingShortcut(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val group: SettingShortcutGroup,
    val requiresCaution: Boolean = false
)

enum class WorldRegion(val id: String) {
    ARGENTINA("AR"),
    LATAM("LATAM"),
    USA("US"),
    EUROPE("EU"),
    ASIA("ASIA");

    companion object {
        private val LATAM_COUNTRIES = setOf(
            "UY", "CL", "PY", "BO", "PE", "EC", "CO", "VE", "BR", "MX",
            "CR", "PA", "GT", "HN", "SV", "NI", "DO", "CU", "PR"
        )
        private val EU_COUNTRIES = setOf(
            "ES", "FR", "DE", "IT", "PT", "GB", "UK", "NL", "BE", "CH",
            "AT", "SE", "NO", "DK", "FI", "PL", "CZ", "IE", "GR", "RO"
        )
        private val ASIA_COUNTRIES = setOf(
            "JP", "KR", "CN", "TW", "HK", "SG", "IN", "TH", "VN", "ID", "MY", "PH"
        )

        fun fromIsoCountry(countryCode: String?): WorldRegion {
            val code = countryCode?.uppercase()?.trim().orEmpty()
            return when {
                code == "AR" -> ARGENTINA
                code in LATAM_COUNTRIES -> LATAM
                code == "US" || code == "CA" -> USA
                code in EU_COUNTRIES -> EUROPE
                code in ASIA_COUNTRIES -> ASIA
                else -> LATAM
            }
        }
    }
}

enum class AppSourceType {
    PLAY_STORE,
    OPEN_SOURCE
}

data class RegionalAppEntry(
    val id: String,
    val title: String,
    val packageName: String,
    val regions: Set<WorldRegion>,
    val category: AppCategory,
    val minSdk: Int,
    val sourceType: AppSourceType,
    val openSourceUrl: String? = null,
    val lightweight: Boolean = true,
    val isInstalled: Boolean = false,
    val isCompatibleWithDevice: Boolean = true
)

data class DonationChannel(
    val id: String,
    val title: String,
    val subtitle: String,
    val url: String,
    val badge: String,
    val isPrimaryForRegion: Boolean
)
