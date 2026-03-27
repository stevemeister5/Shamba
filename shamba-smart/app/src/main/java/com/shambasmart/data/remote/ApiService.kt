package com.shambasmart.data.remote

import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.data.local.entity.Expense
import com.shambasmart.data.local.entity.Income
import com.shambasmart.data.local.entity.Worker
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Animals
    @GET("api/animals")
    suspend fun getAnimals(): Response<List<Animal>>

    @POST("api/animals")
    suspend fun uploadAnimal(@Body animal: Animal): Response<Animal>

    @PUT("api/animals/{id}")
    suspend fun updateAnimal(@Path("id") id: Long, @Body animal: Animal): Response<Animal>

    @DELETE("api/animals/{id}")
    suspend fun deleteAnimal(@Path("id") id: Long): Response<Unit>

    // Crops
    @GET("api/crops")
    suspend fun getCrops(): Response<List<CropPlanting>>

    @POST("api/crops")
    suspend fun uploadCrop(@Body crop: CropPlanting): Response<CropPlanting>

    @PUT("api/crops/{id}")
    suspend fun updateCrop(@Path("id") id: Long, @Body crop: CropPlanting): Response<CropPlanting>

    @DELETE("api/crops/{id}")
    suspend fun deleteCrop(@Path("id") id: Long): Response<Unit>

    // Financial - Income
    @GET("api/income")
    suspend fun getIncome(): Response<List<Income>>

    @POST("api/income")
    suspend fun uploadIncome(@Body income: Income): Response<Income>

    // Financial - Expenses
    @GET("api/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("api/expenses")
    suspend fun uploadExpense(@Body expense: Expense): Response<Expense>

    // Workers
    @GET("api/workers")
    suspend fun getWorkers(): Response<List<Worker>>

    @POST("api/workers")
    suspend fun uploadWorker(@Body worker: Worker): Response<Worker>

    // Sync status
    @GET("api/sync/status")
    suspend fun getSyncStatus(): Response<SyncStatusResponse>

    @POST("api/sync/heartbeat")
    suspend fun sendHeartbeat(): Response<Unit>
}

data class SyncStatusResponse(
    val lastSyncTimestamp: Long,
    val pendingChanges: Int,
    val serverTime: Long
)