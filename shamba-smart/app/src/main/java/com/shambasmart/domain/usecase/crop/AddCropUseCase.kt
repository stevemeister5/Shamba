package com.shambasmart.domain.usecase.crop

import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.domain.repository.CropRepository
import javax.inject.Inject

class AddCropUseCase @Inject constructor(
    private val cropRepository: CropRepository
) {
    suspend operator fun invoke(crop: CropPlanting): Result<Long> {
        return try {
            val id = cropRepository.insertCrop(crop)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}