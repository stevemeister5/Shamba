package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lenderName: String,
    val amount: Double,
    val disbursementDate: LocalDate,
    val interestRate: Double? = null,
    val repaymentSchedule: String? = null,
    val totalRepaid: Double = 0.0,
    val balance: Double,
    val dueDate: LocalDate? = null,
    val status: String = "active",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)