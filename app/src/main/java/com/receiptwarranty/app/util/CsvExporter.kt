package com.receiptwarranty.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportToDownloads(
        context: Context,
        csvContent: String,
        filePrefix: String = "receipt_warranty"
    ): Result<Uri> {
        val resolver = context.contentResolver
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${filePrefix}_$timestamp.csv"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values)
            ?: return Result.failure(IllegalStateException("Could not create CSV file"))

        return try {
            resolver.openOutputStream(uri)?.use { output ->
                output.write(csvContent.toByteArray())
                output.flush()
            } ?: return Result.failure(IllegalStateException("Could not write CSV file"))

            Result.success(uri)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Result.failure(e)
        }
    }
}
