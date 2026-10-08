package com.example.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    onLockAppNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "More & Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Financial Tools",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MoreMenuCard(
                    title = "AI Expense Assistant",
                    subtitle = "Ask questions using your real records via Gemini 3.5 Flash",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF00A86B),
                    onClick = { onNavigate(Screen.Chat.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Monthly Budgets",
                    subtitle = "Set category-wise and overall spending limits",
                    icon = Icons.Default.Savings,
                    iconTint = Color(0xFF7B1FA2),
                    onClick = { onNavigate(Screen.Budgets.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Calendar View",
                    subtitle = "Inspect daily expense & income transactions",
                    icon = Icons.Default.CalendarMonth,
                    iconTint = Color(0xFF0288D1),
                    onClick = { onNavigate(Screen.Calendar.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Categories & Subcategories",
                    subtitle = "Manage expense and income tags",
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFF00A86B),
                    onClick = { onNavigate(Screen.Categories.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Payment Methods",
                    subtitle = "Cash, UPI, Credit Card, Bank Accounts",
                    icon = Icons.Default.Payment,
                    iconTint = Color(0xFFF57C00),
                    onClick = { onNavigate(Screen.PaymentMethods.route) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Security & Data",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MoreMenuCard(
                    title = "Security & Privacy",
                    subtitle = "PIN lock, Biometrics, Screenshot protection, Auto-lock",
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF00A86B),
                    onClick = { onNavigate(Screen.Security.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Backup & Export",
                    subtitle = "Export JSON/CSV, restore local financial data",
                    icon = Icons.Default.Sync,
                    iconTint = Color(0xFF00B4D8),
                    onClick = { onNavigate(Screen.Backup.route) }
                )
            }

            item {
                MoreMenuCard(
                    title = "Lock App Now",
                    subtitle = "Immediately lock the session",
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFFE53935),
                    onClick = onLockAppNow
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Expense Manager • 100% Offline & Private",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No cloud syncing, no trackers, no external APIs. Data resides solely on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
