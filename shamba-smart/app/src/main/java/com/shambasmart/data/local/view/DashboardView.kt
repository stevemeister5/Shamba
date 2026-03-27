package com.shambasmart.data.local.view

import androidx.room.DatabaseView

/**
 * Pre-joined view for Dashboard KPI data.
 * Optimizes dashboard queries by pre-computing joins across multiple tables.
 */
@DatabaseView(
    """
    SELECT 
        1 as id,
        (SELECT COUNT(*) FROM animals WHERE status = 'active') as herd_size,
        (SELECT COUNT(*) FROM animals WHERE species = 'goat' AND status = 'active') as goat_count,
        (SELECT COUNT(*) FROM animals WHERE species = 'sheep' AND status = 'active') as sheep_count,
        (SELECT COALESCE(SUM(quantityLitres), 0) FROM milk_collections 
         WHERE date = date('now')) as today_milk_yield,
        (SELECT COUNT(*) FROM cheese_batches WHERE status = 'aging') as cheese_batches_in_aging,
        (SELECT COUNT(*) FROM tasks WHERE status = 'pending' AND dueDate <= date('now')) as pending_tasks,
        (SELECT COUNT(*) FROM tasks WHERE status = 'pending' AND dueDate < date('now')) as overdue_tasks,
        (SELECT COUNT(*) FROM feed_inventory WHERE stockLevel <= COALESCE(reorderThreshold, 0)) as low_feed_alerts,
        (SELECT COUNT(*) FROM scouting_reports WHERE isResolved = 0 AND severityScore >= 4) as critical_pest_alerts,
        (SELECT COUNT(*) FROM calendar_events WHERE date = date('now')) as today_events
    """
)
data class DashboardView(
    val id: Int = 1,
    val herd_size: Int = 0,
    val goat_count: Int = 0,
    val sheep_count: Int = 0,
    val today_milk_yield: Double = 0.0,
    val cheese_batches_in_aging: Int = 0,
    val pending_tasks: Int = 0,
    val overdue_tasks: Int = 0,
    val low_feed_alerts: Int = 0,
    val critical_pest_alerts: Int = 0,
    val today_events: Int = 0
)