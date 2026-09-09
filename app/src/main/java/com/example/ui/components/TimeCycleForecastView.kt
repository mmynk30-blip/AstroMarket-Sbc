package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TimeCycleForecastView(
    prediction: DailyMarketPrediction,
    trendReport: Trend24HourReport? = null,
    selectedTrendPoint: TrendDataPoint? = null,
    sbcState: SarvatobhadraState? = null,
    panchang: PanchangDetails? = null,
    onSelectTrendPoint: (TrendDataPoint) -> Unit = {},
    isRolling24Hours: Boolean = false,
    onToggleRolling: (Boolean) -> Unit = {},
    onNavigateToOutlook: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Macro Stance & Executive Summary Card
        PredictionExecutiveBanner(prediction = prediction)

        // Visual Dashboard Gauge: Calculated Market Intensity derived from Vedic Transit Analysis
        MarketIntensityGaugeCard(
            asset = prediction.asset,
            trendReport = trendReport,
            selectedPoint = selectedTrendPoint,
            sbcState = sbcState,
            panchang = panchang,
            onNavigateToCycles = null
        )

        // Gemini AI Sentiment Shortcut Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToOutlook() }
                .testTag("gemini_outlook_shortcut_card"),
            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(VedicGold.copy(alpha = 0.8f), CelestialCyan.copy(alpha = 0.8f)))
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = CosmicDeepNavy,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(VedicGold)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VedicGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Gemini Daily Outlook Dashboard",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = VedicGoldLight
                        )
                        Text(
                            text = "Synthesizing calculated planetary ephemeris via Firebase GenAI SDK",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View Outlook",
                    tint = CelestialCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 24-Hour Predicted Trend Intensity Recharts Component
        trendReport?.let { report ->
            RechartsTrendIntensityChart(
                report = report,
                selectedPoint = selectedTrendPoint,
                onSelectPoint = onSelectTrendPoint,
                isRolling24Hours = isRolling24Hours,
                onToggleRolling = onToggleRolling
            )
        }

        // Intraday Harmonic Turning Points
        TurningPointsRow(reversalTimes = prediction.keyReversalTimes)

        // Intraday Time Cycles List
        Text(
            text = "Intraday Astro Time Cycles & Harmonic Turns",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )

        prediction.timeCycles.forEach { cycle ->
            TimeCycleCard(cycle = cycle)
        }

        // Astro Harmonic Levels (Support & Resistance)
        AstroLevelsCard(
            support = prediction.astroSupportPoints,
            resistance = prediction.astroResistancePoints
        )

        // Trading Strategy Playbook
        StrategyPlaybookCard(strategy = prediction.tradingStrategyAdvice)

        // Sector Rotation Astro Matrix
        SectorRotationMatrixCard(sectors = prediction.sectorRankings)
    }
}

@Composable
private fun PredictionExecutiveBanner(prediction: DailyMarketPrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(prediction.overallBias.badgeColorHex))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${prediction.asset.displayName} (${prediction.asset.region})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                    Text(
                        text = "Vedic Time Cycle & SBC Forecast",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                Surface(
                    color = Color(prediction.overallBias.badgeColorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = prediction.overallBias.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(prediction.overallBias.badgeColorHex)
                    )
                }
            }

            // Confidence & VIX Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Astro Confidence", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${prediction.astroConfidenceScore}% Match",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BullishEmeraldLight
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1.5f),
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Astro-VIX Expectation", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prediction.expectedVixTrend,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan
                        )
                    }
                }
            }

            // Narrative summary
            Text(
                text = prediction.summaryNarrative,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimaryLight
            )
        }
    }
}

@Composable
private fun TurningPointsRow(reversalTimes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = VedicGold, modifier = Modifier.size(18.dp))
                Text(
                    text = "Astro Harmonic Turning Points",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            val scroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reversalTimes.forEach { time ->
                    Surface(
                        color = CosmicDeepNavy,
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold.copy(alpha = 0.5f)))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = VedicGoldLight
                            )
                            Text(
                                text = "Pivot Alert",
                                fontSize = 9.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeCycleCard(cycle: MarketTimeCycle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                Color(cycle.predictedDirection.badgeColorHex).copy(alpha = 0.5f)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CosmicDeepNavy,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = cycle.timeRange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VedicGoldLight
                        )
                    }
                    Text(
                        text = cycle.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Surface(
                    color = Color(cycle.predictedDirection.badgeColorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = cycle.predictedDirection.title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(cycle.predictedDirection.badgeColorHex)
                    )
                }
            }

            // Turning point pill if available
            cycle.potentialTurningPoint?.let { tp ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = CelestialCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Peak Pivot expected near $tp",
                        style = MaterialTheme.typography.labelSmall,
                        color = CelestialCyan
                    )
                }
            }

            Text(
                text = "Driver: ${cycle.astrologicalDriver}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )

            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = BullishEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cycle.actionableGuidance,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun AstroLevelsCard(support: String, resistance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.LineAxis, contentDescription = null, tint = VedicGold, modifier = Modifier.size(18.dp))
                Text(
                    text = "Astro Harmonic S/R Levels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BearishRuby.copy(alpha = 0.3f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = BearishRuby, modifier = Modifier.size(16.dp))
                    Column {
                        Text(text = "Astro Resistance", style = MaterialTheme.typography.labelSmall, color = BearishRubyLight)
                        Text(text = resistance, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                    }
                }
            }

            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BullishEmerald.copy(alpha = 0.3f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = BullishEmerald, modifier = Modifier.size(16.dp))
                    Column {
                        Text(text = "Astro Support", style = MaterialTheme.typography.labelSmall, color = BullishEmeraldLight)
                        Text(text = support, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyPlaybookCard(strategy: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = VedicGold, modifier = Modifier.size(20.dp))
                Text(
                    text = "Vedic Trading Strategy Playbook",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VedicGold
                )
            }

            Text(
                text = strategy,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimaryLight
            )
        }
    }
}

@Composable
private fun SectorRotationMatrixCard(sectors: List<SectorPerformance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = CelestialCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "Sector Rotation Astro Matrix",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            sectors.forEach { sec ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = sec.rulingPlanet.symbol, color = sec.rulingPlanet.displayColor, fontSize = 14.sp)
                                Text(text = sec.sectorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                            }

                            Text(
                                text = sec.trend,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sec.sentimentScore >= 0) BullishEmeraldLight else BearishRubyLight
                            )
                        }

                        LinearProgressIndicator(
                            progress = { ((sec.sentimentScore + 100) / 200f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (sec.sentimentScore >= 0) BullishEmerald else BearishRuby,
                            trackColor = CosmicSurface
                        )

                        Text(
                            text = sec.vedhaStatus,
                            fontSize = 9.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }
        }
    }
}
