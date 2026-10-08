package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(private val database: AppDatabase) {

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // Expenses
        val expenses = database.expenseDao().getExpensesInRangeSync(0L, Long.MAX_VALUE)
        val expArray = JSONArray()
        for (e in expenses) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("amountPaise", e.amountPaise)
                put("categoryId", e.categoryId)
                put("subCategory", e.subCategory ?: "")
                put("timestamp", e.timestamp)
                put("paymentMethodId", e.paymentMethodId)
                put("merchant", e.merchant)
                put("description", e.description)
                put("isRecurring", e.isRecurring)
                put("recurringInterval", e.recurringInterval ?: "")
            }
            expArray.put(obj)
        }
        root.put("expenses", expArray)

        // Incomes
        val incomes = database.incomeDao().getIncomeInRangeSync(0L, Long.MAX_VALUE)
        val incArray = JSONArray()
        for (i in incomes) {
            val obj = JSONObject().apply {
                put("id", i.id)
                put("amountPaise", i.amountPaise)
                put("source", i.source)
                put("timestamp", i.timestamp)
                put("notes", i.notes)
                put("isRecurring", i.isRecurring)
                put("recurringInterval", i.recurringInterval ?: "")
            }
            incArray.put(obj)
        }
        root.put("incomes", incArray)

        // Categories
        val categories = database.categoryDao().getAllCategoriesSync()
        val catArray = JSONArray()
        for (c in categories) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("iconName", c.iconName)
                put("colorHex", c.colorHex)
                put("subCategories", JSONArray(c.subCategories))
                put("isDefault", c.isDefault)
            }
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Payment Methods
        val paymentMethods = database.paymentMethodDao().getAllPaymentMethodsSync()
        val pmArray = JSONArray()
        for (p in paymentMethods) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("iconName", p.iconName)
                put("isDefault", p.isDefault)
            }
            pmArray.put(obj)
        }
        root.put("paymentMethods", pmArray)

        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", 1)

        root.toString(2)
    }

    suspend fun exportExpensesToCsv(): String = withContext(Dispatchers.IO) {
        val expenses = database.expenseDao().getExpensesInRangeSync(0L, Long.MAX_VALUE)
        val categories = database.categoryDao().getAllCategoriesSync().associateBy { it.id }
        val paymentMethods = database.paymentMethodDao().getAllPaymentMethodsSync().associateBy { it.id }

        val sb = StringBuilder()
        sb.append("ID,Date,Category,Subcategory,Merchant,Description,PaymentMethod,AmountPaise,AmountRupees,IsRecurring\n")
        for (e in expenses) {
            val catName = categories[e.categoryId]?.name ?: "Unknown"
            val pmName = paymentMethods[e.paymentMethodId]?.name ?: "Unknown"
            val rupees = e.amountPaise.toDouble() / 100.0
            sb.append("${e.id},")
            sb.append("${e.timestamp},")
            sb.append("\"${catName.replace("\"", "\"\"")}\",")
            sb.append("\"${(e.subCategory ?: "").replace("\"", "\"\"")}\",")
            sb.append("\"${e.merchant.replace("\"", "\"\"")}\",")
            sb.append("\"${e.description.replace("\"", "\"\"")}\",")
            sb.append("\"${pmName.replace("\"", "\"\"")}\",")
            sb.append("${e.amountPaise},")
            sb.append(String.format("%.2f", rupees))
            sb.append(",${e.isRecurring}\n")
        }
        sb.toString()
    }

    suspend fun restoreFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("expenses")) {
                val expArray = root.getJSONArray("expenses")
                for (idx in 0 until expArray.length()) {
                    val obj = expArray.getJSONObject(idx)
                    val expense = ExpenseEntity(
                        id = obj.optLong("id", 0L),
                        amountPaise = obj.getLong("amountPaise"),
                        categoryId = obj.getLong("categoryId"),
                        subCategory = obj.optString("subCategory").takeIf { it.isNotEmpty() },
                        timestamp = obj.getLong("timestamp"),
                        paymentMethodId = obj.getLong("paymentMethodId"),
                        merchant = obj.optString("merchant", ""),
                        description = obj.optString("description", ""),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        recurringInterval = obj.optString("recurringInterval").takeIf { it.isNotEmpty() }
                    )
                    database.expenseDao().insertExpense(expense)
                }
            }

            if (root.has("incomes")) {
                val incArray = root.getJSONArray("incomes")
                for (idx in 0 until incArray.length()) {
                    val obj = incArray.getJSONObject(idx)
                    val income = IncomeEntity(
                        id = obj.optLong("id", 0L),
                        amountPaise = obj.getLong("amountPaise"),
                        source = obj.getString("source"),
                        timestamp = obj.getLong("timestamp"),
                        notes = obj.optString("notes", ""),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        recurringInterval = obj.optString("recurringInterval").takeIf { it.isNotEmpty() }
                    )
                    database.incomeDao().insertIncome(income)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
