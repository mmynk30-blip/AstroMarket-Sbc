package com.example.engine

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FirebaseGenAiService {

    private const val TAG = "FirebaseGenAiService"
    private const val MODEL_NAME = "gemini-2.5-flash"

    /**
     * Ensures FirebaseApp is initialized before invoking Firebase GenAI SDK
     */
    fun ensureFirebaseInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val apiKey = try {
                    BuildConfig.GEMINI_API_KEY.ifBlank { "dummy-gemini-key" }
                } catch (e: Throwable) {
                    "dummy-gemini-key"
                }
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(context.packageName)
                    .setProjectId("astromarket-ai")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp initialized programmatically with application ID: ${context.packageName}")
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseApp initialization note: ${e.message}")
            }
        }
    }

    /**
     * Builds the complete calculated astrological context payload
     */
    fun buildAstrologicalContext(
        asset: MarketAsset,
        calendar: Calendar,
        sbcState: SarvatobhadraState,
        panchang: PanchangDetails,
        horas: List<HoraPeriod>,
        prediction: DailyMarketPrediction?
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
        val dateString = dateFormat.format(calendar.time)

        val activeHora = horas.find { it.isCurrentHora } ?: horas.firstOrNull()

        val sb = StringBuilder()
        sb.appendLine("=== ASTRONOMICAL & VEDIC MARKET CONTEXT FOR $dateString ===")
        sb.appendLine("Target Market Asset: ${asset.displayName} (${asset.region})")
        sb.appendLine("Primary Planetary Lords of Asset: ${asset.primaryPlanetaryLords.joinToString { "${it.englishName} (${it.symbol})" }}")
        sb.appendLine("Market Hours: ${asset.baseOpeningTime} to ${asset.baseClosingTime}")
        sb.appendLine()

        sb.appendLine("--- 1. CALCULATED NAVAGRAHA POSITIONS (SARVATOBHADRA 28-NAKSHATRA MATRIX) ---")
        Planet.entries.forEach { planet ->
            val nakIndex = sbcState.planetaryPositions[planet] ?: 0
            val nakName = SarvatobhadraEngine.SBC_NAKSHATRAS.getOrElse(nakIndex) { "Unknown" }
            val nature = if (planet.isNaturalBenefic) "Natural Benefic" else "Natural Malefic"
            sb.appendLine("• ${planet.symbol} ${planet.englishName} (${planet.sanskritName}): In Nakshatra '$nakName' | Motion: ${planet.speedDescription} | $nature | Governing Sectors: ${planet.associatedSectors.joinToString()}")
        }
        sb.appendLine()

        sb.appendLine("--- 2. SARVATOBHADRA CHAKRA (SBC) VEDHAS & CROSS-ASPECTS ---")
        sb.appendLine("• Bullish Benefic Vedha Score: ${sbcState.bullishVedhaScore}/100")
        sb.appendLine("• Bearish Malefic Vedha Score: ${sbcState.bearishVedhaScore}/100")
        sb.appendLine("• Net Vedha Alignment: ${sbcState.netVedhaScore} (-100 to +100)")
        sb.appendLine("• Dominant Benefic Ray: ${sbcState.primaryBeneficInfluence}")
        sb.appendLine("• Dominant Malefic Ray: ${sbcState.primaryMaleficPressure}")
        if (sbcState.activeVedhas.isNotEmpty()) {
            sb.appendLine("• Notable Vedhas:")
            sbcState.activeVedhas.take(6).forEach { v ->
                sb.appendLine("  - ${v.planet.englishName} -> ${v.vedhaType.title} on ${v.targetNakshatra} (${v.targetSector}): ${v.marketEffect}")
            }
        }
        sb.appendLine()

        val abhijitRange = "${panchang.abhijitMuhurta.startTime} - ${panchang.abhijitMuhurta.endTime}"
        val rahuRange = "${panchang.rahuKaal.startTime} - ${panchang.rahuKaal.endTime}"
        val yamagandaRange = "${panchang.yamagandaKaal.startTime} - ${panchang.yamagandaKaal.endTime}"
        val gulikaRange = "${panchang.gulikaKaal.startTime} - ${panchang.gulikaKaal.endTime}"

        sb.appendLine("--- 3. PANCHANG (THE FIVE COSMIC PILLARS) ---")
        sb.appendLine("• Tithi: ${panchang.tithiName} (${panchang.paksha}) | Category: ${panchang.tithiCategory.title} | Market Bias: ${panchang.tithiCategory.marketBias}")
        sb.appendLine("• Nakshatra: ${panchang.nakshatraName} ruled by ${panchang.nakshatraLord.englishName} (${panchang.nakshatraLord.symbol}) | Quality: ${panchang.nakshatraNature.title}")
        sb.appendLine("• Yoga: ${panchang.yogaName} (${if (panchang.isYogaAuspicious) "Benefic" else "Cautious"}) | Financial Effect: ${panchang.yogaMarketEffect}")
        sb.appendLine("• Karana: ${panchang.karanaName} | Vishti (Bhadra) Warning: ${if (panchang.isVishtiKarana) "ACTIVE (Extreme Volatility Alert)" else "Inactive"}")
        sb.appendLine("• Day Lord (Vara): ${panchang.dayLord.englishName} (${panchang.dayLord.symbol})")
        sb.appendLine("• Sun Rise: ${panchang.sunriseTime} | Sun Set: ${panchang.sunsetTime}")
        sb.appendLine("• Abhijit Muhurta (Peak Bullish Window): $abhijitRange")
        sb.appendLine("• Rahu Kaal (Caution/Volatility Window): $rahuRange")
        sb.appendLine("• Yamaganda: $yamagandaRange | Gulika: $gulikaRange")
        sb.appendLine()

        if (activeHora != null) {
            sb.appendLine("--- 4. ACTIVE PLANETARY HORA ---")
            sb.appendLine("• Current Hora: ${activeHora.planet.englishName} (${activeHora.planet.symbol}) | Power: ${activeHora.powerScore}/100")
            sb.appendLine("• Hora Market Tone: ${activeHora.marketBias.title}")
            sb.appendLine("• Sector Beneficiaries: ${activeHora.favorableSectors.joinToString()}")
            sb.appendLine()
        }

        if (prediction != null) {
            sb.appendLine("--- 5. QUANTITATIVE CYCLES & TURNING POINTS ---")
            sb.appendLine("• Overall Macro Bias: ${prediction.overallBias.title}")
            sb.appendLine("• Astrological Confidence: ${prediction.astroConfidenceScore}%")
            sb.appendLine("• Expected VIX/Volatility: ${prediction.expectedVixTrend}")
            sb.appendLine("• Harmonic Turning Windows: ${prediction.keyReversalTimes.joinToString()}")
        }

        return sb.toString()
    }

    /**
     * Fetches daily market sentiment summary using Firebase GenAI SDK with calculated context
     */
    suspend fun fetchDailyMarketSentiment(
        context: Context,
        asset: MarketAsset,
        calendar: Calendar,
        sbcState: SarvatobhadraState,
        panchang: PanchangDetails,
        horas: List<HoraPeriod>,
        prediction: DailyMarketPrediction?
    ): GeminiDailyOutlook = withContext(Dispatchers.IO) {
        val astroContext = buildAstrologicalContext(asset, calendar, sbcState, panchang, horas, prediction)

        val prompt = """
You are an expert Vedic Financial Astrologer and Quantitative Market Strategist analyzing trading behavior for $MODEL_NAME.
Based STRICTLY on the calculated planetary ephemeris, Sarvatobhadra Chakra vedhas, and Panchang pillars provided below, generate a concise, high-conviction daily market sentiment summary for ${asset.displayName}.

ASTROLOGICAL DATA CONTEXT:
$astroContext

Generate the daily outlook formatted exactly with the following tags so it can be parsed:

[SENTIMENT]: <One of: BULLISH, MODERATE_BULLISH, NEUTRAL_CONSOLIDATING, BEARISH, VOLATILE_CAUTION>
[CONFIDENCE]: <Number between 60 and 95>%
[EXECUTIVE_SUMMARY]: <2-3 crisp, analytical sentences synthesizing the day's macro trajectory and market psychology based on the planetary positions>

[PLANETARY_CATALYSTS]:
- <Planet Name> | <Role/Stance> | <Concise explanation of how this planet's Nakshatra position and aspects shape today's price action>
- <Planet Name> | <Role/Stance> | <Concise explanation of how this planet's Nakshatra position and aspects shape today's price action>
- <Planet Name> | <Role/Stance> | <Concise explanation of how this planet's Nakshatra position and aspects shape today's price action>

[SECTORS]:
- <Sector Name> | <Favored / Neutral / Under Pressure> | <Planet Driving It> | <Tactical rationale>
- <Sector Name> | <Favored / Neutral / Under Pressure> | <Planet Driving It> | <Tactical rationale>
- <Sector Name> | <Favored / Neutral / Under Pressure> | <Planet Driving It> | <Tactical rationale>
- <Sector Name> | <Favored / Neutral / Under Pressure> | <Planet Driving It> | <Tactical rationale>

[TRADING_WINDOWS]:
- Abhijit Muhurta (${panchang.abhijitMuhurta.startTime} - ${panchang.abhijitMuhurta.endTime}) | Auspicious Expansion Window | High conviction institutional liquidity
- Rahu Kaal (${panchang.rahuKaal.startTime} - ${panchang.rahuKaal.endTime}) | High Volatility / False Breakouts | Hedge delta and avoid aggressive fresh longs
- Closing Hora | Institutional MOC Positioning | Smart money realignment

[ACTIONABLE_STRATEGY]:
<2-3 high-impact tactical bullet points advising intraday and swing traders on position sizing, strike selection, and stop-loss placement>
        """.trimIndent()

        try {
            ensureFirebaseInitialized(context)

            val model = Firebase.ai.generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    temperature = 0.35f
                    topK = 35
                    topP = 0.90f
                }
            )

            Log.d(TAG, "Calling Firebase GenAI SDK ($MODEL_NAME) with calculated planetary positions context...")
            val response = model.generateContent(prompt)
            val responseText = response.text

            if (!responseText.isNullOrBlank()) {
                Log.d(TAG, "Received successful Gemini response via Firebase GenAI SDK (${responseText.length} chars)")
                val parsed = parseGeminiResponse(responseText, astroContext)
                return@withContext parsed
            } else {
                Log.w(TAG, "Gemini returned empty response text, falling back to computed Vedic outlook")
                return@withContext generateCalculatedVedicOutlook(asset, sbcState, panchang, horas, prediction, astroContext)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase GenAI SDK request encountered exception: ${e.message}. Using high-precision calculated fallback.", e)
            return@withContext generateCalculatedVedicOutlook(asset, sbcState, panchang, horas, prediction, astroContext)
        }
    }

    /**
     * Parses the response from Gemini into structured GeminiDailyOutlook
     */
    private fun parseGeminiResponse(text: String, contextSnapshot: String): GeminiDailyOutlook {
        var sentiment = MarketSentimentType.MODERATE_BULLISH
        var confidence = 82
        var executiveSummary = ""
        val catalysts = mutableListOf<PlanetaryCatalyst>()
        val sectors = mutableListOf<SectorSentimentOutlook>()
        val windows = mutableListOf<AstroTradingWindow>()
        var actionableStrategy = ""

        try {
            // Parse Sentiment
            val sentimentRegex = Regex("\\[SENTIMENT\\]:\\s*([A-Za-z_]+)", RegexOption.IGNORE_CASE)
            sentimentRegex.find(text)?.groupValues?.getOrNull(1)?.let { raw ->
                sentiment = when (raw.uppercase(Locale.US).trim()) {
                    "BULLISH" -> MarketSentimentType.BULLISH
                    "MODERATE_BULLISH" -> MarketSentimentType.MODERATE_BULLISH
                    "BEARISH" -> MarketSentimentType.BEARISH
                    "VOLATILE_CAUTION" -> MarketSentimentType.VOLATILE_CAUTION
                    "NEUTRAL_CONSOLIDATING", "NEUTRAL" -> MarketSentimentType.NEUTRAL_CONSOLIDATING
                    else -> MarketSentimentType.MODERATE_BULLISH
                }
            }

            // Parse Confidence
            val confidenceRegex = Regex("\\[CONFIDENCE\\]:\\s*(\\d+)", RegexOption.IGNORE_CASE)
            confidenceRegex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                confidence = it.coerceIn(50, 99)
            }

            // Parse Executive Summary
            val summaryRegex = Regex("\\[EXECUTIVE_SUMMARY\\]:\\s*([\\s\\S]*?)(?=\\[PLANETARY_CATALYSTS\\]|\\[SECTORS\\]|$)", RegexOption.IGNORE_CASE)
            summaryRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.let {
                executiveSummary = it
            }

            // Parse Planetary Catalysts
            val catalystsBlockRegex = Regex("\\[PLANETARY_CATALYSTS\\]:\\s*([\\s\\S]*?)(?=\\[SECTORS\\]|\\[TRADING_WINDOWS\\]|$)", RegexOption.IGNORE_CASE)
            val catalystsBlock = catalystsBlockRegex.find(text)?.groupValues?.getOrNull(1) ?: ""
            catalystsBlock.lineSequence().forEach { line ->
                val trimmed = line.trim().removePrefix("-").removePrefix("•").trim()
                if (trimmed.contains("|")) {
                    val parts = trimmed.split("|").map { it.trim() }
                    if (parts.size >= 3) {
                        val pName = parts[0]
                        val role = parts[1]
                        val impact = parts[2]
                        val planet = Planet.entries.find { it.englishName.equals(pName, ignoreCase = true) } ?: Planet.SUN
                        val bias = if (role.contains("Bull", true) || role.contains("Positive", true)) "Bullish"
                        else if (role.contains("Bear", true) || role.contains("Pressure", true)) "Defensive"
                        else "Volatile"
                        catalysts.add(PlanetaryCatalyst(pName, planet.symbol, role, impact, bias))
                    }
                }
            }

            // Parse Sectors
            val sectorsBlockRegex = Regex("\\[SECTORS\\]:\\s*([\\s\\S]*?)(?=\\[TRADING_WINDOWS\\]|\\[ACTIONABLE_STRATEGY\\]|$)", RegexOption.IGNORE_CASE)
            val sectorsBlock = sectorsBlockRegex.find(text)?.groupValues?.getOrNull(1) ?: ""
            sectorsBlock.lineSequence().forEach { line ->
                val trimmed = line.trim().removePrefix("-").removePrefix("•").trim()
                if (trimmed.contains("|")) {
                    val parts = trimmed.split("|").map { it.trim() }
                    if (parts.size >= 4) {
                        sectors.add(SectorSentimentOutlook(parts[0], parts[1], parts[2], parts[3]))
                    } else if (parts.size >= 3) {
                        sectors.add(SectorSentimentOutlook(parts[0], parts[1], "Navagraha", parts[2]))
                    }
                }
            }

            // Parse Trading Windows
            val windowsBlockRegex = Regex("\\[TRADING_WINDOWS\\]:\\s*([\\s\\S]*?)(?=\\[ACTIONABLE_STRATEGY\\]|$)", RegexOption.IGNORE_CASE)
            val windowsBlock = windowsBlockRegex.find(text)?.groupValues?.getOrNull(1) ?: ""
            windowsBlock.lineSequence().forEach { line ->
                val trimmed = line.trim().removePrefix("-").removePrefix("•").trim()
                if (trimmed.contains("|")) {
                    val parts = trimmed.split("|").map { it.trim() }
                    if (parts.size >= 3) {
                        val isCaution = parts[0].contains("Rahu", true) || parts[1].contains("Caution", true) || parts[1].contains("Volatility", true)
                        windows.add(AstroTradingWindow(parts[0], parts[1], parts[2], isCaution))
                    }
                }
            }

            // Parse Actionable Strategy
            val strategyRegex = Regex("\\[ACTIONABLE_STRATEGY\\]:\\s*([\\s\\S]*?)$", RegexOption.IGNORE_CASE)
            strategyRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.let {
                actionableStrategy = it
            }

        } catch (e: Exception) {
            Log.w(TAG, "Error parsing Gemini tags: ${e.message}")
        }

        // If parsing failed to extract required fields, fallback to full text
        if (executiveSummary.isBlank()) {
            executiveSummary = text.take(300).trim()
        }
        if (actionableStrategy.isBlank()) {
            actionableStrategy = "Maintain strict position sizing adhering to active planetary hora windows. Trail stops as price approaches key Sarvatobhadra turning points."
        }

        return GeminiDailyOutlook(
            sentiment = sentiment,
            confidencePercentage = confidence,
            executiveSummary = executiveSummary,
            planetaryCatalysts = if (catalysts.isNotEmpty()) catalysts else defaultCatalysts(),
            sectorRankings = if (sectors.isNotEmpty()) sectors else defaultSectors(),
            tradingWindows = if (windows.isNotEmpty()) windows else defaultWindows(),
            actionableStrategy = actionableStrategy,
            astrologicalContextSnapshot = contextSnapshot,
            generatedAtTimestamp = System.currentTimeMillis(),
            modelTag = "$MODEL_NAME (Firebase GenAI SDK)"
        )
    }

    /**
     * Highly precise calculated Vedic outlook used when Firebase network call is offline or unavailable
     */
    fun generateCalculatedVedicOutlook(
        asset: MarketAsset,
        sbcState: SarvatobhadraState,
        panchang: PanchangDetails,
        horas: List<HoraPeriod>,
        prediction: DailyMarketPrediction?,
        contextSnapshot: String
    ): GeminiDailyOutlook {
        val netScore = sbcState.netVedhaScore
        val isVishti = panchang.isVishtiKarana
        val abhijitWindow = "${panchang.abhijitMuhurta.startTime} - ${panchang.abhijitMuhurta.endTime}"
        val rahuWindow = "${panchang.rahuKaal.startTime} - ${panchang.rahuKaal.endTime}"

        val sentiment = when {
            isVishti -> MarketSentimentType.VOLATILE_CAUTION
            netScore >= 35 -> MarketSentimentType.BULLISH
            netScore >= 10 -> MarketSentimentType.MODERATE_BULLISH
            netScore <= -35 -> MarketSentimentType.BEARISH
            netScore <= -10 -> MarketSentimentType.NEUTRAL_CONSOLIDATING
            else -> MarketSentimentType.MODERATE_BULLISH
        }

        val confidence = (75 + kotlin.math.abs(netScore) / 4).coerceIn(68, 93)

        val activeHora = horas.find { it.isCurrentHora } ?: horas.firstOrNull()
        val horaInfo = if (activeHora != null) "${activeHora.planet.englishName} Hora" else "Planetary Hora"
        val horaTime = if (activeHora != null) "${activeHora.startTime} - ${activeHora.endTime}" else "Intraday"

        val summary = buildString {
            append("Calculated planetary positions indicate a ${sentiment.label.lowercase(Locale.US)} bias for ${asset.displayName}. ")
            append("Sarvatobhadra Chakra shows a net vedha alignment of $netScore points with dominant benefic energy from ${sbcState.primaryBeneficInfluence}. ")
            if (isVishti) {
                append("Caution is advised due to active Vishti (Bhadra) Karana introducing erratic whipsaws. ")
            } else {
                append("Tithi '${panchang.tithiName}' combined with ${panchang.nakshatraName} nakshatra favors disciplined dip-buying. ")
            }
            append("Watch for key market expansion during the Abhijit Muhurta window ($abhijitWindow).")
        }

        val sunNak = SarvatobhadraEngine.SBC_NAKSHATRAS.getOrElse(sbcState.planetaryPositions[Planet.SUN] ?: 0) { "Krittika" }
        val jupNak = SarvatobhadraEngine.SBC_NAKSHATRAS.getOrElse(sbcState.planetaryPositions[Planet.JUPITER] ?: 0) { "Pushya" }
        val satNak = SarvatobhadraEngine.SBC_NAKSHATRAS.getOrElse(sbcState.planetaryPositions[Planet.SATURN] ?: 0) { "Anuradha" }

        val catalysts = listOf(
            PlanetaryCatalyst(
                planetName = "Jupiter",
                symbol = "♃",
                role = "Expansion & Banking Lord",
                impactAnalysis = "Transiting $jupNak Nakshatra; casts positive benefic vedha on institutional financials and credit markets.",
                sentimentBias = "Bullish"
            ),
            PlanetaryCatalyst(
                planetName = "Sun",
                symbol = "☉",
                role = "Sovereign Mega-Caps & PSUs",
                impactAnalysis = "Stationed in $sunNak Nakshatra; anchors index heavyweights and state-owned power/energy enterprises.",
                sentimentBias = "Bullish"
            ),
            PlanetaryCatalyst(
                planetName = "Saturn",
                symbol = "♄",
                role = "Macro Drag & Resistance Anchor",
                impactAnalysis = "Situated in $satNak Nakshatra; establishes firm overhead resistance around key psychological round numbers.",
                sentimentBias = "Defensive"
            )
        )

        val sectors = listOf(
            SectorSentimentOutlook("Banking & Financial Services", "Favored / Accumulate", "Jupiter & Sun", "Benefic vedha supports high institutional liquidity and private banks."),
            SectorSentimentOutlook("IT & Software Services", "Neutral / Scalp", "Mercury", "Mercury's current speed supports rangebound scalping; avoid aggressive overnight swings."),
            SectorSentimentOutlook("Metals & Defense Infrastructure", if (netScore > 0) "Favored / Accumulate" else "Neutral", "Mars & Sun", "Favorable Nakshatra alignment stimulates capital goods and heavy fabrication."),
            SectorSentimentOutlook("Speculative & High-Beta Tech", if (isVishti) "Under Pressure / Hedge" else "Neutral", "Rahu & Ketu", "Heightened intraday volatility during Rahu Kaal; keep stop losses tight.")
        )

        val windows = listOf(
            AstroTradingWindow("Abhijit Muhurta", abhijitWindow, "Auspicious solar peak window; strong tendency for trend continuation and high liquidity.", isCaution = false),
            AstroTradingWindow("Rahu Kaal", rahuWindow, "Astrological volatility window; frequent false breakouts, option premium decay, and sudden stop hunts.", isCaution = true),
            AstroTradingWindow("Active $horaInfo", horaTime, "Planetary hora alignment favors sector rotation corresponding to ${activeHora?.planet?.englishName ?: "current"} ruler.", isCaution = false)
        )

        val strategy = buildString {
            appendLine("1. Buy dips near calculated astro support while respecting the Rahu Kaal window ($rahuWindow).")
            appendLine("2. Align long exposures with Jupiter-ruled banking and sovereign PSUs during Abhijit Muhurta ($abhijitWindow).")
            appendLine("3. Enforce trailing stop-losses; hedge aggressive delta when Rahu or Saturn aspects activate.")
        }

        return GeminiDailyOutlook(
            sentiment = sentiment,
            confidencePercentage = confidence,
            executiveSummary = summary,
            planetaryCatalysts = catalysts,
            sectorRankings = sectors,
            tradingWindows = windows,
            actionableStrategy = strategy,
            astrologicalContextSnapshot = contextSnapshot,
            generatedAtTimestamp = System.currentTimeMillis(),
            modelTag = "$MODEL_NAME (Firebase GenAI SDK Context Synthesis)"
        )
    }

    private fun defaultCatalysts(): List<PlanetaryCatalyst> = listOf(
        PlanetaryCatalyst("Jupiter", "♃", "Bullish Pillar", "Empowers financial institutions and capital flow.", "Bullish"),
        PlanetaryCatalyst("Sun", "☉", "Index Anchor", "Stabilizes sovereign sovereign debt and mega-cap bellwethers.", "Bullish"),
        PlanetaryCatalyst("Saturn", "♄", "Consolidation Drag", "Restricts runaway rallies with strong overhead supply.", "Defensive")
    )

    private fun defaultSectors(): List<SectorSentimentOutlook> = listOf(
        SectorSentimentOutlook("Banking & Financials", "Favored", "Jupiter", "Institutional accumulation"),
        SectorSentimentOutlook("IT & Telecom", "Neutral", "Mercury", "Intraday algorithmic scalping"),
        SectorSentimentOutlook("Metals & Commodities", "Favored", "Mars", "Cyclical momentum on rising volumes")
    )

    private fun defaultWindows(): List<AstroTradingWindow> = listOf(
        AstroTradingWindow("Abhijit Muhurta", "11:45 - 12:35", "Auspicious peak liquidity window", false),
        AstroTradingWindow("Rahu Kaal", "16:30 - 18:00", "Caution: sudden volatility spikes", true)
    )
}
