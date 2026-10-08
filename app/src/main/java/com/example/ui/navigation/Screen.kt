package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Bottom Bar tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.ReceiptLong)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Bills : Screen("bills", "Bills", Icons.Default.AccountBalanceWallet)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)

    // Sub-screens under "More"
    object Budgets : Screen("budgets", "Budgets", Icons.Default.Savings)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object PaymentMethods : Screen("payment_methods", "Payment Methods", Icons.Default.Payment)
    object Security : Screen("security", "Security & Privacy", Icons.Default.Security)
    object Backup : Screen("backup", "Backup & Export", Icons.Default.Sync)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Analytics,
    Screen.Bills,
    Screen.More
)
