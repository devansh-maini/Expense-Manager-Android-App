package com.example.ui.screens.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BillEntity
import com.example.ui.components.CategoryIconHelper
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: BillsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<BillEntity?>(null) }
    var billToDelete by remember { mutableStateOf<BillEntity?>(null) }

    val catMap = remember(uiState.categories) {
        uiState.categories.associateBy { it.id }
    }

    val (unpaidBills, paidBills) = remember(uiState.bills) {
        uiState.bills.partition { !it.isPaid }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bills & Subscriptions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBill = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("bills_fab_add")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bills_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pending Bills Due",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.formatPaise(uiState.totalPendingPaise),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE53935).copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${uiState.pendingCount} unpaid",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Unpaid Bills Section
            item {
                Text(
                    text = "Upcoming & Unpaid (${unpaidBills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (unpaidBills.isEmpty()) {
                item {
                    Text(
                        text = "No pending bills! You are all caught up.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(unpaidBills, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        categoryName = catMap[bill.categoryId]?.name ?: "Bills",
                        onTogglePaid = { viewModel.togglePaid(bill) },
                        onEdit = {
                            editingBill = bill
                            showAddDialog = true
                        },
                        onDelete = { billToDelete = bill }
                    )
                }
            }

            // Paid Bills Section
            if (paidBills.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paid (${paidBills.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(paidBills, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        categoryName = catMap[bill.categoryId]?.name ?: "Bills",
                        onTogglePaid = { viewModel.togglePaid(bill) },
                        onEdit = {
                            editingBill = bill
                            showAddDialog = true
                        },
                        onDelete = { billToDelete = bill }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add / Edit Bill Dialog
    if (showAddDialog) {
        BillFormDialog(
            bill = editingBill,
            categories = uiState.categories,
            onDismiss = {
                showAddDialog = false
                editingBill = null
            },
            onSave = { bill ->
                if (editingBill != null) {
                    viewModel.updateBill(bill)
                } else {
                    viewModel.addBill(bill)
                }
                showAddDialog = false
                editingBill = null
            }
        )
    }

    // Delete Confirmation
    if (billToDelete != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Delete Bill") },
            text = { Text("Delete '${billToDelete?.title}'? This will remove upcoming reminders.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        billToDelete?.let { viewModel.deleteBill(it) }
                        billToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BillCard(
    bill: BillEntity,
    categoryName: String,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTogglePaid) {
                Icon(
                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Paid",
                    tint = if (bill.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Due ${DateUtils.formatDisplayDate(bill.dueDate)} • ${bill.recurrence}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatPaise(bill.amountPaise),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isPaid) Color(0xFF2E7D32) else Color(0xFFE53935)
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillFormDialog(
    bill: BillEntity?,
    categories: List<com.example.data.local.entity.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (BillEntity) -> Unit
) {
    var title by remember { mutableStateOf(bill?.title ?: "") }
    var amountText by remember { mutableStateOf(bill?.let { CurrencyFormatter.paiseToDecimalString(it.amountPaise) } ?: "") }
    var selectedCategoryId by remember { mutableLongStateOf(bill?.categoryId ?: categories.firstOrNull()?.id ?: 1L) }
    var recurrence by remember { mutableStateOf(bill?.recurrence ?: "MONTHLY") }
    var recurrenceExpanded by remember { mutableStateOf(false) }

    val recurrenceOptions = listOf("MONTHLY", "WEEKLY", "QUARTERLY", "YEARLY", "ONE_TIME")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bill != null) "Edit Bill" else "Add New Bill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Subscription Name") },
                    placeholder = { Text("e.g. Netflix, Electricity, Rent") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = recurrenceExpanded,
                    onExpandedChange = { recurrenceExpanded = !recurrenceExpanded }
                ) {
                    OutlinedTextField(
                        value = recurrence,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = recurrenceExpanded,
                        onDismissRequest = { recurrenceExpanded = false }
                    ) {
                        for (opt in recurrenceOptions) {
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    recurrence = opt
                                    recurrenceExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amountPaise = CurrencyFormatter.parseAmountToPaise(amountText)
            Button(
                onClick = {
                    val entity = BillEntity(
                        id = bill?.id ?: 0L,
                        title = title.trim(),
                        amountPaise = amountPaise,
                        dueDate = bill?.dueDate ?: (System.currentTimeMillis() + 7 * 86400000L),
                        categoryId = selectedCategoryId,
                        recurrence = recurrence,
                        isPaid = bill?.isPaid ?: false,
                        reminderDaysBefore = 3
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && amountPaise > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
