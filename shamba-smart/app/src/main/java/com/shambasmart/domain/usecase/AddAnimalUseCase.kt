package com.shambasmart.domain.usecase

import com.shambasmart.data.local.entity.Animal
import com.shambasmart.domain.repository.AnimalRepository
import javax.inject.Inject

class AddAnimalUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(animal: Animal): Result<Long> {
        return try {
            val id = animalRepository.insertAnimal(animal)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(animal: Animal): Result<Unit> {
        return try {
            animalRepository.updateAnimal(animal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(animal: Animal): Result<Unit> {
        return try {
            animalRepository.deleteAnimal(animal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatus(id: Long, status: String): Result<Unit> {
        return try {
            animalRepository.updateStatus(id, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}