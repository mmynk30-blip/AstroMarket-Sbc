package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

enum class GaugeDisplayMode(val label: String) {
    ACTIVE_HOUR("Current Hour"),
    DAILY_AVERAGE("Session Average"),
    PEAK_BULLISH("Zenith Peak"),
    MAX_CAUTION("Max Caution")
}

/**
 * Types of Vedic Transits contributing to the Market Intensity Score
 */
enum class TransitTooltipType(val title: String, val shortLabel: String, val icon: String, val weightPercent: Int) {
    COMPOSITE_SYNTHESIS("Net Transit Synthesis", "🎯 All Transits", "🎯", 100),
    HORA_TRANSIT("Hora Planetary Lord", "🪐 Hora Lord", "🪐", 45),
    PANCHANG_TRANSIT("Panchang Macro Sentiment", "🕉️ Panchang", "🕉️", 25),
    SBC_VEDHA_TRANSIT("Sarvatobhadra Cross-Vedha", "☸️ SBC Vedha", "☸️", 15),
    MUHURTA_TRANSIT("Muhurta & Timing Window", "⏳ Timing", "⏳", 15)
}

/**
 * Visual Dashboard Gauge displaying the calculated 'Market Intensity' score
 * derived from Vedic transit analysis (Hora, Panchang, Sarvatobhadra Chakra Vedhas, and Muhurtas),
 * featuring interactive tooltips explaining each specific Vedic transit contribution.
 */
