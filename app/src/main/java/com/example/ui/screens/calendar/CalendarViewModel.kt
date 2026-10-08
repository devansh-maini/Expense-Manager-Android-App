package com.example.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.IncomeRepository
import com.example.data.repository.PaymentMethodRepository
import com.example.ui.screens.transactions.TransactionItem
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DaySummary(
    val day: Int,
    val hasExpense: Boolean,
    val hasIncome: Boolean,
    val totalExpensePaise: Long,
    val totalIncomePaise: Long
)

data class CalendarUiState(
    val month: Int = DateUtils.getCurrentMonth(),
    val year: Int = DateUtils.getCurrentYear(),
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val daysInMonth: Int = 31,
    val firstDayOfWeek: Int = 1, // Calendar.SUNDAY = 1
    val daySummaries: Map<Int, DaySummary> = emptyMap(),
    val selectedDayTransactions: List<TransactionItem> = emptyList(),
    val selectedDayExpensePaise: Long = 0L,
    val selectedDayIncomePaise: Long = 0L,
    val categories: List<CategoryEntity> = emptyList(),
    val paymentMethods: List<PaymentMethodEntity> = emptyList()
)

class CalendarViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _monthState = MutableStateFlow(Pair(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()))
    private val _selectedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))

    val uiState: StateFlow<CalendarUiState> = combine(
        _monthState,
        _selectedDay
    ) { (month, year), selectedDay ->
        Triple(month, year, selectedDay)
    }.flatMapLatest { (month, year, selectedDay) ->
        val (monthStart, monthEnd) = DateUtils.getMonthRange(month, year)

        combine(
            expenseRepository.getExpensesInRange(monthStart, monthEnd),
            incomeRepository.getIncomeInRange(monthStart, monthEnd),
            categoryRepository.allCategories,
            paymentMethodRepository.allPaymentMethods
        ) { expenses, incomes, categories, paymentMethods ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val daysCount = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDow = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

            // Group by day of month
            val expenseByDay = expenses.groupBy {
                val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                c.get(Calendar.DAY_OF_MONTH)
            }
            val incomeByDay = incomes.groupBy {
                val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                c.get(Calendar.DAY_OF_MONTH)
            }

            val summaryMap = mutableMapOf<Int, DaySummary>()
            for (d in 1..daysCount) {
                val dayExps = expenseByDay[d] ?: emptyList()
                val dayIncs = incomeByDay[d] ?: emptyList()
                summaryMap[d] = DaySummary(
                    day = d,
                    hasExpense = dayExps.isNotEmpty(),
                    hasIncome = dayIncs.isNotEmpty(),
                    totalExpensePaise = dayExps.sumOf { it.amountPaise },
                    totalIncomePaise = dayIncs.sumOf { it.amountPaise }
                )
            }

            // Selected day transactions
            val dayExpenses = expenseByDay[selectedDay] ?: emptyList()
            val dayIncomes = incomeByDay[selectedDay] ?: emptyList()
            val txList = mutableListOf<TransactionItem>()
            txList.addAll(dayExpenses.map { TransactionItem.Expense(it) })
            txList.addAll(dayIncomes.map { TransactionItem.Income(it) })
            txList.sortByDescending { it.timestamp }

            CalendarUiState(
                month = month,
                year = year,
                selectedDay = selectedDay,
                daysInMonth = daysCount,
                firstDayOfWeek = firstDow,
                daySummaries = summaryMap,
                selectedDayTransactions = txList,
                selectedDayExpensePaise = dayExpenses.sumOf { it.amountPaise },
                selectedDayIncomePaise = dayIncomes.sumOf { it.amountPaise },
                categories = categories,
                paymentMethods = paymentMethods
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun previousMonth() {
        val (m, y) = _monthState.value
        _monthState.value = if (m == 1) Pair(12, y - 1) else Pair(m - 1, y)
        _selectedDay.value = 1
    }

    fun nextMonth() {
        val (m, y) = _monthState.value
        _monthState.value = if (m == 12) Pair(1, y + 1) else Pair(m + 1, y)
        _selectedDay.value = 1
    }
}
