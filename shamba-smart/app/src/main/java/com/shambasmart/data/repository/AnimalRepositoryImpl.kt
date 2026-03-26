package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.AnimalDao
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.domain.repository.AnimalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimalRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao
) : AnimalRepository {

    override fun getAllActiveAnimals(): Flow<List<Animal>> = animalDao.getAllActiveAnimals()

    override suspend fun getAnimalById(id: Long): Animal? = animalDao.getAnimalById(id)

    override suspend fun getAnimalByTagId(tagId: String): Animal? = animalDao.getAnimalByTagId(tagId)

    override fun getAnimalsBySpecies(species: String): Flow<List<Animal>> = animalDao.getAnimalsBySpecies(species)

    override fun getActiveAnimalCount(): Flow<Int> = animalDao.getActiveAnimalCount()

    override fun getCountBySpecies(species: String): Flow<Int> = animalDao.getCountBySpecies(species)

    override suspend fun insertAnimal(animal: Animal): Long {
        val now = System.currentTimeMillis()
        return animalDao.insertAnimal(animal.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateAnimal(animal: Animal) {
        animalDao.updateAnimal(animal.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteAnimal(animal: Animal) = animalDao.deleteAnimal(animal)

    override suspend fun updateStatus(id: Long, status: String) {
        animalDao.updateStatus(id, status, System.currentTimeMillis())
    }
}