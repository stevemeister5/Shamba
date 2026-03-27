package com.shambasmart.di

import com.shambasmart.data.repository.AnimalRepositoryImpl
import com.shambasmart.data.repository.CropRepositoryImpl
import com.shambasmart.data.repository.FinancialRepositoryImpl
import com.shambasmart.domain.repository.AnimalRepository
import com.shambasmart.domain.repository.CropRepository
import com.shambasmart.domain.repository.FinancialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnimalRepository(
        animalRepositoryImpl: AnimalRepositoryImpl
    ): AnimalRepository

    @Binds
    @Singleton
    abstract fun bindCropRepository(
        cropRepositoryImpl: CropRepositoryImpl
    ): CropRepository

    @Binds
    @Singleton
    abstract fun bindFinancialRepository(
        financialRepositoryImpl: FinancialRepositoryImpl
    ): FinancialRepository
}
