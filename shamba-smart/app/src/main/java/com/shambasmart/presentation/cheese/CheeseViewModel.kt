package com.shambasmart.presentation.cheese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.CheeseDao
import com.shambasmart.data.local.dao.FinancialDao
import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.MilkCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class CheeseViewModel @Inject constructor(
    private val cheeseDao: CheeseDao,
    private val financialDao: FinancialDao
) : ViewModel() {

    val allCheeseBatches: StateFlow<List<CheeseBatch>> = cheeseDao.getAllCheeseBatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMilkCollections: StateFlow<List<MilkCollection>> = cheeseDao.getAllMilkCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCheeseBatch(batch: CheeseBatch) {
        viewModelScope.launch {
            cheeseDao.insertBatch(batch)
        }
    }

    fun updateCheeseBatch(batch: CheeseBatch) {
        viewModelScope.launch {
            cheeseDao.updateBatch(batch.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteCheeseBatch(batch: CheeseBatch) {
        viewModelScope.launch {
            cheeseDao.deleteBatch(batch)
        }
    }

    fun addMilkCollection(collection: MilkCollection) {
        viewModelScope.launch {
            cheeseDao.insertCollection(collection)
        }
    }

    fun deleteMilkCollection(collection: MilkCollection) {
        viewModelScope.launch {
            cheeseDao.deleteCollection(collection)
        }
    }

    fun sellCheeseBatch(batch: CheeseBatch, quantityKg: Double, pricePerKg: Double, buyerName: String) {
        viewModelScope.launch {
            // Update batch status to sold and record sale details
            val updatedBatch = batch.copy(
                status = "sold",
                yieldKg = batch.yieldKg - quantityKg, // Reduce yield by sold quantity
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            cheeseDao.updateBatch(updatedBatch)
            
            // Create income record for the sale
            val totalSale = quantityKg * pricePerKg
            val income = Income(
                category = "cheese_sale",
                amount = totalSale,
                date = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()),
                description = "Cheese sale: ${batch.batchId} - ${quantityKg}kg to $buyerName",
                isSynced = false
            )
            financialDao.insertIncome(income)
        }
    }
}
