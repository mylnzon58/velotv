package io.github.mylnzon58.velotv.data.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.domain.model.NativeSettingShortcut
import io.github.mylnzon58.velotv.domain.model.SettingShortcutGroup
import io.github.mylnzon58.velotv.domain.repository.NativeSettingsRepository

/**
 * Resolves real native Android TV Settings, Developer Options, and OEM menus dynamically.
 * Only exposes shortcuts whose Activity actually resolves on this TV (zero dead buttons).
 */
class AndroidNativeSettingsRepository(private val context: Context) : NativeSettingsRepository {

    private data class Spec(
        val shortcut: NativeSettingShortcut,
        val intents: List<Intent>
    )

    private val specs: List<Spec> = listOf(
        // --- Developer & Engineering ---
        Spec(
            NativeSettingShortcut("dev_options", R.string.dev_native_options, R.string.dev_native_options_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.system.development.DevelopmentActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("unknown_sources", R.string.dev_unknown_sources, R.string.dev_unknown_sources_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES"),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.apps.specialaccess.ManageExternalSourcesActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("about_device", R.string.dev_about_device, R.string.dev_about_device_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_DEVICE_INFO_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.about.AboutActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("device_status", R.string.dev_device_status, R.string.dev_device_status_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.about.StatusActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("apps_manage", R.string.dev_apps_manage, R.string.dev_apps_manage_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_APPLICATION_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.apps.AppsActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("storage_reset", R.string.dev_storage, R.string.dev_storage_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.StorageResetActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("privacy", R.string.dev_privacy, R.string.dev_privacy_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_PRIVACY_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.privacy.PrivacyActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("security", R.string.dev_security, R.string.dev_security_sub, SettingShortcutGroup.DEVELOPER),
            listOf(
                Intent(Settings.ACTION_SECURITY_SETTINGS),
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.system.SecurityActivity"))
            )
        ),
        Spec(
            NativeSettingShortcut("location", R.string.dev_location, R.string.dev_location_sub, SettingShortcutGroup.DEVELOPER),
            listOf(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        ),

        // --- System & Connectivity ---
        Spec(
            NativeSettingShortcut("network", R.string.sys_network, R.string.sys_network_sub, SettingShortcutGroup.SYSTEM),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.connectivity.NetworkActivity")),
                Intent(Settings.ACTION_WIFI_SETTINGS)
            )
        ),
        Spec(
            NativeSettingShortcut("bluetooth", R.string.sys_bluetooth, R.string.sys_bluetooth_sub, SettingShortcutGroup.SYSTEM),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.accessories.AddAccessoryActivity")),
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        ),
        Spec(
            NativeSettingShortcut("sound", R.string.sys_sound, R.string.sys_sound_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent(Settings.ACTION_SOUND_SETTINGS))
        ),
        Spec(
            NativeSettingShortcut("language", R.string.sys_language, R.string.sys_language_sub, SettingShortcutGroup.SYSTEM),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.system.LanguageActivity")),
                Intent(Settings.ACTION_LOCALE_SETTINGS)
            )
        ),
        Spec(
            NativeSettingShortcut("keyboard", R.string.sys_keyboard, R.string.sys_keyboard_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        ),
        Spec(
            NativeSettingShortcut("datetime", R.string.sys_datetime, R.string.sys_datetime_sub, SettingShortcutGroup.SYSTEM),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.system.DateTimeActivity")),
                Intent(Settings.ACTION_DATE_SETTINGS)
            )
        ),
        Spec(
            NativeSettingShortcut("captions", R.string.sys_captions, R.string.sys_captions_sub, SettingShortcutGroup.SYSTEM),
            listOf(
                Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.system.CaptionSetupActivity")),
                Intent(Settings.ACTION_CAPTIONING_SETTINGS)
            )
        ),
        Spec(
            NativeSettingShortcut("accessibility", R.string.sys_accessibility, R.string.sys_accessibility_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        ),
        Spec(
            NativeSettingShortcut("accounts", R.string.sys_accounts, R.string.sys_accounts_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent(Settings.ACTION_SYNC_SETTINGS))
        ),
        Spec(
            NativeSettingShortcut("device_name", R.string.sys_device_name, R.string.sys_device_name_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.name.DeviceNameSettingsActivity")))
        ),
        Spec(
            NativeSettingShortcut("all_settings", R.string.sys_all_settings, R.string.sys_all_settings_sub, SettingShortcutGroup.SYSTEM),
            listOf(Intent(Settings.ACTION_SETTINGS))
        ),

        // --- Hardware / OEM Menus (Only shown when present on the TV) ---
        Spec(
            NativeSettingShortcut("mtk_live_tv", R.string.hw_live_tv_settings, R.string.hw_live_tv_settings_sub, SettingShortcutGroup.HARDWARE),
            listOf(Intent().setComponent(ComponentName("com.mediatek.wwtv.setting", "com.mediatek.wwtv.setting.LiveTvSetting")))
        ),
        Spec(
            NativeSettingShortcut("skyworth_factory", R.string.hw_factory_menu, R.string.hw_factory_menu_sub, SettingShortcutGroup.HARDWARE, requiresCaution = true),
            listOf(Intent("skyworth.intent.action.FACTORY"))
        )
    )

    override fun getAvailableShortcuts(): List<NativeSettingShortcut> {
        val pm = context.packageManager
        return specs.mapNotNull { spec ->
            val available = spec.intents.any { intent ->
                runCatching { pm.resolveActivity(intent, 0) != null }.getOrDefault(false)
            }
            if (available) spec.shortcut else null
        }
    }

    override fun openShortcut(shortcutId: String): Boolean {
        val spec = specs.firstOrNull { it.shortcut.id == shortcutId } ?: return false
        for (template in spec.intents) {
            val intent = Intent(template).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val ok = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)
            if (ok) return true
        }
        return false
    }
}

