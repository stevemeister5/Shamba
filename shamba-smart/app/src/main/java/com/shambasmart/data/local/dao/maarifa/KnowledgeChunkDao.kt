package com.shambasmart.data.local.dao.maarifa

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Maarifa Knowledge Chunks.
 * 
 * Supports the three-layer retrieval pipeline:
 * 1. Pre-filter by topic tags (fast elimination)
 * 2. BM25 keyword search via FTS5
 * 3. Vector similarity via manual cosine computation
 */
@Dao
interface KnowledgeChunkDao {
    
    // === INSERT ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chunk: KnowledgeChunk)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chunks: List<KnowledgeChunk>)
    
    // === QUERY ===
    @Query("SELECT * FROM knowledge_chunks WHERE id = :chunkId")
    suspend fun getChunkById(chunkId: String): KnowledgeChunk?
    
    @Query("SELECT * FROM knowledge_chunks WHERE id = :chunkId")
    fun observeChunkById(chunkId: String): Flow<KnowledgeChunk?>
    
    @Query("SELECT * FROM knowledge_chunks ORDER BY date_added DESC")
    fun observeAllChunks(): Flow<List<KnowledgeChunk>>
    
    @Query("SELECT COUNT(*) FROM knowledge_chunks")
    suspend fun getChunkCount(): Int
    
    @Query("SELECT COUNT(*) FROM knowledge_chunks WHERE source_type = 'bundled'")
    suspend fun getBundledChunkCount(): Int
    
    @Query("SELECT COUNT(*) FROM knowledge_chunks WHERE source_type = 'ingested'")
    suspend fun getIngestedChunkCount(): Int
    
    // === TOPIC TAG PRE-FILTER (Stage 1 retrieval) ===
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE topic_tags LIKE '%' || :tag || '%'
        ORDER BY source_credibility DESC, date_added DESC
    """)
    suspend fun getChunksByTag(tag: String): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE topic_tags LIKE '%' || :tag1 || '%' 
           OR topic_tags LIKE '%' || :tag2 || '%'
        ORDER BY source_credibility DESC, date_added DESC
    """)
    suspend fun getChunksByAnyTag(tag1: String, tag2: String): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE topic_tags LIKE '%' || :tag || '%'
          AND source_type = :sourceType
        ORDER BY date_added DESC
    """)
    suspend fun getChunksByTagAndSource(tag: String, sourceType: String): List<KnowledgeChunk>
    
    // === BM25 FTS5 KEYWORD SEARCH (Stage 2a retrieval) ===
    
    /**
     * FTS5 BM25 search returning KnowledgeChunks ranked by relevance.
     * Pass a SimpleSQLiteQuery with MATCH operator and bm5() ORDER BY.
     */
    @RawQuery
    suspend fun ftsSearchBm25(query: SupportSQLiteQuery): List<KnowledgeChunk>
    
    /**
     * FTS5 BM25 search returning raw scores for reranking.
     */
    @RawQuery
    suspend fun rawBm25Query(query: SupportSQLiteQuery): List<Bm25Result>
    
    /**
     * Fallback LIKE search when FTS5 returns no results.
     */
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE display_text LIKE '%' || :keyword || '%'
        ORDER BY 
            CASE WHEN source_type = 'bundled' THEN 0 ELSE 1 END,
            source_credibility DESC,
            date_added DESC
        LIMIT :limit
    """)
    suspend fun searchByKeyword(keyword: String, limit: Int = 20): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE display_text LIKE '%' || :keyword1 || '%' 
          AND display_text LIKE '%' || :keyword2 || '%'
        ORDER BY 
            CASE WHEN source_type = 'bundled' THEN 0 ELSE 1 END,
            medical_content DESC,
            date_added DESC
        LIMIT :limit
    """)
    suspend fun searchByMultipleKeywords(
        keyword1: String, 
        keyword2: String, 
        limit: Int = 20
    ): List<KnowledgeChunk>
    
    // === MEDICAL CONTENT ===
    @Query("SELECT * FROM knowledge_chunks WHERE medical_content = 1")
    suspend fun getMedicalChunks(): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE medical_content = 1 
          AND display_text LIKE '%' || :drugName || '%'
        LIMIT :limit
    """)
    suspend fun searchMedicalChunks(drugName: String, limit: Int = 10): List<KnowledgeChunk>
    
    // === SOURCE QUERIES ===
    @Query("SELECT DISTINCT source_title FROM knowledge_chunks ORDER BY source_title")
    suspend fun getAllSourceTitles(): List<String>
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE source_document_id = :sourceDocumentId 
        ORDER BY chunk_index ASC
    """)
    suspend fun getChunksBySourceDocument(sourceDocumentId: String): List<KnowledgeChunk>
    
    // === CONTEXT WINDOW ===
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE chunk_index = :chunkIndex - 1 
          AND source_document_id = :sourceDocumentId
        LIMIT 1
    """)
    suspend fun getPreviousChunk(sourceDocumentId: String, chunkIndex: Int): KnowledgeChunk?
    
    @Query("""
        SELECT * FROM knowledge_chunks 
        WHERE chunk_index = :chunkIndex + 1 
          AND source_document_id = :sourceDocumentId
        LIMIT 1
    """)
    suspend fun getNextChunk(sourceDocumentId: String, chunkIndex: Int): KnowledgeChunk?
    
    // === DELETE ===
    @Query("DELETE FROM knowledge_chunks WHERE id = :chunkId")
    suspend fun deleteById(chunkId: String)
    
    @Query("DELETE FROM knowledge_chunks WHERE source_document_id = :sourceDocumentId")
    suspend fun deleteBySourceDocument(sourceDocumentId: String)
    
    @Query("DELETE FROM knowledge_chunks WHERE source_type = 'ingested'")
    suspend fun deleteAllIngested()
    
    // === SYNC SUPPORT ===
    @Query("SELECT * FROM knowledge_chunks WHERE is_synced = 0")
    suspend fun getUnsyncedChunks(): List<KnowledgeChunk>
    
    @Query("UPDATE knowledge_chunks SET is_synced = 1 WHERE id = :chunkId")
    suspend fun markSynced(chunkId: String)
}

/**
 * Result class for BM25 FTS5 queries
 */
data class Bm25Result(
    val chunkId: String,
    val bm25Score: Double
)