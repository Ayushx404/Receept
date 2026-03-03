package com.receiptwarranty.app.data

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.receiptwarranty.app.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

enum class IconPackStyle {
    LUCIDE,
    PHOSPHOR_DUOTONE,
    MATERIAL_TWOTONE
}

data class AppearanceSettings(
    val useDynamicColor: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColorHex: String = "#0A84FF",
    val useAmoledBlack: Boolean = false,
    val iconStyle: IconPackStyle = IconPackStyle.LUCIDE,
    val currencySymbol: String = "₹",
    val isDeveloperModeEnabled: Boolean = false
)

private const val LEGACY_PREFS_NAME = "appearance_preferences"
private const val DATASTORE_NAME = "appearance_preferences_ds"

private val Context.appearanceDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, LEGACY_PREFS_NAME)) }
)

@Singleton
class AppearancePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settings: StateFlow<AppearanceSettings> = context.appearanceDataStore.data
        .map { prefs ->
            AppearanceSettings(
                useDynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
                themeMode = parseThemeMode(prefs[KEY_THEME_MODE]),
                primaryColorHex = prefs[KEY_PRIMARY_COLOR] ?: "#0A84FF",
                useAmoledBlack = prefs[KEY_AMOLED_BLACK] ?: false,
                iconStyle = parseIconStyle(prefs[KEY_ICON_STYLE]),
                currencySymbol = prefs[KEY_CURRENCY_SYMBOL] ?: "₹",
                isDeveloperModeEnabled = prefs[KEY_DEVELOPER_MODE] ?: false
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppearanceSettings()
        )

    fun setUseDynamicColor(enabled: Boolean) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_DYNAMIC_COLOR] = enabled
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_THEME_MODE] = mode.toString()
            }
        }
    }

    fun setPrimaryColor(hex: String) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_PRIMARY_COLOR] = hex
            }
        }
    }

    fun setUseAmoledBlack(enabled: Boolean) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_AMOLED_BLACK] = enabled
            }
        }
    }

    fun setIconStyle(style: IconPackStyle) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_ICON_STYLE] = style.toString()
            }
        }
    }

    fun setCurrencySymbol(symbol: String) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_CURRENCY_SYMBOL] = symbol
            }
        }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        scope.launch {
            context.appearanceDataStore.edit { prefs ->
                prefs[KEY_DEVELOPER_MODE] = enabled
            }
        }
    }

    private fun parseThemeMode(rawValue: String?): ThemeMode {
        return when (rawValue) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    private fun parseIconStyle(rawValue: String?): IconPackStyle {
        return when (rawValue) {
            "PHOSPHOR_DUOTONE" -> IconPackStyle.PHOSPHOR_DUOTONE
            "MATERIAL_TWOTONE" -> IconPackStyle.MATERIAL_TWOTONE
            else -> IconPackStyle.LUCIDE
        }
    }

    companion object {
        private val KEY_DYNAMIC_COLOR: Preferences.Key<Boolean> = booleanPreferencesKey("dynamic_color")
        private val KEY_THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        private val KEY_PRIMARY_COLOR: Preferences.Key<String> = stringPreferencesKey("primary_color")
        private val KEY_AMOLED_BLACK: Preferences.Key<Boolean> = booleanPreferencesKey("amoled_black")
        private val KEY_ICON_STYLE: Preferences.Key<String> = stringPreferencesKey("icon_style")
        private val KEY_CURRENCY_SYMBOL: Preferences.Key<String> = stringPreferencesKey("currency_symbol")
        private val KEY_DEVELOPER_MODE: Preferences.Key<Boolean> = booleanPreferencesKey("developer_mode")
    }
}
