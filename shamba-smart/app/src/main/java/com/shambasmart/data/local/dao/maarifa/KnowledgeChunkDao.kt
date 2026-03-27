package com.shambasmart.data.local.dao.maarifa

import androidx.room.*
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Maarifa Knowledge Chunks.
 * 
 * Supports the three-layer retrieval pipeline:
 * 1. Pre-filter by topic tags (fast elimination)
 * 2. BM25 keyword search via full-text index
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
    @Query("SELECT * FROM maarifa_chunks WHERE chunkId = :chunkId")
    suspend fun getChunkById(chunkId: String): KnowledgeChunk?
    
    @Query("SELECT * FROM maarifa_chunks WHERE chunkId = :chunkId")
    fun observeChunkById(chunkId: String): Flow<KnowledgeChunk?>
    
    @Query("SELECT * FROM maarifa_chunks ORDER BY dateAdded DESC")
    fun observeAllChunks(): Flow<List<KnowledgeChunk>>
    
    @Query("SELECT COUNT(*) FROM maarifa_chunks")
    suspend fun getChunkCount(): Int
    
    @Query("SELECT COUNT(*) FROM maarifa_chunks WHERE sourceType = 'bundled'")
    suspend fun getBundledChunkCount(): Int
    
    @Query("SELECT COUNT(*) FROM maarifa_chunks WHERE sourceType = 'ingested'")
    suspend fun getIngestedChunkCount(): Int
    
    // === TOPIC TAG PRE-FILTER (Stage 1 retrieval) ===
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE topicTags LIKE '%' || :tag || '%'
        ORDER BY sourceCredibility DESC, dateAdded DESC
    """)
    suspend fun getChunksByTag(tag: String): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE topicTags LIKE '%' || :tag1 || '%' 
           OR topicTags LIKE '%' || :tag2 || '%'
        ORDER BY sourceCredibility DESC, dateAdded DESC
    """)
    suspend fun getChunksByAnyTag(tag1: String, tag2: String): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE topicTags LIKE '%' || :tag || '%'
          AND sourceType = :sourceType
        ORDER BY dateAdded DESC
    """)
    suspend fun getChunksByTagAndSource(tag: String, sourceType: String): List<KnowledgeChunk>
    
    // === BM25 KEYWORD SEARCH (Stage 2a retrieval) ===
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE text LIKE '%' || :keyword || '%'
        ORDER BY 
            CASE WHEN sourceType = 'bundled' THEN 0 ELSE 1 END,
            sourceCredibility DESC,
            dateAdded DESC
        LIMIT :limit
    """)
    suspend fun searchByKeyword(keyword: String, limit: Int = 20): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE text LIKE '%' || :keyword1 || '%' 
          AND text LIKE '%' || :keyword2 || '%'
        ORDER BY 
            CASE WHEN sourceType = 'bundled' THEN 0 ELSE 1 END,
            medicalContent DESC,
            dateAdded DESC
        LIMIT :limit
    """)
    suspend fun searchByMultipleKeywords(
        keyword1: String, 
        keyword2: String, 
        limit: Int = 20
    ): List<KnowledgeChunk>
    
    // === MEDICAL CONTENT ===
    @Query("SELECT * FROM maarifa_chunks WHERE medicalContent = 1")
    suspend fun getMedicalChunks(): List<KnowledgeChunk>
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE medicalContent = 1 
          AND text LIKE '%' || :drugName || '%'
        LIMIT :limit
    """)
    suspend fun searchMedicalChunks(drugName: String, limit: Int = 10): List<KnowledgeChunk>
    
    // === SOURCE QUERIES ===
    @Query("SELECT DISTINCT sourceTitle FROM maarifa_chunks ORDER BY sourceTitle")
    suspend fun getAllSourceTitles(): List<String>
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE sourceTitle = :sourceTitle 
        ORDER BY chunkIndex ASC
    """)
    suspend fun getChunksBySource(sourceTitle: String): List<KnowledgeChunk>
    
    // === CONTEXT WINDOW ===
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE chunkIndex = :chunkIndex - 1 
          AND sourceTitle = :sourceTitle
        LIMIT 1
    """)
    suspend fun getPreviousChunk(sourceTitle: String, chunkIndex: Int): KnowledgeChunk?
    
    @Query("""
        SELECT * FROM maarifa_chunks 
        WHERE chunkIndex = :chunkIndex + 1 
          AND sourceTitle = :sourceTitle
        LIMIT 1
    """)
    suspend fun getNextChunk(sourceTitle: String, chunkIndex: Int): KnowledgeChunk?
    
    // === DELETE ===
    @Query("DELETE FROM maarifa_chunks WHERE chunkId = :chunkId")
    suspend fun deleteById(chunkId: String)
    
    @Query("DELETE FROM maarifa_chunks WHERE sourceTitle = :sourceTitle")
    suspend fun deleteBySource(sourceTitle: String)
    
    @Query("DELETE FROM maarifa_chunks WHERE sourceType = 'ingested'")
    suspend fun deleteAllIngested()
    
    // === SYNC SUPPORT ===
    @Query("SELECT * FROM maarifa_chunks WHERE isSynced = 0")
    suspend fun getUnsyncedChunks(): List<KnowledgeChunk>
    
    @Query("UPDATE maarifa_chunks SET isSynced = 1 WHERE chunkId = :chunkId")
    suspend fun markSynced(chunkId: String)
}