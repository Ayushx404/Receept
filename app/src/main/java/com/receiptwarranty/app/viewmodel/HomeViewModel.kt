package com.receiptwarranty.app.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.data.sync.SyncStatus
import com.receiptwarranty.app.util.ConnectivityObserver
import com.receiptwarranty.app.util.CsvExporter
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeUiState(
    val items: List<ReceiptWarranty> = emptyList(),
    val searchQuery: String = "",
    val warrantyFilter: WarrantyFilter = WarrantyFilter.ALL,
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isOffline: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val lastSyncTime: Long = 0L,
    val pendingSyncCount: Int = 0
)

private data class HomeFilterState(
    val items: List<ReceiptWarranty>,
    val searchQuery: String,
    val warrantyFilter: WarrantyFilter,
    val selectedTags: List<String>
)

private data class HomeSelectionState(
    val isSelectionMode: Boolean,
    val selectedIds: Set<Long>
)

private data class HomeSyncState(
    val isOffline: Boolean,
    val syncStatus: SyncStatus,
    val lastSyncTime: Long,
    val pendingSyncCount: Int
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository,
    private val reminderScheduler: WarrantyReminderScheduler,
    private val syncStateManager: SyncStateManager?,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val warrantyFilter = MutableStateFlow(WarrantyFilter.ALL)
    private val selectedTags = MutableStateFlow<List<String>>(emptyList())
    private val isSelectionMode = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    private val syncStatusFlow: Flow<SyncStatus> = syncStateManager?.syncStatus
        ?: MutableStateFlow(SyncStatus.Idle)
    private val syncStatus = syncStatusFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus.Idle)

    private val lastSyncTimeFlow: Flow<Long> = syncStateManager?.lastSyncTime
        ?: MutableStateFlow(0L)
    private val lastSyncTime = lastSyncTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val isOffline = connectivityObserver.observe()
        .map { connected -> !connected }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allItems = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedItems = repository.getArchivedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allTags = repository.getAllTags()
        .map { tagStrings ->
            tagStrings.flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveItem(id: Long) {
        viewModelScope.launch {
            repository.unarchiveItem(id)
        }
    }

    fun toggleTag(tag: String) {
        val current = selectedTags.value.toMutableList()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        selectedTags.value = current
    }

    fun clearTags() {
        selectedTags.value = emptyList()
    }

    private val pendingSyncCount = MutableStateFlow(0)

    private val filteredItems = combine(allItems, searchQuery, warrantyFilter, selectedTags) { items, query, filter, tags ->
        repository.searchAndFilter(items, query, filter, tags = tags)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Type-safe nested combines ---

    private val homeFilterState = combine(
        filteredItems,
        searchQuery,
        warrantyFilter,
        selectedTags
    ) { items, query, filter, tags ->
        HomeFilterState(items, query, filter, tags)
    }

    private val homeSelectionState = combine(
        isSelectionMode,
        selectedIds
    ) { selection, ids ->
        HomeSelectionState(selection, ids)
    }

    private val homeSyncState = combine(
        isOffline,
        syncStatus,
        lastSyncTime,
        pendingSyncCount
    ) { offline, status, time, pending ->
        HomeSyncState(offline, status, time, pending)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        homeFilterState,
        homeSelectionState,
        homeSyncState,
        allTags,
        selectedTags
    ) { fs, sel, sync, availableTags, selTags ->
        HomeUiState(
            items = fs.items,
            searchQuery = fs.searchQuery,
            warrantyFilter = fs.warrantyFilter,
            selectedTags = selTags,
            availableTags = availableTags,
            isSelectionMode = sel.isSelectionMode,
            selectedIds = sel.selectedIds,
            isOffline = sync.isOffline,
            syncStatus = sync.syncStatus,
            lastSyncTime = sync.lastSyncTime,
            pendingSyncCount = sync.pendingSyncCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setWarrantyFilter(filter: WarrantyFilter) {
        warrantyFilter.value = filter
    }

    fun enterSelectionMode(item: ReceiptWarranty) {
        isSelectionMode.value = true
        selectedIds.value = setOf(item.id)
    }

    fun enterSelectionModeEmpty() {
        isSelectionMode.value = true
        selectedIds.value = emptySet()
    }

    fun toggleSelection(itemId: Long) {
        val current = selectedIds.value
        selectedIds.value = if (current.contains(itemId)) current - itemId else current + itemId
        if (selectedIds.value.isEmpty()) {
            isSelectionMode.value = false
        }
    }

    fun exitSelectionMode() {
        isSelectionMode.value = false
        selectedIds.value = emptySet()
    }

    private val _shareCardResult = MutableStateFlow<Result<android.net.Uri>?>(null)
    val shareCardResult: StateFlow<Result<android.net.Uri>?> = _shareCardResult

    fun generateShareCard(context: Context, theme: com.receiptwarranty.app.util.share.ShareTheme) {
        val selectedId = selectedIds.value.firstOrNull() ?: return
        val item = allItems.value.firstOrNull { it.id == selectedId } ?: return
        
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
                warrantyProgressText = String.format(Locale.ROOT, "%d%%", ((calculateWarrantyProgress(item) ?: 0f) * 100).toInt()), // Can calculate months remaining here if desired
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

    private val _undoDeleteEvent = MutableSharedFlow<Int>()
    /** Emits the count of items pending deletion — collect in UI to show Snackbar. */
    val undoDeleteEvent: SharedFlow<Int> = _undoDeleteEvent

    private var pendingDeleteJob: Job? = null

    fun deleteSelectedItems() {
        pendingDeleteJob?.cancel()
        val ids = selectedIds.value
        val itemsToDelete = allItems.value.filter { ids.contains(it.id) }
        if (itemsToDelete.isEmpty()) return
        pendingDeleteJob = viewModelScope.launch {
            _undoDeleteEvent.emit(itemsToDelete.size)
            exitSelectionMode()
            delay(5_000)
            itemsToDelete.forEach { item ->
                syncStateManager?.deleteItem(item)
                repository.delete(item)
                reminderScheduler.cancelReminder(item.id)
            }
        }
    }

    fun undoDelete() {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
    }

    fun retrySync(itemId: Long) {
        val manager = syncStateManager ?: return
        val item = allItems.value.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            manager.syncItem(item)
        }
    }

    fun exportToCSV(context: Context) {
        viewModelScope.launch {
            try {
                val csv = repository.exportToCSV()
                val exportResult = withContext(Dispatchers.IO) {
                    CsvExporter.exportToDownloads(context, csv)
                }

                if (exportResult.isSuccess) {
                    Toast.makeText(context, "CSV exported to Downloads", Toast.LENGTH_SHORT).show()
                } else {
                    val error = exportResult.exceptionOrNull()?.message ?: "Unknown export error"
                    Toast.makeText(context, "Export failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "CSV export failed", e)
                Toast.makeText(context, "Export failed", Toast.LENGTH_LONG).show()
            }
        }
    }

}
