package com.example.engine

import com.example.model.*
import java.util.Calendar

object TimeCycleEngine {

    fun generateDailyPrediction(
        asset: MarketAsset,
        calendar: Calendar,
        sbcState: SarvatobhadraState,
        panchang: PanchangDetails,
        horas: List<HoraPeriod>
    ): DailyMarketPrediction {
        // Find Horas during market session (09:00 to 16:00)
        val marketHoras = horas.filter { it.isMarketHours }

        // Determine Overall Bias based on SBC net score + Panchang sentiment + Day lord
        val sbcScore = sbcState.netVedhaScore
        val panchangScore = panchang.marketSentimentScore
        val combinedScore = (sbcScore * 0.5 + panchangScore * 0.5).toInt()

        val overallBias = when {
            combinedScore >= 40 -> CycleDirection.STRONG_UPTREND
            combinedScore in 10..39 -> CycleDirection.MODERATE_BULLISH
            combinedScore in -10..9 -> CycleDirection.SIDEWAYS_CONSOLIDATION
            combinedScore in -39..-11 -> CycleDirection.BEARISH_CORRECTION
            panchang.tithiCategory == TithiCategory.RIKTA || !panchang.isYogaAuspicious -> CycleDirection.VOLATILE_WHIPSAW
            else -> CycleDirection.BEARISH_CORRECTION
        }

        val confidence = (55 + (kotlin.math.abs(combinedScore) * 0.4).toInt()).coerceIn(60, 95)

        // Intraday Time Cycles for the trading day
        val timeCycles = listOf(
            MarketTimeCycle(
                cycleId = "C1",
                timeRange = if (asset.region.contains("NSE")) "09:15 - 09:45" else "09:30 - 10:00",
                title = "Opening Impulse & Transit Resonance",
                predictedDirection = if (panchang.tithiCategory == TithiCategory.RIKTA) CycleDirection.VOLATILE_WHIPSAW else if (combinedScore > 0) CycleDirection.MODERATE_BULLISH else CycleDirection.BEARISH_CORRECTION,
                confidencePercentage = 78,
                volatilityLevel = VolatilityLevel.HIGH,
                astrologicalDriver = "Planetary transit absorption & Day Lord (${panchang.dayLord.englishName}) dominant impulse.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "09:36 AM" else "09:48 AM",
                actionableGuidance = "Wait for initial 15-minute high/low range to form before committing fresh capital."
            ),
            MarketTimeCycle(
                cycleId = "C2",
                timeRange = if (asset.region.contains("NSE")) "09:45 - 10:45" else "10:00 - 11:00",
                title = "Morning Institutional Trend Leg",
                predictedDirection = when {
                    marketHoras.getOrNull(0)?.planet?.isNaturalBenefic == true -> CycleDirection.STRONG_UPTREND
                    marketHoras.getOrNull(0)?.planet == Planet.SATURN -> CycleDirection.BEARISH_CORRECTION
                    else -> CycleDirection.MODERATE_BULLISH
                },
                confidencePercentage = 84,
                volatilityLevel = VolatilityLevel.MODERATE,
                astrologicalDriver = "Hora of ${marketHoras.getOrNull(0)?.planet?.englishName ?: "Active Lord"} commanding order flow.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "10:24 AM" else "10:35 AM",
                actionableGuidance = "Ride trend continuation; buy dips towards opening support."
            ),
            MarketTimeCycle(
                cycleId = "C3",
                timeRange = if (asset.region.contains("NSE")) "10:45 - 11:45" else "11:00 - 12:00",
                title = "Harmonic Inflection & Volatility Window",
                predictedDirection = if (panchang.rahuKaal.startTime <= "11:45" && panchang.rahuKaal.endTime >= "10:45") CycleDirection.VOLATILE_WHIPSAW else CycleDirection.SHARP_REVERSAL,
                confidencePercentage = 76,
                volatilityLevel = VolatilityLevel.HIGH,
                astrologicalDriver = "SBC diagonal Vedha angle inflection and Rahu Kaal / Yamaganda pressure.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "11:18 AM" else "11:32 AM",
                actionableGuidance = "Tighten stop-losses; watch for false breakdown/breakout trap patterns."
            ),
            MarketTimeCycle(
                cycleId = "C4",
                timeRange = if (asset.region.contains("NSE")) "11:45 - 12:45" else "12:00 - 13:00",
                title = "Abhijit Muhurta Golden Mid-Day Lift",
                predictedDirection = CycleDirection.MODERATE_BULLISH,
                confidencePercentage = 88,
                volatilityLevel = VolatilityLevel.LOW,
                astrologicalDriver = "Peak mid-day solar alignment (Abhijit Muhurta) dispelling morning negativity.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "12:12 PM" else "12:28 PM",
                actionableGuidance = "High probability recovery bounce; accumulation of heavyweights."
            ),
            MarketTimeCycle(
                cycleId = "C5",
                timeRange = if (asset.region.contains("NSE")) "12:45 - 13:45" else "13:00 - 14:00",
                title = "Global Ingress & Second Trend Cycle",
                predictedDirection = if (sbcState.bullishVedhaScore > 50) CycleDirection.STRONG_UPTREND else CycleDirection.SIDEWAYS_CONSOLIDATION,
                confidencePercentage = 81,
                volatilityLevel = VolatilityLevel.MODERATE,
                astrologicalDriver = "Hora of ${marketHoras.getOrNull(3)?.planet?.englishName ?: "Jupiter"} coupled with European opening volumes.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "13:15 PM" else "13:30 PM",
                actionableGuidance = "Look for breakout setups in Banking & IT sectors."
            ),
            MarketTimeCycle(
                cycleId = "C6",
                timeRange = if (asset.region.contains("NSE")) "13:45 - 14:45" else "14:00 - 15:00",
                title = "Power Hour Momentum Expansion",
                predictedDirection = when {
                    marketHoras.getOrNull(4)?.planet == Planet.JUPITER -> CycleDirection.STRONG_UPTREND
                    marketHoras.getOrNull(4)?.planet == Planet.MARS -> CycleDirection.VOLATILE_WHIPSAW
                    else -> CycleDirection.MODERATE_BULLISH
                },
                confidencePercentage = 85,
                volatilityLevel = VolatilityLevel.HIGH,
                astrologicalDriver = "Planetary transit degrees peaking before sunset cycle.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "14:24 PM" else "14:38 PM",
                actionableGuidance = "Trail profits on winning long contracts; prepare for final settlement."
            ),
            MarketTimeCycle(
                cycleId = "C7",
                timeRange = if (asset.region.contains("NSE")) "14:45 - 15:30" else "15:00 - 16:00",
                title = "Climax & Settlement Cycle",
                predictedDirection = if (panchang.tithiCategory == TithiCategory.PURNA) CycleDirection.SHARP_REVERSAL else CycleDirection.SIDEWAYS_CONSOLIDATION,
                confidencePercentage = 79,
                volatilityLevel = VolatilityLevel.MODERATE,
                astrologicalDriver = "Day-end planetary settlement and intraday positions squaring off.",
                potentialTurningPoint = if (asset.region.contains("NSE")) "15:10 PM" else "15:45 PM",
                actionableGuidance = "Avoid fresh overnight naked positions unless hedged with spreads."
            )
        )

        // Sector rankings
        val sectors = listOf(
            SectorPerformance("Banking & Financials", Planet.JUPITER, (combinedScore + 25).coerceIn(-100, 100), if (combinedScore > -10) "Strong Bullish" else "Neutral", "Supported by Jupiter Front Vedha"),
            SectorPerformance("IT & Software Tech", Planet.MERCURY, (combinedScore + 15).coerceIn(-100, 100), if (combinedScore > -20) "Bullish Accumulation" else "Consolidation", "Mercury harmonic transit"),
            SectorPerformance("Automobile & Mobility", Planet.VENUS, (combinedScore + 10).coerceIn(-100, 100), "Steady Upward Flow", "Venus aspect on consumer discretionary"),
            SectorPerformance("Defense & Heavy Engg", Planet.MARS, (combinedScore - 5).coerceIn(-100, 100), "Volatile Spikes", "Mars active in dynamic nakshatra"),
            SectorPerformance("Oil, Gas & Energy", Planet.SATURN, (combinedScore - 30).coerceIn(-100, 100), "Correction / Rangebound", "Saturn slow consolidation"),
            SectorPerformance("Metals & Mining", Planet.MARS, (combinedScore - 15).coerceIn(-100, 100), "Two-way Whipsaws", "Afflicted by diagonal Vedha"),
            SectorPerformance("FMCG & Consumption", Planet.MOON, (combinedScore + 5).coerceIn(-100, 100), "Defensive Cushion", "Moon waxing liquidity"),
            SectorPerformance("Gold & Bullion", Planet.SUN, (combinedScore + 20).coerceIn(-100, 100), "Strong Safe-Haven", "Sun & Ketu auspicious alignment")
        ).sortedByDescending { it.sentimentScore }

        val keyReversals = listOf(
            if (asset.region.contains("NSE")) "10:24 AM" else "10:35 AM",
            if (asset.region.contains("NSE")) "11:18 AM" else "11:32 AM",
            if (asset.region.contains("NSE")) "13:15 PM" else "13:30 PM",
            if (asset.region.contains("NSE")) "14:24 PM" else "14:38 PM"
        )

        val vixTrend = when (overallBias) {
            CycleDirection.STRONG_UPTREND -> "Cooling down / Falling VIX (Favorable for Call buyers)"
            CycleDirection.VOLATILE_WHIPSAW -> "Spiking VIX (High option premium decay & wild swings)"
            CycleDirection.BEARISH_CORRECTION -> "Expanding VIX (Fear index elevated)"
            else -> "Rangebound / Moderate volatility"
        }

        val strategy = when (overallBias) {
            CycleDirection.STRONG_UPTREND -> "Trend-Following Strategy: Buy call options on dips towards the 15-min opening EMA support. Bank Nifty and Tech counters lead the charge. Trail profits during Power Hour."
            CycleDirection.MODERATE_BULLISH -> "Disciplined Dip Accumulation: Long positions preferred in Banking, FMCG, and Auto. Avoid chasing opening gap-ups; accumulate during the Abhijit Muhurta window (11:48 - 12:38)."
            CycleDirection.VOLATILE_WHIPSAW -> "Hedging & Non-Directional: Market under Rikta Tithi / Rahu influence. Sharp stop-loss hunting likely. Avoid directional naked calls; trade Iron Condors or defined-risk spreads."
            CycleDirection.BEARISH_CORRECTION -> "Sell on Rallies / Protective Puts: Saturn Vedha dominates heavyweights. Energy and Metals drag the index. Fade sharp morning up-moves with Bear Put Spreads."
            CycleDirection.SHARP_REVERSAL -> "Counter-Trend Reversal Play: Watch for morning exhaustion followed by a violent reversal near 11:18 AM and 13:15 PM turning points."
            CycleDirection.SIDEWAYS_CONSOLIDATION -> "Range-Bound Straddle: Theta decay strategy favored between Astro Support 1 and Resistance 1."
        }

        val support = when (asset) {
            MarketAsset.NIFTY_50 -> "S1: 24,680 (Krittika Solar base) | S2: 24,540 (Saturn Floor)"
            MarketAsset.BANK_NIFTY -> "S1: 52,200 (Jupiter Harmonic) | S2: 51,850 (Pushya Support)"
            MarketAsset.SP_500 -> "S1: 5,720 (SBC Gann Node) | S2: 5,680 (Solar Ingress Base)"
            MarketAsset.NASDAQ -> "S1: 20,150 (Mercury Pivot) | S2: 19,920 (Tech Node)"
            MarketAsset.GOLD -> "S1: $2,580 / ₹74,800 | S2: $2,540 / ₹74,100"
            MarketAsset.CRUDE_OIL -> "S1: $72.50 / ₹6,080 | S2: $70.80 / ₹5,950"
        }

        val resistance = when (asset) {
            MarketAsset.NIFTY_50 -> "R1: 24,920 (Abhijit Peak) | R2: 25,050 (Front Vedha Ceiling)"
            MarketAsset.BANK_NIFTY -> "R1: 52,850 (Golden Hora Target) | R2: 53,300 (Expansion Node)"
            MarketAsset.SP_500 -> "R1: 5,820 (Jupiter Crest) | R2: 5,875 (Harmonic Top)"
            MarketAsset.NASDAQ -> "R1: 20,480 (Mercury Momentum) | R2: 20,720 (Bull Crest)"
            MarketAsset.GOLD -> "R1: $2,640 / ₹75,900 | R2: $2,680 / ₹76,600"
            MarketAsset.CRUDE_OIL -> "R1: $75.20 / ₹6,250 | R2: $77.00 / ₹6,400"
        }

        val narrative = buildString {
            append("Today's market structure is guided by ${panchang.dayLord.englishName} as Day Lord on ${panchang.tithiName} (${panchang.tithiCategory.title}). ")
            append("In the Sarvatobhadra Chakra (SBC), the net Vedha score registers at ${if (sbcScore >= 0) "+$sbcScore (Bullish)" else "$sbcScore (Bearish)"}. ")
            append(sbcState.primaryBeneficInfluence)
            append(" ")
            append(sbcState.primaryMaleficPressure)
            append(" Watch the key turning cycles at ${keyReversals.joinToString(", ")}, with Abhijit Muhurta (11:48 - 12:38) acting as the harmonic mid-day trend anchor.")
        }

        return DailyMarketPrediction(
            asset = asset,
            dateString = panchang.dateString,
            overallBias = overallBias,
            astroConfidenceScore = confidence,
            expectedVixTrend = vixTrend,
            keyReversalTimes = keyReversals,
            timeCycles = timeCycles,
            sectorRankings = sectors,
            tradingStrategyAdvice = strategy,
            astroSupportPoints = support,
            astroResistancePoints = resistance,
            summaryNarrative = narrative
        )
    }
}
