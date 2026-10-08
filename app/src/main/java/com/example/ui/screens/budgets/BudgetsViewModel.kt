package com.example.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.repository.BudgetRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryBudgetStatus(
    val budget: BudgetEntity,
    val category: CategoryEntity?,
    val spentPaise: Long,
    val remainingPaise: Long,
    val percentageUsed: Int
)

data class BudgetsUiState(
    val month: Int = DateUtils.getCurrentMonth(),
    val year: Int = DateUtils.getCurrentYear(),
    val overallBudget: BudgetEntity? = null,
    val totalSpentPaise: Long = 0L,
    val categoryBudgets: List<CategoryBudgetStatus> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _monthState = MutableStateFlow(Pair(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()))

    val uiState: StateFlow<BudgetsUiState> = _monthState.flatMapLatest { (month, year) ->
        val (startTime, endTime) = DateUtils.getMonthRange(month, year)

        combine(
            budgetRepository.getBudgetsForMonth(month, year),
            categoryRepository.allCategories,
            expenseRepository.getExpensesInRange(startTime, endTime)
        ) { budgets, categories, expenses ->
            val catMap = categories.associateBy { it.id }
            val expensesByCat = expenses.groupBy { it.categoryId }
                .mapValues { it.value.sumOf { e -> e.amountPaise } }

            val overall = budgets.find { it.categoryId == 0L }
            val totalSpent = expenses.sumOf { it.amountPaise }

            val catBudgets = budgets.filter { it.categoryId != 0L }.map { budget ->
                val spent = expensesByCat[budget.categoryId] ?: 0L
                val remaining = budget.amountPaise - spent
                val pct = if (budget.amountPaise > 0) ((spent * 100) / budget.amountPaise).toInt() else 0
                CategoryBudgetStatus(
                    budget = budget,
                    category = catMap[budget.categoryId],
                    spentPaise = spent,
                    remainingPaise = remaining,
                    percentageUsed = pct
                )
            }

            BudgetsUiState(
                month = month,
                year = year,
                overallBudget = overall,
                totalSpentPaise = totalSpent,
                categoryBudgets = catBudgets,
                categories = categories
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState()
    )

    fun previousMonth() {
        val (m, y) = _monthState.value
        _monthState.value = if (m == 1) Pair(12, y - 1) else Pair(m - 1, y)
    }

    fun nextMonth() {
        val (m, y) = _monthState.value
        _monthState.value = if (m == 12) Pair(1, y + 1) else Pair(m + 1, y)
    }

    fun setBudget(categoryId: Long?, amountPaise: Long) {
        val (m, y) = _monthState.value
        val actualCatId = categoryId ?: 0L
        viewModelScope.launch {
            val existing = budgetRepository.getBudgetForCategory(actualCatId, m, y)
            val budget = BudgetEntity(
                id = existing?.id ?: 0L,
                categoryId = actualCatId,
                amountPaise = amountPaise,
                month = m,
                year = y
            )
            budgetRepository.insertOrUpdateBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}
