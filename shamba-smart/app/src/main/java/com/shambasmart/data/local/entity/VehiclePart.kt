package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_parts")
data class VehiclePart(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val partName: String,
    val partNumber: String? = null,
    val category: PartCategory,
    val quantityInStock: Int = 0,
    val reorderLevel: Int = 1,
    val unitCost: Double = 0.0,
    val supplier: String? = null,
    val lastReplacedDate: Long? = null,
    val lastReplacedMileage: Double? = null,
    val lastReplacedHours: Double? = null,
    val expectedLifespanMiles: Double? = null,
    val expectedLifespanHours: Double? = null,
    val nextReplacementMileage: Double? = null,
    val nextReplacementHours: Double? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class PartCategory {
    ENGINE,
    TRANSMISSION,
    BRAKES,
    TIRES,
    FILTERS,
    BELTS,
    HOSES,
    ELECTRICAL,
    BODY,
    INTERIOR,
    HYDRAULICS,
    PTO,
    OTHER
}