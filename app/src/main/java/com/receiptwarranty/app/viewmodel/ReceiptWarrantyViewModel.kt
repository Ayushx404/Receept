package com.receiptwarranty.app.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReceiptWarrantyUiState(
    val items: List<ReceiptWarranty> = emptyList(),
    val searchQuery: String = "",
    val warrantyFilter: WarrantyFilter = WarrantyFilter.ALL,
    val categoryFilter: String? = null,
    val categories: List<String> = emptyList(),
    val receiptCount: Int = 0,
    val warrantyCount: Int = 0,
    val activeWarrantyCount: Int = 0,
    val expiredWarrantyCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val categoryStats: List<CategoryCount> = emptyList(),
    val expiringSoonItems: List<ReceiptWarranty> = emptyList(),
    val companies: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncError: String? = null,
    val isSyncing: Boolean = false
)

class ReceiptWarrantyViewModel(
    private val repository: ReceiptWarrantyRepository,
    private val warrantyReminderScheduler: WarrantyReminderScheduler,
    private val syncStateManager: SyncStateManager? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _warrantyFilter = MutableStateFlow(WarrantyFilter.ALL)
    val warrantyFilter: StateFlow<WarrantyFilter> = _warrantyFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    val items: StateFlow<List<ReceiptWarranty>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receiptCount: StateFlow<Int> = repository.getReceiptCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val warrantyCount: StateFlow<Int> = repository.getWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeWarrantyCount: StateFlow<Int> = repository.getActiveWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiredWarrantyCount: StateFlow<Int> = repository.getExpiredWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringSoonCount: StateFlow<Int> = repository.getExpiringSoonCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categoryStats: StateFlow<List<CategoryCount>> = repository.getCategoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringSoonItems: StateFlow<List<ReceiptWarranty>> = repository.getExpiringSoonItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companies: StateFlow<List<String>> = repository.getAllCompanies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<ReceiptWarranty>> = combine(
        items,
        searchQuery,
        warrantyFilter,
        categoryFilter
    ) { allItems, query, filter, category ->
        var result = allItems

        if (query.isNotBlank()) {
            val lower = query.lowercase().trim()
            result = result.filter {
                it.title.lowercase().contains(lower) ||
                it.company.lowercase().contains(lower) ||
                it.notes?.lowercase()?.contains(lower) == true ||
                it.category?.lowercase()?.contains(lower) == true
            }
        }

        result = when (filter) {
            WarrantyFilter.ALL -> result
            WarrantyFilter.ALL_RECEIPTS -> result.filter { it.type == com.receiptwarranty.app.data.ReceiptType.RECEIPT }
            WarrantyFilter.ALL_WARRANTIES -> result.filter { it.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY }
            WarrantyFilter.EXPIRING_SOON -> result.filter {
                it.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY && it.warrantyStatus() == com.receiptwarranty.app.data.WarrantyStatus.EXPIRING_SOON
            }
            WarrantyFilter.EXPIRED -> result.filter {
                it.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY && it.warrantyStatus() == com.receiptwarranty.app.data.WarrantyStatus.EXPIRED
            }
        }

        if (category != null) {
            result = result.filter { it.category == category }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<ReceiptWarrantyUiState> = combine(
        filteredItems,
        searchQuery,
        warrantyFilter,
        categoryFilter,
        categories,
        receiptCount,
        warrantyCount,
        activeWarrantyCount,
        expiredWarrantyCount,
        expiringSoonCount,
        categoryStats,
        expiringSoonItems,
        companies
    ) { values ->
        ReceiptWarrantyUiState(
            items = values[0] as List<ReceiptWarranty>,
            searchQuery = values[1] as String,
            warrantyFilter = values[2] as WarrantyFilter,
            categoryFilter = values[3] as String?,
            categories = values[4] as List<String>,
            receiptCount = values[5] as Int,
            warrantyCount = values[6] as Int,
            activeWarrantyCount = values[7] as Int,
            expiredWarrantyCount = values[8] as Int,
            expiringSoonCount = values[9] as Int,
            categoryStats = values[10] as List<CategoryCount>,
            expiringSoonItems = values[11] as List<ReceiptWarranty>,
            companies = values[12] as List<String>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReceiptWarrantyUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setWarrantyFilter(filter: WarrantyFilter) {
        _warrantyFilter.value = filter
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun clearSyncError() {
        _syncError.value = null
    }

    fun insertOrUpdate(item: ReceiptWarranty) {
        viewModelScope.launch {
            try {
                // Update updatedAt timestamp for conflict resolution
                val itemWithTimestamp = item.copy(updatedAt = System.currentTimeMillis())
                
                val id = if (item.id == 0L) {
                    repository.insert(itemWithTimestamp)
                } else {
                    repository.update(itemWithTimestamp)
                    item.id
                }
                val saved = itemWithTimestamp.copy(id = id)
                warrantyReminderScheduler.cancelReminder(id)
                if (item.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY) {
                    warrantyReminderScheduler.scheduleReminder(saved)
                }
                
                Log.d("ReceiptWarrantyVM", "Item saved locally, attempting sync for: ${saved.title}")
                
                if (syncStateManager != null) {
                    _isSyncing.value = true
                    _syncError.value = null
                    
                    val syncResult = syncStateManager.syncItem(saved)
                    if (syncResult.isSuccess) {
                        val cloudId = syncResult.getOrNull()
                        if (cloudId != null) {
                            // Update local item with cloudId
                            val updatedItem = saved.copy(cloudId = cloudId)
                            repository.update(updatedItem)
                            Log.d("ReceiptWarrantyVM", "Saved cloudId: $cloudId for item: ${saved.title}")
                        }
                        Log.d("ReceiptWarrantyVM", "Sync successful for: ${saved.title}")
                    } else {
                        val errorMsg = syncResult.exceptionOrNull()?.message ?: "Unknown sync error"
                        Log.e("ReceiptWarrantyVM", "Sync failed for ${saved.title}: $errorMsg")
                        _syncError.value = "Sync failed: $errorMsg"
                    }
                    _isSyncing.value = false
                } else {
                    Log.w("ReceiptWarrantyVM", "syncStateManager is null - skipping sync")
                }
            } catch (e: Exception) {
                Log.e("ReceiptWarrantyVM", "Error saving item: ${e.message}", e)
                _syncError.value = "Error saving: ${e.message}"
                _isSyncing.value = false
            }
        }
    }

    fun delete(item: ReceiptWarranty) {
        viewModelScope.launch {
            try {
                // 1. Delete from Firestore and Google Drive
                if (syncStateManager != null) {
                    _isSyncing.value = true
                    val deleteResult = syncStateManager.deleteItem(item)
                    if (deleteResult.isFailure) {
                        Log.w("ReceiptWarrantyVM", "Cloud delete failed: ${deleteResult.exceptionOrNull()?.message}")
                    }
                    _isSyncing.value = false
                }
                
                // 2. Delete from local Room database
                repository.delete(item)
                warrantyReminderScheduler.cancelReminder(item.id)
                
            } catch (e: Exception) {
                Log.e("ReceiptWarrantyVM", "Error deleting: ${e.message}", e)
                _isSyncing.value = false
            }
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            try {
                // First, get the item to delete from cloud storage
                val itemToDelete: ReceiptWarranty? = repository.getById(id).first()
                if (itemToDelete != null) {
                    // Delete from Firestore and Google Drive first
                    if (syncStateManager != null) {
                        _isSyncing.value = true
                        val deleteResult = syncStateManager.deleteItem(itemToDelete)
                        if (deleteResult.isFailure) {
                            Log.w("ReceiptWarrantyVM", "Cloud delete failed: ${deleteResult.exceptionOrNull()?.message}")
                        }
                        _isSyncing.value = false
                    }
                }
                
                // Then delete from local Room database
                repository.deleteById(id)
                warrantyReminderScheduler.cancelReminder(id)
                
                // No need to call syncAll() - deletion was already done in deleteItem()
                
            } catch (e: Exception) {
                Log.e("ReceiptWarrantyVM", "Error deleting: ${e.message}", e)
                _isSyncing.value = false
            }
        }
    }

    fun deleteMultiple(items: List<ReceiptWarranty>) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                
                // Delete each item from Firestore, Drive, and local DB
                for (item in items) {
                    try {
                        // Delete from Firestore and Drive
                        syncStateManager?.deleteItem(item)
                        
                        // Delete from local Room database
                        repository.delete(item)
                        warrantyReminderScheduler.cancelReminder(item.id)
                    } catch (e: Exception) {
                        Log.e("ReceiptWarrantyVM", "Error deleting item ${item.title}: ${e.message}")
                    }
                }
                
                _isSyncing.value = false
            } catch (e: Exception) {
                Log.e("ReceiptWarrantyVM", "Error deleting multiple: ${e.message}", e)
                _isSyncing.value = false
            }
        }
    }

    fun exportToCSV(context: Context) {
        viewModelScope.launch {
            try {
                val csv = repository.exportToCSV()
                shareCSV(context, csv)
            } catch (e: Exception) {
            }
        }
    }

    private fun shareCSV(context: Context, csvContent: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Receipt & Warranty Export")
            putExtra(Intent.EXTRA_TEXT, csvContent)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
    }

    class Factory(
        private val repository: ReceiptWarrantyRepository,
        private val warrantyReminderScheduler: WarrantyReminderScheduler,
        private val syncStateManager: com.receiptwarranty.app.data.sync.SyncStateManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReceiptWarrantyViewModel(repository, warrantyReminderScheduler, syncStateManager) as T
        }
    }
}
