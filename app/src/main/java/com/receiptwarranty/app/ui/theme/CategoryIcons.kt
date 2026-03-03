package com.receiptwarranty.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.receiptwarranty.app.data.IconPackStyle

object CategoryIcons {
    fun getIcon(category: String?, style: IconPackStyle): ImageVector {
        return when (style) {
            IconPackStyle.LUCIDE -> getLucideIcon(category)
            IconPackStyle.PHOSPHOR_DUOTONE -> getPhosphorIcon(category)
            IconPackStyle.MATERIAL_TWOTONE -> getTwoToneIcon(category)
        }
    }

    private fun getLucideIcon(category: String?): ImageVector {
        return when (category) {
            "Electronics" -> Icons.Outlined.Devices
            "Appliances" -> Icons.Outlined.Kitchen
            "Furniture" -> Icons.Outlined.Chair
            "Vehicles" -> Icons.Outlined.DirectionsCar
            "Clothing" -> Icons.Outlined.Checkroom
            "Sports Equipment" -> Icons.Outlined.SportsBasketball
            "Tools" -> Icons.Outlined.Construction
            "Books" -> Icons.Outlined.MenuBook
            "Gaming" -> Icons.Outlined.Gamepad
            "Health & Beauty" -> Icons.Outlined.HealthAndSafety
            "Groceries" -> Icons.Outlined.ShoppingCart
            "Dining" -> Icons.Outlined.Restaurant
            "Travel" -> Icons.Outlined.Flight
            "Subscription" -> Icons.Outlined.Subscriptions
            "Services" -> Icons.Outlined.SettingsSuggest
            "Personal Care" -> Icons.Outlined.Spa
            "Education" -> Icons.Outlined.School
            "Business" -> Icons.Outlined.Business
            "Home Decor" -> Icons.Outlined.Brush
            "Stationery" -> Icons.Outlined.Edit
            "Electricity" -> Icons.Outlined.FlashOn
            "Water" -> Icons.Outlined.WaterDrop
            "Internet" -> Icons.Outlined.Wifi
            "Mobile" -> Icons.Outlined.Smartphone
            "Insurance" -> Icons.Outlined.Security
            "Rent" -> Icons.Outlined.Home
            "Streaming" -> Icons.Outlined.OndemandVideo
            "EMI" -> Icons.Outlined.CreditCard
            "Utilities" -> Icons.Outlined.Settings
            else -> Icons.Outlined.Category
        }
    }

    private fun getPhosphorIcon(category: String?): ImageVector {
        // Phosphor is rounded and soft
        return when (category) {
            "Electronics" -> Icons.Rounded.Devices
            "Appliances" -> Icons.Rounded.Kitchen
            "Furniture" -> Icons.Rounded.Chair
            "Vehicles" -> Icons.Rounded.DirectionsCar
            "Clothing" -> Icons.Rounded.Checkroom
            "Sports Equipment" -> Icons.Rounded.SportsBasketball
            "Tools" -> Icons.Rounded.Construction
            "Books" -> Icons.Rounded.MenuBook
            "Gaming" -> Icons.Rounded.Gamepad
            "Health & Beauty" -> Icons.Rounded.HealthAndSafety
            "Groceries" -> Icons.Rounded.ShoppingCart
            "Dining" -> Icons.Rounded.Restaurant
            "Travel" -> Icons.Rounded.Flight
            "Subscription" -> Icons.Rounded.Subscriptions
            "Services" -> Icons.Rounded.SettingsSuggest
            "Personal Care" -> Icons.Rounded.Spa
            "Education" -> Icons.Rounded.School
            "Business" -> Icons.Rounded.Business
            "Home Decor" -> Icons.Rounded.Brush
            "Stationery" -> Icons.Rounded.Edit
            "Electricity" -> Icons.Rounded.FlashOn
            "Water" -> Icons.Rounded.WaterDrop
            "Internet" -> Icons.Rounded.Wifi
            "Mobile" -> Icons.Rounded.Smartphone
            "Insurance" -> Icons.Rounded.Security
            "Rent" -> Icons.Rounded.Home
            "Streaming" -> Icons.Rounded.OndemandVideo
            "EMI" -> Icons.Rounded.CreditCard
            "Utilities" -> Icons.Rounded.Settings
            else -> Icons.Rounded.Category
        }
    }

    private fun getTwoToneIcon(category: String?): ImageVector {
        return when (category) {
            "Electronics" -> Icons.TwoTone.Devices
            "Appliances" -> Icons.TwoTone.Kitchen
            "Furniture" -> Icons.TwoTone.Chair
            "Vehicles" -> Icons.TwoTone.DirectionsCar
            "Clothing" -> Icons.TwoTone.Checkroom
            "Sports Equipment" -> Icons.TwoTone.SportsBasketball
            "Tools" -> Icons.TwoTone.Construction
            "Books" -> Icons.TwoTone.MenuBook
            "Gaming" -> Icons.TwoTone.Gamepad
            "Health & Beauty" -> Icons.TwoTone.HealthAndSafety
            "Groceries" -> Icons.TwoTone.ShoppingCart
            "Dining" -> Icons.TwoTone.Restaurant
            "Travel" -> Icons.TwoTone.Flight
            "Subscription" -> Icons.TwoTone.Subscriptions
            "Services" -> Icons.TwoTone.SettingsSuggest
            "Personal Care" -> Icons.TwoTone.Spa
            "Education" -> Icons.TwoTone.School
            "Business" -> Icons.TwoTone.Business
            "Home Decor" -> Icons.TwoTone.Brush
            "Stationery" -> Icons.TwoTone.Edit
            "Electricity" -> Icons.TwoTone.FlashOn
            "Water" -> Icons.TwoTone.WaterDrop
            "Internet" -> Icons.TwoTone.Wifi
            "Mobile" -> Icons.TwoTone.Smartphone
            "Insurance" -> Icons.TwoTone.Security
            "Rent" -> Icons.TwoTone.Home
            "Streaming" -> Icons.TwoTone.OndemandVideo
            "EMI" -> Icons.TwoTone.CreditCard
            "Utilities" -> Icons.TwoTone.Settings
            else -> Icons.TwoTone.Category
        }
    }
}
