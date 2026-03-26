package com.shambasmart.presentation.financial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.FinancialDao
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Loan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val financialDao: FinancialDao
) : ViewModel() {

    val allIncome: StateFlow<List<Income>> = financialDao.getAllIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = financialDao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoans: StateFlow<List<Loan>> = financialDao.getActiveLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addIncome(income: Income) {
        viewModelScope.launch {
            financialDao.insertIncome(income)
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            financialDao.deleteIncome(income)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            financialDao.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            financialDao.deleteExpense(expense)
        }
    }

    fun addLoan(loan: Loan) {
        viewModelScope.launch {
            financialDao.insertLoan(loan)
        }
    }

    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            financialDao.updateLoan(loan)
        }
    }
}