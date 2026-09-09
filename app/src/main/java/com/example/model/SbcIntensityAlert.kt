package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Categorization of significant Sarvatobhadra Chakra intensity shifts
 */
enum class SbcShiftType(
    val title: String,
    val shortLabel: String,
    val description: String,
    val indicatorColor: Color,
    val iconSymbol: String
) {
    BULLISH_SURGE(
        title = "Bullish Surge",
        shortLabel = "▲ Bull Surge",
        description = "Rapid expansion in positive market intensity driven by benefic SBC rays & auspicious Hora.",
        indicatorColor = BullishEmerald,
        iconSymbol = "▲"
    ),
    BEARISH_DIVE(
        title = "Bearish Pressure Drop",
        shortLabel = "▼ Bear Dive",
        description = "Sharp contraction in predicted intensity due to malefic planetary drag or heavy aspect.",
        indicatorColor = BearishRuby,
        iconSymbol = "▼"
    ),
    REGIME_FLIP_BULLISH(
        title = "Regime Reversal (Bullish)",
        shortLabel = "⟳ Bear → Bull",
        description = "Polarity shifted from negative/bearish drag into positive accumulation territory.",
        indicatorColor = VedicGold,
        iconSymbol = "⟳"
    ),
    REGIME_FLIP_BEARISH(
        title = "Regime Reversal (Bearish)",
        shortLabel = "⟳ Bull → Bear",
        description = "Polarity flipped from positive bullish momentum into negative distribution zone.",
        indicatorColor = BearishRubyDark,
        iconSymbol = "⟳"
    ),
    ABHIJIT_MUHURTA_PEAK(
        title = "Abhijit Muhurta Ingress",
        shortLabel = "★ Abhijit Peak",
        description = "Mid-day solar zenith window providing apex institutional liquidity and auspicious momentum.",
        indicatorColor = VedicGold,
        iconSymbol = "★"
    ),
    RAHU_KAAL_VOLATILITY(
        title = "Rahu Kaal Shadow Shift",
        shortLabel = "⚠ Rahu Volatility",
        description = "Severe volatility window with high probability of false breakouts and aggressive stop hunting.",
        indicatorColor = Color(0xFFF43F5E),
        iconSymbol = "⚠"
    ),
    HARMONIC_INFLECTION(
        title = "Harmonic Turning Point",
        shortLabel = "◆ Harmonic Turn",
        description = "Astrological cycle inflection where planetary hora ruler changes intraday trend flow.",
        indicatorColor = CelestialCyan,
        iconSymbol = "◆"
    ),
    BULLISH_THRESHOLD_BREACH(
        title = "Bullish Intensity Threshold Reached",
        shortLabel = "▲ Bull Threshold",
        description = "Calculated market intensity score breached the user-defined bullish alert threshold.",
        indicatorColor = BullishEmeraldLight,
        iconSymbol = "▲"
    ),
    BEARISH_THRESHOLD_BREACH(
        title = "Bearish Intensity Threshold Reached",
        shortLabel = "▼ Bear Threshold",
        description = "Calculated market intensity score breached the user-defined bearish alert threshold.",
        indicatorColor = BearishRuby,
        iconSymbol = "▼"
    )
}

/**
 * Detailed representation of a detected intensity shift across the SBC cycle
 */
data class SbcIntensityShift(
    val id: String,
    val hourIndex: Int,
    val clockHour: Int,
    val timeRange: String,
    val shiftType: SbcShiftType,
    val previousIntensity: Float,
    val newIntensity: Float,
    val intensityDelta: Float,
    val horaLord: Planet,
    val horaMarketBias: HoraMarketBias,
    val asset: MarketAsset,
    val favoredSectors: List<String>,
    val cautiousSectors: List<String>,
    val alertTitle: String,
    val alertMessage: String,
    val detailedAnalysis: String,
    val actionableGuidance: String,
    val isCurrentHourActive: Boolean = false,
    val isUpcoming: Boolean = false
)

/**
 * Summary of all detected shifts for a 24-hour cycle
 */
data class SbcCycleShiftSummary(
    val dateString: String,
    val asset: MarketAsset,
    val totalShiftsDetected: Int,
    val shifts: List<SbcIntensityShift>,
    val activeShift: SbcIntensityShift?,
    val nextUpcomingShift: SbcIntensityShift?,
    val maxPositiveShift: SbcIntensityShift?,
    val maxNegativeShift: SbcIntensityShift?,
    val cycleVolatilityRisk: String
)
