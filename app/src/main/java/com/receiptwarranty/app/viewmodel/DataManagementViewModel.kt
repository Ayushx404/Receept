package com.receiptwarranty.app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.util.ExportCategory
import com.receiptwarranty.app.util.ExportDateRange
import com.receiptwarranty.app.util.ExportDestination
import com.receiptwarranty.app.util.JsonExporter
import com.receiptwarranty.app.util.JsonImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportState(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val lastExportUri: Uri? = null,
    val error: String? = null
)

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Preview(val preview: com.receiptwarranty.app.data.model.ImportPreview) : ImportState()
    data class Importing(val progress: Float) : ImportState()
    data class Success(val imported: Int, val replaced: Int, val skipped: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val repository: ReceiptWarrantyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private var currentImportUri: Uri? = null
    private var currentPreview: com.receiptwarranty.app.data.model.ImportPreview? = null

    fun loadImportPreview(context: Context, uri: Uri) {
        currentImportUri = uri
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val result = JsonImporter.getImportPreview(context, uri)
                if (result.isSuccess) {
                    currentPreview = result.getOrNull()
                    _importState.value = ImportState.Preview(result.getOrNull()!!)
                } else {
                    _importState.value = ImportState.Error(result.exceptionOrNull()?.message ?: "Failed to parse file")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun performImport() {
        val uri = currentImportUri ?: return
        val preview = currentPreview ?: return

        viewModelScope.launch {
            _importState.value = ImportState.Importing(0f)
            try {
                _importState.value = ImportState.Importing(0.3f)

                val existingItems = repository.getAllItemsForExport()
                
                val importResult = JsonImporter.importDataWithAutoConflict(
                    context = context,
                    uri = uri,
                    existingItems = existingItems
                )

                _importState.value = ImportState.Importing(0.8f)

                if (importResult.isSuccess) {
                    val result = importResult.getOrNull()!!
                    var totalImported = 0
                    
                    for (item in result.items) {
                        repository.insert(item)
                        totalImported++
                    }

                    _importState.value = ImportState.Success(
                        imported = result.totalImported,
                        replaced = 0,
                        skipped = result.totalSkipped
                    )
                } else {
                    _importState.value = ImportState.Error(importResult.exceptionOrNull()?.message ?: "Import failed")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun exportData(
        context: Context,
        category: ExportCategory,
        dateRange: ExportDateRange,
        customStartDate: Long?,
        customEndDate: Long?,
        includeImages: Boolean,
        destination: ExportDestination
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState(isExporting = true, progress = 0f)

            try {
                val items = repository.getAllItemsForExport()
                _exportState.value = _exportState.value.copy(progress = 0.3f)

                val filteredItems = filterItems(items, category, dateRange, customStartDate, customEndDate)
                _exportState.value = _exportState.value.copy(progress = 0.5f)

                val result = when (destination) {
                    ExportDestination.DOWNLOADS -> {
                        JsonExporter.exportToJson(
                            context = context,
                            items = filteredItems,
                            category = category,
                            dateRange = dateRange,
                            customStartDate = customStartDate,
                            customEndDate = customEndDate,
                            includeImages = includeImages
                        )
                    }
                    ExportDestination.SHARE -> {
                        JsonExporter.shareJson(
                            context = context,
                            items = filteredItems,
                            category = category,
                            dateRange = dateRange,
                            customStartDate = customStartDate,
                            customEndDate = customEndDate,
                            includeImages = includeImages
                        )
                    }
                }

                _exportState.value = _exportState.value.copy(progress = 1f)

                if (result.isSuccess) {
                    val uri = result.getOrNull()!!
                    _exportState.value = ExportState(
                        isExporting = false,
                        progress = 1f,
                        lastExportUri = uri
                    )

                    if (destination == ExportDestination.SHARE) {
                        shareFile(context, uri)
                    } else {
                        Toast.makeText(context, "Exported to Downloads", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Export failed"
                    _exportState.value = ExportState(error = error)
                    Toast.makeText(context, "Export failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _exportState.value = ExportState(error = e.message)
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState()
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
        currentImportUri = null
        currentPreview = null
    }

    private fun filterItems(
        items: List<com.receiptwarranty.app.data.ReceiptWarranty>,
        category: ExportCategory,
        dateRange: ExportDateRange,
        customStartDate: Long?,
        customEndDate: Long?
    ): List<com.receiptwarranty.app.data.ReceiptWarranty> {
        var result = items.filter { !it.isDeleted }

        result = when (category) {
            ExportCategory.ALL -> result
            ExportCategory.RECEIPTS -> result.filter { it.type == ReceiptType.RECEIPT }
            ExportCategory.WARRANTIES -> result.filter { it.type == ReceiptType.WARRANTY }
            ExportCategory.BILLS -> result.filter { it.type == ReceiptType.BILL }
            ExportCategory.SUBSCRIPTIONS -> result.filter { it.type == ReceiptType.SUBSCRIPTION }
        }

        val now = System.currentTimeMillis()
        val startDate = when (dateRange) {
            ExportDateRange.ALL_TIME -> null
            ExportDateRange.LAST_MONTH -> now - (30L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_3_MONTHS -> now - (90L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_6_MONTHS -> now - (180L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_YEAR -> now - (365L * 24 * 60 * 60 * 1000)
            ExportDateRange.CUSTOM -> customStartDate
        }

        val endDate = if (dateRange == ExportDateRange.CUSTOM) customEndDate else now

        if (startDate != null) {
            result = result.filter { item ->
                val itemDate = item.purchaseDate ?: item.createdAt
                itemDate >= startDate
            }
        }

        if (endDate != null) {
            result = result.filter { item ->
                val itemDate = item.purchaseDate ?: item.createdAt
                itemDate <= endDate
            }
        }

        return result
    }

    private fun shareFile(context: Context, uri: Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Vault Backup"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
