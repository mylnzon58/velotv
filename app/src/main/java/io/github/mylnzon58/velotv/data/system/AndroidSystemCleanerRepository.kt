package io.github.mylnzon58.velotv.data.system

import android.app.ActivityManager
import android.content.Context
import io.github.mylnzon58.velotv.data.apps.BoundedIconCache
import io.github.mylnzon58.velotv.data.wallpaper.OpenWallpaperRepository
import io.github.mylnzon58.velotv.domain.model.QuickCleanResult
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.repository.SystemCleanerRepository
import io.github.mylnzon58.velotv.domain.usecase.SystemCleanPolicyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Honest, non-root Android TV quick cleaner.
 * - Uses standard ActivityManager.killBackgroundProcesses() on safe non-critical apps only.
 * - Trims VeloTV's in-memory caches and stale temporary files.
 * - Measures real ActivityManager.MemoryInfo.availMem before and after (never fabricated).
 */
class AndroidSystemCleanerRepository(
    private val context: Context,
    private val policy: SystemCleanPolicyUseCase,
    private val iconCache: BoundedIconCache,
    private val wallpaperRepository: OpenWallpaperRepository
) : SystemCleanerRepository {

    override suspend fun runQuickClean(installedApps: List<TvApp>): QuickCleanResult = withContext(Dispatchers.IO) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val beforeMb = readAvailableRamMb(am)

        val targets = policy.filterSafeKillTargets(installedApps, context.packageName)
        var killed = 0
        for (pkg in targets) {
            runCatching {
                am.killBackgroundProcesses(pkg)
                killed++
            }
        }

        iconCache.clear()
        wallpaperRepository.trimMemory()
        val clearedBytes = clearStaleTempFiles(context.cacheDir)
        System.gc()

        // Allow ActivityManager/LMK a short window to release pages before reading real post-clean RAM
        delay(280L)
        val afterMb = readAvailableRamMb(am)

        QuickCleanResult(
            ramBeforeMb = beforeMb,
            ramAfterMb = afterMb,
            reclaimedMb = (afterMb - beforeMb).coerceAtLeast(0L),
            processedPackages = killed,
            clearedCacheKb = clearedBytes / 1024L
        )
    }

    private fun readAvailableRamMb(am: ActivityManager): Long {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.availMem / (1024L * 1024L)
    }

    private fun clearStaleTempFiles(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var freed = 0L
        dir.listFiles()?.forEach { f ->
            if (f.isFile && (f.name.endsWith(".tmp") || f.name.endsWith(".bak"))) {
                val len = f.length()
                if (f.delete()) freed += len
            } else if (f.isDirectory && f.name != "velotv_wallpapers") {
                freed += clearStaleTempFiles(f)
            }
        }
        return freed
    }
}

