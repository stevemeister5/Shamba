package com.shambasmart.domain.usecase.financial

import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val financialRepository: FinancialRepository
) {
    fun getAllIncome(): Flow<List<Income>> = financialRepository.getAllIncome()

    fun getIncomeByCategory(category: String): Flow<List<Income>> = 
        financialRepository.getIncomeByCategory(category)

    fun getAllExpenses(): Flow<List<Expense>> = financialRepository.getAllExpenses()

    fun getExpensesByCategory(category: String): Flow<List<Expense>> = 
        financialRepository.getExpensesByCategory(category)
}