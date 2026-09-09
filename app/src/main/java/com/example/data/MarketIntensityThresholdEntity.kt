package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing user-defined alert thresholds for Market Intensity Scores.
 * Persisted locally in SQLite so alert rules and threshold preferences are remembered
 * between app launches.
 */
@Entity(tableName = "market_intensity_thresholds")
data class MarketIntensityThresholdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val assetSymbol: String = "ALL", // "ALL" or specific MarketAsset.name (e.g., "NIFTY_50")
    val conditionType: String = CONDITION_GTE, // "GREATER_THAN_OR_EQUAL" or "LESS_THAN_OR_EQUAL"
    val thresholdScore: Float, // Score between -100f and +100f
    val isEnabled: Boolean = true,
    val description: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    val isBullish: Boolean
        get() = conditionType == CONDITION_GTE || thresholdScore >= 0f

    val formattedCondition: String
        get() = if (conditionType == CONDITION_GTE) "≥" else "≤"

    val formattedScore: String
        get() = if (thresholdScore > 0f) "+${thresholdScore.toInt()}" else "${thresholdScore.toInt()}"

    companion object {
        const val CONDITION_GTE = "GREATER_THAN_OR_EQUAL"
        const val CONDITION_LTE = "LESS_THAN_OR_EQUAL"

        fun createDefaultThresholds(): List<MarketIntensityThresholdEntity> {
            return listOf(
                MarketIntensityThresholdEntity(
                    id = 1,
                    label = "Zenith Bullish Breakout",
                    assetSymbol = "ALL",
                    conditionType = CONDITION_GTE,
                    thresholdScore = 45f,
                    isEnabled = true,
                    description = "Alert when Market Intensity reaches strong bullish impulse (≥ +45 pts)"
                ),
                MarketIntensityThresholdEntity(
                    id = 2,
                    label = "Severe Planetary Affliction",
                    assetSymbol = "ALL",
                    conditionType = CONDITION_LTE,
                    thresholdScore = -45f,
                    isEnabled = true,
                    description = "Alert when Market Intensity drops into severe malefic affliction (≤ -45 pts)"
                ),
                MarketIntensityThresholdEntity(
                    id = 3,
                    label = "Nifty Momentum Accumulation",
                    assetSymbol = "NIFTY_50",
                    conditionType = CONDITION_GTE,
                    thresholdScore = 30f,
                    isEnabled = true,
                    description = "Favorable long entry accumulation window for Nifty 50 (≥ +30 pts)"
                ),
                MarketIntensityThresholdEntity(
                    id = 4,
                    label = "Bank Nifty Volatility Caution",
                    assetSymbol = "BANK_NIFTY",
                    conditionType = CONDITION_LTE,
                    thresholdScore = -30f,
                    isEnabled = true,
                    description = "Risk mitigation signal for high-beta banking sector (≤ -30 pts)"
                )
            )
        }
    }
}
