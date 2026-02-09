package com.receiptwarranty.app.data

import android.content.Context
import com.receiptwarranty.app.workers.WarrantyReminderScheduler

class AppContainer(context: Context) {
    val database = AppDatabase.getInstance(context)
    val receiptWarrantyDao = database.receiptWarrantyDao()
    val repository = ReceiptWarrantyRepository(receiptWarrantyDao)
    val warrantyReminderScheduler: WarrantyReminderScheduler by lazy {
        WarrantyReminderScheduler(context)
    }
}
