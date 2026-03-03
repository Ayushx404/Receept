package com.receiptwarranty.app.util.share

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.graphics.withTranslation
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import com.receiptwarranty.app.data.WarrantyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ShareCardGenerator(private val context: Context) {

    suspend fun generateCard(data: ShareCardData): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val style = ShareCardStyle
            val colors = ShareCardStyle.getColors(data.theme)

            // Setup Paints
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.textPrimary
                textSize = style.TEXT_SIZE_TITLE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.textSecondary
                textSize = style.TEXT_SIZE_SUBTITLE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val sectionHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.accent
                textSize = style.TEXT_SIZE_SECTION_HEADER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.1f
            }
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.textPrimary
                textSize = style.TEXT_SIZE_BODY
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.textSecondary
                textSize = style.TEXT_SIZE_CAPTION
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.surface
                if (!colors.isGradient) {
                    setShadowLayer(40f, 0f, 20f, Color.argb(40, 0, 0, 0))
                }
            }
            
            val itemBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.surfaceVariant
            }

            // Calculations
            val width = style.CARD_WIDTH.toInt()
            var currentY = style.PADDING_OUTER * 2 // Start inside card
            val innerContentWidth = width - (style.PADDING_OUTER * 2) - (style.PADDING_INNER * 2)
            val contentStartX = style.PADDING_OUTER + style.PADDING_INNER

            // Load Image if exists
            var heroBitmap: Bitmap? = null
            if (data.heroImageUri != null) {
                heroBitmap = decodeScalingBitmap(data.heroImageUri, innerContentWidth.toInt(), 600)
            }

            // PRE-MEASUREMENT
            val titleLayout = createStaticLayout(data.title, titlePaint, innerContentWidth.toInt())
            val notesLayout = data.notes?.let { createStaticLayout(it, captionPaint, innerContentWidth.toInt()) }

            // Calculate Total Height
            var totalHeight = style.PADDING_OUTER * 2 + style.PADDING_INNER * 2 // Outer Margins + Inner top/bottom padding
            
            // Header height
            totalHeight += 80f // App Logo/Brand reserve
            totalHeight += style.SECTION_SPACING
            
            // Image Height
            if (heroBitmap != null) {
                val scale = innerContentWidth / heroBitmap.width.toFloat()
                val scaledHeight = heroBitmap.height * scale
                totalHeight += scaledHeight + style.SECTION_SPACING
            }
            
            // Title & Subtitle
            totalHeight += titleLayout.height + 20f
            totalHeight += subtitlePaint.textSize + style.SECTION_SPACING
            
            // Info Grid Height
            val gridRows = 2 // (Purchase Date, Price) + (Store, Category)
            totalHeight += (160f * gridRows) + style.SECTION_SPACING
            
            // Warranty Height
            if (data.hasWarranty) {
                totalHeight += style.TEXT_SIZE_SECTION_HEADER + 40f
                totalHeight += 240f + style.SECTION_SPACING
            }
            
            // Notes Height
            if (notesLayout != null) {
                totalHeight += style.TEXT_SIZE_SECTION_HEADER + 40f
                totalHeight += notesLayout.height + style.SECTION_SPACING
            }
            
            // Footer
            totalHeight += 80f 

            // Initialize Bitmap & Canvas
            val bitmap = createBitmap(width, totalHeight.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw Background
            if (colors.isGradient && colors.gradientColors != null) {
                val bgPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, width.toFloat(), totalHeight, 
                        colors.gradientColors, 
                        null, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), totalHeight, bgPaint)
            } else {
                canvas.drawColor(colors.background)
            }

            // Draw Card Base
            val cardRect = RectF(
                style.PADDING_OUTER, 
                style.PADDING_OUTER, 
                width - style.PADDING_OUTER, 
                totalHeight - style.PADDING_OUTER
            )
            canvas.drawRoundRect(cardRect, style.RADIUS_CARD, style.RADIUS_CARD, cardPaint)

            currentY = style.PADDING_OUTER + style.PADDING_INNER

            // --- DRAW CONTENT ---
            
            // App Header
            canvas.drawText("VAULT", contentStartX, currentY + 40f, subtitlePaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = colors.accent })
            val timestamp = android.text.format.DateFormat.format("MMM dd, yyyy", java.util.Date()).toString()
            val timeWidth = captionPaint.measureText(timestamp)
            canvas.drawText(timestamp, width - style.PADDING_OUTER - style.PADDING_INNER - timeWidth, currentY + 40f, captionPaint)
            currentY += 80f + style.SECTION_SPACING

            // Hero Image
            if (heroBitmap != null) {
                val scale = innerContentWidth / heroBitmap.width.toFloat()
                val scaledHeight = heroBitmap.height * scale
                
                val imageRect = RectF(contentStartX, currentY, contentStartX + innerContentWidth, currentY + scaledHeight)
                
                // Draw rounded image
                val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = BitmapShader(
                        Bitmap.createScaledBitmap(heroBitmap, innerContentWidth.toInt(), scaledHeight.toInt(), true),
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
                    ).apply {
                        val matrix = Matrix()
                        matrix.postTranslate(contentStartX, currentY)
                        setLocalMatrix(matrix)
                    }
                }
                canvas.drawRoundRect(imageRect, style.RADIUS_IMAGE, style.RADIUS_IMAGE, imgPaint)
                
                // Subtle border
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = 2f
                    this.color = colors.divider
                }
                canvas.drawRoundRect(imageRect, style.RADIUS_IMAGE, style.RADIUS_IMAGE, borderPaint)
                
                currentY += scaledHeight + style.SECTION_SPACING
            }

            // Title and Category
            canvas.withTranslation(x = contentStartX, y = currentY) {
                titleLayout.draw(this)
            }
            currentY += titleLayout.height + 20f

            canvas.drawText(data.brandCategory, contentStartX, currentY + subtitlePaint.textSize, subtitlePaint)
            currentY += subtitlePaint.textSize + style.SECTION_SPACING

            // Info Grid
            val gridWidth = (innerContentWidth - 40f) / 2
            
            fun drawGridItem(x: Float, y: Float, label: String, value: String) {
                val rect = RectF(x, y, x + gridWidth, y + 140f)
                canvas.drawRoundRect(rect, style.RADIUS_GRID_ITEM, style.RADIUS_GRID_ITEM, itemBackgroundPaint)
                
                canvas.drawText(label, x + 40f, y + 60f, captionPaint)
                // Truncate value if too long
                val availableWidth = gridWidth - 80f
                val truncValue = android.text.TextUtils.ellipsize(value, bodyPaint, availableWidth, android.text.TextUtils.TruncateAt.END).toString()
                canvas.drawText(truncValue, x + 40f, y + 110f, bodyPaint)
            }

            // Row 1
            drawGridItem(contentStartX, currentY, "Purchase Date", data.purchaseDate ?: "—")
            drawGridItem(contentStartX + gridWidth + 40f, currentY, "Amount Paid", data.price ?: "—")
            currentY += 160f
            
            // Row 2
            val storeVal = data.store?.takeIf { it.isNotBlank() } ?: "—"
            val typeVal = data.category ?: data.itemType
            drawGridItem(contentStartX, currentY, "Store / Vendor", storeVal)
            drawGridItem(contentStartX + gridWidth + 40f, currentY, "Category", typeVal)
            currentY += 160f + style.SECTION_SPACING

            // Warranty Section
            if (data.hasWarranty) {
                // Divider
                canvas.drawLine(contentStartX, currentY - style.SECTION_SPACING/2, width - style.PADDING_OUTER - style.PADDING_INNER, currentY - style.SECTION_SPACING/2, Paint().apply { color = colors.divider; strokeWidth = 2f })
                
                canvas.drawText("WARRANTY INFORMATION", contentStartX, currentY + style.TEXT_SIZE_SECTION_HEADER, sectionHeaderPaint)
                currentY += style.TEXT_SIZE_SECTION_HEADER + 40f

                val wRect = RectF(contentStartX, currentY, contentStartX + innerContentWidth, currentY + 240f)
                canvas.drawRoundRect(wRect, style.RADIUS_GRID_ITEM, style.RADIUS_GRID_ITEM, itemBackgroundPaint)

                // Dates
                canvas.drawText("Valid From: ${data.warrantyStartDate ?: "—"}", contentStartX + 40f, currentY + 70f, captionPaint)
                canvas.drawText("Expires On:  ${data.warrantyEndDate ?: "—"}", contentStartX + 40f, currentY + 120f, captionPaint)

                // Status chip
                val (bg, txt, label) = when (data.warrantyStatus) {
                    WarrantyStatus.VALID -> Triple(colors.successBg, colors.successText, "Active")
                    WarrantyStatus.EXPIRING_SOON -> Triple(colors.warningBg, colors.warningText, "Expiring Soon")
                    WarrantyStatus.EXPIRED -> Triple(colors.errorBg, colors.errorText, "Expired")
                    else -> Triple(colors.surfaceVariant, colors.textSecondary, "Unknown")
                }

                val labelWidth = bodyPaint.measureText(label)
                val chipRect = RectF(contentStartX + innerContentWidth - labelWidth - 80f, currentY + 40f, contentStartX + innerContentWidth - 40f, currentY + 100f)
                canvas.drawRoundRect(chipRect, style.RADIUS_CHIP, style.RADIUS_CHIP, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg })
                canvas.drawText(label, chipRect.left + 20f, chipRect.bottom - 20f, bodyPaint.apply { color = txt })
                bodyPaint.color = colors.textPrimary // Restore

                // Progress Bar
                if (data.warrantyProgressPercent != null) {
                    val pRectBg = RectF(contentStartX + 40f, currentY + 170f, contentStartX + innerContentWidth - 40f, currentY + 190f)
                    canvas.drawRoundRect(pRectBg, 10f, 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.divider })

                    val fillWidth = (innerContentWidth - 80f) * data.warrantyProgressPercent
                    val pRectFg = RectF(contentStartX + 40f, currentY + 170f, contentStartX + 40f + fillWidth, currentY + 190f)
                    
                    val gradient = LinearGradient(pRectFg.left, pRectFg.top, pRectFg.right, pRectFg.bottom,
                        colors.accent, ColorUtils.blendARGB(colors.accent, Color.WHITE, 0.3f), Shader.TileMode.CLAMP)
                    canvas.drawRoundRect(pRectFg, 10f, 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient })
                    
                    if (data.warrantyProgressText != null) {
                        canvas.drawText(data.warrantyProgressText, contentStartX + 40f, currentY + 225f, captionPaint)
                    }
                }

                currentY += 240f + style.SECTION_SPACING
            }

            // Notes Section
            if (notesLayout != null) {
                // Divider
                canvas.drawLine(contentStartX, currentY - style.SECTION_SPACING/2, width - style.PADDING_OUTER - style.PADDING_INNER, currentY - style.SECTION_SPACING/2, Paint().apply { color = colors.divider; strokeWidth = 2f })

                canvas.drawText("ADDITIONAL NOTES", contentStartX, currentY + style.TEXT_SIZE_SECTION_HEADER, sectionHeaderPaint)
                currentY += style.TEXT_SIZE_SECTION_HEADER + 40f

                canvas.save()
                canvas.translate(contentStartX, currentY)
                notesLayout.draw(canvas)
                canvas.restore()
                
                currentY += notesLayout.height + style.SECTION_SPACING
            }

            // Footer
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.textTertiary
                textSize = style.TEXT_SIZE_CAPTION
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Generated by Vault App", width / 2f, totalHeight - style.PADDING_OUTER - 40f, footerPaint)

            // Save File
            val file = File(context.cacheDir, "vault_share_card.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            fos.close()
            heroBitmap?.recycle()
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Result.success(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun decodeScalingBitmap(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createStaticLayout(text: String, textPaint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()
    }
}

// Simple ColorUtils stub inside file
object ColorUtils {
    fun blendARGB(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1 - ratio
        val a = (Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio).toInt()
        val r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio).toInt()
        val g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio).toInt()
        val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }
}
