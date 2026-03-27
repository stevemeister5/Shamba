package com.shambasmart.maarifa

import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.maarifa.chunker.SemanticChunker
import com.shambasmart.maarifa.retrieval.KnowledgeRetriever
import com.shambasmart.maarifa.rules.RuleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MaarifaModule {

    @Provides
    @Singleton
    fun provideSemanticChunker(): SemanticChunker = SemanticChunker()

    @Provides
    @Singleton
    fun provideRuleEngine(ruleDao: OperationalRuleDao): RuleEngine = RuleEngine(ruleDao)

    @Provides
    @Singleton
    fun provideKnowledgeRetriever(
        chunkDao: KnowledgeChunkDao,
        ruleDao: OperationalRuleDao
    ): KnowledgeRetriever = KnowledgeRetriever(chunkDao, ruleDao)
}