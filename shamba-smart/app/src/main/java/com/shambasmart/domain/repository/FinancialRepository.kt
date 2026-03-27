package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Loan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface FinancialRepository {
    // Income
    fun getAllIncome(): Flow<List<Income>>
    fun getIncomeByCategory(category: String): Flow<List<Income>>
    suspend fun getTotalIncome(startDate: LocalDate, endDate: LocalDate): Double?
    suspend fun insertIncome(income: Income): Long
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(income: Income)

    // Expenses
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    suspend fun getTotalExpenses(startDate: LocalDate, endDate: LocalDate): Double?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)

    // Loans
    fun getActiveLoans(): Flow<List<Loan>>
    suspend fun getTotalOutstandingLoans(): Double?
    suspend fun insertLoan(loan: Loan): Long
    suspend fun updateLoan(loan: Loan)
    suspend fun deleteLoan(loan: Loan)
    suspend fun updateLoanRepayment(id: Long, repaid: Double, balance: Double, status: String)

    // Sync
    suspend fun getUnsyncedIncome(): List<Income>
    suspend fun getUnsyncedExpenses(): List<Expense>
    suspend fun getUnsyncedLoans(): List<Loan>
    suspend fun updateIncomeSyncStatus(id: Long, synced: Boolean)
    suspend fun updateExpenseSyncStatus(id: Long, synced: Boolean)
    suspend fun updateLoanSyncStatus(id: Long, synced: Boolean)
}