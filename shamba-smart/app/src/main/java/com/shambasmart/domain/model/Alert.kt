package com.shambasmart.domain.model

import kotlinx.datetime.LocalDate

data class Alert(
    val id: Long = 0,
    val type: AlertType,
    val priority: AlertPriority,
    val title: String,
    val message: String,
    val relatedEntityId: Long? = null,
    val relatedEntityType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false,
    val dismissedAt: Long? = null
)

enum class AlertPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AlertType {
    VACCINATION_OVERDUE,
    ANIMAL_NOT_WEIGHED,
    LOW_FEED_STOCK,
    HARVEST_READY,
    CHEESE_AGING_COMPLETE,
    LOAN_REPAYMENT_DUE,
    MAINTENANCE_DUE,
    WEATHER_WARNING,
    FINANCIAL_THRESHOLD,
    TASK_OVERDUE
}