@Composable
fun MarketIntensityGaugeCard(
    asset: MarketAsset,
    trendReport: Trend24HourReport?,
    selectedPoint: TrendDataPoint?,
    sbcState: SarvatobhadraState?,
    panchang: PanchangDetails?,
    onNavigateToCycles: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(GaugeDisplayMode.ACTIVE_HOUR) }
    var isExpandedBreakdown by remember { mutableStateOf(false) }

    // Active Interactive Tooltip state explaining the specific Vedic transits
    var activeTooltip by remember { mutableStateOf<TransitTooltipType?>(null) }

    // Derive the target data point and intensity score based on display mode
    val activePoint = selectedPoint ?: trendReport?.points?.find { it.isCurrentHour } ?: trendReport?.points?.firstOrNull()

    val currentScore: Float
    val subtitleText: String
    val displayPoint: TrendDataPoint?

    when (displayMode) {
        GaugeDisplayMode.ACTIVE_HOUR -> {
            currentScore = activePoint?.intensityScore ?: 0f
            subtitleText = activePoint?.let { "${it.timeFormatted} (${it.horaPlanet.englishName} Hora)" } ?: "Live Session Hour"
            displayPoint = activePoint
        }
        GaugeDisplayMode.DAILY_AVERAGE -> {
            currentScore = trendReport?.averageIntensity ?: 0f
            subtitleText = "24-Hour Vedic Session Mean Composite"
            displayPoint = activePoint
        }
        GaugeDisplayMode.PEAK_BULLISH -> {
            val peak = trendReport?.peakBullishPoint ?: activePoint
            currentScore = peak?.intensityScore ?: 0f
            subtitleText = peak?.let { "${it.timeFormatted} • ${it.specialMarker ?: "Bullish Apex"}" } ?: "Zenith Window"
            displayPoint = peak
        }
        GaugeDisplayMode.MAX_CAUTION -> {
            val trough = trendReport?.peakBearishPoint ?: activePoint
            currentScore = trough?.intensityScore ?: 0f
            subtitleText = trough?.let { "${it.timeFormatted} • ${it.specialMarker ?: "Affliction Zone"}" } ?: "Maximum Caution Window"
            displayPoint = trough
        }
    }

    // Regime classification
    val (regimeTitle, regimeColor, regimeIcon) = when {
        currentScore >= 50f -> Triple("STRONG BULLISH IMPULSE", BullishEmeraldLight, Icons.AutoMirrored.Filled.TrendingUp)
        currentScore in 15f..49.9f -> Triple("MODERATE BULLISH ACCUMULATION", BullishEmerald, Icons.AutoMirrored.Filled.TrendingUp)
        currentScore in -14.9f..14.9f -> Triple("NEUTRAL / CONSOLIDATION RANGE", VedicGold, Icons.Default.SwapHoriz)
        currentScore in -49.9f..-15f -> Triple("MILD BEARISH PRESSURE", VolatileAmber, Icons.AutoMirrored.Filled.TrendingDown)
        else -> Triple("SEVERE AFFLICTION / SELLOFF DRAG", BearishRuby, Icons.AutoMirrored.Filled.TrendingDown)
    }

    val horaScore = displayPoint?.horaPower ?: 0f
    val panchangScore = displayPoint?.panchangFactor ?: panchang?.marketSentimentScore?.toFloat() ?: 0f
    val sbcVedhaScore = displayPoint?.sbcResonance ?: sbcState?.netVedhaScore?.toFloat() ?: 0f
    val timingMod = displayPoint?.panchangContribution ?: 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_intensity_gauge_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    regimeColor.copy(alpha = 0.7f),
                    CosmicCardBorder,
                    VedicGold.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CosmicDeepNavy,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(regimeColor)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Market Intensity Gauge",
                                tint = regimeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "MARKET INTENSITY GAUGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = VedicGoldLight,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "${asset.displayName} Vedic Transit Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Regime Pill Badge
                    Surface(
                        color = regimeColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(regimeColor.copy(alpha = 0.6f))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = regimeIcon,
                                contentDescription = null,
                                tint = regimeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = formatSignedScore(currentScore),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = regimeColor
                            )
                        }
                    }

                    // Interactive Tooltip Toggle Button
                    IconButton(
                        onClick = {
                            activeTooltip = if (activeTooltip == null) TransitTooltipType.COMPOSITE_SYNTHESIS else null
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("gauge_transit_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Inspect Vedic Transit Tooltip",
                            tint = if (activeTooltip != null) VedicGoldLight else TextSecondaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Mode Selector Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GaugeDisplayMode.entries.forEach { mode ->
                    val isSelected = displayMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { displayMode = mode },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gauge_mode_${mode.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CosmicSurfaceElevated,
                            selectedContainerColor = regimeColor.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimaryLight,
                            labelColor = TextSecondaryLight
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) regimeColor else CosmicCardBorder
                        )
                    )
                }
            }

            // Interactive Tooltip Selection Bar: One-tap transit inspection
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = CelestialCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Interactive Vedic Transit Tooltips:",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan
                        )
                    }
                    if (activeTooltip != null) {
                        Text(
                            text = "Tap active chip to close",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TransitTooltipType.entries.forEach { tooltipType ->
                        val isSelected = activeTooltip == tooltipType
                        val chipColor = when (tooltipType) {
                            TransitTooltipType.COMPOSITE_SYNTHESIS -> regimeColor
                            TransitTooltipType.HORA_TRANSIT -> if (horaScore >= 0) BullishEmerald else BearishRuby
                            TransitTooltipType.PANCHANG_TRANSIT -> if (panchangScore >= 0) CelestialCyan else BearishRubyLight
                            TransitTooltipType.SBC_VEDHA_TRANSIT -> if (sbcVedhaScore >= 0) BullishEmeraldLight else VolatileAmber
                            TransitTooltipType.MUHURTA_TRANSIT -> if (timingMod >= 0) VedicGold else BearishRuby
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    activeTooltip = if (isSelected) null else tooltipType
                                }
                                .testTag("tooltip_chip_${tooltipType.name.lowercase()}"),
                            color = if (isSelected) chipColor.copy(alpha = 0.28f) else CosmicSurfaceElevated,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) chipColor else CosmicCardBorder
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = tooltipType.icon, fontSize = 11.sp)
                                Text(
                                    text = tooltipType.shortLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimaryLight else TextSecondaryLight
                                )
                                Text(
                                    text = "(${tooltipType.weightPercent}%)",
                                    fontSize = 9.sp,
                                    color = if (isSelected) chipColor else TextMutedLight
                                )
                            }
                        }
                    }
                }
            }

            // Central Canvas Gauge Component (Clickable to trigger composite tooltip)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        activeTooltip = if (activeTooltip == null) TransitTooltipType.COMPOSITE_SYNTHESIS else null
                    }
                    .testTag("interactive_canvas_gauge_box"),
                contentAlignment = Alignment.Center
            ) {
                MarketIntensityCanvasGauge(
                    intensityScore = currentScore,
                    regimeColor = regimeColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Central Readout Overlay at Pivot Base
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatSignedScore(currentScore),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = regimeColor
                    )
                    Text(
                        text = regimeTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = TextSecondaryLight
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    // Tap Indicator
                    Surface(
                        color = CosmicDeepNavy.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, VedicGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = VedicGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = if (activeTooltip == null) "Tap gauge to inspect transits" else "Inspecting ${activeTooltip?.title}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VedicGoldLight
                            )
                        }
                    }
                }
            }

            // Interactive Tooltip Card Container: Explains the specific Vedic transits
            AnimatedVisibility(
                visible = activeTooltip != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeTooltip?.let { tooltipType ->
                    MarketIntensityTransitTooltipCard(
                        tooltipType = tooltipType,
                        asset = asset,
                        currentScore = currentScore,
                        horaScore = horaScore,
                        panchangScore = panchangScore,
                        sbcVedhaScore = sbcVedhaScore,
                        timingMod = timingMod,
                        displayPoint = displayPoint,
                        panchang = panchang,
                        sbcState = sbcState,
                        onClose = { activeTooltip = null },
                        onSelectTooltipType = { activeTooltip = it }
                    )
                }
            }

            // Dynamic Actionable Tactical Guidance
            displayPoint?.let { point ->
                Surface(
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CosmicCardBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = VedicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = point.actionableGuidance,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextPrimaryLight
                        )
                    }
                }
            }

            // Vedic Transit Decomposition Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vedic Transit Factors (Tap factor to inspect)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VedicGoldLight
                )
                Text(
                    text = "4-Factor Model",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = TextSecondaryLight
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Factor 1: Hora Lord (45% weight)
                TransitFactorMetricCard(
                    title = "Hora Lord (45%)",
                    value = formatSignedScore(horaScore),
                    detail = displayPoint?.let { "${it.horaPlanet.englishName} (${it.horaBias.title})" } ?: "Planetary Lord",
                    color = if (horaScore >= 0) BullishEmerald else BearishRuby,
                    isSelected = activeTooltip == TransitTooltipType.HORA_TRANSIT,
                    onClick = {
                        activeTooltip = if (activeTooltip == TransitTooltipType.HORA_TRANSIT) null else TransitTooltipType.HORA_TRANSIT
                    },
                    modifier = Modifier.weight(1f)
                )

                // Factor 2: Panchang Macro (25% weight)
                TransitFactorMetricCard(
                    title = "Panchang (25%)",
                    value = formatSignedScore(panchangScore),
                    detail = panchang?.let { "${it.tithiCategory.title} / ${it.nakshatraName}" } ?: "Tithi & Yoga",
                    color = if (panchangScore >= 0) CelestialCyan else BearishRubyLight,
                    isSelected = activeTooltip == TransitTooltipType.PANCHANG_TRANSIT,
                    onClick = {
                        activeTooltip = if (activeTooltip == TransitTooltipType.PANCHANG_TRANSIT) null else TransitTooltipType.PANCHANG_TRANSIT
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Factor 3: SBC Vedha Resonance (15% weight)
                val bullVedhas = sbcState?.activeVedhas?.count { it.isBenefic } ?: 0
                val bearVedhas = sbcState?.activeVedhas?.count { !it.isBenefic } ?: 0
                TransitFactorMetricCard(
                    title = "SBC Vedha (15%)",
                    value = formatSignedScore(sbcVedhaScore),
                    detail = if (sbcState != null) "$bullVedhas Bull / $bearVedhas Bear" else "Aspect Rays",
                    color = if (sbcVedhaScore >= 0) BullishEmeraldLight else VolatileAmber,
                    isSelected = activeTooltip == TransitTooltipType.SBC_VEDHA_TRANSIT,
                    onClick = {
                        activeTooltip = if (activeTooltip == TransitTooltipType.SBC_VEDHA_TRANSIT) null else TransitTooltipType.SBC_VEDHA_TRANSIT
                    },
                    modifier = Modifier.weight(1f)
                )

                // Factor 4: Muhurta & Harmonics (15% weight)
                TransitFactorMetricCard(
                    title = "Muhurta/Window (15%)",
                    value = formatSignedScore(timingMod),
                    detail = displayPoint?.specialMarker ?: "Diurnal Ingress",
                    color = if (timingMod >= 0) VedicGold else BearishRuby,
                    isSelected = activeTooltip == TransitTooltipType.MUHURTA_TRANSIT,
                    onClick = {
                        activeTooltip = if (activeTooltip == TransitTooltipType.MUHURTA_TRANSIT) null else TransitTooltipType.MUHURTA_TRANSIT
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable Vedic Formula Inspector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpandedBreakdown = !isExpandedBreakdown },
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CosmicCardBorder))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = CelestialCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "How Vedic Market Intensity is Calculated",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CelestialCyan
                            )
                        }
                        Icon(
                            imageVector = if (isExpandedBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Formula",
                            tint = TextSecondaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpandedBreakdown) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Market Intensity Score Formula:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Surface(
                                color = CosmicDeepNavy,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Intensity = (HoraPower × 0.45) + (PanchangScore × 0.25) + (SbcVedha × 0.15) + TimingWindows + AssetAffinity + DiurnalHarmonics",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = VedicGold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Text(
                                text = "• Hora Planetary Power (45%): Calibrated from active hora lord nature, combustion, and exaltation state.\n" +
                                       "• Panchang Macro Sentiment (25%): Tithi category (Nanda, Jaya, Bhadra, Purna, Rikta), Nakshatra nature, and Karana auspiciousness.\n" +
                                       "• Sarvatobhadra Net Vedha (15%): Direct cross-vedhas cast by transiting benefics (Jupiter, Venus, Mercury) vs malefics (Saturn, Mars, Rahu, Ketu).\n" +
                                       "• Timing Modifiers: Abhijit Muhurta Zenith (+38 pts), Rahu Kaal Shadow (-36 pts), and Yamaganda (-22 pts).\n" +
                                       "• Score bounds: -100 (Maximum Bearish Affliction) to +100 (Maximum Bullish Impulse).",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = TextSecondaryLight,
                                lineHeight = 15.sp
                            )

                            if (onNavigateToCycles != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = onNavigateToCycles,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CelestialCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explore 24-Hour Cycle Timeline & Recharts", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Tooltip Inspector Card explaining the specific Vedic transit
 * contributing to the current Market Intensity score.
 */
@Composable
private fun MarketIntensityTransitTooltipCard(
    tooltipType: TransitTooltipType,
    asset: MarketAsset,
    currentScore: Float,
    horaScore: Float,
    panchangScore: Float,
    sbcVedhaScore: Float,
    timingMod: Float,
    displayPoint: TrendDataPoint?,
    panchang: PanchangDetails?,
    sbcState: SarvatobhadraState?,
    onClose: () -> Unit,
    onSelectTooltipType: (TransitTooltipType) -> Unit,
    modifier: Modifier = Modifier
) {
    val factorColor = when (tooltipType) {
        TransitTooltipType.COMPOSITE_SYNTHESIS -> if (currentScore >= 0) BullishEmeraldLight else BearishRuby
        TransitTooltipType.HORA_TRANSIT -> if (horaScore >= 0) BullishEmerald else BearishRuby
        TransitTooltipType.PANCHANG_TRANSIT -> if (panchangScore >= 0) CelestialCyan else BearishRubyLight
        TransitTooltipType.SBC_VEDHA_TRANSIT -> if (sbcVedhaScore >= 0) BullishEmeraldLight else VolatileAmber
        TransitTooltipType.MUHURTA_TRANSIT -> if (timingMod >= 0) VedicGold else BearishRuby
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_intensity_transit_tooltip_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                listOf(factorColor, VedicGoldLight.copy(alpha = 0.8f), CosmicCardBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tooltip Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = tooltipType.icon, fontSize = 20.sp)
                    Column {
                        Text(
                            text = "VEDIC TRANSIT EXPLANATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = VedicGoldLight,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = tooltipType.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tooltip",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Contribution Badge
            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, factorColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weight: ${tooltipType.weightPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = when (tooltipType) {
                            TransitTooltipType.COMPOSITE_SYNTHESIS -> "Net Total: ${formatSignedScore(currentScore)} pts"
                            TransitTooltipType.HORA_TRANSIT -> "Score: ${formatSignedScore(horaScore)} pts (Net: ${formatSignedScore(horaScore * 0.45f)})"
                            TransitTooltipType.PANCHANG_TRANSIT -> "Score: ${formatSignedScore(panchangScore)} pts (Net: ${formatSignedScore(panchangScore * 0.25f)})"
                            TransitTooltipType.SBC_VEDHA_TRANSIT -> "Score: ${formatSignedScore(sbcVedhaScore)} pts (Net: ${formatSignedScore(sbcVedhaScore * 0.15f)})"
                            TransitTooltipType.MUHURTA_TRANSIT -> "Modifier: ${formatSignedScore(timingMod)} pts (Net: ${formatSignedScore(timingMod * 0.15f)})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = factorColor
                    )
                }
            }

            // Transit Specific Explanation Content
            when (tooltipType) {
                TransitTooltipType.COMPOSITE_SYNTHESIS -> {
                    CompositeSynthesisExplanation(
                        asset = asset,
                        currentScore = currentScore,
                        horaScore = horaScore,
                        panchangScore = panchangScore,
                        sbcVedhaScore = sbcVedhaScore,
                        timingMod = timingMod,
                        displayPoint = displayPoint
                    )
                }
                TransitTooltipType.HORA_TRANSIT -> {
                    HoraTransitExplanation(
                        asset = asset,
                        displayPoint = displayPoint,
                        horaScore = horaScore
                    )
                }
                TransitTooltipType.PANCHANG_TRANSIT -> {
                    PanchangTransitExplanation(
                        displayPoint = displayPoint,
                        panchang = panchang,
                        panchangScore = panchangScore
                    )
                }
                TransitTooltipType.SBC_VEDHA_TRANSIT -> {
                    SbcVedhaTransitExplanation(
                        sbcState = sbcState,
                        displayPoint = displayPoint,
                        sbcVedhaScore = sbcVedhaScore
                    )
                }
                TransitTooltipType.MUHURTA_TRANSIT -> {
                    MuhurtaTransitExplanation(
                        displayPoint = displayPoint,
                        timingMod = timingMod
                    )
                }
            }

            // Interactive Switcher Bar at the bottom of the tooltip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Switch Transit Tooltip:",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = TextSecondaryLight
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TransitTooltipType.entries.forEach { type ->
                        if (type != tooltipType) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onSelectTooltipType(type) },
                                color = CosmicDeepNavy,
                                border = BorderStroke(0.5.dp, CosmicCardBorder)
                            ) {
                                Text(
                                    text = type.icon,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompositeSynthesisExplanation(
    asset: MarketAsset,
    currentScore: Float,
    horaScore: Float,
    panchangScore: Float,
    sbcVedhaScore: Float,
    timingMod: Float,
    displayPoint: TrendDataPoint?
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Mathematical Transit Formulation:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VedicGoldLight
        )
        Text(
            text = "The needle position is calculated via dynamic Vedic multi-factor regression, synthesizing real-time ephemeris transits onto ${asset.displayName}:",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextSecondaryLight
        )

        // Math Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CosmicDeepNavy, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            FormulaRow(
                factor = "Hora Lord Impact (45%)",
                calc = "${formatSignedScore(horaScore)} × 0.45",
                net = formatSignedScore(horaScore * 0.45f),
                color = if (horaScore >= 0) BullishEmerald else BearishRuby
            )
            FormulaRow(
                factor = "Panchang Macro (25%)",
                calc = "${formatSignedScore(panchangScore)} × 0.25",
                net = formatSignedScore(panchangScore * 0.25f),
                color = if (panchangScore >= 0) CelestialCyan else BearishRubyLight
            )
            FormulaRow(
                factor = "Sarvatobhadra Vedha (15%)",
                calc = "${formatSignedScore(sbcVedhaScore)} × 0.15",
                net = formatSignedScore(sbcVedhaScore * 0.15f),
                color = if (sbcVedhaScore >= 0) BullishEmeraldLight else VolatileAmber
            )
            FormulaRow(
                factor = "Muhurta & Harmonic (15%)",
                calc = "${formatSignedScore(timingMod)} × 0.15",
                net = formatSignedScore(timingMod * 0.15f),
                color = if (timingMod >= 0) VedicGold else BearishRuby
            )
            HorizontalDivider(color = CosmicCardBorder, modifier = Modifier.padding(vertical = 2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Gauge Score:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "${formatSignedScore(currentScore)} pts",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (currentScore >= 0) BullishEmeraldLight else BearishRuby
                )
            }
        }

        displayPoint?.let {
            Text(
                text = "⚡ Primary Transit Driver: ${it.dominantInfluence}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CelestialCyan
            )
        }
    }
}

@Composable
private fun FormulaRow(factor: String, calc: String, net: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = factor, fontSize = 10.sp, color = TextSecondaryLight)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = calc, fontSize = 10.sp, color = TextMutedLight)
            Text(text = "= $net pts", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun HoraTransitExplanation(
    asset: MarketAsset,
    displayPoint: TrendDataPoint?,
    horaScore: Float
) {
    val planet = displayPoint?.horaPlanet ?: Planet.JUPITER
    val bias = displayPoint?.horaBias ?: HoraMarketBias.STRONG_BULLISH

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "${planet.symbol} ${planet.englishName} (${planet.sanskritName}) Planetary Ingress",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = planet.displayColor
        )

        Text(
            text = "• Planetary Mandate: ${planet.marketRole}\n" +
                   "• Active Bias: ${bias.title}\n" +
                   "• Elemental Tatwa: ${displayPoint?.horaElement ?: "Akasha (Ether)"} — dictates market fluid momentum\n" +
                   "• Dhatu / Metal: ${displayPoint?.horaDhatu ?: "Gold & Copper"}\n" +
                   "• Asset Affinity: ${if (displayPoint?.isAssetRulerResonance == true) "★ Strong Planetary Ruler Affinity with ${asset.displayName} (+15 pts bonus)" else "Neutral baseline affinity"}",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextPrimaryLight,
            lineHeight = 16.sp
        )

        displayPoint?.horaInfluenceSummary?.takeIf { it.isNotBlank() }?.let { summary ->
            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Astrological Rationale: $summary",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = VedicGoldLight,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        displayPoint?.horaTradingTip?.takeIf { it.isNotBlank() }?.let { tip ->
            Text(
                text = "Tactical Playbook: $tip",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CelestialCyan
            )
        }
    }
}

