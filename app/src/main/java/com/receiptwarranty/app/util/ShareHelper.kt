package com.receiptwarranty.app.util

import android.content.Context
import android.content.Intent
import com.receiptwarranty.app.data.ReceiptWarranty
import androidx.core.content.FileProvider
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.graphics.ImageDecoder
import android.provider.MediaStore
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareItem(context: Context, item: ReceiptWarranty) {
        val text = buildString {
            appendLine("${item.type.name} Details: ${item.title}")
            item.category?.let { appendLine("Category: $it") }
            item.purchaseDate?.let { appendLine("Purchased: ${CurrencyUtils.formatDate(it)}") }
            item.price?.let { appendLine("Amount: ${CurrencyUtils.formatRupee(it)}") }
            item.warrantyExpiryDate?.let { appendLine("Warranty Expiry: ${CurrencyUtils.formatDate(it)}") }
            if (!item.notes.isNullOrBlank()) appendLine("\nNotes: ${item.notes}")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share ${item.type.name} Text"))
    }

    fun shareImage(context: Context, item: ReceiptWarranty) {
        if (item.imageUri == null) return
        try {
            val sourceUri = Uri.parse(item.imageUri)
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val file = File(context.cacheDir, "shared_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
