package com.shambasmart.domain.usecase

import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.*
import com.shambasmart.domain.model.Alert
import com.shambasmart.domain.model.AlertPriority
import com.shambasmart.domain.model.AlertType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
            val overdueRecords = healthRecordDao.getOverdueVaccinations(today.toEpochDays().toLong())
            overdueRecords.forEach { record ->
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
            val thirtyDaysAgo = today.minus(30, kotlinx.datetime.DateTimeUnit.DAY)
            val animals = animalDao.getAnimalsNotWeighedSince(thirtyDaysAgo.toEpochDays().toLong())
            animals.forEach { animal ->
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
        } catch (e: Exception) {
            // Handle error silently
        }
        return alerts
    }
    
    private suspend fun checkLowFeedStock(): List<Alert> {
        val alerts = mutableListOf<Alert>()
        try {
            val lowStockFeeds = feedDao.getLowStockFeeds()
            lowStockFeeds.forEach { feed ->
                alerts.add(
                    Alert(
                        type = AlertType.LOW_FEED_STOCK,
                        priority = AlertPriority.HIGH,
                        title = "Low Feed Stock",
                        message = "${feed.name} is below reorder level (${feed.currentStock} ${feed.unit} remaining)",
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
            val overdueTasks = taskDao.getOverdueTasks(today.toEpochDays().toLong())
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
            val upcomingLoans = loanDao.getUpcomingRepayments(today.toEpochDays().toLong())
            upcomingLoans.forEach { loan ->
                alerts.add(
                    Alert(
                        type = AlertType.LOAN_REPAYMENT_DUE,
                        priority = AlertPriority.HIGH,
                        title = "Loan Repayment Due",
                        message = "Loan repayment of ${loan.repaymentAmount} due on ${loan.nextRepaymentDate}",
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
            val readyCheese = cheeseDao.getReadyCheese(today.toEpochDays().toLong())
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
            val readyCrops = cropDao.getReadyForHarvest(today.toEpochDays().toLong())
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