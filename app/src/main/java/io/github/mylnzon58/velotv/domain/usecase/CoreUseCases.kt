package io.github.mylnzon58.velotv.domain.usecase

import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.DonationChannel
import io.github.mylnzon58.velotv.domain.model.HomeRow
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.RegionalAppEntry
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.domain.model.WorldRegion
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom

class ClassifyAppUseCase {

    fun classify(packageName: String, label: String, isSystemApp: Boolean): AppCategory {
        val pkg = packageName.lowercase()
        val name = label.lowercase()

        return when {
            STREAMING_KEYWORDS.any { pkg.contains(it) || name.contains(it) } -> AppCategory.STREAMING
            MEDIA_WEB_KEYWORDS.any { pkg.contains(it) || name.contains(it) } -> AppCategory.MEDIA_WEB
            GAME_KEYWORDS.any { pkg.contains(it) || name.contains(it) } -> AppCategory.GAMES
            TOOLS_KEYWORDS.any { pkg.contains(it) || name.contains(it) } || isSystemApp -> AppCategory.TOOLS
            else -> AppCategory.ALL_APPS
        }
    }

    companion object {
        private val STREAMING_KEYWORDS = listOf(
            "netflix", "disney", "youtube", "smarttube", "teamsmart", "spotify", "primevideo",
            "amazonvideo", "hbo", "wbd.stream", "paramount", "cbs.ca", "pluto", "flow",
            "directv", "dgo", "mercadoplay", "mercadolibre.play", "cine.ar", "telecentro",
            "movistar", "tyc", "twitch", "hulu", "peacock", "tubi", "crunchyroll", "viki",
            "abema", "bilibili", "apple.atve", "starplus", "vix", "clarovideo", "max"
        )
        private val MEDIA_WEB_KEYWORDS = listOf(
            "tvwebbrowser", "tvbro", "browser", "chrome", "brave", "firefox", "mozilla",
            "vlc", "kodi", "xbmc", "jellyfin", "plex", "stremio", "nova", "mxtech",
            "mediaplayer", "gallery", "photo", "music", "iptv", "tivimate"
        )
        private val GAME_KEYWORDS = listOf(
            "game", "retroarch", "ppsspp", "dolphin", "moonlight", "geforcenow", "steamlink",
            "xbox", "playstation", "play.games", "emulator"
        )
        private val TOOLS_KEYWORDS = listOf(
            "settings", "vending", "downloader", "localsend", "sendfilestotv", "explorer",
            "filemanager", "xplore", "fx", "speedtest", "aida64", "buttonmapper", "vpn",
            "wireguard", "tailscale", "adb", "terminal"
        )
    }
}

class BuildHomeRowsUseCase {

    fun build(
        rawApps: List<TvApp>,
        rawInputs: List<TvInputSource>,
        settings: LauncherSettings
    ): List<HomeRow> {
        val favoriteIndex = settings.favoritePackages
            .withIndex()
            .associate { (idx, pkg) -> pkg to idx }

        val enrichedApps = rawApps.map { app ->
            val overrideCat = settings.categoryOverrides[app.packageName] ?: app.categoryId
            val favIdx = favoriteIndex[app.packageName]
            val displayLabel = settings.customAppLabels[app.packageName]
                ?.takeIf { it.isNotBlank() }
                ?: app.defaultLabel
            app.copy(
                label = displayLabel,
                categoryId = overrideCat,
                isFavorite = favIdx != null,
                isHidden = app.packageName in settings.hiddenPackages,
                isLocked = app.packageName in settings.lockedPackages,
                customOrder = favIdx ?: Int.MAX_VALUE
            )
        }

        val visibleApps = if (settings.showHiddenApps) {
            enrichedApps
        } else {
            enrichedApps.filterNot { it.isHidden }
        }

        val enrichedInputs = rawInputs.map { input ->
            val displayLabel = settings.customInputLabels[input.id]
                ?.takeIf { it.isNotBlank() }
                ?: input.defaultLabel
            input.copy(
                label = displayLabel,
                isHidden = input.id in settings.hiddenInputIds,
                isLocked = input.id in settings.lockedInputIds
            )
        }
        val visibleInputs = if (settings.showHiddenApps) {
            enrichedInputs
        } else {
            enrichedInputs.filterNot { it.isHidden }
        }

        val customCatTitles = settings.customCategories.associate { it.id to it.title }
        val orderedIds = LinkedHashSet<String>().apply {
            addAll(settings.rowOrder)
            addAll(LauncherSettings.DEFAULT_ROW_ORDER)
            addAll(settings.customCategories.map { it.id })
        }

        val rows = mutableListOf<HomeRow>()

        for (rowId in orderedIds) {
            if (rowId in settings.hiddenRows) continue

            when (rowId) {
                AppCategory.FAVORITES.id -> {
                    val favs = visibleApps
                        .filter { it.isFavorite }
                        .sortedWith(compareBy<TvApp> { it.customOrder }.thenBy { it.label.lowercase() })
                    if (favs.isNotEmpty()) rows += HomeRow.AppsRow(rowId, null, favs)
                }

                AppCategory.TV_INPUTS.id -> {
                    if (settings.showTvInputsRow && visibleInputs.isNotEmpty()) {
                        rows += HomeRow.InputsRow(rowId, null, visibleInputs)
                    }
                }

                AppCategory.ALL_APPS.id -> {
                    val all = visibleApps.sortedBy { it.label.lowercase() }
                    if (all.isNotEmpty()) rows += HomeRow.AppsRow(rowId, null, all)
                }

                else -> {
                    val matches = visibleApps
                        .filter { it.categoryId == rowId }
                        .sortedBy { it.label.lowercase() }
                    if (matches.isNotEmpty()) {
                        rows += HomeRow.AppsRow(rowId, customCatTitles[rowId], matches)
                    }
                }
            }
        }

        return rows
    }
}

