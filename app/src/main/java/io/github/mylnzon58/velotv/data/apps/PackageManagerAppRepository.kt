package io.github.mylnzon58.velotv.data.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import io.github.mylnzon58.velotv.domain.model.AppCategory
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.repository.AppRepository
import io.github.mylnzon58.velotv.domain.usecase.ClassifyAppUseCase
import io.github.mylnzon58.velotv.domain.usecase.WallpaperPolicyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class PackageManagerAppRepository(
    private val context: Context,
    private val classifier: ClassifyAppUseCase,
    val iconCache: BoundedIconCache
) : AppRepository {

    private val pm: PackageManager = context.packageManager
    private val accentColorCache = ConcurrentHashMap<String, Int>()

    fun getCachedBannerOrIcon(packageName: String): Pair<Bitmap, Boolean>? {
        iconCache.get("b:$packageName")?.let { return it to true }
        iconCache.get("i:$packageName")?.let { return it to false }
        return null
    }

    fun getAmbientColorForApp(packageName: String, policy: WallpaperPolicyUseCase, fallbackColor: Int): Int {
        accentColorCache[packageName]?.let { return it }
        val bmp = getCachedBannerOrIcon(packageName)?.first ?: return fallbackColor
        val w = bmp.width
        val h = bmp.height
        if (w <= 0 || h <= 0) return fallbackColor
        val cols = 8
        val rows = 6
        val samples = IntArray(cols * rows)
        var idx = 0
        for (ry in 0 until rows) {
            val py = ((ry + 0.5f) * h / rows).toInt().coerceIn(0, h - 1)
            for (cx in 0 until cols) {
                val px = ((cx + 0.5f) * w / cols).toInt().coerceIn(0, w - 1)
                samples[idx++] = bmp.getPixel(px, py)
            }
        }
        val color = policy.extractAmbientAccentColor(samples, fallbackColor)
        accentColorCache[packageName] = color
        return color
    }

    override suspend fun loadInstalledApps(includeAlternativeLaunchers: Boolean): List<TvApp> =
        withContext(Dispatchers.IO) {
            val selfPkg = context.packageName
            val discovered = LinkedHashMap<String, TvApp>()
            val homePackages = queryAlternativeHomePackages(selfPkg)

            val tvIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            for (info in pm.queryIntentActivities(tvIntent, 0)) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg == selfPkg || pkg in discovered) continue
                val label = info.loadLabel(pm)?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: pkg
                val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                discovered[pkg] = TvApp(
                    packageName = pkg,
                    label = label,
                    defaultLabel = label,
                    categoryId = classifier.classify(pkg, label, isSystem).id,
                    isLeanback = true,
                    isSystemApp = isSystem,
                    isHomeLauncher = pkg in homePackages
                )
            }

            val mobileIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            for (info in pm.queryIntentActivities(mobileIntent, 0)) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg == selfPkg || pkg in discovered) continue
                val label = info.loadLabel(pm)?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: pkg
                val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                discovered[pkg] = TvApp(
                    packageName = pkg,
                    label = label,
                    defaultLabel = label,
                    categoryId = classifier.classify(pkg, label, isSystem).id,
                    isLeanback = false,
                    isSystemApp = isSystem,
                    isHomeLauncher = pkg in homePackages
                )
            }

            if (includeAlternativeLaunchers) {
                for (launcher in loadAlternativeLaunchers()) {
                    if (launcher.packageName !in discovered) {
                        discovered[launcher.packageName] = launcher
                    }
                }
            }

            discovered.values.sortedBy { it.label.lowercase() }
        }

    override suspend fun loadAlternativeLaunchers(): List<TvApp> = withContext(Dispatchers.IO) {
        val selfPkg = context.packageName
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val seen = LinkedHashSet<String>()
        val result = mutableListOf<TvApp>()

        for (info in pm.queryIntentActivities(homeIntent, 0)) {
            val ai = info.activityInfo ?: continue
            val pkg = ai.packageName ?: continue
            if (pkg == selfPkg || pkg in IGNORED_HOME_PACKAGES || !seen.add(pkg)) continue
            val isSystem = (ai.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val rawLabel = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            val friendlyLabel = when {
                pkg == "com.google.android.tvlauncher" && rawLabel.isBlank() -> "Android TV Home"
                rawLabel.isNotBlank() -> rawLabel
                else -> pkg
            }
            result += TvApp(
                packageName = pkg,
                label = friendlyLabel,
                defaultLabel = friendlyLabel,
                categoryId = AppCategory.TOOLS.id,
                isLeanback = true,
                isSystemApp = isSystem,
                isHomeLauncher = true
            )
        }
        result.sortedBy { it.label.lowercase() }
    }

    private fun queryAlternativeHomePackages(selfPkg: String): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(homeIntent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .filterNot { it == selfPkg || it in IGNORED_HOME_PACKAGES }
            .toSet()
    }

    suspend fun loadBannerOrIcon(packageName: String): Pair<Bitmap?, Boolean> = withContext(Dispatchers.IO) {
        val bannerKey = "b:$packageName"
        iconCache.get(bannerKey)?.let { return@withContext it to true }
        val iconKey = "i:$packageName"
        iconCache.get(iconKey)?.let { return@withContext it to false }

        try {
            val activityBanner = pm.getLeanbackLaunchIntentForPackage(packageName)
                ?.let { pm.resolveActivity(it, 0)?.activityInfo?.loadBanner(pm) }
            val bannerDrawable = activityBanner ?: pm.getApplicationBanner(packageName)
            if (bannerDrawable != null) {
                val bmp = iconCache.drawableToBoundedBitmap(bannerDrawable, 320, 180)
                iconCache.put(bannerKey, bmp)
                return@withContext bmp to true
            }
            val iconDrawable = pm.getApplicationIcon(packageName)
            val bmp = iconCache.drawableToBoundedBitmap(iconDrawable, 144, 144)
            iconCache.put(iconKey, bmp)
            bmp to false
        } catch (_: Exception) {
            null to false
        }
    }

    override fun launchApp(packageName: String): Boolean {
        val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
            ?: pm.getLaunchIntentForPackage(packageName)
            ?: return launchHomeLauncher(packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    override fun launchHomeLauncher(packageName: String): Boolean {
        val query = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            setPackage(packageName)
        }
        val resolved = pm.queryIntentActivities(query, 0).firstOrNull()?.activityInfo ?: return false
        val launch = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            component = ComponentName(resolved.packageName, resolved.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        return runCatching {
            context.startActivity(launch)
            true
        }.getOrDefault(false)
    }

    override fun openAppInfo(packageName: String): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    override fun requestUninstall(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    override fun openPlayStoreListing(packageName: String): Boolean {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(marketIntent)
            true
        }.getOrElse {
            launchApp("com.android.vending")
        }
    }

    companion object {
        private val IGNORED_HOME_PACKAGES = setOf(
            "com.android.tv.settings",
            "com.android.settings",
            "com.android.boot.fallbackhome"
        )
    }
}
