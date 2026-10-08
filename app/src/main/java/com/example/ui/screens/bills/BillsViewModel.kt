package com.example.ui.screens.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.repository.BillRepository
import com.example.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BillsUiState(
    val bills: List<BillEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val totalPendingPaise: Long = 0L,
    val pendingCount: Int = 0
)

class BillsViewModel(
    private val billRepository: BillRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<BillsUiState> = combine(
        billRepository.allBills,
        categoryRepository.allCategories
    ) { bills, categories ->
        val pendingBills = bills.filter { !it.isPaid }
        val totalPending = pendingBills.sumOf { it.amountPaise }

        BillsUiState(
            bills = bills,
            categories = categories,
            totalPendingPaise = totalPending,
            pendingCount = pendingBills.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BillsUiState()
    )

    fun addBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.insertBill(bill)
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.updateBill(bill)
        }
    }

    fun togglePaid(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.setPaidStatus(bill.id, !bill.isPaid)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }
}
