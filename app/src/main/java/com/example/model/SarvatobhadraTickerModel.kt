package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.engine.SarvatobhadraEngine
import com.example.ui.theme.*

/**
 * Represents a stock market ticker mapped to a Lunar Mansion (Nakshatra)
 * in Sarvatobhadra Chakra financial astrology.
 */
data class StockTickerNakshatraMapping(
    val tickerSymbol: String,           // e.g. "RELIANCE", "NIFTY", "TCS", "HDFCBANK"
    val companyName: String,            // e.g. "Reliance Industries", "Nifty 50 Index"
    val nakshatraName: String,          // e.g. "Krittika", "Abhijit", "Ardra"
    val sector: String,                 // e.g. "Energy & Conglomerate", "Benchmark Index", "IT Services"
    val namaAkshara: String = "",       // Traditional name sound / seed syllable (e.g., "Ra", "Ta", "I")
    val exchange: String = "NSE",       // "NSE", "BSE", "NASDAQ", "NYSE", "MCX"
    val isCustomUserMapped: Boolean = false
)

/**
 * Astrological market stance for a stock ticker under current SBC transits and Vedhas.
 */
enum class TickerMarketStance(val label: String, val badgeColor: Color, val containerColor: Color) {
    STRONG_BULLISH("Strong Bullish (Benefic Vedha)", BullishEmeraldLight, BullishContainer),
    ACCUMULATE("Accumulate on Dips", BullishEmerald, BullishContainer),
    NEUTRAL_RANGE("Rangebound / Neutral", TextSecondaryLight, CosmicSurfaceElevated),
    CAUTION("Caution / High Beta", VolatileAmber, VolatileContainer),
    BEARISH_DRAG("Bearish Resistance (Malefic Vedha)", BearishRubyLight, BearishContainer),
    HIGH_ALERT("Heavy Malefic Affliction", BearishRuby, BearishContainer)
}

/**
 * Comprehensive evaluated posture of a stock ticker in the Sarvatobhadra Chakra.
 */
data class StockTickerAstroPosture(
    val mapping: StockTickerNakshatraMapping,
    val occupyingPlanets: List<Planet>,
    val incomingVedhas: List<VedhaImpact>,
    val sentimentScore: Int, // -100 to +100
    val stance: TickerMarketStance,
    val summaryText: String
)

/**
 * Repository and helper engine for mapping stock market tickers to 28 Lunar Mansions
 * in the Sarvatobhadra Chakra layout.
 */
object SarvatobhadraTickerRepository {

