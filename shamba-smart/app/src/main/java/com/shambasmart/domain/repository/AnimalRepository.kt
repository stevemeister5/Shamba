package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.Animal
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {
    fun getAllActiveAnimals(): Flow<List<Animal>>
    suspend fun getAnimalById(id: Long): Animal?
    suspend fun getAnimalByTagId(tagId: String): Animal?
    fun getAnimalsBySpecies(species: String): Flow<List<Animal>>
    fun getActiveAnimalCount(): Flow<Int>
    fun getCountBySpecies(species: String): Flow<Int>
    suspend fun insertAnimal(animal: Animal): Long
    suspend fun updateAnimal(animal: Animal)
    suspend fun deleteAnimal(animal: Animal)
    suspend fun updateStatus(id: Long, status: String)
}