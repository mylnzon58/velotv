package io.github.mylnzon58.velotv.data.region

import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.AppSourceType
import io.github.mylnzon58.velotv.domain.model.RegionalAppEntry
import io.github.mylnzon58.velotv.domain.model.WorldRegion
import io.github.mylnzon58.velotv.domain.repository.RegionalCatalogRepository

/**
 * Curated, 100% legal regional catalog (Play Store + Official Open-Source GitHub releases).
 * Automatically adapts to country (AR, LATAM, USA, Europe, Asia) and Android SDK version.
 */
class StaticRegionalCatalogRepository : RegionalCatalogRepository {

    private val allRegions = WorldRegion.entries.toSet()
    private val latamAndAr = setOf(WorldRegion.ARGENTINA, WorldRegion.LATAM)
    private val arOnly = setOf(WorldRegion.ARGENTINA)

    override fun getCatalog(): List<RegionalAppEntry> = listOf(
        // --- Essential Lightweight Open-Source Pack (All Regions, works on Android 7-15) ---
        RegionalAppEntry(
            id = "tvbro",
            title = "TV Bro (Chromium WebView)",
            packageName = "com.phlox.tvwebbrowser",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 21,
            sourceType = AppSourceType.OPEN_SOURCE,
            openSourceUrl = "https://github.com/truefedex/tv-bro/releases",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "smarttube",
            title = "SmartTube (Open YouTube)",
            packageName = "org.smarttube.stable",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.OPEN_SOURCE,
            openSourceUrl = "https://github.com/yuliskov/SmartTube/releases",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "smarttwitch",
            title = "SmartTwitchTV",
            packageName = "com.fgl27.twitch",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.OPEN_SOURCE,
            openSourceUrl = "https://github.com/fgl27/SmartTwitchTV/releases",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "vlc",
            title = "VLC Media Player",
            packageName = "org.videolan.vlc",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE,
            openSourceUrl = "https://get.videolan.org/vlc-android/",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "localsend",
            title = "LocalSend (Wi-Fi Files)",
            packageName = "org.localsend.localsend_app",
            regions = allRegions,
            category = AppCategory.TOOLS,
            minSdk = 21,
            sourceType = AppSourceType.OPEN_SOURCE,
            openSourceUrl = "https://github.com/localsend/localsend/releases",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "stremio",
            title = "Stremio",
            packageName = "com.stremio.one",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 24,
            sourceType = AppSourceType.PLAY_STORE,
            lightweight = true
        ),
        RegionalAppEntry(
            id = "jellyfin",
            title = "Jellyfin TV",
            packageName = "org.jellyfin.androidtv",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE,
            openSourceUrl = "https://github.com/jellyfin/jellyfin-androidtv/releases",
            lightweight = true
        ),
        RegionalAppEntry(
            id = "kodi",
            title = "Kodi",
            packageName = "org.xbmc.kodi",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE,
            lightweight = false
        ),
        RegionalAppEntry(
            id = "playstore",
            title = "Google Play Store",
            packageName = "com.android.vending",
            regions = allRegions,
            category = AppCategory.TOOLS,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE,
            lightweight = true
        ),
        // Browsers requiring newer Android (Android 10+ API 29+)
        RegionalAppEntry(
            id = "brave",
            title = "Brave Browser (Android 10+)",
            packageName = "com.brave.browser",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 29,
            sourceType = AppSourceType.OPEN_SOURCE,
            openSourceUrl = "https://github.com/brave/brave-browser/releases",
            lightweight = false
        ),
        RegionalAppEntry(
            id = "chrome",
            title = "Google Chrome (Android 10+)",
            packageName = "com.android.chrome",
            regions = allRegions,
            category = AppCategory.MEDIA_WEB,
            minSdk = 29,
            sourceType = AppSourceType.PLAY_STORE,
            lightweight = false
        ),

        // --- Global Streaming ---
        RegionalAppEntry(
            id = "netflix",
            title = "Netflix",
            packageName = "com.netflix.ninja",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "disney",
            title = "Disney+",
            packageName = "com.disney.disneyplus",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "prime",
            title = "Prime Video",
            packageName = "com.amazon.amazonvideo.livingroom",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "youtube",
            title = "YouTube",
            packageName = "com.google.android.youtube.tv",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "spotify",
            title = "Spotify",
            packageName = "com.spotify.tv.android",
            regions = allRegions,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "max",
            title = "Max (HBO)",
            packageName = "com.wbd.stream",
            regions = setOf(WorldRegion.ARGENTINA, WorldRegion.LATAM, WorldRegion.USA, WorldRegion.EUROPE),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "paramount",
            title = "Paramount+",
            packageName = "com.cbs.ca",
            regions = setOf(WorldRegion.ARGENTINA, WorldRegion.LATAM, WorldRegion.USA, WorldRegion.EUROPE),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "plutotv",
            title = "Pluto TV (Gratis)",
            packageName = "tv.pluto.android",
            regions = setOf(WorldRegion.ARGENTINA, WorldRegion.LATAM, WorldRegion.USA, WorldRegion.EUROPE),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),

        // --- Argentina & LATAM Favorites ---
        RegionalAppEntry(
            id = "mercadoplay",
            title = "Mercado Play (Gratis)",
            packageName = "com.mercadolibre.play",
            regions = latamAndAr,
            category = AppCategory.STREAMING,
            minSdk = 24,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "flow",
            title = "Flow (Personal)",
            packageName = "com.telecom.flow",
            regions = arOnly,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "dgo",
            title = "DGO (DirecTV)",
            packageName = "com.att.tv.dgo",
            regions = latamAndAr,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "cinear",
            title = "CINE.AR PLAY",
            packageName = "ar.gob.incaa.cinear",
            regions = arOnly,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "vix",
            title = "ViX",
            packageName = "com.univision.prendetv",
            regions = latamAndAr,
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),

        // --- USA Favorites ---
        RegionalAppEntry(
            id = "hulu",
            title = "Hulu",
            packageName = "com.hulu.livingroomplus",
            regions = setOf(WorldRegion.USA),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "tubi",
            title = "Tubi (Free)",
            packageName = "com.tubitv",
            regions = setOf(WorldRegion.USA, WorldRegion.LATAM),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "peacock",
            title = "Peacock TV",
            packageName = "com.peacocktv.peacockandroid",
            regions = setOf(WorldRegion.USA),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),

        // --- Europe Favorites ---
        RegionalAppEntry(
            id = "arte",
            title = "ARTE TV (Free)",
            packageName = "tv.arte.plus7",
            regions = setOf(WorldRegion.EUROPE),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "skyshowtime",
            title = "SkyShowtime",
            packageName = "com.skyshowtime.skyshowtime.google",
            regions = setOf(WorldRegion.EUROPE),
            category = AppCategory.STREAMING,
            minSdk = 24,
            sourceType = AppSourceType.PLAY_STORE
        ),

        // --- Asia Favorites ---
        RegionalAppEntry(
            id = "crunchyroll",
            title = "Crunchyroll",
            packageName = "com.crunchyroll.crunchyroid",
            regions = setOf(WorldRegion.ASIA, WorldRegion.USA, WorldRegion.EUROPE, WorldRegion.LATAM, WorldRegion.ARGENTINA),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "viki",
            title = "Rakuten Viki",
            packageName = "com.viki.android",
            regions = setOf(WorldRegion.ASIA, WorldRegion.USA, WorldRegion.LATAM, WorldRegion.ARGENTINA),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        ),
        RegionalAppEntry(
            id = "abema",
            title = "ABEMA",
            packageName = "tv.abema",
            regions = setOf(WorldRegion.ASIA),
            category = AppCategory.STREAMING,
            minSdk = 21,
            sourceType = AppSourceType.PLAY_STORE
        )
    )
}