@Composable
private fun PanchangTransitExplanation(
    displayPoint: TrendDataPoint?,
    panchang: PanchangDetails?,
    panchangScore: Float
) {
    val tithiName = displayPoint?.tithiName?.takeIf { it.isNotBlank() } ?: panchang?.tithiName ?: "Shukla Dashami"
    val tithiCat = displayPoint?.tithiCategory ?: panchang?.tithiCategory ?: TithiCategory.JAYA
    val nakshatraName = displayPoint?.nakshatraName?.takeIf { it.isNotBlank() } ?: panchang?.nakshatraName ?: "Pushya"
    val yogaName = displayPoint?.yogaName?.takeIf { it.isNotBlank() } ?: panchang?.yogaName ?: "Siddha"
    val karanaName = displayPoint?.karanaName?.takeIf { it.isNotBlank() } ?: panchang?.karanaName ?: "Bava"

    val isTithiPositive = tithiCat == TithiCategory.NANDA || tithiCat == TithiCategory.JAYA || tithiCat == TithiCategory.PURNA

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Macro Panchang Lunar-Solar Alignment",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CelestialCyan
        )

        Text(
            text = "• Tithi: $tithiName (${tithiCat.title} Category) — ${tithiCat.financialSignificance} (${if (isTithiPositive) "+16 pts institutional confidence" else "-18 pts risk penalty"})\n" +
                   "• Nakshatra: $nakshatraName (${displayPoint?.nakshatraNature?.title ?: "Fixed/Sthira"} Nature, Lord: ${displayPoint?.nakshatraLord?.englishName ?: "Saturn"})\n" +
                   "• Yoga: $yogaName — governs execution liquidity and algorithmic trend consistency\n" +
                   "• Karana: $karanaName (${if (displayPoint?.isVishtiKarana == true) "⚠ Vishti/Bhadra Karana Active: Whipsaw & liquidation risk" else "Auspicious commercial flow"})",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextPrimaryLight,
            lineHeight = 16.sp
        )

        displayPoint?.panchangInfluenceSummary?.takeIf { it.isNotBlank() }?.let { summary ->
            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Vedic Principle: $summary",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = VedicGoldLight,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun SbcVedhaTransitExplanation(
    sbcState: SarvatobhadraState?,
    displayPoint: TrendDataPoint?,
    sbcVedhaScore: Float
) {
    val bullVedhas = sbcState?.activeVedhas?.count { it.isBenefic } ?: 0
    val bearVedhas = sbcState?.activeVedhas?.count { !it.isBenefic } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "9x9 Sarvatobhadra Matrix Cross-Vedha Rays",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = BullishEmeraldLight
        )

        Text(
            text = "• Net Vedha Score: ${formatSignedScore(sbcVedhaScore)} pts (${bullVedhas} Bullish Rays vs ${bearVedhas} Bearish Rays)\n" +
                   "• Benefic Rays: Jupiter, Venus, Mercury cast supportive front (Samukha) and diagonal aspects across sensitive index nakshatras.\n" +
                   "• Malefic Affliction: Saturn, Mars, Rahu, and Ketu rays exert directional drag on affected sectors.\n" +
                   "• Vedha Dynamics: In classical astrology (Phaladeepika & Narapati Jayacharya), cross-vedhas act as invisible support/resistance thresholds where market intensity reverses.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextPrimaryLight,
            lineHeight = 16.sp
        )

        sbcState?.primaryBeneficInfluence?.takeIf { it.isNotBlank() }?.let { benefic ->
            Text(
                text = "✓ Key Benefic Ray: $benefic",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = BullishEmerald
            )
        }

        sbcState?.primaryMaleficPressure?.takeIf { it.isNotBlank() }?.let { malefic ->
            Text(
                text = "⚠ Key Malefic Pressure: $malefic",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = BearishRuby
            )
        }
    }
}

