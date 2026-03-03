package com.receiptwarranty.app.domain.usecases

import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.sync.SyncStateManager
import javax.inject.Inject

class SyncItemsUseCase @Inject constructor(
    private val syncStateManager: SyncStateManager
) {
    suspend fun syncAll(): Result<Pair<Int, Int>> = syncStateManager.syncAll()

    suspend fun syncItem(item: ReceiptWarranty): Result<String> = syncStateManager.syncItem(item)

    suspend fun deleteItem(item: ReceiptWarranty): Result<Unit> = syncStateManager.deleteItem(item)
}
