package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Restaurant" -> Icons.Default.Restaurant
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "Bolt" -> Icons.Default.Bolt
            "WaterDrop" -> Icons.Default.WaterDrop
            "PhoneAndroid" -> Icons.Default.PhoneAndroid
            "Wifi" -> Icons.Default.Wifi
            "Home" -> Icons.Default.Home
            "LocalHospital" -> Icons.Default.LocalHospital
            "School" -> Icons.Default.School
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "Movie" -> Icons.Default.Movie
            "SportsEsports" -> Icons.Default.SportsEsports
            "Flight" -> Icons.Default.Flight
            "Subscriptions" -> Icons.Default.Subscriptions
            "AccountBalance" -> Icons.Default.AccountBalance
            "Security" -> Icons.Default.Security
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "TrendingUp" -> Icons.Default.TrendingUp
            "Payments" -> Icons.Default.Payments
            "QrCode" -> Icons.Default.QrCode
            "CreditCard" -> Icons.Default.CreditCard
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.Category
        }
    }
}
