package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    paymentMethods: List<PaymentMethodEntity>,
    onDismiss: () -> Unit,
    onSaveExpense: (ExpenseEntity) -> Unit,
    onSaveIncome: (IncomeEntity) -> Unit,
    existingExpense: ExpenseEntity? = null,
    existingIncome: IncomeEntity? = null
) {
    var isExpense by remember { mutableStateOf(existingIncome == null) }
    var amountInput by remember {
        mutableStateOf(
            when {
                existingExpense != null -> CurrencyFormatter.paiseToDecimalString(existingExpense.amountPaise)
                existingIncome != null -> CurrencyFormatter.paiseToDecimalString(existingIncome.amountPaise)
                else -> ""
            }
        )
    }
    var selectedCategoryId by remember {
        mutableLongStateOf(
            existingExpense?.categoryId ?: categories.firstOrNull()?.id ?: 1L
        )
    }
    var selectedSubCategory by remember {
        mutableStateOf(existingExpense?.subCategory ?: "")
    }
    var selectedPaymentMethodId by remember {
        mutableLongStateOf(
            existingExpense?.paymentMethodId ?: paymentMethods.firstOrNull()?.id ?: 1L
        )
    }
    var merchantOrSource by remember {
        mutableStateOf(
            existingExpense?.merchant ?: existingIncome?.source ?: ""
        )
    }
    var notes by remember {
        mutableStateOf(
            existingExpense?.description ?: existingIncome?.notes ?: ""
        )
    }
    var isRecurring by remember {
        mutableStateOf(
            existingExpense?.isRecurring ?: existingIncome?.isRecurring ?: false
        )
    }
    var recurringInterval by remember {
        mutableStateOf(
            existingExpense?.recurringInterval ?: existingIncome?.recurringInterval ?: "MONTHLY"
        )
    }
    var selectedTimestamp by remember {
        mutableLongStateOf(
            existingExpense?.timestamp ?: existingIncome?.timestamp ?: System.currentTimeMillis()
        )
    }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingExpense != null || existingIncome != null) "Edit Transaction" else "New Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expense / Income Toggle
            if (existingExpense == null && existingIncome == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExpense) Color(0xFFE53935) else Color.Transparent)
                            .clickable { isExpense = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isExpense) Color(0xFF2E7D32) else Color.Transparent)
                            .clickable { isExpense = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Amount Field
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                        amountInput = input
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                label = { Text("Amount (₹)") },
                prefix = {
                    Text(
                        text = "₹ ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isExpense) Color(0xFFE53935) else Color(0xFF2E7D32)
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isExpense) Color(0xFFE53935) else Color(0xFF2E7D32),
                    focusedLabelColor = if (isExpense) Color(0xFFE53935) else Color(0xFF2E7D32)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isExpense) {
                // Category Picker Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cat in categories) {
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = Color(cat.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryId = cat.id
                                selectedSubCategory = ""
                            },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(cat.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else catColor
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Subcategories if available
                if (selectedCategory != null && selectedCategory.subCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Subcategory (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (sub in selectedCategory.subCategories) {
                            val isSelected = selectedSubCategory == sub
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSubCategory = if (isSelected) "" else sub
                                },
                                label = { Text(sub, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (pm in paymentMethods) {
                        val isSelected = pm.id == selectedPaymentMethodId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPaymentMethodId = pm.id },
                            label = { Text(pm.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(pm.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Merchant / Payee
                OutlinedTextField(
                    value = merchantOrSource,
                    onValueChange = { merchantOrSource = it },
                    modifier = Modifier.fillMaxWidth().testTag("merchant_input"),
                    label = { Text("Merchant / Store (Optional)") },
                    placeholder = { Text("e.g. DMart, Shell, Amazon") },
                    singleLine = true
                )
            } else {
                // Income Source
                OutlinedTextField(
                    value = merchantOrSource,
                    onValueChange = { merchantOrSource = it },
                    modifier = Modifier.fillMaxWidth().testTag("source_input"),
                    label = { Text("Income Source") },
                    placeholder = { Text("e.g. Monthly Salary, Freelance, Dividend") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes / Description
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes / Description (Optional)") },
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recurring Transaction",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isRecurring) "Repeats $recurringInterval" else "One-time entry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save CTA Button
            val canSave = amountInput.isNotBlank() && (CurrencyFormatter.parseAmountToPaise(amountInput) > 0)
            Button(
                onClick = {
                    val amountPaise = CurrencyFormatter.parseAmountToPaise(amountInput)
                    if (isExpense) {
                        val expense = ExpenseEntity(
                            id = existingExpense?.id ?: 0L,
                            amountPaise = amountPaise,
                            categoryId = selectedCategoryId,
                            subCategory = selectedSubCategory.takeIf { it.isNotBlank() },
                            timestamp = selectedTimestamp,
                            paymentMethodId = selectedPaymentMethodId,
                            merchant = merchantOrSource.trim(),
                            description = notes.trim(),
                            isRecurring = isRecurring,
                            recurringInterval = if (isRecurring) recurringInterval else null
                        )
                        onSaveExpense(expense)
                    } else {
                        val income = IncomeEntity(
                            id = existingIncome?.id ?: 0L,
                            amountPaise = amountPaise,
                            source = if (merchantOrSource.isNotBlank()) merchantOrSource.trim() else "General Income",
                            timestamp = selectedTimestamp,
                            notes = notes.trim(),
                            isRecurring = isRecurring,
                            recurringInterval = if (isRecurring) recurringInterval else null
                        )
                        onSaveIncome(income)
                    }
                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) Color(0xFFE53935) else Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = if (existingExpense != null || existingIncome != null) "Update" else "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
