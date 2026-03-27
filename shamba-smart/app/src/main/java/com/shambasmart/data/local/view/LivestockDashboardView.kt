package com.shambasmart.data.local.view

import androidx.room.DatabaseView

/**
 * Pre-joined view for Livestock Dashboard data.
 * Aggregates livestock data for efficient dashboard queries.
 */
@DatabaseView(
    """
    SELECT 
        1 as id,
        (SELECT COUNT(*) FROM animals WHERE species = 'goat' AND status = 'active') as goat_count,
        (SELECT COUNT(*) FROM animals WHERE species = 'sheep' AND status = 'active') as sheep_count,
        (SELECT COUNT(*) FROM animals WHERE species = 'cattle' AND status = 'active') as cattle_count,
        (SELECT COUNT(*) FROM animals WHERE species = 'chicken' AND status = 'active') as chicken_count,
        (SELECT COUNT(*) FROM animals WHERE status = 'active') as total_active,
        (SELECT COUNT(*) FROM animals WHERE status = 'sick') as sick_count,
        (SELECT COUNT(*) FROM animals WHERE status = 'quarantined') as quarantined_count,
        (SELECT AVG(weight) FROM weight_entries we 
         WHERE we.animalId IN (SELECT id FROM animals WHERE status = 'active') 
         AND we.date >= date('now', '-30 days')) as avg_weight_kg,
        (SELECT COALESCE(SUM(quantityLitres), 0) FROM milk_production mp 
         WHERE mp.date >= date('now', '-7 days')) as weekly_milk_yield,
        (SELECT COUNT(*) FROM reproduction_records rr 
         WHERE rr.expectedBirthDate BETWEEN date('now') AND date('now', '+30 days')) as upcoming_births,
        (SELECT COUNT(*) FROM health_records hr 
         WHERE hr.date >= date('now', '-7 days') AND hr.status = 'treatment') as active_treatments
    """
)
data class LivestockDashboardView(
    val id: Int = 1,
    val goat_count: Int = 0,
    val sheep_count: Int = 0,
    val cattle_count: Int = 0,
    val chicken_count: Int = 0,
    val total_active: Int = 0,
    val sick_count: Int = 0,
    val quarantined_count: Int = 0,
    val avg_weight_kg: Double? = null,
    val weekly_milk_yield: Double = 0.0,
    val upcoming_births: Int = 0,
    val active_treatments: Int = 0
)