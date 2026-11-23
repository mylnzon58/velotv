package io.github.mylnzon58.velotv.data.system

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.view.WindowManager
import io.github.mylnzon58.velotv.domain.model.SystemDiagnostics
import io.github.mylnzon58.velotv.domain.repository.SystemDiagnosticsRepository
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class AndroidSystemDiagnosticsRepository(private val context: Context) : SystemDiagnosticsRepository {

    override fun readDiagnostics(): SystemDiagnostics {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availRamMb = (memInfo.availMem / (1024L * 1024L)).coerceIn(0L, totalRamMb)
        val usedPct = (((totalRamMb - availRamMb) * 100L) / totalRamMb).toInt().coerceIn(0, 100)

        val dataStat = StatFs(Environment.getDataDirectory().path)
        val dataTotalGb = (dataStat.totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()
        val dataFreeGb = (dataStat.availableBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()

        val sysStat = StatFs(Environment.getRootDirectory().path)
        val sysTotalMb = sysStat.totalBytes / (1024L * 1024L)
        val sysFreeMb = sysStat.availableBytes / (1024L * 1024L)

        val (netLabel, connected) = readNetworkState()
        val (resLabel, hz) = readDisplayInfo()

        val cr = context.contentResolver
        val adbOn = Settings.Global.getInt(cr, Settings.Global.ADB_ENABLED, 0) == 1
        val devOn = Settings.Global.getInt(cr, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        @Suppress("DEPRECATION")
        val unknownSources = Settings.Secure.getInt(cr, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1

        val animWin = Settings.Global.getString(cr, Settings.Global.WINDOW_ANIMATION_SCALE) ?: "1.0"
        val animTrans = Settings.Global.getString(cr, Settings.Global.TRANSITION_ANIMATION_SCALE) ?: "1.0"
        val animDur = Settings.Global.getString(cr, Settings.Global.ANIMATOR_DURATION_SCALE) ?: "1.0"

        return SystemDiagnostics(
            manufacturer = Build.MANUFACTURER ?: "-",
            model = Build.MODEL ?: "-",
            board = readSysProp("persist.sys.product.board").ifBlank { Build.BOARD ?: "-" },
            platform = readSysProp("ro.board.platform").ifBlank { Build.HARDWARE ?: "-" },
            androidVersion = Build.VERSION.RELEASE ?: "-",
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "-",
            buildId = Build.DISPLAY ?: Build.ID ?: "-",
            kernelVersion = readKernelShort(),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm",
            webviewVersion = readWebViewVersion(),
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            usedRamPercent = usedPct,
            dataFreeGb = dataFreeGb,
            dataTotalGb = dataTotalGb,
            systemFreeMb = sysFreeMb,
            systemTotalMb = sysTotalMb,
            displayResolution = resLabel,
            refreshRateHz = hz,
            networkLabel = netLabel,
            isConnected = connected,
            localIp = readLocalIpv4(),
            uptimeLabel = formatUptime(SystemClock.elapsedRealtime()),
            adbEnabled = adbOn,
            developmentEnabled = devOn,
            unknownSourcesEnabled = unknownSources,
            bootloaderLocked = readSysProp("ro.boot.flash.locked").ifBlank { "-" },
            trebleEnabled = readSysProp("ro.treble.enabled").ifBlank { "-" },
            animWindowScale = "${animWin}x",
            animTransitionScale = "${animTrans}x",
            animDurationScale = "${animDur}x"
        )
    }

    private fun readNetworkState(): Pair<String, Boolean> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "Sin red" to false
        val active = cm.activeNetwork ?: return "Sin red" to false
        val caps = cm.getNetworkCapabilities(active) ?: return "Sin red" to false
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet" to true
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi" to true
            else -> "Red activa" to true
        }
    }

    @Suppress("DEPRECATION")
    private fun readDisplayInfo(): Pair<String, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return "1920x1080" to 60
        val display = wm.defaultDisplay ?: return "1920x1080" to 60
        val mode = display.mode
        val panelOverride = readSysProp("sys.display-size")
        val res = if (panelOverride.contains("x")) {
            "${mode.physicalWidth}x${mode.physicalHeight} (Panel $panelOverride)"
        } else {
            "${mode.physicalWidth}x${mode.physicalHeight}"
        }
        return res to mode.refreshRate.toInt().coerceAtLeast(30)
    }

    private fun readLocalIpv4(): String = runCatching {
        val ifaces = NetworkInterface.getNetworkInterfaces() ?: return "-"
        for (iface in ifaces.toList()) {
            if (!iface.isUp || iface.isLoopback) continue
            for (addr in iface.inetAddresses.toList()) {
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    return addr.hostAddress ?: "-"
                }
            }
        }
        "-"
    }.getOrDefault("-")

    private fun readKernelShort(): String = runCatching {
        val raw = File("/proc/version").readText().trim()
        Regex("""Linux version\s+([^\s]+)""").find(raw)?.groupValues?.getOrNull(1)
            ?: System.getProperty("os.version")
            ?: "-"
    }.getOrDefault(System.getProperty("os.version") ?: "-")

    private fun readWebViewVersion(): String = runCatching {
        val pm = context.packageManager
        val pkgs = listOf("com.google.android.webview", "com.android.webview", "com.android.chrome")
        for (p in pkgs) {
            val ver = runCatching { pm.getPackageInfo(p, 0).versionName }.getOrNull()
            if (!ver.isNullOrBlank()) return ver
        }
        "-"
    }.getOrDefault("-")

    private fun readSysProp(key: String): String = runCatching {
        val proc = Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", key))
        proc.inputStream.bufferedReader().use { it.readLine()?.trim().orEmpty() }
    }.getOrDefault("")

    private fun formatUptime(ms: Long): String {
        val totalMin = ms / 60_000L
        val days = totalMin / (60 * 24)
        val hours = (totalMin / 60) % 24
        val mins = totalMin % 60
        return when {
            days > 0 -> String.format(Locale.US, "%dd %dh %dm", days, hours, mins)
            hours > 0 -> String.format(Locale.US, "%dh %dm", hours, mins)
            else -> String.format(Locale.US, "%dm", mins)
        }
    }
}

