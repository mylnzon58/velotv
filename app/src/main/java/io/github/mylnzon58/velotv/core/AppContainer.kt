package io.github.mylnzon58.velotv.core

import android.content.Context
import io.github.mylnzon58.velotv.data.apps.BoundedIconCache
import io.github.mylnzon58.velotv.data.apps.PackageManagerAppRepository
import io.github.mylnzon58.velotv.data.inputs.AndroidTvInputRepository
import io.github.mylnzon58.velotv.data.prefs.LauncherPrefsRepository
import io.github.mylnzon58.velotv.data.region.StaticRegionalCatalogRepository
import io.github.mylnzon58.velotv.data.system.AndroidNativeSettingsRepository
import io.github.mylnzon58.velotv.data.system.AndroidSystemCleanerRepository
import io.github.mylnzon58.velotv.data.system.AndroidSystemDiagnosticsRepository
import io.github.mylnzon58.velotv.data.wallpaper.OpenWallpaperRepository
import io.github.mylnzon58.velotv.domain.repository.NativeSettingsRepository
import io.github.mylnzon58.velotv.domain.repository.PreferencesRepository
import io.github.mylnzon58.velotv.domain.repository.RegionalCatalogRepository
import io.github.mylnzon58.velotv.domain.repository.SystemCleanerRepository
import io.github.mylnzon58.velotv.domain.repository.SystemDiagnosticsRepository
import io.github.mylnzon58.velotv.domain.repository.TvInputRepository
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

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val classifyAppUseCase = ClassifyAppUseCase()
    val smartShelfUseCase = SmartShelfUseCase()
    val nightComfortPolicyUseCase = NightComfortPolicyUseCase()
    val buildHomeRowsUseCase = BuildHomeRowsUseCase(smartShelfUseCase)
    val searchLauncherUseCase = SearchLauncherUseCase()
    val parentalScheduleUseCase = ParentalScheduleUseCase()
    val filterRegionalHubUseCase = FilterRegionalHubUseCase()
    val buildDonationPlanUseCase = BuildDonationPlanUseCase()
    val pinSecurityUseCase = PinSecurityUseCase()
    val wallpaperPolicyUseCase = WallpaperPolicyUseCase()
    val systemCleanPolicyUseCase = SystemCleanPolicyUseCase()

    val iconCache = BoundedIconCache()
    val appRepository = PackageManagerAppRepository(appContext, classifyAppUseCase, iconCache)
    val tvInputRepository: TvInputRepository = AndroidTvInputRepository(appContext)
    val preferencesRepository: PreferencesRepository = LauncherPrefsRepository(appContext)
    val systemDiagnosticsRepository: SystemDiagnosticsRepository = AndroidSystemDiagnosticsRepository(appContext)
    val nativeSettingsRepository: NativeSettingsRepository = AndroidNativeSettingsRepository(appContext)
    val regionalCatalogRepository: RegionalCatalogRepository = StaticRegionalCatalogRepository()
    val wallpaperRepository = OpenWallpaperRepository(appContext, wallpaperPolicyUseCase)
    val systemCleanerRepository: SystemCleanerRepository =
        AndroidSystemCleanerRepository(appContext, systemCleanPolicyUseCase, iconCache, wallpaperRepository)
}
