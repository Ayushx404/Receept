package com.receiptwarranty.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

/**
 * Centralized utility for currency and date formatting.
 * All monetary values use Indian Rupee (₹) via en_IN locale.
 */
object CurrencyUtils {

    private val INDIA_LOCALE = Locale("en", "IN")

    private val rupeeFormatter: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(INDIA_LOCALE).apply {
            currency = Currency.getInstance("INR")
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    /**
     * Formats a Double as an Indian Rupee string (e.g. ₹1,99,000 or ₹199)
     * Returns "—" for null values.
     */
    fun formatRupee(amount: Double?): String {
        if (amount == null) return "—"
        return rupeeFormatter.format(amount)
    }

    /**
     * Formats a Long timestamp (millis) as a readable date string.
     * Returns "—" for null values.
     */
    fun formatDate(timestamp: Long?, pattern: String = "MMM d, yyyy"): String {
        if (timestamp == null) return "—"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}
