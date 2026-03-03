package com.receiptwarranty.app.util.share

import android.net.Uri
import com.receiptwarranty.app.data.WarrantyStatus

data class ShareCardData(
    val title: String,
    val brandCategory: String,
    val purchaseDate: String?,
    val price: String?,
    val store: String?,
    val category: String?,
    
    // Warranty info
    val hasWarranty: Boolean,
    val warrantyStartDate: String?,
    val warrantyEndDate: String?,
    val warrantyStatus: WarrantyStatus?,
    val warrantyProgressText: String?,
    val warrantyProgressPercent: Float?, // 0.0 to 1.0

    // Item type text (Receipt, Warranty, etc)
    val itemType: String,
    
    // Photos
    val heroImageUri: Uri?,
    
    // Notes
    val notes: String?,

    // Theme (added)
    val theme: ShareTheme
)
