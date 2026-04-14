package com.shambasmart.data.local.fts

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room callback that creates the FTS5 virtual table for BM25 full-text search.
 * 
 * Uses an external content table pattern:
 * - knowledge_chunks_fts references knowledge_chunks rows by docid
 * - Triggers keep the FTS index in sync with the main table
 * 
 * This gives us SQLite's BM25 ranking algorithm for free:
 * MATCH 'query' → ORDER BY bm25(knowledge_chunks_fts, ...)
 */
class KnowledgeFts5Callback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        createFtsTable(db)
        createTriggers(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Ensure FTS table exists even after destructive migration
        if (!ftsTableExists(db)) {
            createFtsTable(db)
            rebuildFtsIndex(db)
            createTriggers(db)
        }
    }

    private fun ftsTableExists(db: SupportSQLiteDatabase): Boolean {
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='knowledge_chunks_fts'"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    private fun createFtsTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS knowledge_chunks_fts 
            USING fts5(
                display_text,
                embedding_text,
                keywords,
                topic_tags,
                domain_tag,
                source_title,
                chunk_id UNINDEXED,
                tokenize='unicode61 remove_diacritics 2'
            )
        """)
    }

    private fun rebuildFtsIndex(db: SupportSQLiteDatabase) {
        // Rebuild FTS index from existing knowledge_chunks data
        db.execSQL("""
            INSERT OR REPLACE INTO knowledge_chunks_fts (
                rowid, 
                display_text, 
                embedding_text, 
                keywords, 
                topic_tags, 
                domain_tag, 
                source_title, 
                chunk_id
            )
            SELECT 
                rowid,
                display_text,
                embedding_text,
                keywords,
                topic_tags,
                domain_tag,
                source_title,
                id
            FROM knowledge_chunks
        """)
    }

    private fun createTriggers(db: SupportSQLiteDatabase) {
        // Drop existing triggers if they exist
        db.execSQL("DROP TRIGGER IF EXISTS knowledge_chunks_ai")
        db.execSQL("DROP TRIGGER IF EXISTS knowledge_chunks_ad")
        db.execSQL("DROP TRIGGER IF EXISTS knowledge_chunks_au")

        // INSERT trigger: Add new rows to FTS index
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS knowledge_chunks_ai
            AFTER INSERT ON knowledge_chunks
            BEGIN
                INSERT INTO knowledge_chunks_fts (
                    rowid, 
                    display_text, 
                    embedding_text, 
                    keywords, 
                    topic_tags, 
                    domain_tag, 
                    source_title, 
                    chunk_id
                )
                VALUES (
                    new.rowid,
                    new.display_text,
                    new.embedding_text,
                    new.keywords,
                    new.topic_tags,
                    new.domain_tag,
                    new.source_title,
                    new.id
                );
            END
        """)

        // DELETE trigger: Remove rows from FTS index
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS knowledge_chunks_ad
            AFTER DELETE ON knowledge_chunks
            BEGIN
                INSERT INTO knowledge_chunks_fts (
                    knowledge_chunks_fts, 
                    rowid, 
                    display_text, 
                    embedding_text, 
                    keywords, 
                    topic_tags, 
                    domain_tag, 
                    source_title, 
                    chunk_id
                )
                VALUES (
                    'delete',
                    old.rowid,
                    old.display_text,
                    old.embedding_text,
                    old.keywords,
                    old.topic_tags,
                    old.domain_tag,
                    old.source_title,
                    old.id
                );
            END
        """)

        // UPDATE trigger: Update rows in FTS index
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS knowledge_chunks_au
            AFTER UPDATE ON knowledge_chunks
            BEGIN
                INSERT INTO knowledge_chunks_fts (
                    knowledge_chunks_fts, 
                    rowid, 
                    display_text, 
                    embedding_text, 
                    keywords, 
                    topic_tags, 
                    domain_tag, 
                    source_title, 
                    chunk_id
                )
                VALUES (
                    'delete',
                    old.rowid,
                    old.display_text,
                    old.embedding_text,
                    old.keywords,
                    old.topic_tags,
                    old.domain_tag,
                    old.source_title,
                    old.id
                );
                INSERT INTO knowledge_chunks_fts (
                    rowid, 
                    display_text, 
                    embedding_text, 
                    keywords, 
                    topic_tags, 
                    domain_tag, 
                    source_title, 
                    chunk_id
                )
                VALUES (
                    new.rowid,
                    new.display_text,
                    new.embedding_text,
                    new.keywords,
                    new.topic_tags,
                    new.domain_tag,
                    new.source_title,
                    new.id
                );
            END
        """)
    }
}