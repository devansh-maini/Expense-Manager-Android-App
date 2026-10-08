package com.example.data.gemini

import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.PaymentMethodRepository
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class PendingExpenseAction {
    data class Delete(
        val expenseId: Long,
        val merchant: String,
        val categoryName: String,
        val amountFormatted: String,
        val dateFormatted: String,
        val description: String,
        val callId: String? = null
    ) : PendingExpenseAction()

    data class Update(
        val expenseId: Long,
        val merchant: String,
        val categoryName: String,
        val oldAmountFormatted: String,
        val newAmountFormatted: String,
        val newCategory: String?,
        val newMerchant: String?,
        val newDescription: String?,
        val callId: String? = null
    ) : PendingExpenseAction()
}

class ExpenseToolsService(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {

    suspend fun getExpenses(
        limit: Int? = 20,
        offset: Int? = 0,
        minAmountRupees: Double? = null,
        maxAmountRupees: Double? = null,
        startDate: String? = null,
        endDate: String? = null,
        category: String? = null
    ): JsonObject {
        val (startTs, endTs) = parseDateRange(null, startDate, endDate)
        val allExpenses = if (startTs != null && endTs != null) {
            expenseRepository.getExpensesInRangeSync(startTs, endTs)
        } else {
            expenseRepository.getAllExpensesSync()
        }

        val categories = categoryRepository.getAllCategoriesSync().associateBy { it.id }
        val categoryFilterId = category?.let { findCategoryId(it) }

        val filtered = allExpenses.filter { exp ->
            var matches = true
            if (categoryFilterId != null && exp.categoryId != categoryFilterId) {
                matches = false
            }
            if (minAmountRupees != null && exp.amountPaise < (minAmountRupees * 100).toLong()) {
                matches = false
            }
            if (maxAmountRupees != null && exp.amountPaise > (maxAmountRupees * 100).toLong()) {
                matches = false
            }
            matches
        }

        val actualOffset = (offset ?: 0).coerceAtLeast(0)
        val actualLimit = (limit ?: 20).coerceIn(1, 100)
        val paged = filtered.drop(actualOffset).take(actualLimit)

        return buildJsonObject {
            put("totalMatchingCount", filtered.size)
            put("returnedCount", paged.size)
            put("totalAmountFormatted", CurrencyFormatter.formatPaise(filtered.sumOf { it.amountPaise }))
            putJsonArray("expenses") {
                paged.forEach { exp ->
                    add(formatExpenseJson(exp, categories[exp.categoryId]?.name ?: "Uncategorized"))
                }
            }
        }
    }

    suspend fun getExpenseById(id: Long): JsonObject {
        val expense = expenseRepository.getExpenseById(id)
        if (expense == null) {
            return buildJsonObject {
                put("status", "NOT_FOUND")
                put("message", "No expense found with ID $id")
            }
        }
        val category = categoryRepository.getCategoryById(expense.categoryId)?.name ?: "Uncategorized"
        return buildJsonObject {
            put("status", "SUCCESS")
            put("expense", formatExpenseJson(expense, category))
        }
    }

    suspend fun searchExpenses(keyword: String): JsonObject {
        val expenses = expenseRepository.searchExpensesSync(keyword)
        val categories = categoryRepository.getAllCategoriesSync().associateBy { it.id }

        return buildJsonObject {
            put("keyword", keyword)
            put("count", expenses.size)
            put("totalAmountFormatted", CurrencyFormatter.formatPaise(expenses.sumOf { it.amountPaise }))
            putJsonArray("results") {
                expenses.take(25).forEach { exp ->
                    add(formatExpenseJson(exp, categories[exp.categoryId]?.name ?: "Uncategorized"))
                }
            }
        }
    }

    suspend fun getExpensesByCategory(
        categoryName: String,
        dateRange: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): JsonObject {
        val categoryId = findCategoryId(categoryName)
        if (categoryId == null) {
            val allCats = categoryRepository.getAllCategoriesSync()
            return buildJsonObject {
                put("status", "CATEGORY_NOT_FOUND")
                put("message", "Could not find a category matching '$categoryName'.")
                putJsonArray("availableCategories") {
                    allCats.forEach { add(JsonPrimitive(it.name)) }
                }
            }
        }

        val (startTs, endTs) = parseDateRange(dateRange, startDate, endDate)
        val expenses = if (startTs != null && endTs != null) {
            expenseRepository.getExpensesByCategoryAndRangeSync(categoryId, startTs, endTs)
        } else {
            expenseRepository.getExpensesByCategorySync(categoryId)
        }

        val cat = categoryRepository.getCategoryById(categoryId)
        val totalPaise = expenses.sumOf { it.amountPaise }

        return buildJsonObject {
            put("category", cat?.name ?: categoryName)
            put("count", expenses.size)
            put("totalSpentFormatted", CurrencyFormatter.formatPaise(totalPaise))
            put("totalSpentRupees", CurrencyFormatter.paiseToDecimalString(totalPaise))
            if (dateRange != null) put("dateRange", dateRange)
            putJsonArray("expenses") {
                expenses.take(30).forEach { exp ->
                    add(formatExpenseJson(exp, cat?.name ?: categoryName))
                }
            }
        }
    }

    suspend fun getTotalExpenses(
        dateRange: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): JsonObject {
        val (startTs, endTs) = parseDateRange(dateRange, startDate, endDate)
        val totalPaise = if (startTs != null && endTs != null) {
            expenseRepository.getTotalExpensesInRangeSync(startTs, endTs)
        } else {
            expenseRepository.getAllExpensesSync().sumOf { it.amountPaise }
        }

        val expensesCount = if (startTs != null && endTs != null) {
            expenseRepository.getExpensesInRangeSync(startTs, endTs).size
        } else {
            expenseRepository.getAllExpensesSync().size
        }

        return buildJsonObject {
            put("dateRange", dateRange ?: "CUSTOM_OR_ALL")
            put("transactionCount", expensesCount)
            put("totalPaise", totalPaise)
            put("totalFormatted", CurrencyFormatter.formatPaise(totalPaise))
            put("totalRupees", CurrencyFormatter.paiseToDecimalString(totalPaise))
        }
    }

    suspend fun getMonthlySummary(
        month: Int? = null,
        year: Int? = null,
        relativeMonth: String? = null
    ): JsonObject {
        val (targetMonth, targetYear) = resolveMonthAndYear(month, year, relativeMonth)
        val (startTs, endTs) = DateUtils.getMonthRange(targetMonth, targetYear)

        val expenses = expenseRepository.getExpensesInRangeSync(startTs, endTs)
        val categories = categoryRepository.getAllCategoriesSync().associateBy { it.id }

        val totalSpentPaise = expenses.sumOf { it.amountPaise }

        // Category breakdown
        val breakdown = expenses.groupBy { it.categoryId }.map { (catId, exps) ->
            val catName = categories[catId]?.name ?: "Uncategorized"
            val spent = exps.sumOf { it.amountPaise }
            val percentage = if (totalSpentPaise > 0) ((spent * 100.0) / totalSpentPaise) else 0.0
            Triple(catName, spent, percentage)
        }.sortedByDescending { it.second }

        val largest = expenses.maxByOrNull { it.amountPaise }

        return buildJsonObject {
            put("month", targetMonth)
            put("year", targetYear)
            put("monthName", DateUtils.formatMonthYear(targetMonth, targetYear))
            put("transactionCount", expenses.size)
            put("totalSpentFormatted", CurrencyFormatter.formatPaise(totalSpentPaise))
            put("totalSpentRupees", CurrencyFormatter.paiseToDecimalString(totalSpentPaise))
            putJsonArray("categoryBreakdown") {
                breakdown.forEach { (name, spent, pct) ->
                    add(buildJsonObject {
                        put("category", name)
                        put("spentFormatted", CurrencyFormatter.formatPaise(spent))
                        put("percentage", String.format(Locale.US, "%.1f%%", pct))
                    })
                }
            }
            if (largest != null) {
                put("largestExpense", formatExpenseJson(largest, categories[largest.categoryId]?.name ?: "Uncategorized"))
            }
        }
    }

    suspend fun getLargestExpense(
        dateRange: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): JsonObject {
        val (startTs, endTs) = parseDateRange(dateRange, startDate, endDate)
        val largest = if (startTs != null && endTs != null) {
            expenseRepository.getLargestExpenseInRangeSync(startTs, endTs)
        } else {
            expenseRepository.getLargestExpenseSync()
        }

        if (largest == null) {
            return buildJsonObject {
                put("status", "NO_EXPENSES")
                put("message", "No expenses found for the specified period.")
            }
        }

        val catName = categoryRepository.getCategoryById(largest.categoryId)?.name ?: "Uncategorized"
        return buildJsonObject {
            put("status", "SUCCESS")
            put("expense", formatExpenseJson(largest, catName))
        }
    }

    suspend fun addExpense(
        amountRupees: Double,
        categoryName: String,
        merchant: String? = null,
        description: String? = null,
        paymentMethodName: String? = null,
        date: String? = null
    ): JsonObject {
        val paise = (amountRupees * 100).toLong()
        if (paise <= 0) {
            return buildJsonObject {
                put("status", "ERROR")
                put("message", "Amount must be greater than zero.")
            }
        }

        // Category resolution
        val categories = categoryRepository.getAllCategoriesSync()
        var category = categories.find { it.name.equals(categoryName.trim(), ignoreCase = true) }
        if (category == null) {
            // Find partial or fallback
            category = categories.find { it.name.contains(categoryName.trim(), ignoreCase = true) }
        }
        val catId = category?.id ?: run {
            // Auto-create category if missing
            categoryRepository.insertCategory(
                CategoryEntity(
                    name = categoryName.trim().replaceFirstChar { it.uppercase() },
                    iconName = "LocalOffer",
                    colorHex = 0xFF00A86BL
                )
            )
        }

        // Payment Method resolution
        val methods = paymentMethodRepository.getAllPaymentMethodsSync()
        val method = if (paymentMethodName != null) {
            methods.find { it.name.contains(paymentMethodName.trim(), ignoreCase = true) }
        } else null
        val methodId = method?.id ?: methods.firstOrNull()?.id ?: 1L

        val timestamp = parseSpecificDateToMillis(date) ?: System.currentTimeMillis()

        val newExpense = ExpenseEntity(
            amountPaise = paise,
            categoryId = catId,
            timestamp = timestamp,
            paymentMethodId = methodId,
            merchant = merchant?.trim() ?: "",
            description = description?.trim() ?: ""
        )

        val generatedId = expenseRepository.insertExpense(newExpense)
        val created = expenseRepository.getExpenseById(generatedId) ?: newExpense.copy(id = generatedId)

        return buildJsonObject {
            put("status", "SUCCESS")
            put("message", "Expense added successfully.")
            put("expense", formatExpenseJson(created, category?.name ?: categoryName))
        }
    }

    suspend fun checkUpdateExpense(
        id: Long,
        amountRupees: Double? = null,
        categoryName: String? = null,
        merchant: String? = null,
        description: String? = null,
        isConfirmed: Boolean = false,
        callId: String? = null
    ): Pair<JsonObject, PendingExpenseAction.Update?> {
        val existing = expenseRepository.getExpenseById(id)
        if (existing == null) {
            return Pair(
                buildJsonObject {
                    put("status", "NOT_FOUND")
                    put("message", "Expense with ID $id does not exist.")
                },
                null
            )
        }

        val catName = categoryRepository.getCategoryById(existing.categoryId)?.name ?: "Uncategorized"
        val oldFormatted = CurrencyFormatter.formatPaise(existing.amountPaise)
        val newPaise = if (amountRupees != null) (amountRupees * 100).toLong() else existing.amountPaise
        val newFormatted = CurrencyFormatter.formatPaise(newPaise)

        if (!isConfirmed) {
            val pending = PendingExpenseAction.Update(
                expenseId = id,
                merchant = merchant ?: existing.merchant,
                categoryName = categoryName ?: catName,
                oldAmountFormatted = oldFormatted,
                newAmountFormatted = newFormatted,
                newCategory = categoryName,
                newMerchant = merchant,
                newDescription = description,
                callId = callId
            )
            val json = buildJsonObject {
                put("status", "CONFIRMATION_REQUIRED")
                put("action", "UPDATE")
                put("expenseId", id)
                put("message", "Modifying expense #$id (${existing.merchant.ifEmpty { catName }}) requires user confirmation.")
                putJsonObject("proposedChanges") {
                    put("currentAmount", oldFormatted)
                    put("newAmount", newFormatted)
                    if (categoryName != null) put("newCategory", categoryName)
                    if (merchant != null) put("newMerchant", merchant)
                    if (description != null) put("newDescription", description)
                }
            }
            return Pair(json, pending)
        }

        // Execute update
        var updatedCatId = existing.categoryId
        if (categoryName != null) {
            findCategoryId(categoryName)?.let { updatedCatId = it }
        }

        val updated = existing.copy(
            amountPaise = newPaise,
            categoryId = updatedCatId,
            merchant = merchant?.trim() ?: existing.merchant,
            description = description?.trim() ?: existing.description
        )
        expenseRepository.updateExpense(updated)

        val updatedCatName = categoryRepository.getCategoryById(updatedCatId)?.name ?: catName
        val json = buildJsonObject {
            put("status", "SUCCESS")
            put("message", "Expense updated successfully.")
            put("expense", formatExpenseJson(updated, updatedCatName))
        }
        return Pair(json, null)
    }

    suspend fun checkDeleteExpense(
        id: Long,
        isConfirmed: Boolean = false,
        callId: String? = null
    ): Pair<JsonObject, PendingExpenseAction.Delete?> {
        val existing = expenseRepository.getExpenseById(id)
        if (existing == null) {
            return Pair(
                buildJsonObject {
                    put("status", "NOT_FOUND")
                    put("message", "Expense with ID $id does not exist.")
                },
                null
            )
        }

        val catName = categoryRepository.getCategoryById(existing.categoryId)?.name ?: "Uncategorized"
        val formattedAmount = CurrencyFormatter.formatPaise(existing.amountPaise)
        val formattedDate = DateUtils.formatDisplayDate(existing.timestamp)

        if (!isConfirmed) {
            val pending = PendingExpenseAction.Delete(
                expenseId = id,
                merchant = existing.merchant,
                categoryName = catName,
                amountFormatted = formattedAmount,
                dateFormatted = formattedDate,
                description = existing.description,
                callId = callId
            )
            val json = buildJsonObject {
                put("status", "CONFIRMATION_REQUIRED")
                put("action", "DELETE")
                put("expenseId", id)
                put("message", "Destructive action: Deleting expense #$id (${existing.merchant.ifEmpty { catName }} - $formattedAmount) requires user confirmation.")
                putJsonObject("expenseDetails") {
                    put("id", id)
                    put("merchant", existing.merchant)
                    put("category", catName)
                    put("amount", formattedAmount)
                    put("date", formattedDate)
                }
            }
            return Pair(json, pending)
        }

        // Execute delete
        expenseRepository.deleteExpense(existing)
        val json = buildJsonObject {
            put("status", "SUCCESS")
            put("message", "Expense #$id ($formattedAmount) was deleted.")
        }
        return Pair(json, null)
    }

    suspend fun executeConfirmedUpdate(action: PendingExpenseAction.Update): JsonObject {
        val (res, _) = checkUpdateExpense(
            id = action.expenseId,
            amountRupees = CurrencyFormatter.parseAmountToPaise(action.newAmountFormatted).toDouble() / 100.0,
            categoryName = action.newCategory,
            merchant = action.newMerchant,
            description = action.newDescription,
            isConfirmed = true
        )
        return res
    }

    suspend fun executeConfirmedDelete(action: PendingExpenseAction.Delete): JsonObject {
        val (res, _) = checkDeleteExpense(id = action.expenseId, isConfirmed = true)
        return res
    }

    // --- Helper Parsing & Formatting ---

    private suspend fun findCategoryId(categoryName: String): Long? {
        val cats = categoryRepository.getAllCategoriesSync()
        val exact = cats.find { it.name.equals(categoryName.trim(), ignoreCase = true) }
        if (exact != null) return exact.id

        val partial = cats.find {
            it.name.contains(categoryName.trim(), ignoreCase = true) ||
                    categoryName.contains(it.name, ignoreCase = true)
        }
        return partial?.id
    }

    private fun formatExpenseJson(exp: ExpenseEntity, categoryName: String): JsonObject {
        return buildJsonObject {
            put("id", exp.id)
            put("amountFormatted", CurrencyFormatter.formatPaise(exp.amountPaise))
            put("amountPaise", exp.amountPaise)
            put("category", categoryName)
            put("merchant", exp.merchant)
            put("description", exp.description)
            put("date", DateUtils.formatDisplayDate(exp.timestamp))
            put("timestamp", exp.timestamp)
        }
    }

    private fun parseDateRange(range: String?, startStr: String?, endStr: String?): Pair<Long?, Long?> {
        if (!startStr.isNullOrBlank() && !endStr.isNullOrBlank()) {
            val s = parseSpecificDateToMillis(startStr)
            val e = parseSpecificDateToMillis(endStr)
            if (s != null && e != null) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = e
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                return Pair(s, cal.timeInMillis)
            }
        }

        if (range.isNullOrBlank()) return Pair(null, null)

        val upper = range.uppercase(Locale.ROOT).trim()
        val now = Calendar.getInstance()

        return when {
            upper == "TODAY" -> {
                DateUtils.getDayRange(System.currentTimeMillis())
            }
            upper == "YESTERDAY" -> {
                val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                DateUtils.getDayRange(yest.timeInMillis)
            }
            upper == "THIS_WEEK" -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            upper == "THIS_MONTH" || upper == "CURRENT_MONTH" -> {
                DateUtils.getMonthRange(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
            }
            upper == "LAST_MONTH" || upper == "PREVIOUS_MONTH" -> {
                val m = DateUtils.getCurrentMonth()
                val y = DateUtils.getCurrentYear()
                val (prevM, prevY) = if (m == 1) Pair(12, y - 1) else Pair(m - 1, y)
                DateUtils.getMonthRange(prevM, prevY)
            }
            upper == "THIS_YEAR" -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            upper.contains("JANUARY") -> getNamedMonthRange(1, upper)
            upper.contains("FEBRUARY") -> getNamedMonthRange(2, upper)
            upper.contains("MARCH") -> getNamedMonthRange(3, upper)
            upper.contains("APRIL") -> getNamedMonthRange(4, upper)
            upper.contains("MAY") -> getNamedMonthRange(5, upper)
            upper.contains("JUNE") -> getNamedMonthRange(6, upper)
            upper.contains("JULY") -> getNamedMonthRange(7, upper)
            upper.contains("AUGUST") -> getNamedMonthRange(8, upper)
            upper.contains("SEPTEMBER") -> getNamedMonthRange(9, upper)
            upper.contains("OCTOBER") -> getNamedMonthRange(10, upper)
            upper.contains("NOVEMBER") -> getNamedMonthRange(11, upper)
            upper.contains("DECEMBER") -> getNamedMonthRange(12, upper)
            else -> Pair(null, null)
        }
    }

    private fun getNamedMonthRange(month: Int, text: String): Pair<Long, Long> {
        val yearRegex = "\\b(20\\d{2})\\b".toRegex()
        val match = yearRegex.find(text)
        val year = match?.value?.toIntOrNull() ?: DateUtils.getCurrentYear()
        return DateUtils.getMonthRange(month, year)
    }

    private fun resolveMonthAndYear(month: Int?, year: Int?, relativeMonth: String?): Pair<Int, Int> {
        val currentM = DateUtils.getCurrentMonth()
        val currentY = DateUtils.getCurrentYear()

        if (relativeMonth != null) {
            val upper = relativeMonth.uppercase(Locale.ROOT)
            if (upper == "LAST_MONTH" || upper == "PREVIOUS_MONTH") {
                return if (currentM == 1) Pair(12, currentY - 1) else Pair(currentM - 1, currentY)
            }
            if (upper == "THIS_MONTH" || upper == "CURRENT_MONTH") {
                return Pair(currentM, currentY)
            }
        }

        val targetMonth = month?.coerceIn(1, 12) ?: currentM
        val targetYear = year ?: currentY
        return Pair(targetMonth, targetYear)
    }

    private fun parseSpecificDateToMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        val trimmed = dateStr.trim().lowercase(Locale.ROOT)
        if (trimmed == "today") return System.currentTimeMillis()
        if (trimmed == "yesterday") {
            return Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        }

        val formats = listOf(
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "dd MMM yyyy"
        )
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.US)
                val d = sdf.parse(dateStr.trim())
                if (d != null) return d.time
            } catch (_: Exception) { }
        }
        return null
    }
}