    /**
     * Default catalog of widely traded stock market tickers mapped to the 28 Nakshatras
     * based on corporate incorporation, IPO Muhurta, and classical planetary-nakshatra sector rulerships.
     */
    val DEFAULT_TICKER_MAPPINGS: List<StockTickerNakshatraMapping> = listOf(
        // 1. Krittika (Sun / Agni - Sovereign, Power, PSUs & Gold)
        StockTickerNakshatraMapping("RELIANCE", "Reliance Industries", "Krittika", "Energy & Conglomerate", "Ra", "NSE"),
        StockTickerNakshatraMapping("ONGC", "Oil & Natural Gas Corp", "Krittika", "PSU Energy", "O", "NSE"),
        StockTickerNakshatraMapping("NTPC", "NTPC Power", "Krittika", "Sovereign Power", "Na", "NSE"),
        StockTickerNakshatraMapping("GOLD", "Gold Bullion (MCX)", "Krittika", "Precious Metals", "Go", "MCX"),

        // 2. Rohini (Moon / Brahma - Auto, Real Estate, Growth)
        StockTickerNakshatraMapping("TATAMOTORS", "Tata Motors Ltd", "Rohini", "Automotive & EV", "Ta", "NSE"),
        StockTickerNakshatraMapping("MARUTI", "Maruti Suzuki India", "Rohini", "Passenger Vehicles", "Ma", "NSE"),
        StockTickerNakshatraMapping("DLF", "DLF Ltd", "Rohini", "Real Estate Infrastructure", "Da", "NSE"),

        // 3. Mrigashira (Mars / Soma - Metals, Steel & Mining)
        StockTickerNakshatraMapping("TATASTEEL", "Tata Steel Ltd", "Mrigashira", "Ferrous Metals & Steel", "Ta", "NSE"),
        StockTickerNakshatraMapping("HINDALCO", "Hindalco Industries", "Mrigashira", "Aluminum & Copper", "Ha", "NSE"),
        StockTickerNakshatraMapping("JSWSTEEL", "JSW Steel Ltd", "Mrigashira", "Steel Manufacturing", "Ja", "NSE"),

        // 4. Ardra (Rahu / Rudra - High Tech, IT, Algorithms & AI)
        StockTickerNakshatraMapping("INFY", "Infosys Ltd", "Ardra", "IT Services & AI", "I", "NSE"),
        StockTickerNakshatraMapping("TCS", "Tata Consultancy Services", "Ardra", "Global Tech Services", "Ta", "NSE"),
        StockTickerNakshatraMapping("NVDA", "Nvidia Corporation", "Ardra", "Semiconductor & AI", "Na", "NASDAQ"),

        // 5. Punarvasu (Jupiter / Aditi - Banking Expansion & Wealth)
        StockTickerNakshatraMapping("HDFCBANK", "HDFC Bank Ltd", "Punarvasu", "Private Banking", "Ha", "NSE"),
        StockTickerNakshatraMapping("KOTAKBANK", "Kotak Mahindra Bank", "Punarvasu", "Banking & Wealth Management", "Ka", "NSE"),
        StockTickerNakshatraMapping("AXISBANK", "Axis Bank Ltd", "Punarvasu", "Financial Services", "Aa", "NSE"),

        // 6. Pushya (Saturn / Brihaspati - Apex Institutions & Heavy Banks)
        StockTickerNakshatraMapping("SBIN", "State Bank of India", "Pushya", "Public Sector Banking", "Sa", "NSE"),
        StockTickerNakshatraMapping("ICICIBANK", "ICICI Bank Ltd", "Pushya", "Universal Banking", "I", "NSE"),
        StockTickerNakshatraMapping("BANKNIFTY", "Nifty Bank Benchmark", "Pushya", "Banking Index", "Ba", "NSE"),

        // 7. Ashlesha (Mercury / Sarpa - Telecom, High Beta & Scalpers)
        StockTickerNakshatraMapping("BHARTIARTL", "Bharti Airtel Ltd", "Ashlesha", "Telecom & 5G Data", "Bha", "NSE"),
        StockTickerNakshatraMapping("IDEA", "Vodafone Idea Ltd", "Ashlesha", "Telecom Infrastructure", "I", "NSE"),

        // 8. Magha (Ketu / Pitris - Heavy Utilities & Power Grid)
        StockTickerNakshatraMapping("ADANIENT", "Adani Enterprises Ltd", "Magha", "Mining & Conglomerate", "Aa", "NSE"),
        StockTickerNakshatraMapping("COALINDIA", "Coal India Ltd", "Magha", "Energy Extraction", "Ka", "NSE"),
        StockTickerNakshatraMapping("POWERGRID", "Power Grid Corp of India", "Magha", "Power Transmission", "Pa", "NSE"),

        // 9. Purva Phalguni (Venus / Bhaga - Luxury, Consumer Discretionary & Media)
        StockTickerNakshatraMapping("TITAN", "Titan Company Ltd", "Purva Phalguni", "Luxury & Jewelry", "Ta", "NSE"),
        StockTickerNakshatraMapping("PVRINOX", "PVR INOX Ltd", "Purva Phalguni", "Media & Entertainment", "Pa", "NSE"),
        StockTickerNakshatraMapping("AAPL", "Apple Inc", "Purva Phalguni", "Consumer Tech & Luxury", "Aa", "NASDAQ"),

        // 10. Uttara Phalguni (Sun / Aryaman - Capital Goods & Mega Infrastructure)
        StockTickerNakshatraMapping("LT", "Larsen & Toubro Ltd", "Uttara Phalguni", "EPC & Infrastructure", "La", "NSE"),
        StockTickerNakshatraMapping("BHEL", "Bharat Heavy Electricals", "Uttara Phalguni", "Heavy Capital Goods", "Bha", "NSE"),

        // 11. Hasta (Moon / Savitr - FMCG & Consumer Staples)
        StockTickerNakshatraMapping("HINDUNILVR", "Hindustan Unilever", "Hasta", "FMCG Staples", "Ha", "NSE"),
        StockTickerNakshatraMapping("ITC", "ITC Ltd", "Hasta", "Conglomerate & FMCG", "I", "NSE"),
        StockTickerNakshatraMapping("DABUR", "Dabur India Ltd", "Hasta", "Consumer Health", "Da", "NSE"),

        // 12. Chitra (Mars / Tvashtar - Industrial Engineering & Precision)
        StockTickerNakshatraMapping("SIEMENS", "Siemens India", "Chitra", "Industrial Automation", "Sa", "NSE"),
        StockTickerNakshatraMapping("ABB", "ABB India Ltd", "Chitra", "Electrification & Robotics", "Aa", "NSE"),

        // 13. Swati (Rahu / Vayu - Aviation, Logistics & Green Energy)
        StockTickerNakshatraMapping("INDIGO", "InterGlobe Aviation", "Swati", "Commercial Aviation", "I", "NSE"),
        StockTickerNakshatraMapping("SUZLON", "Suzlon Energy Ltd", "Swati", "Wind Power Equipment", "Sa", "NSE"),
        StockTickerNakshatraMapping("ADANIPORTS", "Adani Ports & SEZ", "Swati", "Ports & Logistics", "Aa", "NSE"),

        // 14. Vishakha (Jupiter / Indra-Agni - Pharma & Life Sciences)
        StockTickerNakshatraMapping("SUNPHARMA", "Sun Pharmaceutical", "Vishakha", "Pharma & Formulations", "Sa", "NSE"),
        StockTickerNakshatraMapping("DRREDDY", "Dr. Reddy's Labs", "Vishakha", "Generics & Biotech", "Da", "NSE"),
        StockTickerNakshatraMapping("CIPLA", "Cipla Ltd", "Vishakha", "Respiratory & Healthcare", "Ca", "NSE"),

        // 15. Anuradha (Saturn / Mitra - Oil Refining & Deep Petrochemicals)
        StockTickerNakshatraMapping("BPCL", "Bharat Petroleum Corp", "Anuradha", "Refining & Marketing", "Ba", "NSE"),
        StockTickerNakshatraMapping("IOC", "Indian Oil Corporation", "Anuradha", "Downstream Oil & Gas", "I", "NSE"),
        StockTickerNakshatraMapping("CRUDE", "Crude Oil (MCX/WTI)", "Anuradha", "Energy Commodity", "Kra", "MCX"),

        // 16. Jyeshtha (Mercury / Indra - Fintech, Exchanges & Brokerages)
        StockTickerNakshatraMapping("BSE", "BSE Ltd", "Jyeshtha", "Securities Exchange", "Ba", "NSE"),
        StockTickerNakshatraMapping("MCX", "Multi Commodity Exchange", "Jyeshtha", "Commodity Exchange", "Ma", "NSE"),
        StockTickerNakshatraMapping("CDSL", "Central Depository Services", "Jyeshtha", "Fintech Infrastructure", "Ca", "NSE"),

        // 17. Mula (Ketu / Nirriti - Raw Commodities & Agro Mining)
        StockTickerNakshatraMapping("VEDL", "Vedanta Ltd", "Mula", "Diversified Mining & Zinc", "Va", "NSE"),
        StockTickerNakshatraMapping("UPL", "UPL Ltd", "Mula", "Crop Protection & Agro", "U", "NSE"),

        // 18. Purva Ashadha (Venus / Apas - Paints, Hospitality & Beverages)
        StockTickerNakshatraMapping("ASIANPAINT", "Asian Paints Ltd", "Purva Ashadha", "Decorative Paints", "Aa", "NSE"),
        StockTickerNakshatraMapping("INDHOTEL", "Indian Hotels Company", "Purva Ashadha", "Luxury Hospitality", "I", "NSE"),

        // 19. Uttara Ashadha (Sun / Vishwadevas - Solar, Hydro & Renewable Power)
        StockTickerNakshatraMapping("TATAPOWER", "Tata Power Co Ltd", "Uttara Ashadha", "Renewable & EV Charging", "Ta", "NSE"),
        StockTickerNakshatraMapping("ADANIGREEN", "Adani Green Energy", "Uttara Ashadha", "Solar Generation", "Aa", "NSE"),

        // 20. Abhijit (Intercalary Auspicious 28th - Apex Benchmark & Large-Cap Indices)
        StockTickerNakshatraMapping("NIFTY", "Nifty 50 Benchmark", "Abhijit", "NSE Core Index", "Na", "NSE"),
        StockTickerNakshatraMapping("SPX", "S&P 500 Index", "Abhijit", "US Benchmark Index", "Sa", "NYSE"),
        StockTickerNakshatraMapping("TSLA", "Tesla Inc", "Abhijit", "EV & Clean Energy", "Ta", "NASDAQ"),

        // 21. Shravana (Moon / Vishnu - Packaged Foods & Media)
        StockTickerNakshatraMapping("BRITANNIA", "Britannia Industries", "Shravana", "Biscuits & Dairy", "Ba", "NSE"),
        StockTickerNakshatraMapping("MARICO", "Marico Ltd", "Shravana", "Consumer Staples", "Ma", "NSE"),
        StockTickerNakshatraMapping("ZOMATO", "Zomato Ltd", "Shravana", "Digital Delivery", "Za", "NSE"),

        // 22. Dhanishta (Mars / Vasus - Aerospace, Defense & Precision Tech)
        StockTickerNakshatraMapping("BEL", "Bharat Electronics Ltd", "Dhanishta", "Defense Electronics & Radar", "Ba", "NSE"),
        StockTickerNakshatraMapping("HAL", "Hindustan Aeronautics", "Dhanishta", "Aero Combat & Helicopters", "Ha", "NSE"),
        StockTickerNakshatraMapping("DIXON", "Dixon Technologies", "Dhanishta", "Electronics Manufacturing", "Da", "NSE"),

        // 23. Shatabhisha (Rahu / Varuna - Biotech, Deep Science & Crypto)
        StockTickerNakshatraMapping("BIOCON", "Biocon Ltd", "Shatabhisha", "Biopharmaceuticals", "Ba", "NSE"),
        StockTickerNakshatraMapping("BTC", "Bitcoin / Crypto Asset", "Shatabhisha", "Digital Asset", "Ba", "CRYPTO"),

        // 24. Purva Bhadrapada (Jupiter / Aja Ekapada - NBFCs & Consumer Finance)
        StockTickerNakshatraMapping("BAJFINANCE", "Bajaj Finance Ltd", "Purva Bhadrapada", "Consumer Lending NBFC", "Ba", "NSE"),
        StockTickerNakshatraMapping("HDFCLIFE", "HDFC Life Insurance", "Purva Bhadrapada", "Life Insurance", "Ha", "NSE"),

        // 25. Uttara Bhadrapada (Saturn / Ahirbudhnya - Deep Sea Marine & Shipyards)
        StockTickerNakshatraMapping("MAZDOCK", "Mazagon Dock Shipbuilders", "Uttara Bhadrapada", "Submarines & Warships", "Ma", "NSE"),
        StockTickerNakshatraMapping("COCHINSHIP", "Cochin Shipyard Ltd", "Uttara Bhadrapada", "Marine Defense", "Ca", "NSE"),

        // 26. Revati (Mercury / Pushan - IT Outsourcing & Wealth Platforms)
        StockTickerNakshatraMapping("WIPRO", "Wipro Ltd", "Revati", "Global IT Consulting", "Va", "NSE"),
        StockTickerNakshatraMapping("HCLTECH", "HCL Technologies Ltd", "Revati", "Digital Engineering & Cloud", "Ha", "NSE"),

        // 27. Ashwini (Ketu / Ashvins - EV Mobility, Two-Wheelers & Quick Trade)
        StockTickerNakshatraMapping("TVSMOTOR", "TVS Motor Company", "Ashwini", "Two-Wheelers & EV", "Ta", "NSE"),
        StockTickerNakshatraMapping("BAJAJ-AUTO", "Bajaj Auto Ltd", "Ashwini", "Mobility & Exports", "Ba", "NSE"),

        // 28. Bharani (Venus / Yama - Specialty Chemicals & Diagnostic Healthcare)
        StockTickerNakshatraMapping("PIIND", "PI Industries Ltd", "Bharani", "Agrochem & Custom Synthesis", "Pa", "NSE"),
        StockTickerNakshatraMapping("APOLLOHOSP", "Apollo Hospitals Enterprise", "Bharani", "Multi-Specialty Healthcare", "Aa", "NSE")
    )

