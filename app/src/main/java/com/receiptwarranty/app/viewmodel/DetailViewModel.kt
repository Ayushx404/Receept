package com.receiptwarranty.app.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Emitted when a delete is pending — UI should show an Undo Snackbar. */
data class UndoDeleteEvent(val itemTitle: String)


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository,
    private val reminderScheduler: WarrantyReminderScheduler,
    private val syncStateManager: SyncStateManager?
) : ViewModel() {

    private val _undoDeleteEvent = MutableSharedFlow<UndoDeleteEvent>()
    val undoDeleteEvent: SharedFlow<UndoDeleteEvent> = _undoDeleteEvent.asSharedFlow()

    private val _shareCardResult = MutableStateFlow<Result<android.net.Uri>?>(null)
    val shareCardResult: StateFlow<Result<android.net.Uri>?> = _shareCardResult.asStateFlow()

    private var pendingDeleteJob: Job? = null

    fun getItem(id: Long): Flow<ReceiptWarranty?> = repository.getById(id)

    fun archiveItem(id: Long) {
        viewModelScope.launch {
            repository.archiveItem(id)
        }
    }

    fun unarchiveItem(id: Long) {
        viewModelScope.launch {
            repository.unarchiveItem(id)
        }
    }

    fun markAsPaid(id: Long) {
        viewModelScope.launch {
            val item = repository.getById(id).first() ?: return@launch
            val now = System.currentTimeMillis()
            val newPaymentHistory = if (item.paymentHistory.isNullOrBlank()) {
                now.toString()
            } else {
                "${item.paymentHistory},$now"
            }
            val updatedItem = item.copy(
                isPaid = true,
                lastPaidDate = now,
                paymentHistory = newPaymentHistory,
                updatedAt = now
            )
            repository.update(updatedItem)
            syncStateManager?.syncItemAsync(updatedItem)
        }
    }

    fun deleteById(id: Long, onComplete: () -> Unit) {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = viewModelScope.launch {
            val item = repository.getById(id).first()
            if (item != null) {
                _undoDeleteEvent.emit(UndoDeleteEvent(item.title))
                onComplete()
                delay(5_000)
                syncStateManager?.deleteItem(item)
                repository.deleteById(id)
                reminderScheduler.cancelReminder(id)
            } else {
                onComplete()
            }
        }
    }

    fun undoDelete() {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
    }

    fun generateShareCard(context: android.content.Context, item: ReceiptWarranty, theme: com.receiptwarranty.app.util.share.ShareTheme) {
        viewModelScope.launch {
            _shareCardResult.value = null // trigger loading state
            
            // Generate Data Model
            val data = com.receiptwarranty.app.util.share.ShareCardData(
                title = item.title,
                brandCategory = item.category ?: "",
                purchaseDate = item.purchaseDate?.let { com.receiptwarranty.app.util.CurrencyUtils.formatDate(it) },
                price = item.price?.let { com.receiptwarranty.app.util.CurrencyUtils.formatRupee(it) },
                store = item.category,
                category = item.category,
                hasWarranty = item.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY && item.warrantyExpiryDate != null,
                warrantyStartDate = item.purchaseDate?.let { com.receiptwarranty.app.util.CurrencyUtils.formatDate(it) },
                warrantyEndDate = item.warrantyExpiryDate?.let { com.receiptwarranty.app.util.CurrencyUtils.formatDate(it) },
                warrantyStatus = item.warrantyStatus(),
                warrantyProgressText = null, // Can calculate months remaining here if desired
                warrantyProgressPercent = calculateWarrantyProgress(item),
                itemType = item.type.name,
                heroImageUri = item.imageUri?.toUri(),
                notes = item.notes?.takeIf { it.isNotBlank() },
                theme = theme
            )

            val generator = com.receiptwarranty.app.util.share.ShareCardGenerator(context)
            val result = generator.generateCard(data)
            _shareCardResult.value = result
        }
    }

    private fun calculateWarrantyProgress(item: ReceiptWarranty): Float? {
        if (item.purchaseDate == null || item.warrantyExpiryDate == null) return null
        val total = item.warrantyExpiryDate - item.purchaseDate
        if (total <= 0) return 1f
        val elapsed = System.currentTimeMillis() - item.purchaseDate
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
