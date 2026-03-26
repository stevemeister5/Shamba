package com.shambasmart.di

import com.shambasmart.data.repository.AnimalRepositoryImpl
import com.shambasmart.domain.repository.AnimalRepository
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
}