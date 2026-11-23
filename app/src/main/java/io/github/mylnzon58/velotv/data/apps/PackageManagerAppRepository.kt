package io.github.mylnzon58.velotv.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import io.github.mylnzon58.velotv.domain.model.TvApp
import io.github.mylnzon58.velotv.domain.repository.AppRepository
import io.github.mylnzon58.velotv.domain.usecase.ClassifyAppUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackageManagerAppRepository(
    private val context: Context,
    private val classifier: ClassifyAppUseCase,
    val iconCache: BoundedIconCache
) : AppRepository {

    private val pm: PackageManager = context.packageManager

    override suspend fun loadInstalledApps(): List<TvApp> = withContext(Dispatchers.IO) {
        val selfPkg = context.packageName
        val discovered = LinkedHashMap<String, TvApp>()

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
                isSystemApp = isSystem
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
                isSystemApp = isSystem
            )
        }

        discovered.values.sortedBy { it.label.lowercase() }
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
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
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
}
