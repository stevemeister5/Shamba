package com.shambasmart.data.local.dao.maarifa

import androidx.room.*
import com.shambasmart.data.local.entity.maarifa.IngestedDocument
import kotlinx.coroutines.flow.Flow

/**
 * DAO for IngestedDocument — tracks documents ingested into the Maarifa knowledge base.
 */
@Dao
interface IngestedDocumentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: IngestedDocument)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<IngestedDocument>)
    
    @Update
    suspend fun update(document: IngestedDocument)
    
    @Delete
    suspend fun delete(document: IngestedDocument)
    
    @Query("SELECT * FROM ingested_documents WHERE id = :id")
    suspend fun getById(id: String): IngestedDocument?
    
    @Query("SELECT * FROM ingested_documents ORDER BY dateIngested DESC")
    fun getAll(): Flow<List<IngestedDocument>>
    
    @Query("SELECT * FROM ingested_documents WHERE processingStatus = :status ORDER BY dateIngested DESC")
    fun getByStatus(status: String): Flow<List<IngestedDocument>>
    
    @Query("SELECT COUNT(*) FROM ingested_documents")
    suspend fun getCount(): Int
    
    @Query("SELECT SUM(chunkCount) FROM ingested_documents WHERE processingStatus = 'complete'")
    suspend fun getTotalChunkCount(): Int?
}