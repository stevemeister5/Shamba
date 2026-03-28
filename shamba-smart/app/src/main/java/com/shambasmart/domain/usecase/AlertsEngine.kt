package com.shambasmart.domain.usecase

import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.*
import com.shambasmart.domain.model.Alert
import com.shambasmart.domain.model.AlertPriority
import com.shambasmart.domain.model.AlertType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsEngine @Inject constructor(
    private val animalDao: AnimalDao,
    private val healthRecordDao: HealthRecordDao,
    private val feedDao: FeedDao,
    private val taskDao: TaskDao,
    private val loanDao: LoanDao,
    private val cheeseDao: CheeseDao,
    private val cropDao: CropDao
) {
    
    fun generateAlerts(): Flow<List<Alert>> = flow {
        val alerts = mutableListOf<Alert>()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // Check vaccination overdue alerts
        alerts.addAll(checkVaccinationOverdue(today))
        
        // Check animals not weighed recently
        alerts.addAll(checkAnimalsNotWeighed(today))
        
        // Check low feed stock
        alerts.addAll(checkLowFeedStock())
        
        // Check overdue tasks
        alerts.addAll(checkOverdueTasks(today))
        
        // Check loan repayments
        alerts.addAll(checkLoanRepayments(today))
        
        // Check cheese aging complete
        alerts.addAll(checkCheeseAging(today))
        
        // Check harvest ready
        alerts.addAll(checkHarvestReady(today))
        
        emit(alerts.sortedByDescending { it.priority.ordinal })
    }
    
    private suspend fun checkVaccinationOverdue(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val overdueRecords = healthRecordDao.getUpcomingDueRecords(today)
            overdueRecords.filter { it.nextDueDate != null && it.nextDueDate <= today }.forEach { record ->
                alerts.add(
                    Alert(
                        type = AlertType.VACCINATION_OVERDUE,
                        priority = AlertPriority.HIGH,
                        title = "Vaccination Overdue",
                        message = "Vaccination for animal #${record.animalId} is overdue",
                        relatedEntityId = record.id,
                        relatedEntityType = "health_record"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkAnimalsNotWeighed(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val allAnimals = animalDao.getAllActiveAnimals().first()
            allAnimals.forEach { animal ->
                // Use updatedAt as proxy for last weigh date since Animal entity doesn't have lastWeighDate field
                val lastUpdateDate = LocalDate.fromEpochDays((animal.updatedAt / (24 * 60 * 60 * 1000)).toInt())
                if (lastUpdateDate < today.minus(30, DateTimeUnit.DAY)) {
                    alerts.add(
                        Alert(
                            type = AlertType.ANIMAL_NOT_WEIGHED,
                            priority = AlertPriority.MEDIUM,
                            title = "Animal Not Weighed",
                            message = "Animal #${animal.tagId ?: "Unknown"} hasn't been weighed in 30+ days",
                            relatedEntityId = animal.id,
                            relatedEntityType = "animal"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkLowFeedStock(): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val lowStockFeeds = feedDao.getLowStockFeed()
            lowStockFeeds.forEach { feed ->
                alerts.add(
                    Alert(
                        type = AlertType.LOW_FEED_STOCK,
                        priority = AlertPriority.HIGH,
                        title = "Low Feed Stock",
                        message = "${feed.feedType} is below reorder level (${feed.stockLevel} ${feed.unit} remaining)",
                        relatedEntityId = feed.id,
                        relatedEntityType = "feed"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkOverdueTasks(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val overdueTasks = taskDao.getOverdueTasks(today)
            overdueTasks.forEach { task ->
                alerts.add(
                    Alert(
                        type = AlertType.TASK_OVERDUE,
                        priority = AlertPriority.MEDIUM,
                        title = "Task Overdue",
                        message = "Task '${task.title}' is overdue",
                        relatedEntityId = task.id,
                        relatedEntityType = "task"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkLoanRepayments(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val activeLoans = loanDao.getActiveLoans().first()
            activeLoans.forEach { loan ->
                alerts.add(
                    Alert(
                        type = AlertType.LOAN_REPAYMENT_DUE,
                        priority = AlertPriority.HIGH,
                        title = "Loan Repayment Due",
                        message = "Loan repayment of ${loan.totalRepaid} due on ${loan.dueDate}",
                        relatedEntityId = loan.id,
                        relatedEntityType = "loan"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkCheeseAging(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val readyCheese = cheeseDao.getBatchesByStatus("aging").first()
            readyCheese.forEach { cheese ->
                alerts.add(
                    Alert(
                        type = AlertType.CHEESE_AGING_COMPLETE,
                        priority = AlertPriority.MEDIUM,
                        title = "Cheese Ready",
                        message = "Cheese batch #${cheese.batchId} has completed aging",
                        relatedEntityId = cheese.id,
                        relatedEntityType = "cheese_batch"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkHarvestReady(today: LocalDate): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val readyCrops = cropDao.getCropsByStatus("ready_for_harvest").first()
            readyCrops.forEach { crop ->
                alerts.add(
                    Alert(
                        type = AlertType.HARVEST_READY,
                        priority = AlertPriority.MEDIUM,
                        title = "Harvest Ready",
                        message = "Crop planting #${crop.id} is ready for harvest",
                        relatedEntityId = crop.id,
                        relatedEntityType = "crop_planting"
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
}
