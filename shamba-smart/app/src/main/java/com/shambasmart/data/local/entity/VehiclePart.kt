package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
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