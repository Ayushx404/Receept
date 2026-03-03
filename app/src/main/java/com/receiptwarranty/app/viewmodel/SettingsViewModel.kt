package com.receiptwarranty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.AppearancePreferences
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.AppearanceSettings
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.data.sync.SyncStatus
import com.receiptwarranty.app.data.IconPackStyle
import com.receiptwarranty.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncStateManager: SyncStateManager?,
    private val appearancePreferences: AppearancePreferences,
    private val repository: ReceiptWarrantyRepository
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> =
        syncStateManager?.syncStatus?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus.Idle)
            ?: MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    val lastSyncTime: StateFlow<Long> =
        syncStateManager?.lastSyncTime?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
            ?: MutableStateFlow(0L)

    val appearanceSettings: StateFlow<AppearanceSettings> = appearancePreferences.settings

    fun syncNow() {
        val manager = syncStateManager ?: return
        viewModelScope.launch {
            manager.syncAll()
        }
    }

    fun resetSyncStatus() {
        syncStateManager?.resetStatus()
    }

    fun setUseDynamicColor(enabled: Boolean) {
        appearancePreferences.setUseDynamicColor(enabled)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        appearancePreferences.setThemeMode(themeMode)
    }

    fun setPrimaryColor(hex: String) {
        appearancePreferences.setPrimaryColor(hex)
    }

    fun setUseAmoledBlack(enabled: Boolean) {
        appearancePreferences.setUseAmoledBlack(enabled)
    }

    fun setIconStyle(style: IconPackStyle) {
        appearancePreferences.setIconStyle(style)
    }

    fun setCurrencySymbol(symbol: String) {
        appearancePreferences.setCurrencySymbol(symbol)
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        appearancePreferences.setDeveloperModeEnabled(enabled)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllItems()
        }
    }

    fun resetSyncMetadata() {
        viewModelScope.launch {
            repository.resetSyncMetadata()
        }
    }
}
