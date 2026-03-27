package com.shambasmart.domain.usecase.crop  
  
import com.shambasmart.data.local.entity.CropPlanting  
import com.shambasmart.domain.repository.CropRepository  
import kotlinx.coroutines.flow.Flow  
import javax.inject.Inject  
  
class GetCropsUseCase @Inject constructor(  
    private val cropRepository: CropRepository  
) {  
    operator fun invoke(): Flow<List<CropPlanting>> = cropRepository.getAllCrops()  
  
    fun getActiveCrops(): Flow<List<CropPlanting>> = cropRepository.getActiveCrops()  
  
    fun getCropsByStatus(status: String): Flow<List<CropPlanting>> = cropRepository.getCropsByStatus(status)  
  
    fun getCropsByPlot(plotId: Long): Flow<List<CropPlanting>> = cropRepository.getCropsByPlot(plotId)  
  
    suspend fun getById(id: Long): CropPlanting? = cropRepository.getCropById(id)  
  
    fun getCropCount(): Flow<Int> = cropRepository.getCropCount()  
}  
