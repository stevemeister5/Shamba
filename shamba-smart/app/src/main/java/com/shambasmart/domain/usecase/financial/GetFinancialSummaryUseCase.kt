package com.shambasmart.domain.usecase.financial

import com.shambasmart.data.local.entity.Loan
import com.shambasmart.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val outstandingLoans: Double
)

class GetFinancialSummaryUseCase @Inject constructor(
    private val financialRepository: FinancialRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): FinancialSummary {
        val totalIncome = financialRepository.getTotalIncome(startDate, endDate) ?: 0.0
        val totalExpenses = financialRepository.getTotalExpenses(startDate, endDate) ?: 0.0
        val outstandingLoans = financialRepository.getTotalOutstandingLoans() ?: 0.0
        
        return FinancialSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netProfit = totalIncome - totalExpenses,
            outstandingLoans = outstandingLoans
        )
    }

    fun getActiveLoans(): Flow<List<Loan>> = financialRepository.getActiveLoans()
}