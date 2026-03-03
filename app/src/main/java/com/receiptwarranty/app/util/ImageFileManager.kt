package com.receiptwarranty.app.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageFileManager(private val context: Context) {

    private fun receiptsDir(): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts").also {
            if (!it.exists()) it.mkdirs()
        }

    fun saveImageFromUri(uri: Uri): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val file = File(receiptsDir(), "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { input.copyTo(it) }
            Uri.fromFile(file).toString()
        }
    }.getOrNull()

    fun createTempFileForCamera(): Uri? = runCatching {
        Uri.fromFile(File(receiptsDir(), "camera_${UUID.randomUUID()}.jpg"))
    }.getOrNull()
}
