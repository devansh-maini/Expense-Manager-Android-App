package com.example.ui.screens.analytics

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.repository.BudgetRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.IncomeRepository
import com.example.ui.components.ChartSlice
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialInsight(
    val title: String,
    val description: String,
    val isPositive: Boolean
)

data class AnalyticsUiState(
    val selectedMonth: Int = DateUtils.getCurrentMonth(),
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val totalExpensePaise: Long = 0L,
    val totalIncomePaise: Long = 0L,
    val averageDailySpendingPaise: Long = 0L,
    val categorySlices: List<ChartSlice> = emptyList(),
    val dailyTrendPoints: List<Pair<String, Long>> = emptyList(),
    val previousMonthExpensePaise: Long = 0L,
    val previousMonthIncomePaise: Long = 0L,
    val insights: List<FinancialInsight> = emptyList()
)

class AnalyticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _monthState = MutableStateFlow(Pair(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()))

    val uiState: StateFlow<AnalyticsUiState> = _monthState.flatMapLatest { (month, year) ->
        val (currentStart, currentEnd) = DateUtils.getMonthRange(month, year)

        // Previous month calculation
        val (prevM, prevY) = if (month == 1) Pair(12, year - 1) else Pair(month - 1, year)
        val (prevStart, prevEnd) = DateUtils.getMonthRange(prevM, prevY)

        val categoriesAndBudgetsFlow = combine(
            categoryRepository.allCategories,
            budgetRepository.getBudgetsForMonth(month, year)
        ) { cats, bgt -> Pair(cats, bgt) }

        combine(
            expenseRepository.getExpensesInRange(currentStart, currentEnd),
            incomeRepository.getIncomeInRange(currentStart, currentEnd),
            expenseRepository.getExpensesInRange(prevStart, prevEnd),
            incomeRepository.getIncomeInRange(prevStart, prevEnd),
            categoriesAndBudgetsFlow
        ) { currentExpenses, currentIncomes, prevExpenses, prevIncomes, (categories, budgets) ->
            val totalExpense = currentExpenses.sumOf { it.amountPaise }
            val totalIncome = currentIncomes.sumOf { it.amountPaise }
            val prevExpense = prevExpenses.sumOf { it.amountPaise }
            val prevIncome = prevIncomes.sumOf { it.amountPaise }

            // Category breakdown slices
            val catMap = categories.associateBy { it.id }
            val groupedByCat = currentExpenses.groupBy { it.categoryId }
                .mapValues { it.value.sumOf { exp -> exp.amountPaise } }
                .toList()
                .sortedByDescending { it.second }

            val slices = groupedByCat.map { (catId, amount) ->
                val category = catMap[catId]
                ChartSlice(
                    label = category?.name ?: "Other",
                    amountPaise = amount,
                    color = category?.let { Color(it.colorHex) } ?: Color(0xFF607D8B)
                )
            }

            // Daily trend points for current month
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val dailyGrouped = currentExpenses.groupBy {
                dayFormat.format(Date(it.timestamp))
            }.mapValues { it.value.sumOf { exp -> exp.amountPaise } }

            val daysInMonth = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)

            val dailyPoints = (1..daysInMonth).map { day ->
                val dayStr = day.toString()
                Pair(dayStr, dailyGrouped[dayStr] ?: 0L)
            }

            // Average daily spending (using days elapsed or total days)
            val avgDaily = if (daysInMonth > 0) totalExpense / daysInMonth else 0L

            // Generate intelligent local rule-based financial insights
            val insights = mutableListOf<FinancialInsight>()

            // 1. Month-to-month change insight
            if (prevExpense > 0) {
                val diff = totalExpense - prevExpense
                val pct = Math.abs((diff * 100) / prevExpense).toInt()
                if (diff > 0) {
                    insights.add(
                        FinancialInsight(
                            title = "Spending Increased",
                            description = "Your spending is $pct% higher than last month (${CurrencyFormatter.formatPaise(prevExpense, showDecimals = false)}).",
                            isPositive = false
                        )
                    )
                } else if (diff < 0) {
                    insights.add(
                        FinancialInsight(
                            title = "Spending Decreased",
                            description = "Great job! You spent $pct% less than last month.",
                            isPositive = true
                        )
                    )
                }
            }

            // 2. Savings rate insight
            if (totalIncome > 0) {
                val savingsRate = (((totalIncome - totalExpense).coerceAtLeast(0L) * 100) / totalIncome).toInt()
                if (savingsRate >= 30) {
                    insights.add(
                        FinancialInsight(
                            title = "Strong Savings Rate",
                            description = "You saved $savingsRate% of your earnings this month. Excellent financial discipline!",
                            isPositive = true
                        )
                    )
                } else if (savingsRate < 10 && totalExpense > 0) {
                    insights.add(
                        FinancialInsight(
                            title = "Tight Cashflow Alert",
                            description = "Your current savings rate is only $savingsRate%. Consider cutting non-essential expenses.",
                            isPositive = false
                        )
                    )
                }
            }

            // 3. Category budget thresholds insight
            val budgetMap = budgets.associateBy { it.categoryId }
            for ((catId, spent) in groupedByCat) {
                val catBudget = budgetMap[catId]
                if (catBudget != null && catBudget.amountPaise > 0) {
                    val catName = catMap[catId]?.name ?: "Category"
                    val usedPct = ((spent * 100) / catBudget.amountPaise).toInt()
                    if (usedPct >= 100) {
                        insights.add(
                            FinancialInsight(
                                title = "$catName Budget Exceeded",
                                description = "You have exhausted 100% of your $catName budget (${CurrencyFormatter.formatPaise(spent)} of ${CurrencyFormatter.formatPaise(catBudget.amountPaise)}).",
                                isPositive = false
                            )
                        )
                    } else if (usedPct >= 80) {
                        insights.add(
                            FinancialInsight(
                                title = "$catName Budget Warning",
                                description = "You have used $usedPct% of your $catName budget.",
                                isPositive = false
                            )
                        )
                    }
                }
            }

            // Fallback insight if empty
            if (insights.isEmpty()) {
                insights.add(
                    FinancialInsight(
                        title = "Healthy Financial Tracking",
                        description = "Keep logging your daily expenses to unlock deeper spending pattern trends and insights.",
                        isPositive = true
                    )
                )
            }

            AnalyticsUiState(
                selectedMonth = month,
                selectedYear = year,
                totalExpensePaise = totalExpense,
                totalIncomePaise = totalIncome,
                averageDailySpendingPaise = avgDaily,
                categorySlices = slices,
                dailyTrendPoints = dailyPoints,
                previousMonthExpensePaise = prevExpense,
                previousMonthIncomePaise = prevIncome,
                insights = insights
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
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
}
