package com.receiptwarranty.app.data.remote

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.model.FileList
import com.receiptwarranty.app.data.auth.GoogleAuthManager
import com.receiptwarranty.app.util.PreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File as LocalFile
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class DriveUploadResult(
    val webViewLink: String,
    val fileId: String,
    val downloadLink: String? = null
)

@Singleton
class DriveStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: GoogleAuthManager
) {

    private val TAG = "DriveStorageManager"
    
    private val userId: String
        get() = authManager.getCurrentUser()?.uid ?: "local"
    
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
            
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA))
            credential.selectedAccount = account.account
            
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

    suspend fun findFileByName(fileName: String): DriveFile? = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext null
            val folderId = getOrCreateAppFolder() ?: return@withContext null
            
            val escapedName = fileName.replace("'", "\\'")
            val result: FileList = service.files().list()
                .setQ("name='$escapedName' and '$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .setPageSize(1)
                .execute()
            result.files.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error finding file: ${e.message}")
            null
        }
    }

    suspend fun uploadImage(localUri: String, itemId: Long? = null): Result<DriveUploadResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload for: $localUri, itemId: $itemId")
            
            val folderId = getOrCreateAppFolder()
            if (folderId == null) {
                Log.e(TAG, "Failed to get or create app folder")
                return@withContext Result.failure(Exception("Failed to access Google Drive folder"))
            }
            
            val uri = localUri.toUri()
            
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
            
            // Use deterministic filename if itemId provided, otherwise use UUID
            val fileName = itemId?.let { "receipt_$it.jpg" } ?: "${UUID.randomUUID()}.jpg"
            
            // Check if file with this name already exists to prevent duplicates
            val existingFile = findFileByName(fileName)
            if (existingFile != null) {
                Log.d(TAG, "Found existing file with same name: ${existingFile.id}")
                val directDownloadLink = "https://drive.google.com/uc?export=download&id=${existingFile.id}"
                val result = DriveUploadResult(
                    webViewLink = "https://drive.google.com/file/d/${existingFile.id}/view",
                    fileId = existingFile.id,
                    downloadLink = directDownloadLink
                )
                return@withContext Result.success(result)
            }
            
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
            
            saveFileIdMapping(file.id, fileName)
            
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

    private fun saveFileIdMapping(fileId: String, fileName: String) {
        try {
            val prefs = context.getSharedPreferences(PreferenceKeys.DRIVE_FILE_MAPPING, Context.MODE_PRIVATE)
            prefs.edit { putString(fileName, fileId) }
        } catch (e: Exception) {
            Log.w(TAG, "Could not save file mapping: ${e.message}")
        }
    }

    private fun removeFileIdMapping(fileId: String) {
        try {
            val prefs = context.getSharedPreferences(PreferenceKeys.DRIVE_FILE_MAPPING, Context.MODE_PRIVATE)
            prefs.edit {
                // Find the filename associated with this fileId and remove it
                val entryToRemove = prefs.all.entries.firstOrNull { it.value == fileId }
                if (entryToRemove != null) {
                    remove(entryToRemove.key)
                } else {
                    // If not found, it might be a legacy entry or already removed.
                    // For now, if we can't find a specific mapping, clear all as a fallback
                    // (this is the original "not ideal but functional" behavior)
                    clear()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not remove file mapping: ${e.message}")
        }
    }
}
