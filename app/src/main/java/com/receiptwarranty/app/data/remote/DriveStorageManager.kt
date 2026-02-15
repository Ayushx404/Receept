package com.receiptwarranty.app.data.remote

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File as LocalFile
import java.util.UUID

data class DriveUploadResult(
    val webViewLink: String,
    val fileId: String,
    val downloadLink: String? = null
)

class DriveStorageManager(private val context: Context, private val userId: String) {

    private val TAG = "DriveStorageManager"
    
    private var driveService: Drive? = null
    private var receiptFolderId: String? = null
    
    companion object {
        private const val APP_FOLDER_LEGACY_NAME = "ReceiptWarranty"
        private const val APP_FOLDER_USER_PREFIX = "ReceiptWarranty_"
    }

    private fun getDriveService(): Drive? {
        if (driveService != null) return driveService
        
        try {
            val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
            
            if (account == null) {
                Log.e(TAG, "No Google account signed in")
                return null
            }
            
            val accessToken = try {
                val email = account.email ?: throw Exception("No email found")
                GoogleAuthUtil.getToken(
                    context,
                    email,
                    "oauth2:${DriveScopes.DRIVE_FILE} ${DriveScopes.DRIVE_APPDATA}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get OAuth token: ${e.message}", e)
                return null
            }
            
            val credential = GoogleCredential().setAccessToken(accessToken)
            
            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("ReceiptWarranty")
                .build()
            
            return driveService
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Drive service: ${e.message}", e)
            return null
        }
    }

    private suspend fun getOrCreateAppFolder(): String? = withContext(Dispatchers.IO) {
        if (receiptFolderId != null) return@withContext receiptFolderId
        
        val service = getDriveService() ?: return@withContext null
        
        try {
            val userFolderName = "$APP_FOLDER_USER_PREFIX$userId"
            val userFolder = findFolderByName(service, userFolderName)
            if (userFolder != null) {
                receiptFolderId = userFolder.id
                Log.d(TAG, "Using user folder: ${userFolder.name} (${userFolder.id})")
                return@withContext receiptFolderId
            }

            val legacyFolder = findFolderByName(service, APP_FOLDER_LEGACY_NAME)
            if (legacyFolder != null) {
                Log.d(TAG, "Legacy shared folder detected: ${legacyFolder.id}")
            }

            val folderMetadata = DriveFile()
            folderMetadata.name = userFolderName
            folderMetadata.mimeType = "application/vnd.google-apps.folder"
            
            val folder: DriveFile = service.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            receiptFolderId = folder.id
            Log.d(TAG, "Created user folder: ${folderMetadata.name} ($receiptFolderId)")
            return@withContext receiptFolderId
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating folder: ${e.message}", e)
            // Best-effort fallback: use legacy folder if it exists and user folder creation failed.
            val legacyFolder = try {
                findFolderByName(service, APP_FOLDER_LEGACY_NAME)
            } catch (_: Exception) {
                null
            }
            if (legacyFolder != null) {
                receiptFolderId = legacyFolder.id
                Log.w(TAG, "Falling back to legacy shared folder: ${legacyFolder.id}")
                return@withContext receiptFolderId
            }
            return@withContext null
        }
    }

    private fun findFolderByName(service: Drive, folderName: String): DriveFile? {
        val escapedName = folderName.replace("'", "\\'")
        val result: FileList = service.files().list()
            .setQ("name='$escapedName' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .setPageSize(1)
            .execute()
        return result.files.firstOrNull()
    }

    suspend fun uploadImage(localUri: String): Result<DriveUploadResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload for: $localUri")
            
            val folderId = getOrCreateAppFolder()
            if (folderId == null) {
                Log.e(TAG, "Failed to get or create app folder")
                return@withContext Result.failure(Exception("Failed to access Google Drive folder"))
            }
            
            val uri = Uri.parse(localUri)
            
            // Check if it's a content URI and try to open
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot access image: $localUri - ${e.message}")
                return@withContext Result.failure(Exception("Cannot access image file: ${e.message}"))
            }
            
            if (inputStream == null) {
                Log.e(TAG, "Cannot open image: $localUri")
                return@withContext Result.failure(Exception("Cannot open image: $localUri"))
            }
            
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            Log.d(TAG, "Image read: ${imageBytes.size} bytes")
            
            val fileName = "${UUID.randomUUID()}.jpg"
            
            val fileMetadata = DriveFile()
            fileMetadata.name = fileName
            fileMetadata.parents = listOf(folderId)
            
            val mediaContent = ByteArrayContent("image/jpeg", imageBytes)
            
            val service = getDriveService()
                ?: return@withContext Result.failure(Exception("Drive service not available"))
            
            val file: DriveFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, webContentLink")
                .execute()
            
            Log.d(TAG, "Upload successful! File ID: ${file.id}, WebLink: ${file.webViewLink}, ContentLink: ${file.webContentLink}")
            
            // Store file ID in SharedPreferences for later use
            saveFileIdMapping(file.id, fileName)
            
            // Use direct download link for image loading (webContentLink)
            val directDownloadLink = file.webContentLink ?: "https://drive.google.com/uc?export=download&id=${file.id}"
            
            val result = DriveUploadResult(
                webViewLink = file.webViewLink ?: "https://drive.google.com/file/d/${file.id}/view",
                fileId = file.id,
                downloadLink = directDownloadLink
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun downloadImage(fileIdOrUrl: String, localFileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading image: $fileIdOrUrl")
            
            val service = getDriveService()
                ?: return@withContext Result.failure(Exception("Drive service not available"))
            
            // Determine if input is file ID or URL
            val fileId = if (fileIdOrUrl.startsWith("http")) {
                // It's a URL, we need to find the file by name or search
                // For now, return failure - should use file ID
                return@withContext Result.failure(Exception("Use file ID for download, not URL"))
            } else {
                fileIdOrUrl
            }
            
            val outputStream = ByteArrayOutputStream()
            service.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
            
            val imageBytes = outputStream.toByteArray()
            
            val localDir = LocalFile(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts")
            if (!localDir.exists()) localDir.mkdirs()
            
            val localFile = LocalFile(localDir, localFileName)
            localFile.writeBytes(imageBytes)
            
            Log.d(TAG, "Downloaded to: ${localFile.absolutePath}")
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteImage(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting image: $fileId")
            
            val service = getDriveService()
                ?: return@withContext Result.failure(Exception("Drive service not available"))
            
            service.files().delete(fileId).execute()
            
            Log.d(TAG, "Deleted successfully")
            removeFileIdMapping(fileId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${e.message}", e)
            // Don't fail - file might already be deleted
            Result.success(Unit)
        }
    }

    suspend fun getFileIdFromUrl(webViewLink: String): String? = withContext(Dispatchers.IO) {
        // Store a mapping of file IDs when uploading
        // For now, return the link as-is and handle differently
        null
    }

    private fun saveFileIdMapping(fileId: String, fileName: String) {
        try {
            val prefs = context.getSharedPreferences("drive_file_mapping", Context.MODE_PRIVATE)
            prefs.edit().putString(fileName, fileId).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Could not save file mapping: ${e.message}")
        }
    }

    private fun removeFileIdMapping(fileId: String) {
        try {
            val prefs = context.getSharedPreferences("drive_file_mapping", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            // We don't have filename here, so we'd need to search
            // For now, just clear all (not ideal but functional)
            editor.clear().apply()
        } catch (e: Exception) {
            Log.w(TAG, "Could not remove file mapping: ${e.message}")
        }
    }
}
