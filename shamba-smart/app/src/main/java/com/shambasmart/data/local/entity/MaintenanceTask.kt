package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_tasks")
data class MaintenanceTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: MaintenanceType,
    val priority: MaintenancePriority,
    val infrastructureId: String? = null,
    val vehicleId: String? = null,
    val partName: String? = null,
    val partNumber: String? = null,
    val mileageAtService: Double? = null,
    val hoursRunAtService: Double? = null,
    val nextServiceMileage: Double? = null,
    val nextServiceHours: Double? = null,
    val scheduledDate: Long,
    val completedDate: Long? = null,
    val status: MaintenanceStatus = MaintenanceStatus.SCHEDULED,
    val assignedTo: String? = null,
    val estimatedDurationHours: Double = 1.0,
    val actualDurationHours: Double? = null,
    val cost: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class MaintenanceType {
    DIPPING_TANK_CLEANING,
    EQUIPMENT_SERVICING,
    VEHICLE_MAINTENANCE,
    VEHICLE_PART_REPLACEMENT,
    INFRASTRUCTURE_REPAIR,
    WATER_SYSTEM_MAINTENANCE,
    SHELTER_CLEANING,
    FENCE_REPAIR,
    TOOL_MAINTENANCE,
    PREVENTIVE_MAINTENANCE
}

enum class MaintenancePriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class MaintenanceStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE
}