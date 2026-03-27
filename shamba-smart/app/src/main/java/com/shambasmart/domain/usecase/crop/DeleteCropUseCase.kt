package com.shambasmart.domain.usecase.crop

import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.domain.repository.CropRepository
import javax.inject.Inject

class DeleteCropUseCase @Inject constructor(
    private val cropRepository: CropRepository
) {
    suspend operator fun invoke(crop: CropPlanting): Result<Unit> {
        return try {
            cropRepository.deleteCrop(crop)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}