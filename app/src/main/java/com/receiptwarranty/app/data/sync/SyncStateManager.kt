package com.receiptwarranty.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyDao
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.data.remote.DriveStorageManager
import com.receiptwarranty.app.data.remote.DriveUploadResult
import com.receiptwarranty.app.data.remote.FirestoreRepository
import com.receiptwarranty.app.data.auth.GoogleAuthManager
import com.receiptwarranty.app.util.PreferenceKeys
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ReceiptWarrantyDao,
    private val repository: ReceiptWarrantyRepository,
    private val firestoreRepository: FirestoreRepository,
    private val authManager: GoogleAuthManager,
    private val driveStorageManager: DriveStorageManager,
    private val reminderScheduler: WarrantyReminderScheduler
) {
    private val TAG = "SyncStateManager"
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val userId: String
        get() = authManager.getCurrentUser()?.uid ?: "local"
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncAll(): Result<Pair<Int, Int>> {
        if (!isOnline()) {
            _syncStatus.value = SyncStatus.Error("No internet connection")
            return Result.failure(Exception("No internet connection"))
        }

        _syncStatus.value = SyncStatus.Syncing

        var uploadedCount = 0
        var downloadedCount = 0
        val uploadErrors = mutableListOf<String>()
        val downloadErrors = mutableListOf<String>()

        try {
            val prefs = context.getSharedPreferences(PreferenceKeys.SYNC_PREFS, Context.MODE_PRIVATE)
            val lastSyncTime = prefs.getLong(PreferenceKeys.LAST_SUCCESSFUL_SYNC, 0L)
            
            val localItems = dao.getAll().first()
            for (item in localItems) {
                try {
                    var imageUrlToSave = item.imageUri
                    var driveFileIdToSave = item.driveFileId
                    
                    if (!item.imageUri.isNullOrEmpty() && 
                        !item.imageUri.startsWith("http") && 
                        !item.imageUri.startsWith("https") &&
                        item.driveFileId.isNullOrEmpty()) {
                        
                        val uploadResult = driveStorageManager.uploadImage(item.imageUri, item.id)
                        if (uploadResult.isSuccess) {
                            val driveResult = uploadResult.getOrNull()
                            if (driveResult != null) {
                                // Use downloadLink for image loading, webViewLink as fallback
                                imageUrlToSave = driveResult.downloadLink ?: driveResult.webViewLink
                                driveFileIdToSave = driveResult.fileId
                            }
                        } else if (uploadResult.isFailure) {
                            uploadErrors.add("${item.title}: Image upload failed")
                        }
                    }

                    val itemToUpload = item.copy(
                        imageUri = imageUrlToSave,
                        driveFileId = driveFileIdToSave
                    )
                    val result = firestoreRepository.uploadItem(itemToUpload)
                    if (result.isSuccess) {
                        uploadedCount++
                        val newCloudId = result.getOrNull()
                        if (newCloudId != null) {
                            // Update ONLY the sync identifiers locally, leaving the local imageUri (content://) intact
                            val savedItem = item.copy(cloudId = newCloudId, driveFileId = driveFileIdToSave)
                            dao.update(savedItem)
                            Log.d(TAG, "Updated local DB with new cloudId: $newCloudId and driveFileId: $driveFileIdToSave for item: ${item.title}")
                        }
                    } else {
                        uploadErrors.add("${item.title}: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    uploadErrors.add("${item.title}: ${e.message}")
                }
            }

            val downloadResult = firestoreRepository.downloadItemsSince(lastSyncTime)
            if (downloadResult.isSuccess) {
                val cloudItems: List<ReceiptWarranty> = downloadResult.getOrNull() ?: emptyList()
                
                // Get all deleted cloud IDs to skip them during sync
                val deletedCloudIds = repository.getAllDeletedCloudIds(userId).toSet()
                
                for (cloudItem in cloudItems) {
                    try {
                        // Skip items that have been marked as deleted locally
                        if (cloudItem.cloudId != null && deletedCloudIds.contains(cloudItem.cloudId)) {
                            Log.d(TAG, "Skipping deleted item: ${cloudItem.title}")
                            continue
                        }
                        
                        var finalImageUri = cloudItem.imageUri
                        
                        // If cloud item has a Drive URL or driveFileId, download the image locally
                        if (!cloudItem.imageUri.isNullOrEmpty() && 
                            (cloudItem.imageUri.contains("drive.google.com") || !cloudItem.driveFileId.isNullOrEmpty())) {
                            
                            val fileIdToDownload = cloudItem.driveFileId ?: extractFileIdFromDriveUrl(cloudItem.imageUri)
                            if (fileIdToDownload != null) {
                                val fileName = "receipt_${cloudItem.id}_${System.currentTimeMillis()}.jpg"
                                val downloadResultLocal = driveStorageManager.downloadImage(fileIdToDownload, fileName)
                                if (downloadResultLocal.isSuccess) {
                                    finalImageUri = downloadResultLocal.getOrNull()
                                    Log.d(TAG, "Downloaded image to local: $finalImageUri")
                                } else {
                                    Log.e(TAG, "Failed to download image for ${cloudItem.title}: ${downloadResultLocal.exceptionOrNull()?.message}")
                                }
                            }
                        }
                        
                        val existing = dao.findByUniqueFields(
                            cloudItem.title,
                            cloudItem.category,
                            cloudItem.purchaseDate
                        )
                        if (existing != null) {
                            // Timestamp-based conflict resolution
                            // Compare updatedAt timestamps to determine which version is newer
                            if (cloudItem.updatedAt > existing.updatedAt) {
                                // Cloud version is newer - update local
                                Log.d(TAG, "Updating local item ${cloudItem.title} with newer cloud version")
                                val driveFileIdToUse = cloudItem.driveFileId ?: existing.driveFileId
                                val imageToUse = if (!finalImageUri.isNullOrEmpty()) finalImageUri else existing.imageUri
                                val cloudIdToUse = cloudItem.cloudId ?: existing.cloudId
                                val updatedItem = cloudItem.copy(
                                    id = existing.id, 
                                    imageUri = imageToUse,
                                    driveFileId = driveFileIdToUse,
                                    cloudId = cloudIdToUse
                                )
                                dao.update(updatedItem)
                                if (updatedItem.type == ReceiptType.WARRANTY) {
                                    reminderScheduler.scheduleReminder(updatedItem)
                                }
                                downloadedCount++
                            } else {
                                // Local version is newer or same - keep local, but update cloudId if missing
                                Log.d(TAG, "Keeping local item ${cloudItem.title} (local version is newer)")
                                if (existing.cloudId == null && cloudItem.cloudId != null) {
                                    val updatedExisting = existing.copy(cloudId = cloudItem.cloudId)
                                    dao.update(updatedExisting)
                                }
                            }
                        } else {
                            // New item from cloud - insert with cloudId
                            val itemWithLocalImage = cloudItem.copy(imageUri = finalImageUri)
                            dao.insert(itemWithLocalImage)
                            if (itemWithLocalImage.type == ReceiptType.WARRANTY) {
                                reminderScheduler.scheduleReminder(itemWithLocalImage)
                            }
                            downloadedCount++
                        }
                    } catch (e: Exception) {
                        downloadErrors.add("${cloudItem.title}: ${e.message}")
                    }
                }
            }

            val currentSyncTime = System.currentTimeMillis()
            prefs.edit().putLong(PreferenceKeys.LAST_SUCCESSFUL_SYNC, currentSyncTime).apply()
            _lastSyncTime.value = currentSyncTime
            
            // Clean up old deleted items (older than 30 days)
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                repository.cleanOldDeletedItems(userId, thirtyDaysAgo)
                Log.d(TAG, "Cleaned up old deleted items")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean up old deleted items: ${e.message}")
            }
            
            val errorMessage = buildString {
                if (uploadErrors.isNotEmpty()) {
                    append("Upload issues: ${uploadErrors.size}")
                }
                if (downloadErrors.isNotEmpty()) {
                    if (isNotEmpty()) append("; ")
                    append("Download issues: ${downloadErrors.size}")
                }
            }
            
            _syncStatus.value = if (errorMessage.isNotEmpty() && uploadedCount == 0 && downloadedCount == 0) {
                SyncStatus.Error(errorMessage)
            } else {
                SyncStatus.Success
            }
            
            return Result.success(Pair(uploadedCount, downloadedCount))
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            return Result.failure(e)
        }
    }

    fun resetStatus() {
        _syncStatus.value = SyncStatus.Idle
    }

    fun syncItemAsync(item: ReceiptWarranty) {
        applicationScope.launch {
            try {
                syncItem(item)
            } catch (e: Exception) {
                Log.e(TAG, "Async sync failed for item: ${item.title}", e)
            }
        }
    }

    suspend fun syncItem(item: ReceiptWarranty): Result<String> {
        Log.d(TAG, "syncItem called for: ${item.title}, imageUri: ${item.imageUri}")
        
        if (!isOnline()) {
            Log.w(TAG, "No internet connection")
            return Result.failure(Exception("No internet connection"))
        }

        try {
            var imageUrlToSave = item.imageUri
            var driveFileIdToSave = item.driveFileId
            
            if (!item.imageUri.isNullOrEmpty() && 
                !item.imageUri.startsWith("http") && 
                !item.imageUri.startsWith("https") &&
                item.driveFileId.isNullOrEmpty()) {
                Log.d(TAG, "Uploading local image to Google Drive: ${item.imageUri}")
                val uploadResult = driveStorageManager.uploadImage(item.imageUri, item.id)
                if (uploadResult.isSuccess) {
                    val driveResult = uploadResult.getOrNull()
                    if (driveResult != null) {
                        // Use downloadLink for image loading, webViewLink as fallback
                        imageUrlToSave = driveResult.downloadLink ?: driveResult.webViewLink
                        driveFileIdToSave = driveResult.fileId
                        Log.d(TAG, "Image uploaded to Drive, URL: $imageUrlToSave, File ID: $driveFileIdToSave")
                    }
                } else if (uploadResult.isFailure) {
                    Log.e(TAG, "Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Image upload failed"))
                }
            } else {
                Log.d(TAG, "No local image to upload or already has cloud URL")
            }

            val itemToUpload = item.copy(
                imageUri = imageUrlToSave,
                driveFileId = driveFileIdToSave
            )
            val result = firestoreRepository.uploadItem(itemToUpload)
            Log.d(TAG, "Firestore upload result: ${result.isSuccess}")
            
            if (result.isSuccess) {
                val newCloudId = result.getOrNull()
                if (newCloudId != null) {
                    // Update ONLY the sync identifiers locally, leaving the local imageUri (content://) intact
                    val savedItem = item.copy(cloudId = newCloudId, driveFileId = driveFileIdToSave)
                    dao.update(savedItem)
                    Log.d(TAG, "Updated local DB with new cloudId: $newCloudId and driveFileId: $driveFileIdToSave for item: ${item.title}")
                }
            }
            
            return result
        } catch (e: Exception) {
            Log.e(TAG, "syncItem failed: ${e.message}", e)
            return Result.failure(e)
        }
    }
    
    private fun extractFileIdFromDriveUrl(url: String): String? {
        return try {
            // Handle formats like:
            // https://drive.google.com/file/d/FILE_ID/view
            // https://drive.google.com/uc?export=download&id=FILE_ID
            val regex = Regex("(?:file/d/|id=)([a-zA-Z0-9_-]+)")
            val match = regex.find(url)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract file ID from URL: $url")
            null
        }
    }
    
    suspend fun deleteItem(item: ReceiptWarranty): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting item: ${item.title}, cloudId: ${item.cloudId}, driveFileId: ${item.driveFileId}")
            
            // 1. Delete from Firestore using cloudId
            if (!item.cloudId.isNullOrEmpty()) {
                val deleteResult = firestoreRepository.deleteItem(item.cloudId)
                if (deleteResult.isFailure) {
                    Log.e(TAG, "Failed to delete from Firestore: ${deleteResult.exceptionOrNull()?.message}")
                } else {
                    // Track deletion in local database to prevent sync from re-adding it
                    repository.trackDeletion(item.cloudId!!, userId)
                    Log.d(TAG, "Tracked deletion for cloudId: ${item.cloudId}")
                }
            } else {
                Log.w(TAG, "No cloudId found, skipping Firestore deletion")
            }
            
            // 2. Delete image from Google Drive
            if (!item.driveFileId.isNullOrEmpty()) {
                val driveDeleteResult = driveStorageManager.deleteImage(item.driveFileId)
                if (driveDeleteResult.isFailure) {
                    Log.e(TAG, "Failed to delete from Drive: ${driveDeleteResult.exceptionOrNull()?.message}")
                }
            } else {
                Log.w(TAG, "No driveFileId found, skipping Drive deletion")
            }
            
            // 3. Delete from local Room database (caller should do this)
            Log.d(TAG, "Delete from cloud completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
