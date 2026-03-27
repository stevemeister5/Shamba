package com.shambasmart.maarifa

import android.content.Context
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.maarifa.chunker.SemanticChunker
import com.shambasmart.maarifa.contextbridge.ContextBridge
import com.shambasmart.maarifa.ingestion.KnowledgeIngestionPipeline
import com.shambasmart.maarifa.retrieval.KnowledgeRetriever
import com.shambasmart.maarifa.retrieval.VectorSearchEngine
import com.shambasmart.maarifa.rules.RuleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideVectorSearchEngine(@ApplicationContext context: Context): VectorSearchEngine =
        VectorSearchEngine(context)

    @Provides
    @Singleton
    fun provideContextBridge(
        animalDao: AnimalDao,
        healthRecordDao: HealthRecordDao,
        reproductionDao: ReproductionDao,
        milkProductionDao: MilkProductionDao,
        plotDao: PlotDao,
        cropDao: CropDao,
        weatherDao: WeatherDao,
        feedDao: FeedDao,
        taskDao: TaskDao,
        calendarDao: CalendarDao
    ): ContextBridge = ContextBridge(
        animalDao, healthRecordDao, reproductionDao, milkProductionDao,
        plotDao, cropDao, weatherDao, feedDao, taskDao, calendarDao
    )

    @Provides
    @Singleton
    fun provideKnowledgeIngestionPipeline(
        chunkDao: KnowledgeChunkDao,
        ruleDao: OperationalRuleDao,
        vectorEngine: VectorSearchEngine,
        semanticChunker: SemanticChunker
    ): KnowledgeIngestionPipeline = KnowledgeIngestionPipeline(
        chunkDao, ruleDao, vectorEngine, semanticChunker
    )
}
