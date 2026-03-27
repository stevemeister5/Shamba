package com.shambasmart.data.local.entity.maarifa

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Maarifa Prose Knowledge Chunk — the fundamental unit of knowledge storage.
 * 
 * Every piece of knowledge — crops, livestock, medicines, cheese, weather,
 * or any future topic — is stored as a plain English text chunk with
 * lightweight metadata. No rigid domain schemas. Infinitely extensible.
 * 
 * This follows the schema-free architecture from KnowledgeEnginePrb.md
 * and integrates directly into shamba-smart's Room database.
 */
@Entity(tableName = "maarifa_chunks")
data class KnowledgeChunk(
    @PrimaryKey
    val chunkId: String,                          // unique, e.g. "chunk_004821"
    
    val text: String,                             // clean display text, 100-600 words
    
    val embeddingText: String,                    // context prefix + text for embedding
                                                  // not displayed to user
    
    val sourceTitle: String,                      // e.g. "ILRI Small Ruminant Manual 2021"
    
    val sourceType: String,                       // "bundled" or "ingested"
    
    val sourceCredibility: String,                // government_research, supplier_datasheet,
                                                  // academic, extension_bulletin,
                                                  // general_reference, unknown
    
    val topicTags: String,                        // comma-separated tags:
                                                  // "goats,disease,CCPP,respiratory"
    
    val sectionHeader: String? = null,            // section title if detectable
    
    val chunkIndex: Int = 0,                      // position in source document
    
    val totalChunksInSource: Int = 0,             // total chunks from this source
    
    val prevChunkTail: String? = null,            // last 50 words of previous chunk
    
    val nextChunkHead: String? = null,            // first 50 words of next chunk
    
    val medicalContent: Boolean = false,          // true if chunk contains drug + dose/withdrawal
    
    val dateAdded: String,                        // ISO date
    
    val lastVerified: String? = null,             // ISO date, null for ingested
    
    val vector: String? = null,                   // JSON-encoded 384-dimension float array
                                                  // null until embedding generated
    
    val language: String = "en",                  // always "en" after quality gate
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    /** Parse topic tags into a list */
    fun getTopicTagsList(): List<String> =
        topicTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    /** Parse vector from JSON string to float array */
    fun getVectorArray(): FloatArray? {
        if (vector == null) return null
        return try {
            vector.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().toFloat() }
                .toFloatArray()
        } catch (e: Exception) {
            null
        }
    }
    
    companion object {
        /** Source credibility levels for ingested documents */
        val SOURCE_CREDIBILITY_LEVELS = listOf(
            "government_research",
            "supplier_datasheet", 
            "academic",
            "extension_bulletin",
            "general_reference",
            "unknown"
        )
        
        /** Credibility weight for retrieval scoring — higher = more trusted */
        fun credibilityWeight(credibility: String): Float = when(credibility) {
            "government_research" -> 1.0f
            "supplier_datasheet" -> 0.9f
            "academic" -> 0.85f
            "extension_bulletin" -> 0.8f
            "general_reference" -> 0.7f
            "bundled" -> 1.0f  // bundled knowledge always trusted
            else -> 0.5f
        }
    }
}