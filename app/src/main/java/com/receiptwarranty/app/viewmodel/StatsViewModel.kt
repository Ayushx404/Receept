package com.receiptwarranty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.ReceiptWarranty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.DecimalFormat
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.receiptwarranty.app.data.AppearancePreferences
import com.receiptwarranty.app.data.ReceiptType
import kotlinx.coroutines.flow.MutableStateFlow

enum class ValueFilter { ACTIVE, ALL }

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository,
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {

    val currencySymbol: StateFlow<String> = appearancePreferences.settings
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    fun formatCurrency(amount: Double?, symbol: String): String {
        val format = DecimalFormat("#,##0.00")
        return "$symbol${format.format(amount ?: 0.0)}"
    }

    val totalSpending: StateFlow<Double?> = repository.getTotalSpending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthSpending: StateFlow<Double?> = repository.getSpendingForMonth(0)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        
    val topCategory: StateFlow<CategoryCount?> = repository.getCategoryStats()
        .map { stats -> stats.maxByOrNull { it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoryStats: StateFlow<List<CategoryCount>> = repository.getCategoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWarrantyCount: StateFlow<Int> = repository.getActiveWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingBillsTotal: StateFlow<Double?> = repository.getUpcomingBillsTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expiredWarrantyCount: StateFlow<Int> = repository.getExpiredWarrantyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringSoonCount: StateFlow<Int> = repository.getExpiringSoonCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlySubscriptionCost: StateFlow<Double?> = repository.getMonthlySubscriptionCost()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendingTrend: StateFlow<Double> = combine(
        repository.getSpendingForMonth(0),
        repository.getSpendingForMonth(-1)
    ) { current, previous ->
        val curr = current ?: 0.0
        val prev = previous ?: 0.0
        if (prev == 0.0) 0.0 else ((curr - prev) / prev) * 100.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val last6MonthsSpending: StateFlow<List<MonthlySpending>> = combine(
        (0 downTo -5).map { offset ->
            repository.getSpendingForMonth(offset).map { amount ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, offset)
                val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                MonthlySpending(monthLabel, amount ?: 0.0)
            }
        }
    ) { amounts ->
        amounts.toList().reversed() // Reverse so the oldest is first on the left side of the chart
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributionStats: StateFlow<Map<String, Int>> = combine(
        repository.getReceiptCount(),
        repository.getWarrantyCount(),
        repository.getBillCount()
    ) { r, w, b ->
        mapOf(
            "Receipts" to r,
            "Warranties" to w,
            "Bills & Subs" to b
        ).filter { it.value > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val valueFilter = MutableStateFlow(ValueFilter.ACTIVE)

    val subscriptionCount: StateFlow<Int> = repository.getSubscriptionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalValueAll: StateFlow<Double> = repository.getTotalValueAll()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val valueByType: StateFlow<Map<String, Double>> = combine(
        repository.getValueByType(ReceiptType.WARRANTY),
        repository.getValueByType(ReceiptType.RECEIPT),
        repository.getValueByType(ReceiptType.BILL),
        repository.getValueByType(ReceiptType.SUBSCRIPTION),
        valueFilter
    ) { warranty, receipt, bill, subscription, _ ->
        mapOf(
            "Warranty" to (warranty ?: 0.0),
            "Receipt" to (receipt ?: 0.0),
            "Bill" to (bill ?: 0.0),
            "Subscription" to (subscription ?: 0.0)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalValueFiltered: StateFlow<Double> = valueByType.map { it.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val upcomingRenewalsList: StateFlow<List<com.receiptwarranty.app.data.ReceiptWarranty>> = repository.getUpcomingRenewals(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributionByType: StateFlow<Map<String, Int>> = combine(
        repository.getReceiptCount(),
        repository.getWarrantyCount(),
        repository.getBillCount(),
        repository.getSubscriptionCount()
    ) { receipts, warranties, bills, subscriptions ->
        mapOf(
            "Receipts" to receipts,
            "Warranties" to warranties,
            "Bills" to bills,
            "Subscriptions" to subscriptions
        ).filter { it.value > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalItemsCount: StateFlow<Int> = distributionByType
        .map { it.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categoryValueStats: StateFlow<List<CategoryCount>> = repository.getCategoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentPayments: StateFlow<List<ReceiptWarranty>> = repository.getRecentPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageItemValue: StateFlow<Double> = combine(
        totalValueFiltered,
        distributionByType
    ) { total, distribution ->
        val count = distribution.values.sum()
        if (count > 0) total / count else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topCategoryName: StateFlow<String?> = categoryStats
        .map { stats -> stats.maxByOrNull { it.count }?.category }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

data class MonthlySpending(
    val monthLabel: String,
    val amount: Double
)
