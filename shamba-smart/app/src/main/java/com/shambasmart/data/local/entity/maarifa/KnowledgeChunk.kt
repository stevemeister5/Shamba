package com.shambasmart.data.local.entity.maarifa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
@Entity(
    tableName = "knowledge_chunks",
    foreignKeys = [
        ForeignKey(
            entity = IngestedDocument::class,
            parentColumns = ["id"],
            childColumns = ["source_document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("source_document_id"),
        Index("domain_tag"),
        Index("medical_content")
    ]
)
data class KnowledgeChunk(
    @PrimaryKey
    val id: String,                               // unique, e.g. "chunk_004821"
    
    @ColumnInfo(name = "display_text")
    val displayText: String,                      // clean display text, 100-600 words
    
    @ColumnInfo(name = "embedding_text")
    val embeddingText: String,                    // context prefix + text for embedding
                                                  // not displayed to user
    
    @ColumnInfo(name = "source_document_id")
    val sourceDocumentId: String,                 // FK → IngestedDocument
    
    @ColumnInfo(name = "source_title")
    val sourceTitle: String,                      // Denormalised for fast display
    
    @ColumnInfo(name = "source_type")
    val sourceType: String,                       // "bundled" | "ingested"
    
    @ColumnInfo(name = "source_credibility")
    val sourceCredibility: String,                // government_research, supplier_datasheet,
                                                  // academic, extension_bulletin,
                                                  // general_reference, unknown
    
    @ColumnInfo(name = "domain_tag")
    val domainTag: String,                        // "crops" | "livestock" | "medicines" | "pests" | etc.
    
    @ColumnInfo(name = "topic_tags")
    val topicTags: String,                        // JSON array stored as string
    
    @ColumnInfo(name = "section_header")
    val sectionHeader: String? = null,            // section title if detectable
    
    @ColumnInfo(name = "chunk_index")
    val chunkIndex: Int = 0,                      // position in source document
    
    @ColumnInfo(name = "total_chunks")
    val totalChunks: Int = 0,                     // total chunks from this source
    
    @ColumnInfo(name = "prev_chunk_tail")
    val prevChunkTail: String? = null,            // last 50 words of previous chunk
    
    @ColumnInfo(name = "next_chunk_head")
    val nextChunkHead: String? = null,            // first 50 words of next chunk
    
    @ColumnInfo(name = "medical_content")
    val medicalContent: Boolean = false,          // true if chunk contains drug name + dose/withdrawal
    
    @ColumnInfo(name = "language")
    val language: String = "en",                  // always "en" after quality gate
    
    @ColumnInfo(name = "keywords")
    val keywords: String,                         // JSON array for BM25 indexing
    
    @ColumnInfo(name = "embedding")
    val embedding: ByteArray? = null,             // 384-dim float32 as blob, null until computed
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,                          // timestamp
    
    @ColumnInfo(name = "last_verified")
    val lastVerified: Long? = null,               // null for ingested documents
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
) {
    /** Parse topic tags into a list */
    fun getTopicTagsList(): List<String> =
        topicTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    /** Parse embedding from ByteArray to float array */
    fun getVectorArray(): FloatArray? {
        if (embedding == null) return null
        return try {
            // Convert ByteArray to FloatArray (4 bytes per float)
            val floats = FloatArray(embedding.size / 4)
            for (i in floats.indices) {
                val bits = (embedding[i * 4].toInt() and 0xFF) or
                           ((embedding[i * 4 + 1].toInt() and 0xFF) shl 8) or
                           ((embedding[i * 4 + 2].toInt() and 0xFF) shl 16) or
                           ((embedding[i * 4 + 3].toInt() and 0xFF) shl 24)
                floats[i] = Float.fromBits(bits)
            }
            floats
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