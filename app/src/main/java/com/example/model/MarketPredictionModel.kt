package com.example.model

/**
 * Supported market indices and assets
 */
enum class MarketAsset(
    val code: String,
    val displayName: String,
    val region: String,
    val baseOpeningTime: String,
    val baseClosingTime: String,
    val primaryPlanetaryLords: List<Planet>
) {
    NIFTY_50("NIFTY", "NIFTY 50", "NSE India", "09:15", "15:30", listOf(Planet.SUN, Planet.JUPITER, Planet.MERCURY)),
    BANK_NIFTY("BANKNIFTY", "BANK NIFTY", "NSE India", "09:15", "15:30", listOf(Planet.JUPITER, Planet.MERCURY, Planet.VENUS)),
    SP_500("SPX", "S&P 500", "US Wall Street", "09:30", "16:00", listOf(Planet.SUN, Planet.SATURN, Planet.JUPITER)),
    NASDAQ("QQQ", "NASDAQ 100", "US Tech", "09:30", "16:00", listOf(Planet.MERCURY, Planet.RAHU, Planet.VENUS)),
    GOLD("GOLD", "GOLD / MCX", "Global Bullion", "09:00", "23:30", listOf(Planet.SUN, Planet.JUPITER, Planet.KETU)),
    CRUDE_OIL("CRUDE", "CRUDE OIL", "Energy / MCX", "09:00", "23:30", listOf(Planet.SATURN, Planet.MARS, Planet.RAHU))
}

/**
 * Specific Time Cycle segment during market session
 */
data class MarketTimeCycle(
    val cycleId: String,
    val timeRange: String,
    val title: String,
    val predictedDirection: CycleDirection,
    val confidencePercentage: Int,
    val volatilityLevel: VolatilityLevel,
    val astrologicalDriver: String,
    val potentialTurningPoint: String?,
    val actionableGuidance: String
)

enum class CycleDirection(val title: String, val badgeColorHex: Long) {
    STRONG_UPTREND("Strong Uptrend", 0xFF10B981),
    MODERATE_BULLISH("Bullish / Buy Dips", 0xFF34D399),
    SIDEWAYS_CONSOLIDATION("Sideways / Rangebound", 0xFF94A3B8),
    VOLATILE_WHIPSAW("Wild Whipsaws / Traps", 0xFFF97316),
    BEARISH_CORRECTION("Bearish Drag", 0xFFEF4444),
    SHARP_REVERSAL("Sharp Reversal Alert", 0xFFA855F7)
}

enum class VolatilityLevel(val label: String, val score: Int) {
    LOW("Low / Calm", 25),
    MODERATE("Moderate / Steady", 50),
    HIGH("High / Fast Swings", 75),
    EXTREME("Extreme / Spikes", 95)
}

/**
 * Sector Astro Performance Ranking
 */
data class SectorPerformance(
    val sectorName: String,
    val rulingPlanet: Planet,
    val sentimentScore: Int, // -100 to +100
    val trend: String, // "Strong Bull", "Neutral", "Bearish Pressure"
    val vedhaStatus: String
)

/**
 * Full Market Prediction and Behavior Report
 */
data class DailyMarketPrediction(
    val asset: MarketAsset,
    val dateString: String,
    val overallBias: CycleDirection,
    val astroConfidenceScore: Int, // 0 to 100
    val expectedVixTrend: String,
    val keyReversalTimes: List<String>,
    val timeCycles: List<MarketTimeCycle>,
    val sectorRankings: List<SectorPerformance>,
    val tradingStrategyAdvice: String,
    val astroSupportPoints: String,
    val astroResistancePoints: String,
    val summaryNarrative: String
)
