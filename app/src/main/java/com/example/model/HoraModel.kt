package com.example.model

/**
 * Hourly Hora Planetary Period
 */
data class HoraPeriod(
    val horaIndex: Int, // 1 to 24 (or 1 to 7 for market session)
    val startTime: String,
    val endTime: String,
    val planet: Planet,
    val marketBias: HoraMarketBias,
    val powerScore: Int, // -100 to +100
    val favorableSectors: List<String>,
    val cautiousSectors: List<String>,
    val tradingTip: String,
    val isMarketHours: Boolean,
    val isCurrentHora: Boolean = false
)

enum class HoraMarketBias(val title: String, val badgeColorHex: Long) {
    STRONG_BULLISH("Bullish Accumulation", 0xFF10B981),
    MILD_BULLISH("Mild Upward Bias", 0xFF34D399),
    NEUTRAL_RANGE("Consolidation / Range", 0xFF94A3B8),
    VOLATILE_CHOPPY("High Beta Volatility", 0xFFF97316),
    BEARISH_PRESSURE("Selling Pressure / Drag", 0xFFEF4444),
    REVERSAL_ZONE("Astro Turning Point", 0xFFA855F7)
}