class SearchLauncherUseCase {

    data class SearchResult(
        val apps: List<TvApp>,
        val inputs: List<TvInputSource>
    )

    fun search(
        query: String,
        apps: List<TvApp>,
        inputs: List<TvInputSource>
    ): SearchResult {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            return SearchResult(
                apps = apps.sortedBy { it.label.lowercase() },
                inputs = inputs
            )
        }
        return SearchResult(
            apps = apps.filter {
                it.label.lowercase().contains(q) ||
                    it.defaultLabel.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
            }.sortedBy { it.label.lowercase() },
            inputs = inputs.filter {
                it.label.lowercase().contains(q) || it.defaultLabel.lowercase().contains(q)
            }
        )
    }
}

class ParentalScheduleUseCase {

    fun isRestrictedNow(settings: LauncherSettings, currentHour24: Int): Boolean {
        if (!settings.parentalScheduleEnabled) return false
        val start = settings.parentalStartHour.coerceIn(0, 23)
        val end = settings.parentalEndHour.coerceIn(0, 23)
        if (start == end) return false
        return if (start < end) {
            currentHour24 in start until end
        } else {
            currentHour24 >= start || currentHour24 < end
        }
    }
}

class FilterRegionalHubUseCase {

    fun filter(
        catalog: List<RegionalAppEntry>,
        region: WorldRegion,
        installedPackages: Set<String>,
        deviceSdkInt: Int
    ): List<RegionalAppEntry> {
        return catalog
            .filter { region in it.regions }
            .map { entry ->
                entry.copy(
                    isInstalled = entry.packageName in installedPackages,
                    isCompatibleWithDevice = deviceSdkInt >= entry.minSdk
                )
            }
            .sortedWith(
                compareByDescending<RegionalAppEntry> { it.isCompatibleWithDevice }
                    .thenBy { it.isInstalled }
                    .thenByDescending { it.lightweight }
                    .thenBy { it.title.lowercase() }
            )
    }
}

class BuildDonationPlanUseCase {

    fun buildForCountry(isoCountryCode: String?): List<DonationChannel> {
        val isArgentina = isoCountryCode?.uppercase()?.trim() == "AR"
        val isLatam = WorldRegion.fromIsoCountry(isoCountryCode) in setOf(WorldRegion.ARGENTINA, WorldRegion.LATAM)

        val mercadoPago = DonationChannel(
            id = "mercadopago_ar",
            title = "Mercado Pago",
            subtitle = "Argentina · Monto libre en ARS",
            url = URL_MERCADOPAGO_AR,
            badge = "AR / ARS",
            isPrimaryForRegion = isArgentina
        )
        val tecito = DonationChannel(
            id = "tecito",
            title = "Tecito",
            subtitle = "Aporte directo · LATAM",
            url = URL_TECITO,
            badge = "LATAM",
            isPrimaryForRegion = isLatam && !isArgentina
        )
        val githubSponsors = DonationChannel(
            id = "github_sponsors",
            title = "GitHub Sponsors",
            subtitle = "Internacional · US / EU / Asia",
            url = URL_GITHUB_SPONSORS,
            badge = "GLOBAL",
            isPrimaryForRegion = !isLatam
        )
        val patreon = DonationChannel(
            id = "patreon",
            title = "Patreon",
            subtitle = "Membresía mensual open-source",
            url = URL_PATREON,
            badge = "SPONSOR",
            isPrimaryForRegion = false
        )

        return when {
            isArgentina -> listOf(mercadoPago, tecito, githubSponsors, patreon)
            isLatam -> listOf(tecito, githubSponsors, patreon, mercadoPago)
            else -> listOf(githubSponsors, patreon, tecito, mercadoPago)
        }
    }

