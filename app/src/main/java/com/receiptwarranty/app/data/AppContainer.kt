package com.receiptwarranty.app.data

import android.content.Context
import com.receiptwarranty.app.data.auth.GoogleAuthManager
import com.receiptwarranty.app.data.remote.DriveStorageManager
import com.receiptwarranty.app.data.remote.FirestoreRepository
import com.receiptwarranty.app.data.sync.SyncManager
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.workers.WarrantyReminderScheduler

class AppContainer(private val context: Context) {
    val database = AppDatabase.getInstance(context)
    val receiptWarrantyDao = database.receiptWarrantyDao()
    val repository = ReceiptWarrantyRepository(receiptWarrantyDao)
    val warrantyReminderScheduler: WarrantyReminderScheduler by lazy {
        WarrantyReminderScheduler(context)
    }

    var googleAuthManager: GoogleAuthManager? = null
    var firestoreRepository: FirestoreRepository? = null
    var syncManager: SyncManager? = null
    var syncStateManager: SyncStateManager? = null
    var driveStorageManager: DriveStorageManager? = null
    var currentUserId: String? = null

    fun initializeAuth(activity: Context) {
        googleAuthManager = GoogleAuthManager(activity as android.app.Activity)
    }

    fun initializeFirestore(userId: String) {
        currentUserId = userId
        firestoreRepository = FirestoreRepository(userId)
        driveStorageManager = DriveStorageManager(context, userId)
        syncManager = SyncManager(context, receiptWarrantyDao, firestoreRepository!!, userId)
        syncStateManager = SyncStateManager(context, receiptWarrantyDao, repository, firestoreRepository!!, userId, driveStorageManager)
    }
}
