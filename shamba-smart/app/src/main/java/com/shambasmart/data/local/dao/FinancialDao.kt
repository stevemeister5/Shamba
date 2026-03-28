package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Loan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface FinancialDao {
    // Income queries
    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE category = :category")
    fun getIncomeByCategory(category: String): Flow<List<Income>>

    @Query("SELECT SUM(amount) FROM income WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncome(startDate: LocalDate, endDate: LocalDate): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIncome(incomes: List<Income>)

    @Update
    suspend fun updateIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)

    // Expense queries
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpenses(startDate: LocalDate, endDate: LocalDate): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Loan queries
    @Query("SELECT * FROM loans WHERE status = 'active' ORDER BY dueDate ASC")
    fun getActiveLoans(): Flow<List<Loan>>

    @Query("SELECT SUM(balance) FROM loans WHERE status = 'active'")
    suspend fun getTotalOutstandingLoans(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    @Query("UPDATE loans SET totalRepaid = :repaid, balance = :balance, status = :status WHERE id = :id")
    suspend fun updateLoanRepayment(id: Long, repaid: Double, balance: Double, status: String)

    // Sync queries
    @Query("UPDATE income SET isSynced = :synced WHERE id = :id")
    suspend fun updateIncomeSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE expenses SET isSynced = :synced WHERE id = :id")
    suspend fun updateExpenseSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE loans SET isSynced = :synced WHERE id = :id")
    suspend fun updateLoanSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM income WHERE isSynced = 0")
    suspend fun getUnsyncedIncome(): List<Income>

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<Expense>

    @Query("SELECT * FROM loans WHERE isSynced = 0")
    suspend fun getUnsyncedLoans(): List<Loan>

    // SyncManager support
    @Query("SELECT * FROM income WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<Income>
}
