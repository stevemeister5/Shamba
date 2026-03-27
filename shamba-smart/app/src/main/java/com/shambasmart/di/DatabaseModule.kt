package com.shambasmart.di

import android.content.Context
import androidx.room.Room
import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.security.EncryptionHelper
import com.shambasmart.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager,
        encryptionHelper: EncryptionHelper
    ): ShambaDatabase {
        // Get or create secure passphrase using Android Keystore
        val passphrase = if (keystoreManager.keyExists()) {
            // Retrieve existing passphrase from encrypted storage
            val encryptedPassphrase = getEncryptedPassphrase(context)
            if (encryptedPassphrase != null) {
                encryptionHelper.decryptToBytes(encryptedPassphrase)
            } else {
                // Fallback: generate new passphrase and store it
                generateAndStorePassphrase(context, encryptionHelper)
            }
        } else {
            // First run or key was reset: generate new passphrase
            generateAndStorePassphrase(context, encryptionHelper)
        }

        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            ShambaDatabase::class.java,
            "shamba_smart.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Retrieves the encrypted passphrase from secure storage.
     */
    private fun getEncryptedPassphrase(context: Context): String? {
        val prefs = context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
        return prefs.getString("encrypted_db_passphrase", null)
    }

    /**
     * Generates a new passphrase, encrypts it, and stores it securely.
     */
    private fun generateAndStorePassphrase(
        context: Context,
        encryptionHelper: EncryptionHelper
    ): ByteArray {
        val newPassphrase = encryptionHelper.generateSecurePassphrase()
        val encryptedPassphrase = encryptionHelper.encryptBytes(newPassphrase)

        // Store encrypted passphrase in SharedPreferences
        val prefs = context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
        prefs.edit().putString("encrypted_db_passphrase", encryptedPassphrase).apply()

        return newPassphrase
    }

    @Provides
    fun provideAnimalDao(database: ShambaDatabase): AnimalDao = database.animalDao()

    @Provides
    fun provideHealthRecordDao(database: ShambaDatabase): HealthRecordDao = database.healthRecordDao()

    @Provides
    fun provideReproductionDao(database: ShambaDatabase): ReproductionDao = database.reproductionDao()

    @Provides
    fun provideMilkProductionDao(database: ShambaDatabase): MilkProductionDao = database.milkProductionDao()

    @Provides
    fun providePlotDao(database: ShambaDatabase): PlotDao = database.plotDao()

    @Provides
    fun provideCropDao(database: ShambaDatabase): CropDao = database.cropDao()

    @Provides
    fun provideHarvestDao(database: ShambaDatabase): HarvestDao = database.harvestDao()

    @Provides
    fun provideSilageDao(database: ShambaDatabase): SilageDao = database.silageDao()

    @Provides
    fun provideWeatherDao(database: ShambaDatabase): WeatherDao = database.weatherDao()

    @Provides
    fun provideCheeseDao(database: ShambaDatabase): CheeseDao = database.cheeseDao()

    @Provides
    fun provideFeedDao(database: ShambaDatabase): FeedDao = database.feedDao()

    @Provides
    fun provideStoreDao(database: ShambaDatabase): StoreDao = database.storeDao()

    @Provides
    fun provideFinancialDao(database: ShambaDatabase): FinancialDao = database.financialDao()

    @Provides
    fun provideWorkerDao(database: ShambaDatabase): WorkerDao = database.workerDao()

    @Provides
    fun provideTaskDao(database: ShambaDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideCalendarDao(database: ShambaDatabase): CalendarDao = database.calendarDao()

    @Provides
    fun provideSyncDao(database: ShambaDatabase): SyncDao = database.syncDao()

    // Maarifa Knowledge Engine DAOs
    @Provides
    fun provideKnowledgeChunkDao(database: ShambaDatabase): KnowledgeChunkDao = database.knowledgeChunkDao()

    @Provides
    fun provideOperationalRuleDao(database: ShambaDatabase): OperationalRuleDao = database.operationalRuleDao()
}