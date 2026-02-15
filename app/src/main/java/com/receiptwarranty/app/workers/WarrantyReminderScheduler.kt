package com.receiptwarranty.app.workers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReminderDays
import java.util.concurrent.TimeUnit

class WarrantyReminderScheduler(private val context: Context) {

    fun scheduleReminder(item: ReceiptWarranty) {
        val expiry = item.warrantyExpiryDate ?: return
        val reminderDays = item.reminderDays ?: ReminderDays.ONE_WEEK
        
        val now = System.currentTimeMillis()
        val reminderDaysMs = reminderDays.days * 24 * 60 * 60 * 1000L
        val reminderTime = expiry - reminderDaysMs

        if (reminderTime <= now) return

        val delay = reminderTime - now

        val data = Data.Builder()
            .putString(WarrantyReminderWorker.KEY_TITLE, item.title)
            .putLong(WarrantyReminderWorker.KEY_ITEM_ID, item.id)
            .putInt(WarrantyReminderWorker.KEY_DAYS_BEFORE, reminderDays.days)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WarrantyReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(TAG_PREFIX + item.id)
            .build()

        cancelReminder(item.id)
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelReminder(itemId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_PREFIX + itemId)
    }

    companion object {
        private const val TAG_PREFIX = "warranty_reminder_"
    }
}
