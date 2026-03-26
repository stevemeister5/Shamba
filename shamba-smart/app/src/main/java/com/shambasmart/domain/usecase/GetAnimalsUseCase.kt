package com.shambasmart.domain.usecase

import com.shambasmart.data.local.entity.Animal
import com.shambasmart.domain.repository.AnimalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnimalsUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    operator fun invoke(): Flow<List<Animal>> = animalRepository.getAllActiveAnimals()

    fun getBySpecies(species: String): Flow<List<Animal>> = animalRepository.getAnimalsBySpecies(species)

    suspend fun getById(id: Long): Animal? = animalRepository.getAnimalById(id)

    suspend fun getByTagId(tagId: String): Animal? = animalRepository.getAnimalByTagId(tagId)

    fun getCount(): Flow<Int> = animalRepository.getActiveAnimalCount()

    fun getCountBySpecies(species: String): Flow<Int> = animalRepository.getCountBySpecies(species)
}