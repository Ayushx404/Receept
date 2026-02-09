package com.receiptwarranty.app.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val error: String? = null
)

class ReceiptWarrantyViewModel(
    private val repository: ReceiptWarrantyRepository,
    private val warrantyReminderScheduler: WarrantyReminderScheduler
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun insertOrUpdate(item: ReceiptWarranty) {
        viewModelScope.launch {
            try {
                val id = if (item.id == 0L) {
                    repository.insert(item)
                } else {
                    repository.update(item)
                    item.id
                }
                val saved = item.copy(id = id)
                warrantyReminderScheduler.cancelReminder(id)
                if (item.type == com.receiptwarranty.app.data.ReceiptType.WARRANTY) {
                    warrantyReminderScheduler.scheduleReminder(saved)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun delete(item: ReceiptWarranty) {
        viewModelScope.launch {
            repository.delete(item)
            warrantyReminderScheduler.cancelReminder(item.id)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            warrantyReminderScheduler.cancelReminder(id)
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
        private val warrantyReminderScheduler: WarrantyReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReceiptWarrantyViewModel(repository, warrantyReminderScheduler) as T
        }
    }
}
