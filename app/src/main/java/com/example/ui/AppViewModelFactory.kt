package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.gemini.ExpenseToolsService
import com.example.data.gemini.GeminiChatRepository
import com.example.data.repository.BackupRepository
import com.example.data.repository.BillRepository
import com.example.data.repository.BudgetRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.IncomeRepository
import com.example.data.repository.PaymentMethodRepository
import com.example.data.repository.SettingsRepository
import com.example.security.SecurityManager
import com.example.ui.screens.analytics.AnalyticsViewModel
import com.example.ui.screens.backup.BackupViewModel
import com.example.ui.screens.bills.BillsViewModel
import com.example.ui.screens.budgets.BudgetsViewModel
import com.example.ui.screens.calendar.CalendarViewModel
import com.example.ui.screens.categories.CategoriesViewModel
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.paymentmethods.PaymentMethodsViewModel
import com.example.ui.screens.security.SecurityViewModel
import com.example.ui.screens.transactions.TransactionsViewModel

class AppViewModelFactory(
    private val database: AppDatabase,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {

    private val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    private val incomeRepository by lazy { IncomeRepository(database.incomeDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    private val paymentMethodRepository by lazy { PaymentMethodRepository(database.paymentMethodDao()) }
    private val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
    private val billRepository by lazy { BillRepository(database.billDao()) }
    private val settingsRepository by lazy { SettingsRepository(database.appSettingsDao()) }
    private val backupRepository by lazy { BackupRepository(database) }
    private val expenseToolsService by lazy {
        ExpenseToolsService(expenseRepository, categoryRepository, paymentMethodRepository)
    }
    private val geminiChatRepository by lazy {
        GeminiChatRepository(expenseToolsService)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(
                    expenseRepository,
                    incomeRepository,
                    categoryRepository,
                    paymentMethodRepository,
                    billRepository
                ) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(geminiChatRepository) as T
            }
            modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> {
                TransactionsViewModel(
                    expenseRepository,
                    incomeRepository,
                    categoryRepository,
                    paymentMethodRepository
                ) as T
            }
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) -> {
                AnalyticsViewModel(
                    expenseRepository,
                    incomeRepository,
                    categoryRepository,
                    budgetRepository
                ) as T
            }
            modelClass.isAssignableFrom(BillsViewModel::class.java) -> {
                BillsViewModel(
                    billRepository,
                    categoryRepository
                ) as T
            }
            modelClass.isAssignableFrom(BudgetsViewModel::class.java) -> {
                BudgetsViewModel(
                    budgetRepository,
                    categoryRepository,
                    expenseRepository
                ) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                CalendarViewModel(
                    expenseRepository,
                    incomeRepository,
                    categoryRepository,
                    paymentMethodRepository
                ) as T
            }
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                CategoriesViewModel(categoryRepository) as T
            }
            modelClass.isAssignableFrom(PaymentMethodsViewModel::class.java) -> {
                PaymentMethodsViewModel(paymentMethodRepository) as T
            }
            modelClass.isAssignableFrom(SecurityViewModel::class.java) -> {
                SecurityViewModel(settingsRepository, securityManager) as T
            }
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> {
                BackupViewModel(backupRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
