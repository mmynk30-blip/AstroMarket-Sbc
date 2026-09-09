package com.example.engine

import com.example.model.*
import java.util.Locale
import kotlin.math.abs

object SbcIntensityAlertEngine {

    /**
     * Scans the 24-hour trend intensity points to identify all significant shifts
     * according to the Sarvatobhadra Chakra cycle and user alert preferences.
     */
    fun analyzeCycleShifts(
        asset: MarketAsset,
        panchang: PanchangDetails,
        sbcState: SarvatobhadraState,
        trendReport: Trend24HourReport,
        currentHour: Int,
        thresholdDelta: Float = 25f,
        alertBullishThreshold: Float = 45f,
        alertBearishThreshold: Float = -45f,
        alertOnBullishThreshold: Boolean = true,
        alertOnBearishThreshold: Boolean = true,
        alertOnRegimeFlip: Boolean = true,
        alertOnAbhijit: Boolean = true,
        alertOnRahuKaal: Boolean = true,
        alertOnHarmonics: Boolean = true,
        customThresholdRules: List<com.example.data.MarketIntensityThresholdEntity> = emptyList()
    ): SbcCycleShiftSummary {
        val points = trendReport.points
        if (points.isEmpty()) {
            return SbcCycleShiftSummary(
                dateString = panchang.dateString,
                asset = asset,
                totalShiftsDetected = 0,
                shifts = emptyList(),
                activeShift = null,
                nextUpcomingShift = null,
                maxPositiveShift = null,
                maxNegativeShift = null,
                cycleVolatilityRisk = "Standard Baseline"
            )
        }

        val shifts = mutableListOf<SbcIntensityShift>()

        for (i in points.indices) {
            val curr = points[i]
            val prev = if (i > 0) points[i - 1] else points.last()

            val delta = curr.intensityScore - prev.intensityScore
            val absDelta = abs(delta)

            val isRegimeFlipToBull = (prev.intensityScore < 0f && curr.intensityScore >= 0f)
            val isRegimeFlipToBear = (prev.intensityScore >= 0f && curr.intensityScore < 0f)
            val isAbhijit = curr.specialMarkerType == TrendMarkerType.ABHIJIT_MUHURTA
            val isRahu = curr.specialMarkerType == TrendMarkerType.RAHU_KAAL
            val isHarmonic = curr.specialMarkerType == TrendMarkerType.HARMONIC_REVERSAL

            val isMagnitudeShift = absDelta >= thresholdDelta

            val isBullishBreach = alertOnBullishThreshold && curr.intensityScore >= alertBullishThreshold && (prev.intensityScore < alertBullishThreshold || absDelta >= 10f)
            val isBearishBreach = alertOnBearishThreshold && curr.intensityScore <= alertBearishThreshold && (prev.intensityScore > alertBearishThreshold || absDelta >= 10f)

            val matchedCustomRule = customThresholdRules.firstOrNull { rule ->
                rule.isEnabled && (rule.assetSymbol == "ALL" || rule.assetSymbol == asset.name) &&
                if (rule.conditionType == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE) {
                    curr.intensityScore >= rule.thresholdScore && (prev.intensityScore < rule.thresholdScore || absDelta >= 8f)
                } else {
                    curr.intensityScore <= rule.thresholdScore && (prev.intensityScore > rule.thresholdScore || absDelta >= 8f)
                }
            }

            val shouldAlert = isMagnitudeShift ||
                    isBullishBreach ||
                    isBearishBreach ||
                    (matchedCustomRule != null) ||
                    (alertOnRegimeFlip && (isRegimeFlipToBull || isRegimeFlipToBear)) ||
                    (alertOnAbhijit && isAbhijit) ||
                    (alertOnRahuKaal && isRahu) ||
                    (alertOnHarmonics && isHarmonic)

            if (!shouldAlert) continue

            // Determine Shift Type
            val shiftType = when {
                matchedCustomRule != null && matchedCustomRule.isBullish -> SbcShiftType.BULLISH_THRESHOLD_BREACH
                matchedCustomRule != null && !matchedCustomRule.isBullish -> SbcShiftType.BEARISH_THRESHOLD_BREACH
                isAbhijit -> SbcShiftType.ABHIJIT_MUHURTA_PEAK
                isRahu -> SbcShiftType.RAHU_KAAL_VOLATILITY
                isBullishBreach -> SbcShiftType.BULLISH_THRESHOLD_BREACH
                isBearishBreach -> SbcShiftType.BEARISH_THRESHOLD_BREACH
                isRegimeFlipToBull && delta >= 15f -> SbcShiftType.REGIME_FLIP_BULLISH
                isRegimeFlipToBear && delta <= -15f -> SbcShiftType.REGIME_FLIP_BEARISH
                delta >= thresholdDelta -> SbcShiftType.BULLISH_SURGE
                delta <= -thresholdDelta -> SbcShiftType.BEARISH_DIVE
                isHarmonic -> SbcShiftType.HARMONIC_INFLECTION
                delta > 0f -> SbcShiftType.BULLISH_SURGE
                else -> SbcShiftType.BEARISH_DIVE
            }

            val sign = if (delta >= 0) "+" else ""
            val deltaFormatted = String.format(Locale.US, "%s%.1f pts", sign, delta)
            val prevFormatted = String.format(Locale.US, "%.1f", prev.intensityScore)
            val currFormatted = String.format(Locale.US, "%.1f", curr.intensityScore)

            val alertTitle = when (shiftType) {
                SbcShiftType.BULLISH_THRESHOLD_BREACH -> {
                    if (matchedCustomRule != null) {
                        "▲ ${matchedCustomRule.label} ($currFormatted pts)"
                    } else {
                        "▲ Bullish Threshold Reached ($currFormatted pts ≥ ${alertBullishThreshold.toInt()})"
                    }
                }
                SbcShiftType.BEARISH_THRESHOLD_BREACH -> {
                    if (matchedCustomRule != null) {
                        "▼ ${matchedCustomRule.label} ($currFormatted pts)"
                    } else {
                        "▼ Bearish Threshold Reached ($currFormatted pts ≤ ${alertBearishThreshold.toInt()})"
                    }
                }
                SbcShiftType.BULLISH_SURGE -> "▲ Bullish Surge Alert ($deltaFormatted)"
                SbcShiftType.BEARISH_DIVE -> "▼ Bearish Pressure Shift ($deltaFormatted)"
                SbcShiftType.REGIME_FLIP_BULLISH -> "⟳ Regime Reversal: Bearish to Bullish ($deltaFormatted)"
                SbcShiftType.REGIME_FLIP_BEARISH -> "⟳ Regime Breakdown: Bullish to Bearish ($deltaFormatted)"
                SbcShiftType.ABHIJIT_MUHURTA_PEAK -> "★ Abhijit Muhurta Zenith Peak ($currFormatted)"
                SbcShiftType.RAHU_KAAL_VOLATILITY -> "⚠ Rahu Kaal Shadow Alert ($deltaFormatted)"
                SbcShiftType.HARMONIC_INFLECTION -> "◆ Harmonic Cycle Turning Point ($currFormatted)"
            }

            val alertMessage = buildString {
                append("${asset.displayName}: Predicted intensity shifts to $currFormatted ($deltaFormatted) during ${curr.timeRange}. ")
                append("Active ruler: ${curr.horaPlanet.englishName} Hora (${curr.horaBias.title}).")
            }

            val detailedAnalysis = buildString {
                append("Sarvatobhadra Chakra analysis registers a significant cycle transition. ")
                append("Previous hour intensity was $prevFormatted, now shifting to $currFormatted. ")
                append("Net SBC Vedha score: ${if (sbcState.netVedhaScore >= 0) "+${sbcState.netVedhaScore}" else "${sbcState.netVedhaScore}"}. ")
                if (curr.horaFavorableSectors.isNotEmpty()) {
                    append("Favored accumulation sectors: ${curr.horaFavorableSectors.joinToString(", ")}. ")
                }
                if (curr.horaCautiousSectors.isNotEmpty()) {
                    append("Caution/Hedge sectors: ${curr.horaCautiousSectors.joinToString(", ")}. ")
                }
                append(curr.dominantInfluence)
            }

            val guidance = curr.actionableGuidance.ifEmpty {
                when (shiftType) {
                    SbcShiftType.BULLISH_THRESHOLD_BREACH ->
                        "Market Intensity has breached your custom bullish alert threshold. Look for trend continuation or confirmed momentum entries."
                    SbcShiftType.BEARISH_THRESHOLD_BREACH ->
                        "Market Intensity has plunged below your custom bearish alert threshold. Implement capital preservation and hedge long exposures."
                    SbcShiftType.BULLISH_SURGE, SbcShiftType.REGIME_FLIP_BULLISH ->
                        "Accumulate index calls or high-beta leaders on minor dips. Trail stop-losses."
                    SbcShiftType.BEARISH_DIVE, SbcShiftType.REGIME_FLIP_BEARISH ->
                        "Protect existing profits. Avoid chasing fresh longs; consider defined-risk bear spreads."
                    SbcShiftType.ABHIJIT_MUHURTA_PEAK ->
                        "Peak solar window of the day. High liquidity and institutional buying flow."
                    SbcShiftType.RAHU_KAAL_VOLATILITY ->
                        "Avoid aggressive directional naked bets. High probability of whipsaw stops."
                    SbcShiftType.HARMONIC_INFLECTION ->
                        "Watch 15-minute price action for reversal confirmation before execution."
                }
            }

            val isCurrent = (curr.hourOfDay == currentHour)
            val isUpcoming = (curr.hourOfDay > currentHour)

            shifts.add(
                SbcIntensityShift(
                    id = "shift_${curr.hourOfDay}_${asset.name}",
                    hourIndex = curr.hourIndex,
                    clockHour = curr.hourOfDay,
                    timeRange = curr.timeRange,
                    shiftType = shiftType,
                    previousIntensity = prev.intensityScore,
                    newIntensity = curr.intensityScore,
                    intensityDelta = delta,
                    horaLord = curr.horaPlanet,
                    horaMarketBias = curr.horaBias,
                    asset = asset,
                    favoredSectors = curr.horaFavorableSectors,
                    cautiousSectors = curr.horaCautiousSectors,
                    alertTitle = alertTitle,
                    alertMessage = alertMessage,
                    detailedAnalysis = detailedAnalysis,
                    actionableGuidance = guidance,
                    isCurrentHourActive = isCurrent,
                    isUpcoming = isUpcoming
                )
            )
        }

        val active = shifts.find { it.isCurrentHourActive }
        val upcoming = shifts.find { it.isUpcoming }
        val maxPos = shifts.maxByOrNull { it.intensityDelta }
        val maxNeg = shifts.minByOrNull { it.intensityDelta }

        val volatilityRisk = when {
            shifts.any { it.shiftType == SbcShiftType.RAHU_KAAL_VOLATILITY && it.isCurrentHourActive } -> "Extreme (Active Rahu Kaal Shadow)"
            shifts.count { abs(it.intensityDelta) >= 35f } >= 3 -> "High Intraday Dispersion"
            shifts.count { abs(it.intensityDelta) >= 20f } >= 2 -> "Moderate Harmonic Volatility"
            else -> "Controlled Planetary Flow"
        }

        return SbcCycleShiftSummary(
            dateString = panchang.dateString,
            asset = asset,
            totalShiftsDetected = shifts.size,
            shifts = shifts,
            activeShift = active,
            nextUpcomingShift = upcoming,
            maxPositiveShift = maxPos,
            maxNegativeShift = maxNeg,
            cycleVolatilityRisk = volatilityRisk
        )
    }
}
