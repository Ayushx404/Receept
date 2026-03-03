package com.receiptwarranty.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.receiptwarranty.app.data.AppearancePreferences

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository,
    private val reminderScheduler: WarrantyReminderScheduler,
    private val syncStateManager: SyncStateManager?,
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    val tags: StateFlow<List<String>> = repository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencySymbol: StateFlow<String> = appearancePreferences.settings
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    fun saveItem(item: ReceiptWarranty, onComplete: () -> Unit) {
        if (_isSaving.value) return

        viewModelScope.launch {
            if (_isSaving.value) return@launch
            _isSaving.value = true
            try {
                val itemWithTimestamp = item.copy(
                    updatedAt = System.currentTimeMillis()
                )

                val id = if (item.id == 0L) {
                    repository.insert(itemWithTimestamp)
                } else {
                    repository.update(itemWithTimestamp)
                    item.id
                }

                val saved = itemWithTimestamp.copy(id = id)
                reminderScheduler.cancelReminder(id)
                if (saved.type == ReceiptType.WARRANTY) {
                    reminderScheduler.scheduleReminder(saved)
                }

                // Call onComplete immediately so the UI can navigate away without waiting for cloud sync
                onComplete()

                syncStateManager?.syncItemAsync(saved)
            } catch (e: Exception) {
                Log.e("AddEditVM", "Save failed: ${e.message}", e)
                _syncError.value = "Error saving: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

}
