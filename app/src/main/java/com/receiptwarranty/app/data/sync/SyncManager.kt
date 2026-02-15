package com.receiptwarranty.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptWarrantyDao
import com.receiptwarranty.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.first

class SyncManager(
    private val context: Context,
    private val dao: ReceiptWarrantyDao,
    private val firestoreRepository: FirestoreRepository,
    private val userId: String
) {

    private var _isSyncing = false
    val isSyncing: Boolean get() = _isSyncing

    private var _lastSyncTime = 0L
    val lastSyncTime: Long get() = _lastSyncTime

    private var _syncError: String? = null
    val syncError: String? get() = _syncError

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncUp(): Result<Int> {
        if (!isOnline()) {
            return Result.failure(Exception("No internet connection"))
        }

        _isSyncing = true
        _syncError = null

        return try {
            val localItems = dao.getAll().first()
            var syncedCount = 0

            for (item in localItems) {
                try {
                    val result = firestoreRepository.uploadItem(item)
                    if (result.isSuccess) {
                        syncedCount++
                    }
                } catch (e: Exception) {
                    // Continue with other items
                }
            }

            _lastSyncTime = System.currentTimeMillis()
            _isSyncing = false
            Result.success(syncedCount)
        } catch (e: Exception) {
            _syncError = e.message
            _isSyncing = false
            Result.failure(e)
        }
    }

    suspend fun syncDown(): Result<Int> {
        if (!isOnline()) {
            return Result.failure(Exception("No internet connection"))
        }

        _isSyncing = true
        _syncError = null

        return try {
            val result = firestoreRepository.downloadAllItems()
            if (result.isFailure) {
                _isSyncing = false
                return Result.failure(result.exceptionOrNull() ?: Exception("Download failed"))
            }

            // Download cloud items - sync by unique fields
            val cloudItems: List<ReceiptWarranty> = result.getOrNull() ?: emptyList()
            var syncedCount = 0

            for (cloudItem in cloudItems) {
                try {
                    // Find existing item by unique fields
                    val existing = dao.findByUniqueFields(
                        cloudItem.title,
                        cloudItem.company,
                        cloudItem.purchaseDate
                    )
                    if (existing != null) {
                        // Item exists - update with cloud data (preserve local ID)
                        val updatedItem = cloudItem.copy(id = existing.id)
                        dao.update(updatedItem)
                        syncedCount++
                    } else {
                        // New item - insert it
                        val id = dao.insert(cloudItem)
                        if (id > 0) syncedCount++
                    }
                } catch (e: Exception) {
                    // Continue
                }
            }

            _lastSyncTime = System.currentTimeMillis()
            _isSyncing = false
            Result.success(syncedCount)
        } catch (e: Exception) {
            _syncError = e.message
            _isSyncing = false
            Result.failure(e)
        }
    }

    suspend fun fullSync(): Result<Pair<Int, Int>> {
        if (!isOnline()) {
            return Result.failure(Exception("No internet connection"))
        }

        _isSyncing = true
        _syncError = null

        return try {
            // First sync down (get cloud changes)
            val downloadResult = firestoreRepository.downloadAllItems()
            if (downloadResult.isFailure) {
                _isSyncing = false
                return Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
            }

            val cloudItems: List<ReceiptWarranty> = downloadResult.getOrNull() ?: emptyList()
            var downloadedCount = 0

            // Merge cloud items to local - sync by unique fields
            for (cloudItem in cloudItems) {
                try {
                    // Find existing item by unique fields
                    val existing = dao.findByUniqueFields(
                        cloudItem.title,
                        cloudItem.company,
                        cloudItem.purchaseDate
                    )
                    if (existing != null) {
                        // Item exists - update with cloud data (preserve local ID)
                        val updatedItem = cloudItem.copy(id = existing.id)
                        dao.update(updatedItem)
                        downloadedCount++
                    } else {
                        // New item - insert it
                        val id = dao.insert(cloudItem)
                        if (id > 0) downloadedCount++
                    }
                } catch (e: Exception) {
                    // Continue
                }
            }

            // Then sync up (upload local changes)
            val localItems = dao.getAll().first()
            var uploadedCount = 0

            for (item in localItems) {
                try {
                    firestoreRepository.uploadItem(item)
                    uploadedCount++
                } catch (e: Exception) {
                    // Continue
                }
            }

            _lastSyncTime = System.currentTimeMillis()
            _isSyncing = false
            Result.success(Pair(downloadedCount, uploadedCount))
        } catch (e: Exception) {
            _syncError = e.message
            _isSyncing = false
            Result.failure(e)
        }
    }

    fun clearError() {
        _syncError = null
    }
}
