package io.github.mylnzon58.velotv.domain

import io.github.mylnzon58.velotv.data.region.StaticRegionalCatalogRepository
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.AppUsageStat
import io.github.mylnzon58.velotv.domain.model.CustomCategory
import io.github.mylnzon58.velotv.domain.model.HomeRow
import io.github.mylnzon58.velotv.domain.model.LauncherSettings
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.TvInputType
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.domain.model.WallpaperSource
import io.github.mylnzon58.velotv.domain.model.WorldRegion
import io.github.mylnzon58.velotv.domain.usecase.BuildDonationPlanUseCase
import io.github.mylnzon58.velotv.domain.usecase.BuildHomeRowsUseCase
import io.github.mylnzon58.velotv.domain.usecase.ClassifyAppUseCase
import io.github.mylnzon58.velotv.domain.usecase.FilterRegionalHubUseCase
import io.github.mylnzon58.velotv.domain.usecase.NightComfortPolicyUseCase
import io.github.mylnzon58.velotv.domain.usecase.ParentalScheduleUseCase
import io.github.mylnzon58.velotv.domain.usecase.PinSecurityUseCase
import io.github.mylnzon58.velotv.domain.usecase.SearchLauncherUseCase
import io.github.mylnzon58.velotv.domain.usecase.SmartShelfUseCase
import io.github.mylnzon58.velotv.domain.usecase.SystemCleanPolicyUseCase
import io.github.mylnzon58.velotv.domain.usecase.WallpaperPolicyUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreUseCasesTest {

    @Test
    fun classifyApp_mapsStreamingMediaGamesAndToolsAccurately() {
        val uc = ClassifyAppUseCase()
        assertEquals(AppCategory.STREAMING, uc.classify("com.netflix.ninja", "Netflix", false))
        assertEquals(AppCategory.STREAMING, uc.classify("org.smarttube.stable", "SmartTube", false))
        assertEquals(AppCategory.STREAMING, uc.classify("com.telecom.flow", "Flow", false))
        assertEquals(AppCategory.MEDIA_WEB, uc.classify("com.phlox.tvwebbrowser", "TV Bro", false))
        assertEquals(AppCategory.MEDIA_WEB, uc.classify("org.videolan.vlc", "VLC", false))
        assertEquals(AppCategory.GAMES, uc.classify("com.retroarch", "RetroArch", false))
        assertEquals(AppCategory.TOOLS, uc.classify("org.localsend.localsend_app", "LocalSend", false))
    }

    @Test
    fun buildHomeRows_appliesCustomLabelsCustomCategoriesRowOrderAndHiding() {
        val uc = BuildHomeRowsUseCase()
        val apps = listOf(
            TvApp("com.netflix.ninja", "Netflix", "Netflix", AppCategory.STREAMING.id, true, false),
            TvApp("org.smarttube.stable", "SmartTube", "SmartTube", AppCategory.STREAMING.id, true, false),
            TvApp("com.phlox.tvwebbrowser", "TV Bro", "TV Bro", AppCategory.MEDIA_WEB.id, true, false),
            TvApp("com.hidden.app", "Hidden", "Hidden", AppCategory.TOOLS.id, false, false)
        )
        val inputs = listOf(
            TvInputSource("hdmi1", "HDMI 1", "HDMI 1", TvInputType.HDMI, true, true)
        )
        val customCat = CustomCategory("custom_cine", "Cine")
        val settings = LauncherSettings(
            favoritePackages = listOf("org.smarttube.stable", "com.netflix.ninja"),
            hiddenPackages = setOf("com.hidden.app"),
            customAppLabels = mapOf("org.smarttube.stable" to "YouTube Sin Anuncios"),
            customInputLabels = mapOf("hdmi1" to "Consola"),
            customCategories = listOf(customCat),
            categoryOverrides = mapOf("com.netflix.ninja" to customCat.id),
            rowOrder = listOf(customCat.id, AppCategory.FAVORITES.id, AppCategory.TV_INPUTS.id, AppCategory.ALL_APPS.id)
        )

        val rows = uc.build(apps, inputs, settings)
        val firstRow = rows.first() as HomeRow.AppsRow
        assertEquals("custom_cine", firstRow.rowId)
        assertEquals("Cine", firstRow.customTitle)
        assertEquals(listOf("com.netflix.ninja"), firstRow.apps.map { it.packageName })

        val favRow = rows[1] as HomeRow.AppsRow
        assertEquals("YouTube Sin Anuncios", favRow.apps.first().label)

        val inputsRow = rows[2] as HomeRow.InputsRow
        assertEquals("Consola", inputsRow.inputs.first().label)

        val allRow = rows.last() as HomeRow.AppsRow
        assertFalse(allRow.apps.any { it.packageName == "com.hidden.app" })
    }

    @Test
    fun searchLauncher_matchesAppLabelsPackagesAndInputs() {
        val uc = SearchLauncherUseCase()
        val apps = listOf(
            TvApp("com.phlox.tvwebbrowser", "Navegador", "TV Bro", AppCategory.MEDIA_WEB.id, true, false),
            TvApp("com.netflix.ninja", "Netflix", "Netflix", AppCategory.STREAMING.id, true, false)
        )
        val inputs = listOf(
            TvInputSource("hdmi1", "PlayStation", "HDMI 1", TvInputType.HDMI, true, true)
        )

        val byDefaultLabel = uc.search("tv bro", apps, inputs)
        assertEquals(1, byDefaultLabel.apps.size)
        assertEquals("com.phlox.tvwebbrowser", byDefaultLabel.apps.first().packageName)

        val byInput = uc.search("hdmi", apps, inputs)
        assertEquals(1, byInput.inputs.size)
        assertEquals("PlayStation", byInput.inputs.first().label)
    }

    @Test
    fun parentalSchedule_handlesOvernightAndDaytimeWindows() {
        val uc = ParentalScheduleUseCase()
        val overnight = LauncherSettings(parentalScheduleEnabled = true, parentalStartHour = 22, parentalEndHour = 7)
        assertTrue(uc.isRestrictedNow(overnight, 23))
        assertTrue(uc.isRestrictedNow(overnight, 3))
        assertFalse(uc.isRestrictedNow(overnight, 14))
        assertFalse(uc.isRestrictedNow(overnight, 7))

        val daytime = LauncherSettings(parentalScheduleEnabled = true, parentalStartHour = 8, parentalEndHour = 13)
        assertTrue(uc.isRestrictedNow(daytime, 10))
        assertFalse(uc.isRestrictedNow(daytime, 15))
    }

    @Test
    fun buildDonationPlan_prioritizesMercadoPagoForArgentinaAndGitHubForGlobal() {
        val uc = BuildDonationPlanUseCase()

        val arChannels = uc.buildForCountry("AR")
        assertEquals("mercadopago_ar", arChannels.first().id)
        assertEquals(BuildDonationPlanUseCase.URL_MERCADOPAGO_AR, arChannels.first().url)
        assertTrue(arChannels.first().isPrimaryForRegion)

        val usChannels = uc.buildForCountry("US")
        assertEquals("github_sponsors", usChannels.first().id)
        assertEquals(BuildDonationPlanUseCase.URL_GITHUB_SPONSORS, usChannels.first().url)
        assertTrue(usChannels.first().isPrimaryForRegion)

        val jpChannels = uc.buildForCountry("JP")
        assertEquals("github_sponsors", jpChannels.first().id)
    }

    @Test
    fun filterRegionalHub_handlesSdkCompatibilityForSkyworthAndroid8() {
        val catalog = StaticRegionalCatalogRepository().getCatalog()
        val uc = FilterRegionalHubUseCase()

        val arApi26 = uc.filter(
            catalog = catalog,
            region = WorldRegion.ARGENTINA,
            installedPackages = setOf("com.netflix.ninja", "com.phlox.tvwebbrowser"),
            deviceSdkInt = 26
        )

        val tvBro = arApi26.first { it.id == "tvbro" }
        assertTrue(tvBro.isCompatibleWithDevice)
        assertTrue(tvBro.isInstalled)

        val brave = arApi26.first { it.id == "brave" }
        assertFalse(brave.isCompatibleWithDevice)

        val flow = arApi26.first { it.id == "flow" }
        assertTrue(flow.isCompatibleWithDevice)
        assertFalse(flow.isInstalled)
    }

    @Test
    fun pinSecurity_saltsHashesAndVerifiesCorrectly() {
        val uc = PinSecurityUseCase()
        val salt1 = uc.generateSalt()
        val salt2 = uc.generateSalt()
        assertNotEquals(salt1, salt2)

        val hash = uc.hashPin("1234", salt1)
        assertNotEquals("1234", hash)
        assertTrue(uc.verifyPin("1234", hash, salt1))
        assertFalse(uc.verifyPin("9999", hash, salt1))
        assertFalse(uc.verifyPin("1234", hash, salt2))
    }

    @Test
    fun wallpaperPolicy_enforcesHttpsAllowlistAndHandlesRotationAndPinning() {
        val policy = WallpaperPolicyUseCase()

        assertTrue(policy.isAllowedWallpaperUrl("https://commons.wikimedia.org/w/api.php?action=query"))
        assertTrue(policy.isAllowedWallpaperUrl("https://thumb.wikimedia.org/wikipedia/commons/a.jpg"))
        assertTrue(policy.isAllowedWallpaperUrl("https://picsum.photos/id/10/1280/720.webp"))
        assertFalse(policy.isAllowedWallpaperUrl("http://picsum.photos/id/10/1280/720.webp"))
        assertFalse(policy.isAllowedWallpaperUrl("https://evil.example.com/tracker.png"))
        assertFalse(policy.isAllowedWallpaperUrl("https://192.168.1.1/admin"))
        assertFalse(policy.isAllowedWallpaperUrl("file:///etc/passwd"))

        val pool = listOf(
            WallpaperItem("a", "A", "Author", "CC0", WallpaperSource.BUNDLED_OFFLINE, assetPath = "a.webp"),
            WallpaperItem("b", "B", "Author", "CC0", WallpaperSource.BUNDLED_OFFLINE, assetPath = "b.webp"),
            WallpaperItem("c", "C", "Author", "CC0", WallpaperSource.BUNDLED_OFFLINE, assetPath = "c.webp")
        )
        assertEquals("a", policy.selectNextWallpaper(pool, null, null)?.id)
        assertEquals("b", policy.selectNextWallpaper(pool, "a", null)?.id)
        assertEquals("a", policy.selectNextWallpaper(pool, "c", null)?.id)
        assertEquals("b", policy.selectNextWallpaper(pool, "a", "b")?.id)

        val crimson = 0xFFE50914.toInt()
        val extracted = policy.extractAmbientAccentColor(intArrayOf(crimson, crimson, 0xFF000000.toInt(), 0xFFFFFFFF.toInt()), 0xFF111111.toInt())
        assertEquals(crimson, extracted)
    }

    @Test
    fun systemCleanPolicy_protectsCriticalTvAndOsPackages() {
        val policy = SystemCleanPolicyUseCase()
        val apps = listOf(
            TvApp("io.github.mylnzon58.velotv", "VeloTV", "VeloTV", AppCategory.TOOLS.id, true, false),
            TvApp("com.mediatek.wwtv.tvcenter", "Live TV", "Live TV", AppCategory.TOOLS.id, true, true),
            TvApp("com.android.tv.settings", "Settings", "Settings", AppCategory.TOOLS.id, true, true),
            TvApp("com.google.android.gms", "GMS", "GMS", AppCategory.TOOLS.id, false, true),
            TvApp("com.netflix.ninja", "Netflix", "Netflix", AppCategory.STREAMING.id, true, true),
            TvApp("com.stremio.one", "Stremio", "Stremio", AppCategory.MEDIA_WEB.id, true, false),
            TvApp("com.phlox.tvwebbrowser", "TV Bro", "TV Bro", AppCategory.MEDIA_WEB.id, true, false)
        )

        val targets = policy.filterSafeKillTargets(apps, "io.github.mylnzon58.velotv")
        assertEquals(listOf("com.netflix.ninja", "com.stremio.one", "com.phlox.tvwebbrowser"), targets)
    }

    @Test
    fun smartShelf_ranksByRecencyAndTimeOfDayAffinityOffline() {
        val uc = SmartShelfUseCase()
        val now = 1_700_000_000_000L
        val apps = listOf(
            TvApp("com.netflix.ninja", "Netflix", "Netflix", AppCategory.STREAMING.id, true, false),
            TvApp("org.smarttube.stable", "SmartTube", "SmartTube", AppCategory.STREAMING.id, true, false),
            TvApp("com.spotify.tv.android", "Spotify", "Spotify", AppCategory.STREAMING.id, true, false)
        )
        val stats = mapOf(
            "com.spotify.tv.android" to AppUsageStat("com.spotify.tv.android", now - 3_600_000L, 10, morningLaunches = 9, afternoonLaunches = 1, eveningLaunches = 0),
            "com.netflix.ninja" to AppUsageStat("com.netflix.ninja", now - 3_600_000L, 10, morningLaunches = 0, afternoonLaunches = 1, eveningLaunches = 9)
        )

        val morningRanked = uc.buildSmartApps(apps, stats, currentHour24 = 8, nowMs = now)
        assertEquals("com.spotify.tv.android", morningRanked.first().packageName)

        val eveningRanked = uc.buildSmartApps(apps, stats, currentHour24 = 22, nowMs = now)
        assertEquals("com.netflix.ninja", eveningRanked.first().packageName)
        assertEquals(2, eveningRanked.size)
    }

    @Test
    fun nightComfortPolicy_supportsManualToggleAndOvernightSchedule() {
        val uc = NightComfortPolicyUseCase()
        val autoOnly = LauncherSettings(nightModeEnabled = false, nightModeAutoSchedule = true)
        assertTrue(uc.isNightComfortActive(autoOnly, 23))
        assertTrue(uc.isNightComfortActive(autoOnly, 3))
        assertFalse(uc.isNightComfortActive(autoOnly, 14))

        val manualOn = LauncherSettings(nightModeEnabled = true, nightModeAutoSchedule = false)
        assertTrue(uc.isNightComfortActive(manualOn, 14))

        val disabled = LauncherSettings(nightModeEnabled = false, nightModeAutoSchedule = false)
        assertFalse(uc.isNightComfortActive(disabled, 23))
    }

    @Test
    fun systemCleanPolicy_neverKillsAlternativeHomeLaunchers() {
        val policy = SystemCleanPolicyUseCase()
        val apps = listOf(
            TvApp("com.spocky.projengmenu", "Projectivy", "Projectivy", AppCategory.TOOLS.id, true, false, isHomeLauncher = true),
            TvApp("com.google.android.tvlauncher", "Android TV Home", "Android TV Home", AppCategory.TOOLS.id, true, true, isHomeLauncher = true),
            TvApp("com.stremio.one", "Stremio", "Stremio", AppCategory.MEDIA_WEB.id, true, false)
        )
        val targets = policy.filterSafeKillTargets(apps, "io.github.mylnzon58.velotv")
        assertEquals(listOf("com.stremio.one"), targets)
    }
}
