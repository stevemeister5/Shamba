package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(tableName = "cheese_batches")
data class CheeseBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: String,
    val productionDate: LocalDate,
    val milkVolumeUsed: Double,
    val cheeseType: String,
    val starterCulture: String? = null,
    val rennetUsed: String? = null,
    val yieldKg: Double,
    val agingStartDate: LocalDate? = null,
    val agingLocation: String? = null,
    val packagingDate: LocalDate? = null,
    val unitsPacked: Int? = null,
    val weightPerUnit: Double? = null,
    val status: String = "aging",
    val qualityNotes: String? = null,
    val outcomeRating: Int? = null,
    // Cost fields (TZS)
    @ColumnInfo(name = "milk_cost_tzs")
    val milkCostTzs: Long = 0L,
    @ColumnInfo(name = "culture_cost_tzs")
    val cultureCostTzs: Long = 0L,
    @ColumnInfo(name = "rennet_cost_tzs")
    val rennetCostTzs: Long = 0L,
    @ColumnInfo(name = "packaging_cost_tzs")
    val packagingCostTzs: Long = 0L,
    @ColumnInfo(name = "labour_cost_tzs")
    val labourCostTzs: Long = 0L,
    @ColumnInfo(name = "other_input_cost_tzs")
    val otherInputCostTzs: Long = 0L,
    // Sale fields
    @ColumnInfo(name = "sale_price_tzs_per_kg")
    val salePriceTzsPerKg: Long = 0L,
    @ColumnInfo(name = "quantity_sold_kg")
    val quantitySoldKg: Float = 0f,
    @ColumnInfo(name = "sale_date")
    val saleDate: Long? = null,
    // Legacy cost fields (deprecated, use new TZS fields)
    val costMilk: Double? = null,
    val costInputs: Double? = null,
    val costLabour: Double? = null,
    val costPackaging: Double? = null,
    val totalCost: Double? = null,
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