    companion object {
        const val CREATOR_NAME = "Juan José Monzón"
        const val CREATOR_HANDLE = "@mylnzon58"
        const val REPO_URL = "https://github.com/mylnzon58/velotv"
        const val URL_MERCADOPAGO_AR = "https://link.mercadopago.com.ar/mylnzon58"
        const val URL_TECITO = "https://tecito.app/juanjoclassic"
        const val URL_GITHUB_SPONSORS = "https://github.com/sponsors/mylnzon58"
        const val URL_PATREON = "https://www.patreon.com/JuanjoClassic"
    }
}

class PinSecurityUseCase {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$pin:velotv-v1".toByteArray(Charsets.UTF_8)
        val hash = digest.digest(input)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(candidatePin: String, expectedHash: String?, salt: String?): Boolean {
        if (expectedHash.isNullOrBlank() || salt.isNullOrBlank()) return false
        val actual = hashPin(candidatePin, salt)
        return constantTimeEquals(actual, expectedHash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}

class WallpaperPolicyUseCase {

    /**
     * OWASP SSRF / privacy allowlist: only verified free-license HTTPS image banks are permitted.
     */
    fun isAllowedWallpaperUrl(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank() || rawUrl.length > 1024) return false
        val uri = runCatching { URI(rawUrl.trim()) }.getOrNull() ?: return false
        if (!"https".equals(uri.scheme, ignoreCase = true)) return false
        val host = uri.host?.lowercase()?.trimEnd('.') ?: return false
        if (uri.userInfo != null) return false
        return host in ALLOWED_HOSTS
    }

    /**
     * Picks the active wallpaper respecting manual pinning first, otherwise advancing round-robin.
     */
    fun selectNextWallpaper(
        pool: List<WallpaperItem>,
        currentId: String?,
        pinnedId: String?
    ): WallpaperItem? {
        if (pool.isEmpty()) return null
        if (!pinnedId.isNullOrBlank()) {
            pool.firstOrNull { it.id == pinnedId }?.let { return it }
        }
        if (currentId.isNullOrBlank()) return pool.first()
        val currentIndex = pool.indexOfFirst { it.id == currentId }
        return if (currentIndex < 0) pool.first() else pool[(currentIndex + 1) % pool.size]
    }

    /**
     * Extracts a restrained cinema accent color from sampled ARGB pixels without external libraries.
     */
    fun extractAmbientAccentColor(pixels: IntArray, fallbackColor: Int): Int {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var weightSum = 0L

        for (px in pixels) {
            val a = (px ushr 24) and 0xFF
            if (a < 180) continue
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF
            val maxC = maxOf(r, g, b)
            val minC = minOf(r, g, b)
            val chroma = maxC - minC
            val luma = (r * 30 + g * 59 + b * 11) / 100
            if (luma < 24 || luma > 235 || chroma < 24) continue
            val weight = (chroma.toLong() + 16L)
            rSum += r * weight
            gSum += g * weight
            bSum += b * weight
            weightSum += weight
        }

        if (weightSum <= 0L) return fallbackColor
        val avgR = (rSum / weightSum).toInt().coerceIn(0, 255)
        val avgG = (gSum / weightSum).toInt().coerceIn(0, 255)
        val avgB = (bSum / weightSum).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB
    }

    companion object {
        val ALLOWED_HOSTS = setOf(
            "commons.wikimedia.org",
            "upload.wikimedia.org",
            "thumb.wikimedia.org",
            "picsum.photos",
            "fastly.picsum.photos",
            "images.unsplash.com"
        )
    }
}

class SystemCleanPolicyUseCase {

    /**
     * Filters safe background kill targets, strictly excluding OS, TV tuner/HDMI, DRM, input,
     * and launcher self packages so cleanup never disrupts live TV or system stability.
     */
    fun filterSafeKillTargets(apps: List<TvApp>, selfPackage: String): List<String> {
        return apps.mapNotNull { app ->
            val pkg = app.packageName
            if (pkg == selfPackage) return@mapNotNull null
            if (PROTECTED_PREFIXES.any { pkg == it || pkg.startsWith("$it.") }) return@mapNotNull null
            if (!app.isSystemApp || pkg in HEAVY_PREINSTALLED_MEDIA) pkg else null
        }.distinct()
    }

    companion object {
        private val PROTECTED_PREFIXES = listOf(
            "android",
            "com.android.systemui",
            "com.android.tv",
            "com.android.settings",
            "com.android.providers",
            "com.android.bluetooth",
            "com.android.inputmethod",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.google.android.inputmethod",
            "com.mediatek",
            "com.mtk",
            "com.skyworth"
        )

        private val HEAVY_PREINSTALLED_MEDIA = setOf(
            "com.netflix.ninja",
            "com.google.android.youtube.tv",
            "com.android.vending",
            "com.amazon.amazonvideo.livingroom",
            "com.spotify.tv.android"
        )
    }
}
