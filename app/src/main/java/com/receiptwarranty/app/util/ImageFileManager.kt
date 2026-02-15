package com.receiptwarranty.app.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImageFileManager(private val context: Context) {

    fun saveImageFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
                Uri.fromFile(file).toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun createTempFileForCamera(): Uri? {
        return try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "camera_${UUID.randomUUID()}.jpg")
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
