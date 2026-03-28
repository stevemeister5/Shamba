package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    
    @Query("SELECT * FROM loans WHERE status = 'active' ORDER BY dueDate ASC")
    fun getActiveLoans(): Flow<List<Loan>>
    
    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: Long): Loan?
    
    @Query("""
        SELECT * FROM loans 
        WHERE status = 'active' 
        AND dueDate IS NOT NULL 
        AND dueDate <= :date
        ORDER BY dueDate ASC
    """)
    suspend fun getUpcomingRepayments(date: Long): List<Loan>
    
    @Query("""
        SELECT * FROM loans 
        WHERE status = 'active' 
        AND dueDate IS NOT NULL 
        AND dueDate < :date
        ORDER BY dueDate ASC
    """)
    suspend fun getOverdueLoans(date: Long): List<Loan>
    
    @Query("SELECT SUM(balance) FROM loans WHERE status = 'active'")
    fun getTotalOutstandingBalance(): Flow<Double?>
    
    @Query("SELECT SUM(totalRepaid) FROM loans")
    fun getTotalRepaid(): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<Loan>)
    
    @Update
    suspend fun updateLoan(loan: Loan)
    
    @Delete
    suspend fun deleteLoan(loan: Loan)
    
    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoanById(id: Long)
}