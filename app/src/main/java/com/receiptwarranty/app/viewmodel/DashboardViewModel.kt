package com.receiptwarranty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class DashboardUiState(
    val receiptCount: Int = 0,
    val warrantyCount: Int = 0,
    val billCount: Int = 0,
    val subscriptionCount: Int = 0,
    val activeWarrantyCount: Int = 0,
    val expiredWarrantyCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val categoryStats: List<CategoryCount> = emptyList(),
    val expiringSoonItems: List<ReceiptWarranty> = emptyList(),
    val totalValueProtected: Double = 0.0,
    val upcomingRenewals: List<ReceiptWarranty> = emptyList(),
    val recentItems: List<ReceiptWarranty> = emptyList(),
    val monthlySubscriptionCost: Double = 0.0
)

private data class DashboardCountState(
    val receiptCount: Int,
    val warrantyCount: Int,
    val billCount: Int,
    val subscriptionCount: Int,
    val activeWarrantyCount: Int,
    val expiredWarrantyCount: Int,
    val expiringSoonCount: Int,
    val totalValueProtected: Double,
    val monthlySubscriptionCost: Double
)

private data class DashboardListState(
    val categoryStats: List<CategoryCount>,
    val expiringSoonItems: List<ReceiptWarranty>,
    val upcomingRenewals: List<ReceiptWarranty>,
    val recentItems: List<ReceiptWarranty>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository
) : ViewModel() {

    private val receiptCount = repository.getReceiptCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val warrantyCount = repository.getWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val billCount = repository.getBillCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val subscriptionCount = repository.getSubscriptionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val totalValueProtected = repository.getTotalValueProtected()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val upcomingRenewals = repository.getUpcomingRenewals(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val monthlySubscriptionCost = repository.getMonthlySubscriptionCost()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val activeWarrantyCount = repository.getActiveWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val expiredWarrantyCount = repository.getExpiredWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val expiringSoonCount = repository.getExpiringSoonCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val categoryStats = repository.getCategoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val expiringSoonItems = repository.getExpiringSoonItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recentItems = repository.getAll()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dashCountState = combine(
        receiptCount,
        warrantyCount,
        billCount,
        subscriptionCount,
        activeWarrantyCount,
        expiredWarrantyCount,
        expiringSoonCount,
        totalValueProtected,
        monthlySubscriptionCost
    ) { values ->
        val active = values[4] as Int
        
        DashboardCountState(
            receiptCount = values[0] as Int,
            warrantyCount = values[1] as Int,
            billCount = values[2] as Int,
            subscriptionCount = values[3] as Int,
            activeWarrantyCount = active,
            expiredWarrantyCount = values[5] as Int,
            expiringSoonCount = values[6] as Int,
            totalValueProtected = values[7] as Double,
            monthlySubscriptionCost = values[8] as Double
        )
    }

    private val dashListState = combine(
        categoryStats,
        expiringSoonItems,
        upcomingRenewals,
        recentItems
    ) { cats, expiring, renewals, recents ->
        DashboardListState(cats, expiring, renewals, recents)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        dashCountState,
        dashListState
    ) { counts, lists ->
        DashboardUiState(
            receiptCount = counts.receiptCount,
            warrantyCount = counts.warrantyCount,
            billCount = counts.billCount,
            subscriptionCount = counts.subscriptionCount,
            activeWarrantyCount = counts.activeWarrantyCount,
            expiredWarrantyCount = counts.expiredWarrantyCount,
            expiringSoonCount = counts.expiringSoonCount,
            categoryStats = lists.categoryStats,
            expiringSoonItems = lists.expiringSoonItems,
            totalValueProtected = counts.totalValueProtected,
            upcomingRenewals = lists.upcomingRenewals,
            recentItems = lists.recentItems,
            monthlySubscriptionCost = counts.monthlySubscriptionCost
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun categoryItems(category: String): Flow<List<ReceiptWarranty>> = repository.getByCategory(category)

    fun markAsPaid(item: ReceiptWarranty) {
        viewModelScope.launch {
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
        }
    }
}
