package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.repository.BillRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.IncomeRepository
import com.example.data.repository.PaymentMethodRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val selectedMonth: Int = DateUtils.getCurrentMonth(),
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val remainingPaise: Long = 0L,
    val savingsPercentage: Int = 0,
    val transactionCount: Int = 0,
    val highestCategory: CategoryEntity? = null,
    val highestCategoryAmountPaise: Long = 0L,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val upcomingBills: List<BillEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val paymentMethods: List<PaymentMethodEntity> = emptyList()
)

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val billRepository: BillRepository
) : ViewModel() {

    private val _monthState = MutableStateFlow(Pair(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()))

    val uiState: StateFlow<DashboardUiState> = _monthState.flatMapLatest { (month, year) ->
        val (startTime, endTime) = DateUtils.getMonthRange(month, year)

        combine(
            expenseRepository.getExpensesInRange(startTime, endTime),
            incomeRepository.getIncomeInRange(startTime, endTime),
            categoryRepository.allCategories,
            paymentMethodRepository.allPaymentMethods,
            billRepository.getUpcomingBillsLimit(4)
        ) { expenses, incomes, categories, paymentMethods, bills ->
            val totalExpense = expenses.sumOf { it.amountPaise }
            val totalIncome = incomes.sumOf { it.amountPaise }
            val remaining = totalIncome - totalExpense
            val savingsPct = if (totalIncome > 0) {
                ((remaining.coerceAtLeast(0L) * 100) / totalIncome).toInt()
            } else 0

            val catMap = categories.associateBy { it.id }
            val spendingByCat = expenses.groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.amountPaise } }
            val topCatEntry = spendingByCat.maxByOrNull { it.value }

            DashboardUiState(
                selectedMonth = month,
                selectedYear = year,
                totalIncomePaise = totalIncome,
                totalExpensePaise = totalExpense,
                remainingPaise = remaining,
                savingsPercentage = savingsPct,
                transactionCount = expenses.size + incomes.size,
                highestCategory = topCatEntry?.key?.let { catMap[it] },
                highestCategoryAmountPaise = topCatEntry?.value ?: 0L,
                recentExpenses = expenses.take(5),
                upcomingBills = bills,
                categories = categories,
                paymentMethods = paymentMethods
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun previousMonth() {
        val (curM, curY) = _monthState.value
        if (curM == 1) {
            _monthState.value = Pair(12, curY - 1)
        } else {
            _monthState.value = Pair(curM - 1, curY)
        }
    }

    fun nextMonth() {
        val (curM, curY) = _monthState.value
        if (curM == 12) {
            _monthState.value = Pair(1, curY + 1)
        } else {
            _monthState.value = Pair(curM + 1, curY)
        }
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.insertExpense(expense)
        }
    }

    fun addIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.insertIncome(income)
        }
    }

    fun markBillPaid(billId: Long) {
        viewModelScope.launch {
            billRepository.setPaidStatus(billId, true)
        }
    }
}