@Composable
private fun MuhurtaTransitExplanation(
    displayPoint: TrendDataPoint?,
    timingMod: Float
) {
    val marker = displayPoint?.specialMarker ?: "Diurnal Ingress Cycle"
    val markerType = displayPoint?.specialMarkerType ?: TrendMarkerType.NONE

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Muhurta Window & Diurnal Harmonics",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VedicGoldLight
        )

        Text(
            text = when (markerType) {
                TrendMarkerType.ABHIJIT_MUHURTA ->
                    "★ Abhijit Muhurta Active (+38 pts boost): Midday solar culmination (8th Muhurta). Classically presided by Lord Vishnu; eliminates all intraday Doshas and attracts high institutional buy-side order flow."
                TrendMarkerType.RAHU_KAAL ->
                    "⚠ Rahu Kaal Shadow Window (-36 pts penalty): Auspicious solar rays obscured by Rahu node. Creates phantom liquidity, sudden stop-loss hunting, and deceptive breakout failures."
                TrendMarkerType.YAMAGANDA ->
                    "⚠ Yamaganda Drag (-22 pts penalty): Jupiter's afflicted sub-period. Induces profit-taking and structural institutional rebalancing."
                TrendMarkerType.MARKET_OPEN ->
                    "▲ Opening Bell Ingress (+15 pts volatility surge): Prana inflection window setting the morning price discovery vector."
                TrendMarkerType.MARKET_CLOSE ->
                    "▼ Closing Bell Settlement: Intraday square-off window harmonizing diurnal cycle balances."
                else ->
                    "Diurnal Harmonic Ingress: Natural solar-lunar sine curve oscillating throughout the 24-hour cycle."
            },
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextPrimaryLight,
            lineHeight = 16.sp
        )

        displayPoint?.timingWindowImpact?.takeIf { it.isNotBlank() }?.let { impact ->
            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Window Impact: $impact",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = VedicGold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas Gauge rendering the 240-degree dial,
 * multi-tier color zones, glowing needle, tick marks, and calibrated values.
 */
@Composable
fun MarketIntensityCanvasGauge(
    intensityScore: Float,
    regimeColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Smooth spring animation for needle motion
    val animatedScore by animateFloatAsState(
        targetValue = intensityScore.coerceIn(-100f, 100f),
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "GaugeNeedleAnimation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Dial geometry: Center pivot positioned near the lower center
        val centerX = width / 2f
        val centerY = height * 0.76f
        val radius = min(width * 0.42f, height * 0.70f)
        val strokeWidth = 14.dp.toPx()

        // 240-degree sweep from 150° (bottom-left) to 390° (bottom-right)
        val startAngle = 150f
        val totalSweep = 240f

        val arcTopLeft = Offset(centerX - radius, centerY - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        // 1. Draw Outer Subtle Background Halo Track
        drawArc(
            color = CosmicSurfaceElevated.copy(alpha = 0.6f),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // 2. Draw 5 Colored Regime Zone Segments along the Arc
        // Zone 1: -100 to -50 (sweep 60 deg, from 150° to 210°) -> Severe Affliction / Dark Ruby
        drawArc(
            color = BearishRubyDark,
            startAngle = 150f,
            sweepAngle = 58f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Zone 2: -50 to -15 (sweep 42 deg, from 210° to 252°) -> Mild Bearish Drag / Soft Ruby
        drawArc(
            color = BearishRuby,
            startAngle = 210f,
            sweepAngle = 40f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Zone 3: -15 to +15 (sweep 36 deg, from 252° to 288°) -> Neutral / Gold Amber (Top Dead Center is 270°)
        drawArc(
            color = VedicGold,
            startAngle = 252f,
            sweepAngle = 34f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Zone 4: +15 to +50 (sweep 42 deg, from 288° to 330°) -> Bullish Accumulation / Cyan Emerald
        drawArc(
            color = CelestialCyan,
            startAngle = 288f,
            sweepAngle = 40f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Zone 5: +50 to +100 (sweep 60 deg, from 330° to 390°) -> Strong Bullish Impulse / Bright Emerald
        drawArc(
            color = BullishEmeraldLight,
            startAngle = 330f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 3. Draw Active Glow Arc from 270° (0 Neutral) to Current Score Angle
        val normalized = (animatedScore + 100f) / 200f
        val needleAngle = startAngle + normalized * totalSweep
        val zeroAngle = 270f
        val activeSweep = needleAngle - zeroAngle

        if (abs(activeSweep) > 1f) {
            drawArc(
                color = regimeColor.copy(alpha = 0.55f),
                startAngle = if (activeSweep >= 0) zeroAngle else needleAngle,
                sweepAngle = abs(activeSweep),
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 4. Tick Marks and Scale Labels
        val majorTicks = listOf(
            -100 to "-100",
            -75 to null,
            -50 to "-50",
            -25 to null,
            0 to "0",
            25 to null,
            50 to "+50",
            75 to null,
            100 to "+100"
        )

        val tickInnerRadius = radius - strokeWidth / 2f - 4.dp.toPx()
        val tickMajorOuterRadius = radius + strokeWidth / 2f + 6.dp.toPx()
        val tickMinorOuterRadius = radius + strokeWidth / 2f + 2.dp.toPx()
        val labelRadius = radius - strokeWidth / 2f - 16.dp.toPx()

        majorTicks.forEach { (score, label) ->
            val tickNorm = (score + 100f) / 200f
            val tickAngle = startAngle + tickNorm * totalSweep
            val rad = Math.toRadians(tickAngle.toDouble())
            val cosA = cos(rad).toFloat()
            val sinA = sin(rad).toFloat()

            val isMajor = label != null
            val outerR = if (isMajor) tickMajorOuterRadius else tickMinorOuterRadius
            val tickColor = if (isMajor) TextPrimaryLight.copy(alpha = 0.8f) else TextSecondaryLight.copy(alpha = 0.4f)
            val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

            drawLine(
                color = tickColor,
                start = Offset(centerX + tickInnerRadius * cosA, centerY + tickInnerRadius * sinA),
                end = Offset(centerX + outerR * cosA, centerY + outerR * sinA),
                strokeWidth = tickWidth,
                cap = StrokeCap.Round
            )

            // Draw Scale Text Label using Compose TextMeasurer
            if (label != null) {
                val labelLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryLight
                    )
                )
                val labelX = centerX + labelRadius * cosA - labelLayout.size.width / 2f
                val labelY = centerY + labelRadius * sinA - labelLayout.size.height / 2f
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(labelX, labelY)
                )
            }
        }

        // Qualitative Zone Text Hints along the perimeter
        drawQualitativeLabel(textMeasurer, "BEAR", startAngle + 0.15f * totalSweep, radius + 14.dp.toPx(), centerX, centerY, BearishRubyLight)
        drawQualitativeLabel(textMeasurer, "NEUTRAL", 270f, radius + 14.dp.toPx(), centerX, centerY, VedicGoldLight)
        drawQualitativeLabel(textMeasurer, "BULL", startAngle + 0.85f * totalSweep, radius + 14.dp.toPx(), centerX, centerY, BullishEmeraldLight)

        // 5. Draw the Glowing Needle
        val needleRad = Math.toRadians(needleAngle.toDouble())
        val cosN = cos(needleRad).toFloat()
        val sinN = sin(needleRad).toFloat()
        val perpCos = -sinN
        val perpSin = cosN

        val needleLength = radius * 0.88f
        val needleBaseWidth = 6.dp.toPx()
        val needleTailLength = 16.dp.toPx()

        val tipPoint = Offset(centerX + needleLength * cosN, centerY + needleLength * sinN)
        val rightBase = Offset(centerX + needleBaseWidth * perpCos, centerY + needleBaseWidth * perpSin)
        val leftBase = Offset(centerX - needleBaseWidth * perpCos, centerY - needleBaseWidth * perpSin)
        val tailPoint = Offset(centerX - needleTailLength * cosN, centerY - needleTailLength * sinN)

        val needlePath = Path().apply {
            moveTo(tipPoint.x, tipPoint.y)
            lineTo(rightBase.x, rightBase.y)
            lineTo(tailPoint.x, tailPoint.y)
            lineTo(leftBase.x, leftBase.y)
            close()
        }

        // Draw Needle Soft Shadow
        drawPath(
            path = needlePath,
            color = Color.Black.copy(alpha = 0.45f)
        )

        // Draw Needle Body with Gradient
        drawPath(
            path = needlePath,
            brush = Brush.linearGradient(
                colors = listOf(regimeColor, Color.White, regimeColor.copy(alpha = 0.8f)),
                start = tailPoint,
                end = tipPoint
            )
        )

        // Needle Tip Glow Dot
        drawCircle(
            color = regimeColor,
            radius = 3.dp.toPx(),
            center = tipPoint
        )

        // 6. Draw Multi-Tier Metallic Pivot Center Cap
        // Outer metallic rim
        drawCircle(
            brush = Brush.radialGradient(
                listOf(VedicGold, CosmicDeepNavy),
                center = Offset(centerX, centerY),
                radius = 16.dp.toPx()
            ),
            radius = 14.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Inner cosmic disc
        drawCircle(
            color = CosmicDeepNavy,
            radius = 9.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Center jewel matched to current regime color
        drawCircle(
            color = regimeColor,
            radius = 4.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

private fun DrawScope.drawQualitativeLabel(
    textMeasurer: TextMeasurer,
    text: String,
    angleDeg: Float,
    labelRadius: Float,
    centerX: Float,
    centerY: Float,
    color: Color
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color.copy(alpha = 0.85f),
            letterSpacing = 0.5.sp
        )
    )

    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            centerX + labelRadius * cosA - layout.size.width / 2f,
            centerY + labelRadius * sinA - layout.size.height / 2f
        )
    )
}

@Composable
private fun TransitFactorMetricCard(
    title: String,
    value: String,
    detail: String,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("transit_factor_card_${title.take(6).lowercase().trim()}"),
        color = if (isSelected) color.copy(alpha = 0.18f) else CosmicDeepNavy,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else color.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isSelected) TextPrimaryLight else TextSecondaryLight
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Inspect $title",
                    tint = if (isSelected) color else TextMutedLight,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                maxLines = 1,
                color = TextPrimaryLight
            )
        }
    }
}

private fun formatSignedScore(score: Float): String {
    return if (score > 0) "+${String.format("%.1f", score)}" else String.format("%.1f", score)
}
