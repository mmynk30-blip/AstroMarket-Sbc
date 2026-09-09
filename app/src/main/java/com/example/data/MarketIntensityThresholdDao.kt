package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User-Defined Market Intensity Alert Thresholds.
 * Provides reactive flows and suspend operations for Room local persistence.
 */
@Dao
interface MarketIntensityThresholdDao {

    @Query("SELECT * FROM market_intensity_thresholds ORDER BY id ASC")
    fun getAllThresholdsFlow(): Flow<List<MarketIntensityThresholdEntity>>

    @Query("SELECT * FROM market_intensity_thresholds WHERE isEnabled = 1 ORDER BY thresholdScore DESC")
    suspend fun getEnabledThresholds(): List<MarketIntensityThresholdEntity>

    @Query("SELECT * FROM market_intensity_thresholds WHERE isEnabled = 1 AND (assetSymbol = 'ALL' OR assetSymbol = :assetSymbol) ORDER BY thresholdScore DESC")
    suspend fun getEnabledThresholdsForAsset(assetSymbol: String): List<MarketIntensityThresholdEntity>

    @Query("SELECT * FROM market_intensity_thresholds WHERE id = :id LIMIT 1")
    suspend fun getThresholdById(id: Int): MarketIntensityThresholdEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreshold(threshold: MarketIntensityThresholdEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(thresholds: List<MarketIntensityThresholdEntity>)

    @Update
    suspend fun updateThreshold(threshold: MarketIntensityThresholdEntity)

    @Query("UPDATE market_intensity_thresholds SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabledState(id: Int, isEnabled: Boolean)

    @Delete
    suspend fun deleteThreshold(threshold: MarketIntensityThresholdEntity)

    @Query("DELETE FROM market_intensity_thresholds WHERE id = :id")
    suspend fun deleteThresholdById(id: Int)

    @Query("DELETE FROM market_intensity_thresholds")
    suspend fun clearAllThresholds()

    @Query("SELECT COUNT(*) FROM market_intensity_thresholds")
    suspend fun getThresholdsCount(): Int
}
