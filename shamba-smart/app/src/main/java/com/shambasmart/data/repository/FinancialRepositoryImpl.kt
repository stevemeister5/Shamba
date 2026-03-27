package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.FinancialDao
import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Loan
import com.shambasmart.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepositoryImpl @Inject constructor(
    private val financialDao: FinancialDao
) : FinancialRepository {

    // Income
    override fun getAllIncome(): Flow<List<Income>> = financialDao.getAllIncome()

    override fun getIncomeByCategory(category: String): Flow<List<Income>> = financialDao.getIncomeByCategory(category)

    override suspend fun getTotalIncome(startDate: LocalDate, endDate: LocalDate): Double? = 
        financialDao.getTotalIncome(startDate, endDate)

    override suspend fun insertIncome(income: Income): Long = 
        financialDao.insertIncome(income.copy(isSynced = false))

    override suspend fun updateIncome(income: Income) = 
        financialDao.updateIncome(income.copy(isSynced = false))

    override suspend fun deleteIncome(income: Income) = financialDao.deleteIncome(income)

    // Expenses
    override fun getAllExpenses(): Flow<List<Expense>> = financialDao.getAllExpenses()

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> = 
        financialDao.getExpensesByCategory(category)

    override suspend fun getTotalExpenses(startDate: LocalDate, endDate: LocalDate): Double? = 
        financialDao.getTotalExpenses(startDate, endDate)

    override suspend fun insertExpense(expense: Expense): Long = 
        financialDao.insertExpense(expense.copy(isSynced = false))

    override suspend fun updateExpense(expense: Expense) = 
        financialDao.updateExpense(expense.copy(isSynced = false))

    override suspend fun deleteExpense(expense: Expense) = financialDao.deleteExpense(expense)

    // Loans
    override fun getActiveLoans(): Flow<List<Loan>> = financialDao.getActiveLoans()

    override suspend fun getTotalOutstandingLoans(): Double? = financialDao.getTotalOutstandingLoans()

    override suspend fun insertLoan(loan: Loan): Long = 
        financialDao.insertLoan(loan.copy(isSynced = false))

    override suspend fun updateLoan(loan: Loan) = 
        financialDao.updateLoan(loan.copy(isSynced = false))

    override suspend fun deleteLoan(loan: Loan) = financialDao.deleteLoan(loan)

    override suspend fun updateLoanRepayment(id: Long, repaid: Double, balance: Double, status: String) {
        financialDao.updateLoanRepayment(id, repaid, balance, status)
    }

    // Sync
    override suspend fun getUnsyncedIncome(): List<Income> = financialDao.getUnsyncedIncome()

    override suspend fun getUnsyncedExpenses(): List<Expense> = financialDao.getUnsyncedExpenses()

    override suspend fun getUnsyncedLoans(): List<Loan> = financialDao.getUnsyncedLoans()

    override suspend fun updateIncomeSyncStatus(id: Long, synced: Boolean) {
        financialDao.updateIncomeSyncStatus(id, synced)
    }

    override suspend fun updateExpenseSyncStatus(id: Long, synced: Boolean) {
        financialDao.updateExpenseSyncStatus(id, synced)
    }

    override suspend fun updateLoanSyncStatus(id: Long, synced: Boolean) {
        financialDao.updateLoanSyncStatus(id, synced)
    }
}