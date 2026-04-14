package com.shambasmart.data.local.entity.maarifa

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * IngestedDocument — tracks documents that have been ingested into the Maarifa knowledge base.
 * 
 * Each ingested document produces multiple KnowledgeChunk entries.
 * Deleting an IngestedDocument cascades to delete all its chunks.
 */
@Entity(tableName = "ingested_documents")
data class IngestedDocument(
    @PrimaryKey
    val id: String,
    val title: String,
    val domainTag: String,
    val filePath: String? = null,
    val sourceCredibility: String,
    val chunkCount: Int,
    val dateIngested: Long,
    val processingStatus: String = "pending", // "pending" | "complete" | "failed"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)