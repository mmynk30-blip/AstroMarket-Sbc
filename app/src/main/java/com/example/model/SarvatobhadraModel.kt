package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Planetary entities in Vedic Astrology (Navagraha + Nodes)
 */
enum class Planet(
    val englishName: String,
    val sanskritName: String,
    val symbol: String,
    val isNaturalBenefic: Boolean,
    val marketRole: String,
    val associatedSectors: List<String>,
    val displayColor: Color
) {
    SUN("Sun", "Surya", "☉", false, "Authority, Govt PSUs, Sovereign debt, Mega Caps", listOf("PSU", "Gold", "Power", "Govt Bonds"), VedicGold),
    MOON("Moon", "Chandra", "☽", true, "Liquidity, Retail sentiment, High Beta, Emotions", listOf("FMCG", "Silver", "Liquidity", "Dairy"), MoonSilver),
    MARS("Mars", "Mangala", "♂", false, "Aggressive moves, Defense, Volatile spikes", listOf("Defense", "Metals", "Real Estate", "Engineering"), BearishRuby),
    MERCURY("Mercury", "Budha", "☿", true, "Trading volume, IT/Tech, Algorithms, Scalping", listOf("IT / Software", "Telecom", "Fintech", "Media"), CelestialCyan),
    JUPITER("Jupiter", "Brihaspati", "♃", true, "Institutional expansion, Bull market, Banking", listOf("Banking", "Financial Services", "NBFC", "Gold"), VedicGoldLight),
    VENUS("Venus", "Shukra", "♀", true, "Luxury, Auto, Media, Steady bullish climb", listOf("Auto", "Luxury Goods", "Textiles", "Entertainment"), CelestialViolet),
    SATURN("Saturn", "Shani", "♄", false, "Heavy drag, Deep consolidation, Bear trends", listOf("Oil & Gas", "Mining", "Steel", "Heavy Infra"), Color(0xFF94A3B8)),
    RAHU("Rahu", "North Node", "☊", false, "Wild speculation, Illusions, Flash rallies/dumps", listOf("Crypto / Speculative", "Aviation", "Foreign Inflow"), Color(0xFFE879F9)),
    KETU("Ketu", "South Node", "☋", false, "Hidden sudden risks, Algo flash crashes, Gold safe-haven", listOf("Precious Metals", "Pharma", "Cybersecurity"), Color(0xFFF97316));

    val speedDescription: String
        get() = when (this) {
            MOON -> "Very Fast (2.25 days/sign)"
            MERCURY, VENUS, SUN -> "Fast (1 month/sign)"
            MARS -> "Medium (45 days/sign)"
            JUPITER -> "Slow Bull (1 year/sign)"
            SATURN -> "Very Slow Anchor (2.5 years/sign)"
            RAHU, KETU -> "Retrograde Axis (1.5 years/sign)"
        }
}

/**
 * Type of Vedha (Cross-aspect) in Sarvatobhadra Chakra
 */
enum class VedhaType(val title: String, val description: String) {
    SAMUKHA("Front Vedha (Purna)", "Direct opposite ray crossing the chakra matrix"),
    DAKSHINA("Right Vedha (Pashchat)", "Right diagonal ray aspecting cross-quadrants"),
    VAMA("Left Vedha", "Left diagonal ray aspecting adjacent quadrants"),
    CONJUNCTION("Direct Sthiti", "Planet currently situated inside this Nakshatra")
}

/**
 * A Vedha relationship cast by a planet onto a target item (Nakshatra, Rasi, or Sector)
 */
data class VedhaImpact(
    val planet: Planet,
    val vedhaType: VedhaType,
    val targetNakshatra: String,
    val targetSector: String,
    val isBenefic: Boolean,
    val marketEffect: String,
    val impactScore: Int // -100 to +100
)

/**
 * A cell in the 9x9 Sarvatobhadra Chakra matrix
 */
data class SbCell(
    val row: Int,
    val col: Int,
    val label: String,
    val subLabel: String = "",
    val cellType: CellType,
    val rulingPlanet: Planet? = null,
    val occupyingPlanets: List<Planet> = emptyList(),
    val activeVedhas: List<VedhaImpact> = emptyList()
) {
    enum class CellType {
        NAKSHATRA_PERIMETER,
        CORNER_SWARA,
        INNER_SWARA,
        INNER_VARNA,
        INNER_RASI,
        INNER_TITHI_VARA,
        CENTER_BINDU
    }

    val isAfflicted: Boolean
        get() = activeVedhas.any { !it.isBenefic }

    val isBlessed: Boolean
        get() = activeVedhas.any { it.isBenefic }
}

/**
 * Sarvatobhadra Chakra Market State for a given date
 */
data class SarvatobhadraState(
    val grid: List<List<SbCell>>,
    val activeVedhas: List<VedhaImpact>,
    val planetaryPositions: Map<Planet, Int>, // Planet to Nakshatra Index (0 to 27)
    val bullishVedhaScore: Int, // 0 to 100
    val bearishVedhaScore: Int, // 0 to 100
    val netVedhaScore: Int, // -100 to +100
    val primaryBeneficInfluence: String,
    val primaryMaleficPressure: String
)
