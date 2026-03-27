package com.shambasmart.domain.usecase.financial

import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Loan
import com.shambasmart.domain.repository.FinancialRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val financialRepository: FinancialRepository
) {
    suspend fun addIncome(income: Income): Result<Long> {
        return try {
            val id = financialRepository.insertIncome(income)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(expense: Expense): Result<Long> {
        return try {
            val id = financialRepository.insertExpense(expense)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addLoan(loan: Loan): Result<Long> {
        return try {
            val id = financialRepository.insertLoan(loan)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}