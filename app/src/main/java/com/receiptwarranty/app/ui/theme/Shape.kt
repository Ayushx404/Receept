package com.receiptwarranty.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Tokenized shape scale used consistently across cards, chips, and dialogs. */
object VaultShape {
    val small  = RoundedCornerShape(18.dp)
    val medium = RoundedCornerShape(28.dp)
    val large  = RoundedCornerShape(32.dp)
    val extraLarge = RoundedCornerShape(40.dp)
    val pill   = CircleShape
}

val AppShapes = Shapes(
    small = VaultShape.small,
    medium = VaultShape.medium,
    large = VaultShape.large,
    extraLarge = VaultShape.extraLarge
)