    /**
     * Evaluates a stock ticker's astrological posture given the current Sarvatobhadra Chakra state.
     */
    fun evaluateTickerPosture(
        mapping: StockTickerNakshatraMapping,
        sbcState: SarvatobhadraState
    ): StockTickerAstroPosture {
        val nakshatraName = mapping.nakshatraName
        val nakIndex = SarvatobhadraEngine.SBC_NAKSHATRAS.indexOf(nakshatraName)

        val occupyingPlanets = sbcState.planetaryPositions
            .filter { it.value == nakIndex }
            .map { it.key }

        val incomingVedhas = sbcState.activeVedhas
            .filter { it.targetNakshatra.equals(nakshatraName, ignoreCase = true) }

        var netScore = 0
        occupyingPlanets.forEach { planet ->
            netScore += if (planet.isNaturalBenefic) 40 else -35
        }
        incomingVedhas.forEach { vedha ->
            netScore += vedha.impactScore
        }

        // Bound sentiment score from -100 to +100
        val clampedScore = netScore.coerceIn(-100, 100)

        val stance = when {
            clampedScore >= 60 -> TickerMarketStance.STRONG_BULLISH
            clampedScore >= 20 -> TickerMarketStance.ACCUMULATE
            clampedScore <= -60 -> TickerMarketStance.HIGH_ALERT
            clampedScore <= -20 -> TickerMarketStance.BEARISH_DRAG
            occupyingPlanets.any { !it.isNaturalBenefic } -> TickerMarketStance.CAUTION
            else -> TickerMarketStance.NEUTRAL_RANGE
        }

        val summaryText = buildString {
            if (occupyingPlanets.isNotEmpty()) {
                append("Planets in Nakshatra: ${occupyingPlanets.joinToString { "${it.englishName} (${it.symbol})" }}. ")
            }
            if (incomingVedhas.isNotEmpty()) {
                val benefics = incomingVedhas.filter { it.isBenefic }
                val malefics = incomingVedhas.filter { !it.isBenefic }
                if (benefics.isNotEmpty()) {
                    append("Lifted by ${benefics.joinToString { it.planet.englishName }}. ")
                }
                if (malefics.isNotEmpty()) {
                    append("Pressed by ${malefics.joinToString { it.planet.englishName }} Vedha. ")
                }
            } else if (occupyingPlanets.isEmpty()) {
                append("Neutral cosmic corridor; trading in accordance with broader sector beta.")
            }
        }

        return StockTickerAstroPosture(
            mapping = mapping,
            occupyingPlanets = occupyingPlanets,
            incomingVedhas = incomingVedhas,
            sentimentScore = clampedScore,
            stance = stance,
            summaryText = summaryText.trim()
        )
    }
}
