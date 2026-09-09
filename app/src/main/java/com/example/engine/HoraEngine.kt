package com.example.engine

import com.example.model.*
import java.util.Calendar

object HoraEngine {

    // Chaldean Order of Planetary Horas:
    // Sun -> Venus -> Mercury -> Moon -> Saturn -> Jupiter -> Mars
    private val CHALDEAN_ORDER = listOf(
        Planet.SUN,
        Planet.VENUS,
        Planet.MERCURY,
        Planet.MOON,
        Planet.SATURN,
        Planet.JUPITER,
        Planet.MARS
    )

    fun calculateDailyHoras(
        calendar: Calendar,
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        sunriseHour: Int = 6
    ): List<HoraPeriod> {
        val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)

        // Day Lord determines 1st Hora at local Sunrise
        val dayLord = when (dayOfWeekInt) {
            Calendar.SUNDAY -> Planet.SUN
            Calendar.MONDAY -> Planet.MOON
            Calendar.TUESDAY -> Planet.MARS
            Calendar.WEDNESDAY -> Planet.MERCURY
            Calendar.THURSDAY -> Planet.JUPITER
            Calendar.FRIDAY -> Planet.VENUS
            else -> Planet.SATURN
        }

        val startOrderIndex = CHALDEAN_ORDER.indexOf(dayLord)

        val horas = mutableListOf<HoraPeriod>()
        // 24 hours starting from local sunrise
        for (i in 0 until 24) {
            val horaHour = (sunriseHour + i) % 24
            val nextHour = (horaHour + 1) % 24
            val planet = CHALDEAN_ORDER[(startOrderIndex + i) % 7]

            val isMarketHours = (horaHour in 9..15)

            val bias = when (planet) {
                Planet.JUPITER -> HoraMarketBias.STRONG_BULLISH
                Planet.VENUS -> HoraMarketBias.MILD_BULLISH
                Planet.MERCURY -> HoraMarketBias.REVERSAL_ZONE
                Planet.SUN -> HoraMarketBias.MILD_BULLISH
                Planet.MOON -> HoraMarketBias.VOLATILE_CHOPPY
                Planet.MARS -> HoraMarketBias.VOLATILE_CHOPPY
                Planet.SATURN -> HoraMarketBias.BEARISH_PRESSURE
                else -> HoraMarketBias.NEUTRAL_RANGE
            }

            val score = when (planet) {
                Planet.JUPITER -> 85
                Planet.VENUS -> 65
                Planet.MERCURY -> 45
                Planet.SUN -> 55
                Planet.MOON -> 10
                Planet.MARS -> -40
                Planet.SATURN -> -75
                else -> 0
            }

            val favorableSectors = when (planet) {
                Planet.JUPITER -> listOf("Banking & NBFC", "Wealth Mgmt", "Large Cap Index", "Gold")
                Planet.VENUS -> listOf("Automobiles", "Luxury Retail", "Textiles", "Entertainment")
                Planet.MERCURY -> listOf("IT & Software", "Fintech", "Telecom", "High-frequency Algo")
                Planet.SUN -> listOf("PSU Banks", "Defence & Sovereign", "Power & Energy")
                Planet.MOON -> listOf("FMCG", "Silver", "Chemicals & Agro", "Dairy")
                Planet.MARS -> listOf("Metals & Mining", "Real Estate Infra", "Capital Goods")
                Planet.SATURN -> listOf("Crude Oil / Gas", "Coal & Heavy Steel", "Shipping")
                else -> emptyList()
            }

            val cautiousSectors = when (planet) {
                Planet.SATURN -> listOf("High Beta Tech", "Overvalued Growth", "Banking Longs")
                Planet.MARS -> listOf("Options Sellers (Spike risk)", "Bond Yields")
                Planet.MOON -> listOf("Breakout Traders (Whipsaw risk)", "Illiquid Smallcaps")
                Planet.SUN -> listOf("Private Sector Monopolies (Regulatory risk)")
                else -> listOf("Speculative Penny Stocks")
            }

            val tip = when (planet) {
                Planet.JUPITER -> "Golden Hora: High institutional inflows into BankNifty & Bluechips. Ride the trend."
                Planet.VENUS -> "Calm upward grind. Consumer & Auto stocks lead the rally with smooth candle structure."
                Planet.MERCURY -> "Quick scalping hour: IT & momentum counters see sharp swings. Watch for 15-min reversal signals."
                Planet.SUN -> "Authoritative control: Index heavyweights support the market baseline."
                Planet.MOON -> "Emotional retail trading: Fast swings & support retests. Don't chase gap moves."
                Planet.MARS -> "Aggressive spikes: Sudden breakout or short-squeeze in metals and capital goods."
                Planet.SATURN -> "Bearish pressure & heavy drag: Avoid fresh longs. Consider trailing stop-losses."
                else -> "Neutral market consolidation."
            }

            val isCurrent = (currentHour == horaHour)

            horas.add(
                HoraPeriod(
                    horaIndex = i + 1,
                    startTime = String.format("%02d:00", horaHour),
                    endTime = String.format("%02d:00", nextHour),
                    planet = planet,
                    marketBias = bias,
                    powerScore = score,
                    favorableSectors = favorableSectors,
                    cautiousSectors = cautiousSectors,
                    tradingTip = tip,
                    isMarketHours = isMarketHours,
                    isCurrentHora = isCurrent
                )
            )
        }

        return horas
    }
}
