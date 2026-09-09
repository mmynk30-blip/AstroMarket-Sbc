package com.example.model

/**
 * Single hour data point for the 24-Hour Trend Intensity Chart
 */
data class TrendDataPoint(
    val hourIndex: Int,              // 0 to 23
    val hourOfDay: Int,              // 0 to 23 (clock hour)
    val timeLabel: String,            // e.g. "09:00"
    val timeFormatted: String,        // e.g. "09:00 AM"
    val timeRange: String,            // e.g. "09:00 - 10:00"
    val intensityScore: Float,        // Combined intensity from -100f to +100f
    val horaPower: Float,             // Hora planetary power component (-100f to +100f)
    val panchangFactor: Float,        // Panchang sentiment component (-100f to +100f)
    val sbcResonance: Float,          // SBC Vedha resonance (-100f to +100f)
    val horaPlanet: Planet,           // Active Hora Lord
    val horaBias: HoraMarketBias,     // Bullish, Bearish, Reversal, etc.
    val isMarketHours: Boolean,       // Regular trading session
    val isCurrentHour: Boolean,       // Currently active real-time hour
    val specialMarker: String? = null,// E.g. "★ Abhijit Peak", "⚠ Rahu Kaal", "▲ NSE Open"
    val specialMarkerType: TrendMarkerType = TrendMarkerType.NONE,
    val actionableGuidance: String,
    val dominantInfluence: String,

    // Specific Panchang Influence Details
    val panchangInfluenceSummary: String = "",
    val tithiName: String = "",
    val tithiCategory: TithiCategory = TithiCategory.NANDA,
    val tithiEffect: String = "",
    val nakshatraName: String = "",
    val nakshatraLord: Planet = Planet.JUPITER,
    val nakshatraNature: NakshatraNature = NakshatraNature.STHIRA,
    val nakshatraEffect: String = "",
    val yogaName: String = "",
    val yogaEffect: String = "",
    val karanaName: String = "",
    val isVishtiKarana: Boolean = false,
    val timingWindowName: String = "",
    val timingWindowImpact: String = "",
    val panchangContribution: Float = 0f,

    // Specific Hora Influence Details
    val horaInfluenceSummary: String = "",
    val horaTradingTip: String = "",
    val horaFavorableSectors: List<String> = emptyList(),
    val horaCautiousSectors: List<String> = emptyList(),
    val horaElement: String = "Ether (Akasha)",
    val horaDhatu: String = "Gold / Copper",
    val isAssetRulerResonance: Boolean = false
)

enum class TrendMarkerType {
    NONE,
    MARKET_OPEN,
    MARKET_CLOSE,
    ABHIJIT_MUHURTA,
    RAHU_KAAL,
    YAMAGANDA,
    HARMONIC_REVERSAL
}

/**
 * Aggregated 24-Hour Report for Data Visualization
 */
data class Trend24HourReport(
    val points: List<TrendDataPoint>,
    val peakBullishPoint: TrendDataPoint,
    val peakBearishPoint: TrendDataPoint,
    val bullishHoursCount: Int,
    val bearishHoursCount: Int,
    val neutralHoursCount: Int,
    val averageIntensity: Float,
    val primaryRegime: String,        // e.g. "High-Beta Bullish Expansion"
    val goldenHourWindow: String,     // e.g. "11:48 - 12:38 (Abhijit)"
    val maximumCautionWindow: String  // e.g. "13:30 - 15:00 (Rahu Kaal)"
)
