package com.receiptwarranty.app.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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

        // Deep-link PendingIntent: tapping the notification opens DetailScreen for this item.
        val deepLinkUri = "vaultapp://detail/$id".toUri()
        val deepLinkIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            deepLinkUri,
            applicationContext,
            MainActivity::class.java
        )
        val pendingIntent: PendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                id.toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!
        }

        val daysText = when (daysBefore) {
            1 -> "tomorrow"
            7 -> "in 1 week"
            14 -> "in 2 weeks"
            30 -> "in 1 month"
            90 -> "in 3 months"
            else -> "in $daysBefore days"
        }

        val notification = NotificationCompat.Builder(applicationContext, ReceiptWarrantyApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Warranty Expiring Soon")
            .setContentText("Warranty for \"$title\" expires $daysText. Tap to view.")
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
