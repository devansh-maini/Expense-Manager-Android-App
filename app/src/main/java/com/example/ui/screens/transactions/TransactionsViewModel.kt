package com.example.ui.screens.transactions

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class TransactionItem {
    data class Expense(val entity: ExpenseEntity) : TransactionItem()
    data class Income(val entity: IncomeEntity) : TransactionItem()

    val timestamp: Long
        get() = when (this) {
            is Expense -> entity.timestamp
            is Income -> entity.timestamp
        }

    val amountPaise: Long
        get() = when (this) {
            is Expense -> entity.amountPaise
            is Income -> entity.amountPaise
        }
}

enum class SortOrder {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

data class TransactionsUiState(
    val transactions: List<TransactionItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val paymentMethods: List<PaymentMethodEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val selectedPaymentMethodId: Long? = null,
    val filterType: String = "ALL", // "ALL", "EXPENSE", "INCOME"
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val minAmountPaise: Long? = null,
    val maxAmountPaise: Long? = null
)

class TransactionsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCategory = MutableStateFlow<Long?>(null)
    private val _filterPaymentMethod = MutableStateFlow<Long?>(null)
    private val _filterType = MutableStateFlow("ALL")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    val uiState: StateFlow<TransactionsUiState> = combine(
        expenseRepository.allExpenses,
        incomeRepository.allIncome,
        categoryRepository.allCategories,
        paymentMethodRepository.allPaymentMethods,
        _searchQuery,
        _filterCategory,
        _filterPaymentMethod,
        _filterType,
        _sortOrder
    ) { args ->
        val expenses = args[0] as List<ExpenseEntity>
        val incomes = args[1] as List<IncomeEntity>
        val categories = args[2] as List<CategoryEntity>
        val paymentMethods = args[3] as List<PaymentMethodEntity>
        val search = args[4] as String
        val catFilter = args[5] as Long?
        val pmFilter = args[6] as Long?
        val typeFilter = args[7] as String
        val sort = args[8] as SortOrder

        val catMap = categories.associateBy { it.id }
        val pmMap = paymentMethods.associateBy { it.id }

        val items = mutableListOf<TransactionItem>()
        if (typeFilter == "ALL" || typeFilter == "EXPENSE") {
            items.addAll(expenses.map { TransactionItem.Expense(it) })
        }
        if (typeFilter == "ALL" || typeFilter == "INCOME") {
            items.addAll(incomes.map { TransactionItem.Income(it) })
        }

        val filtered = items.filter { item ->
            when (item) {
                is TransactionItem.Expense -> {
                    val exp = item.entity
                    val catName = catMap[exp.categoryId]?.name ?: ""
                    val pmName = pmMap[exp.paymentMethodId]?.name ?: ""
                    val matchesSearch = search.isEmpty() ||
                            exp.merchant.contains(search, ignoreCase = true) ||
                            exp.description.contains(search, ignoreCase = true) ||
                            catName.contains(search, ignoreCase = true)
                    val matchesCat = catFilter == null || exp.categoryId == catFilter
                    val matchesPm = pmFilter == null || exp.paymentMethodId == pmFilter
                    matchesSearch && matchesCat && matchesPm
                }
                is TransactionItem.Income -> {
                    val inc = item.entity
                    val matchesSearch = search.isEmpty() ||
                            inc.source.contains(search, ignoreCase = true) ||
                            inc.notes.contains(search, ignoreCase = true)
                    val matchesCat = catFilter == null // Income doesn't have categoryId
                    val matchesPm = pmFilter == null
                    matchesSearch && matchesCat && matchesPm
                }
            }
        }.sortedWith(
            when (sort) {
                SortOrder.DATE_DESC -> compareByDescending { it.timestamp }
                SortOrder.DATE_ASC -> compareBy { it.timestamp }
                SortOrder.AMOUNT_DESC -> compareByDescending { it.amountPaise }
                SortOrder.AMOUNT_ASC -> compareBy { it.amountPaise }
            }
        )

        TransactionsUiState(
            transactions = filtered,
            categories = categories,
            paymentMethods = paymentMethods,
            searchQuery = search,
            selectedCategoryId = catFilter,
            selectedPaymentMethodId = pmFilter,
            filterType = typeFilter,
            sortOrder = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Long?) {
        _filterCategory.value = categoryId
    }

    fun setPaymentMethodFilter(paymentMethodId: Long?) {
        _filterPaymentMethod.value = paymentMethodId
    }

    fun setFilterType(type: String) {
        _filterType.value = type
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }

    fun updateIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.updateIncome(income)
        }
    }

    fun insertExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.insertExpense(expense)
        }
    }

    fun insertIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.insertIncome(income)
        }
    }
}
