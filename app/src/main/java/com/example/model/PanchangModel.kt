package com.example.model

/**
 * Tithi categories in Financial Astrology
 */
enum class TithiCategory(val title: String, val financialSignificance: String, val marketBias: String) {
    NANDA("Nanda (Joy & Growth)", "Promotes accumulation, new long positions, expansionary buying", "Bullish Bias"),
    BHADRA("Bhadra (Balance & Auspicious)", "Consolidation, stable institutional ranges, balanced liquidity", "Range-bound / Steady"),
    JAYA("Jaya (Conquest & Victory)", "Breakout momentum, trend continuation, aggressive swings", "Strong Momentum"),
    RIKTA("Rikta (Empty / Volatile)", "Whipsaws, stop-loss hunting, false breakouts, bear traps", "High Volatility / Caution"),
    PURNA("Purna (Fullness & Culmination)", "High liquidity, profit-booking surges, volume climaxes", "Profit Booking / Climax")
}

/**
 * Nakshatra Classification for Market Dynamics
 */
enum class NakshatraNature(val title: String, val marketBehavior: String) {
    KSHIPRA("Swift / Short (Kshipra)", "High intra-day volatility, rapid scalping swings, fast momentum"),
    UGRA("Fierce / Aggressive (Ugra)", "Sharp pullbacks, sudden gap downs, stop-loss triggers, high VIX"),
    STHIRA("Fixed / Enduring (Sthira)", "Low beta, strong baseline accumulation, swing trends endure"),
    CHARA("Movable / Dynamic (Chara)", "Directional trending market, sector rotations, breakout moves"),
    MRIDU("Soft / Tender (Mridu)", "Gentle upward bias, calm retail participation, low panic"),
    MISHRA("Mixed (Mishra)", "Two-sided choppy action, intraday mean-reversion favored")
}

/**
 * Panchang Timing Window (e.g. Rahu Kaal, Abhijit Muhurta)
 */
data class TimingWindow(
    val name: String,
    val startTime: String,
    val endTime: String,
    val isAuspicious: Boolean,
    val tradingImpact: String,
    val cautionLevel: String // "Safe", "Caution", "High Risk", "Golden Hour"
)

/**
 * Full Panchang Details for Market Session
 */
data class PanchangDetails(
    val dateString: String,
    val dayOfWeek: String,
    val dayLord: Planet,
    val tithiName: String,
    val tithiNumber: Int,
    val tithiCategory: TithiCategory,
    val paksha: String, // "Shukla Paksha" or "Krishna Paksha"
    val nakshatraName: String,
    val nakshatraLord: Planet,
    val nakshatraNature: NakshatraNature,
    val yogaName: String,
    val isYogaAuspicious: Boolean,
    val yogaMarketEffect: String,
    val karanaName: String,
    val isVishtiKarana: Boolean, // Vishti / Bhadra warning
    val rahuKaal: TimingWindow,
    val yamagandaKaal: TimingWindow,
    val gulikaKaal: TimingWindow,
    val abhijitMuhurta: TimingWindow,
    val moonRasi: String,
    val sunRasi: String,
    val marketSentimentScore: Int, // -100 (Deep Bearish) to +100 (Euphoric Bullish)
    val locationName: String = "Mumbai (NSE)",
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val sunriseTime: String = "06:00",
    val sunsetTime: String = "18:00",
    val ayanamsaSystem: AyanamsaSystem = AyanamsaSystem.LAHIRI,
    val ayanamsaFormatted: String = "24° 13' 50\""
)
