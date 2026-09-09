package com.example.engine

import com.example.model.*
import java.util.Calendar

object TrendIntensityEngine {

    /**
     * Calculates 24-Hour predicted trend intensity based on Panchang, Horas, and SBC calculations.
     * Supports both Vedic Day mode (06:00 AM sunrise to next day 06:00 AM) and Rolling 24 Hours.
     */
    fun calculate24HourTrend(
        asset: MarketAsset,
        calendar: Calendar,
        panchang: PanchangDetails,
        horas: List<HoraPeriod>,
        sbcState: SarvatobhadraState,
        currentHour: Int,
        isRolling24Hours: Boolean = false
    ): Trend24HourReport {
        val startHour = if (isRolling24Hours) currentHour else 6 // Vedic Day starts at 06:00 AM
        val points = mutableListOf<TrendDataPoint>()

        val sbcVedhaScore = sbcState.netVedhaScore.toFloat()
        val panchangMacroScore = panchang.marketSentimentScore.toFloat()

        // Parse timing window hours
        val rahuStartHour = parseHour(panchang.rahuKaal.startTime)
        val rahuEndHour = parseHour(panchang.rahuKaal.endTime)
        val yamaStartHour = parseHour(panchang.yamagandaKaal.startTime)
        val yamaEndHour = parseHour(panchang.yamagandaKaal.endTime)
        val abhijitHour = 12 // 11:48 - 12:38

        val marketOpenHour = if (asset.region.contains("NSE")) 9 else if (asset.region.contains("US")) 9 else 9
        val marketCloseHour = if (asset.region.contains("NSE")) 15 else if (asset.region.contains("US")) 16 else 23

        for (i in 0 until 24) {
            val clockHour = (startHour + i) % 24
            val nextClockHour = (clockHour + 1) % 24
            val timeLabel = String.format("%02d:00", clockHour)
            val timeFormatted = formatClockHour(clockHour)
            val timeRange = String.format("%02d:00 - %02d:00", clockHour, nextClockHour)

            // Locate matching hora for this clock hour
            val hora = horas.find { h ->
                val hStart = parseHour(h.startTime)
                hStart == clockHour
            } ?: horas[i % horas.size]

            val horaBasePower = hora.powerScore.toFloat()
            val isCurrent = (clockHour == currentHour)
            val isMarketHours = when {
                asset.region.contains("NSE") -> clockHour in 9..15
                asset.region.contains("US") -> clockHour in 9..16
                else -> clockHour in 9..23 // Commodities
            }

            // Planetary Lord Affinity with target asset
            val isAssetRulingPlanet = hora.planet in asset.primaryPlanetaryLords
            val assetAffinityBoost = if (isAssetRulingPlanet) 22f else 0f

            // Tithi Modulation
            val tithiMod = when (panchang.tithiCategory) {
                TithiCategory.NANDA -> 12f
                TithiCategory.JAYA -> 16f
                TithiCategory.BHADRA -> 2f
                TithiCategory.PURNA -> if (isMarketHours && clockHour >= 14) -14f else 8f
                TithiCategory.RIKTA -> {
                    // Rikta introduces intraday harmonic swings
                    if (i % 2 == 0) -22f else 15f
                }
            }

            // Timing Window Impact & Special Markers
            var windowModifier = 0f
            var marker: String? = null
            var markerType = TrendMarkerType.NONE

            val inRahu = clockHour >= rahuStartHour && clockHour < rahuEndHour
            val inYamaganda = clockHour >= yamaStartHour && clockHour < yamaEndHour
            val inAbhijit = clockHour == abhijitHour || (clockHour == 11 && panchang.abhijitMuhurta.startTime >= "11:30")

            when {
                inAbhijit -> {
                    marker = "★ Abhijit Muhurta (Peak)"
                    markerType = TrendMarkerType.ABHIJIT_MUHURTA
                    windowModifier += 38f
                }
                inRahu -> {
                    marker = if (clockHour == marketOpenHour) "⚠ Rahu Kaal Open (Volatile)"
                    else if (clockHour == marketCloseHour) "⚠ Rahu Kaal Settlement"
                    else "⚠ Rahu Kaal (Caution)"
                    markerType = TrendMarkerType.RAHU_KAAL
                    windowModifier -= 36f
                }
                clockHour == marketOpenHour && isMarketHours -> {
                    marker = "▲ Opening Impulse"
                    markerType = TrendMarkerType.MARKET_OPEN
                    windowModifier += if (panchangMacroScore >= 0) 15f else -10f
                }
                clockHour == marketCloseHour && isMarketHours -> {
                    marker = "▼ Climax Settlement"
                    markerType = TrendMarkerType.MARKET_CLOSE
                    windowModifier += if (panchang.tithiCategory == TithiCategory.PURNA) -18f else -5f
                }
                inYamaganda -> {
                    marker = "⚠ Yamaganda Drag"
                    markerType = TrendMarkerType.YAMAGANDA
                    windowModifier -= 22f
                }
                hora.marketBias == HoraMarketBias.REVERSAL_ZONE -> {
                    marker = "◆ Harmonic Turning Point"
                    markerType = TrendMarkerType.HARMONIC_REVERSAL
                }
            }

            // Smooth Harmonic Sine Wave Component (reflecting planetary ingress cycles)
            val harmonicAngle = (i.toDouble() / 24.0) * 2.0 * Math.PI
            val diurnalHarmonic = (kotlin.math.sin(harmonicAngle) * 8.0).toFloat()

            // Composite Intensity Formula
            // Hora (45%) + Panchang (25%) + SBC Vedha (15%) + Timing Windows & Tithi (15%) + Asset Affinity
            val rawIntensity = (horaBasePower * 0.45f) +
                    (panchangMacroScore * 0.25f) +
                    (sbcVedhaScore * 0.15f) +
                    tithiMod +
                    windowModifier +
                    assetAffinityBoost +
                    diurnalHarmonic

            val intensityScore = rawIntensity.coerceIn(-100f, 100f)

            // Dominant astrological driver for this hour
            val dominant = buildString {
                append("${hora.planet.englishName} Hora (${hora.marketBias.title})")
                if (inAbhijit) append(" • Abhijit Harmonic Peak")
                if (inRahu) append(" • Rahu Kaal Shadow")
                if (isAssetRulingPlanet) append(" • ${asset.displayName} Key Lord Resonance")
            }

            val guidance = when {
                intensityScore >= 50f -> "Strong Bullish Impulse: High institutional demand. Look to buy dips on intraday EMAs."
                intensityScore in 15f..49f -> "Mild Bullish Bias: Moderate upward accumulation. Favorable for momentum longs."
                intensityScore in -14f..14f -> "Neutral / Rangebound: Consolidation mode. Premium decay / straddle favored."
                intensityScore in -49f..-15f -> "Mild Bearish Pressure: Supply overhang. Hedge long positions or trail stop-losses."
                else -> "High Caution / Sell Pressure: Severe astrologic affliction. Avoid aggressive long exposure."
            }

            val horaElement = when (hora.planet) {
                Planet.SUN -> "Fire (Agni / Sovereign Authority)"
                Planet.MOON -> "Water (Jala / Fluid Sentiment)"
                Planet.MARS -> "Fire (Tejas / Aggressive Action)"
                Planet.MERCURY -> "Earth (Prithvi / Commercial Speed)"
                Planet.JUPITER -> "Ether (Akasha / Expansion & Wisdom)"
                Planet.VENUS -> "Water (Jala / Harmony & Luxury)"
                Planet.SATURN -> "Air (Vayu / Structural Drag)"
                Planet.RAHU -> "Air / Shadow (Speculative Illusions)"
                Planet.KETU -> "Fire / Void (Hidden Reversals)"
            }

            val horaDhatu = when (hora.planet) {
                Planet.SUN -> "Gold & Copper"
                Planet.MOON -> "Silver & Liquid Assets"
                Planet.MARS -> "Steel, Copper & Iron"
                Planet.MERCURY -> "Bronze, Silicon & Currencies"
                Planet.JUPITER -> "Pure Gold & Sovereign Reserves"
                Planet.VENUS -> "Silver, Platinum & Diamonds"
                Planet.SATURN -> "Iron, Crude Oil & Base Metals"
                Planet.RAHU -> "Lead & Synthetic High-Beta Derivatives"
                Planet.KETU -> "Rare Earths & Volatile Hedges"
            }

            val activeWindowName = when {
                inAbhijit -> panchang.abhijitMuhurta.name
                inRahu -> panchang.rahuKaal.name
                inYamaganda -> panchang.yamagandaKaal.name
                else -> "Standard Vedic Trading Muhurta"
            }

            val activeWindowImpact = when {
                inAbhijit -> panchang.abhijitMuhurta.tradingImpact
                inRahu -> panchang.rahuKaal.tradingImpact
                inYamaganda -> panchang.yamagandaKaal.tradingImpact
                else -> "Balanced baseline flow without planetary shadow interference"
            }

            val panchangContributionValue = (panchangMacroScore * 0.25f) + tithiMod + windowModifier
            val panchangSummary = "${panchang.tithiName} (${panchang.tithiCategory.title}) • ${panchang.nakshatraName} • ${panchang.yogaName} Yoga"
            val horaSummary = "${hora.planet.englishName} (${hora.planet.sanskritName}) Hora • ${hora.marketBias.title} • Power ${if (hora.powerScore >= 0) "+" else ""}${hora.powerScore}"

            points.add(
                TrendDataPoint(
                    hourIndex = i,
                    hourOfDay = clockHour,
                    timeLabel = timeLabel,
                    timeFormatted = timeFormatted,
                    timeRange = timeRange,
                    intensityScore = intensityScore,
                    horaPower = horaBasePower,
                    panchangFactor = panchangMacroScore,
                    sbcResonance = sbcVedhaScore,
                    horaPlanet = hora.planet,
                    horaBias = hora.marketBias,
                    isMarketHours = isMarketHours,
                    isCurrentHour = isCurrent,
                    specialMarker = marker,
                    specialMarkerType = markerType,
                    actionableGuidance = guidance,
                    dominantInfluence = dominant,
                    panchangInfluenceSummary = panchangSummary,
                    tithiName = panchang.tithiName,
                    tithiCategory = panchang.tithiCategory,
                    tithiEffect = panchang.tithiCategory.financialSignificance,
                    nakshatraName = panchang.nakshatraName,
                    nakshatraLord = panchang.nakshatraLord,
                    nakshatraNature = panchang.nakshatraNature,
                    nakshatraEffect = panchang.nakshatraNature.marketBehavior,
                    yogaName = panchang.yogaName,
                    yogaEffect = panchang.yogaMarketEffect,
                    karanaName = panchang.karanaName,
                    isVishtiKarana = panchang.isVishtiKarana,
                    timingWindowName = activeWindowName,
                    timingWindowImpact = activeWindowImpact,
                    panchangContribution = panchangContributionValue,
                    horaInfluenceSummary = horaSummary,
                    horaTradingTip = hora.tradingTip,
                    horaFavorableSectors = hora.favorableSectors,
                    horaCautiousSectors = hora.cautiousSectors,
                    horaElement = horaElement,
                    horaDhatu = horaDhatu,
                    isAssetRulerResonance = isAssetRulingPlanet
                )
            )
        }

        // Compute summary metrics
        val peakBullish = points.maxByOrNull { it.intensityScore } ?: points.first()
        val peakBearish = points.minByOrNull { it.intensityScore } ?: points.first()
        val bullishCount = points.count { it.intensityScore >= 15f }
        val bearishCount = points.count { it.intensityScore <= -15f }
        val neutralCount = points.size - bullishCount - bearishCount
        val avgIntensity = points.map { it.intensityScore }.average().toFloat()

        val primaryRegime = when {
            avgIntensity >= 30f -> "Bullish Expansion Regime"
            avgIntensity in 5f..29f -> "Mild Upward Accumulation"
            avgIntensity in -10f..4f -> "Two-Way Rangebound Consolidation"
            avgIntensity in -30f..-11f -> "Corrective Selling Regime"
            else -> "High Volatility Drag Regime"
        }

        val goldenHour = "${panchang.abhijitMuhurta.startTime} - ${panchang.abhijitMuhurta.endTime} (Abhijit Muhurta)"
        val cautionHour = "${panchang.rahuKaal.startTime} - ${panchang.rahuKaal.endTime} (Rahu Kaal)"

        return Trend24HourReport(
            points = points,
            peakBullishPoint = peakBullish,
            peakBearishPoint = peakBearish,
            bullishHoursCount = bullishCount,
            bearishHoursCount = bearishCount,
            neutralHoursCount = neutralCount,
            averageIntensity = avgIntensity,
            primaryRegime = primaryRegime,
            goldenHourWindow = goldenHour,
            maximumCautionWindow = cautionHour
        )
    }

    private fun parseHour(timeString: String): Int {
        return try {
            timeString.split(":").first().trim().toInt()
        } catch (e: Exception) {
            9
        }
    }

    private fun formatClockHour(hour: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayH = when (hour) {
            0 -> 12
            in 1..12 -> hour
            else -> hour - 12
        }
        return String.format("%02d:00 %s", displayH, amPm)
    }
}
