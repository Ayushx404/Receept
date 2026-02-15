package com.receiptwarranty.app.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.receiptwarranty.app.MainActivity
import com.receiptwarranty.app.ReceiptWarrantyApp

class WarrantyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return Result.failure()
            }
        }

        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val id = inputData.getLong(KEY_ITEM_ID, -1L)
        val daysBefore = inputData.getInt(KEY_DAYS_BEFORE, 7)

        if (id == -1L) return Result.failure()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val daysText = when (daysBefore) {
            1 -> "tomorrow"
            7 -> "in 7 days"
            else -> "in $daysBefore days"
        }

        val notification = NotificationCompat.Builder(applicationContext, ReceiptWarrantyApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Warranty Expiring Soon")
            .setContentText("Warranty for \"$title\" expires $daysText. Check now?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_BASE_ID + id.toInt(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_ITEM_ID = "item_id"
        const val KEY_DAYS_BEFORE = "days_before"
        private const val NOTIFICATION_BASE_ID = 10000
    }
}
