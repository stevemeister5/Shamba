package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: VehicleType,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val licensePlate: String? = null,
    val currentMileage: Double = 0.0,
    val currentHours: Double = 0.0,
    val lastServiceMileage: Double? = null,
    val lastServiceHours: Double? = null,
    val nextServiceMileage: Double? = null,
    val nextServiceHours: Double? = null,
    val fuelType: FuelType = FuelType.DIESEL,
    val status: VehicleStatus = VehicleStatus.OPERATIONAL,
    val purchaseDate: Long? = null,
    val purchasePrice: Double? = null,
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

enum class VehicleType {
    TRACTOR,
    TRUCK,
    MOTORCYCLE,
    WATER_TANKER,
    FEED_MIXER,
    HARVESTER,
    SPRAYER,
    GENERATOR,
    OTHER
}

enum class FuelType {
    DIESEL,
    PETROL,
    ELECTRIC,
    HYBRID
}

enum class VehicleStatus {
    OPERATIONAL,
    NEEDS_SERVICE,
    UNDER_REPAIR,
    OUT_OF_SERVICE
}