package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyOutlookDashboard(
    outlookState: GeminiOutlookUiState,
    selectedAsset: MarketAsset,
    selectedDateString: String,
    onRefreshOutlook: () -> Unit,
    trendReport: Trend24HourReport? = null,
    selectedTrendPoint: TrendDataPoint? = null,
    sbcState: SarvatobhadraState? = null,
    panchangDetails: PanchangDetails? = null,
    onNavigateToCycles: (() -> Unit)? = null,
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
        // Top Header Banner
        OutlookHeaderBanner(
            asset = selectedAsset,
            dateString = selectedDateString,
            isLoading = outlookState is GeminiOutlookUiState.Loading,
            onRefresh = onRefreshOutlook
        )

        // Visual Dashboard Gauge: Calculated Market Intensity derived from Vedic Transit Analysis
        MarketIntensityGaugeCard(
            asset = selectedAsset,
            trendReport = trendReport,
            selectedPoint = selectedTrendPoint,
            sbcState = sbcState,
            panchang = panchangDetails,
            onNavigateToCycles = onNavigateToCycles
        )

        when (outlookState) {
            is GeminiOutlookUiState.Loading -> {
                OutlookLoadingState()
            }
            is GeminiOutlookUiState.Error -> {
                OutlookErrorCard(
                    errorMessage = outlookState.message,
                    onRetry = onRefreshOutlook
                )
                // If there's a fallback outlook, display it below the notice
                outlookState.fallbackOutlook?.let { fallback ->
                    OutlookContentSections(outlook = fallback, isFallback = true)
                }
            }
            is GeminiOutlookUiState.Success -> {
                OutlookContentSections(outlook = outlookState.outlook, isFallback = false)
            }
            is GeminiOutlookUiState.Idle -> {
                OutlookLoadingState()
            }
        }
    }
}

@Composable
private fun OutlookHeaderBanner(
    asset: MarketAsset,
    dateString: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_outlook_header_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(VedicGold, CelestialCyan))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VedicGold
                    )
                    Text(
                        text = "GEMINI DAILY OUTLOOK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VedicGoldLight,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${asset.displayName} Market Sentiment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            // Refresh / Fetch Button
            FilledTonalIconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("refresh_gemini_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = CosmicSurface,
                    contentColor = VedicGold
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = VedicGold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Outlook"
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlookContentSections(
    outlook: GeminiDailyOutlook,
    isFallback: Boolean
) {
    var showContextDetails by remember { mutableStateOf(false) }

    // 1. Core Sentiment & Confidence Hero Card
    SentimentHeroCard(outlook = outlook, isFallback = isFallback)

    // 2. Executive Summary Card
    ExecutiveSummaryCard(summary = outlook.executiveSummary)

    // 3. Planetary Catalysts
    PlanetaryCatalystsSection(catalysts = outlook.planetaryCatalysts)

    // 4. Sector Sentiment Posture
    SectorSentimentSection(sectors = outlook.sectorRankings)

    // 5. Intraday Astrological Trading Windows
    TradingWindowsSection(windows = outlook.tradingWindows)

    // 6. Actionable Strategy & Playbook
    ActionableStrategyCard(strategy = outlook.actionableStrategy)

    // 7. Expandable Planetary Context Payload Inspector
    AstrologicalContextInspectorCard(
        contextSnapshot = outlook.astrologicalContextSnapshot,
        expanded = showContextDetails,
        onToggle = { showContextDetails = !showContextDetails }
    )
}

@Composable
private fun SentimentHeroCard(
    outlook: GeminiDailyOutlook,
    isFallback: Boolean
) {
    val sentimentColor = Color(outlook.sentiment.badgeColorHex)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    val generatedTime = timeFormat.format(Date(outlook.generatedAtTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sentiment_hero_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(sentimentColor.copy(alpha = 0.8f), CosmicSurfaceElevated))
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sentiment Badge
                Surface(
                    color = sentimentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(sentimentColor.copy(alpha = 0.6f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val icon = when (outlook.sentiment) {
                            MarketSentimentType.BULLISH, MarketSentimentType.MODERATE_BULLISH -> Icons.AutoMirrored.Filled.TrendingUp
                            MarketSentimentType.BEARISH -> Icons.AutoMirrored.Filled.TrendingDown
                            MarketSentimentType.VOLATILE_CAUTION -> Icons.Default.Warning
                            MarketSentimentType.NEUTRAL_CONSOLIDATING -> Icons.Default.SwapHoriz
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = sentimentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = outlook.sentiment.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = sentimentColor
                        )
                    }
                }

                // Confidence Gauge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VedicGold
                    )
                    Text(
                        text = "${outlook.confidencePercentage}% Conviction",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
            }

            // Confidence progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { outlook.confidencePercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = sentimentColor,
                    trackColor = CosmicDeepNavy
                )
            }

            // Model & Timestamp Tag
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
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CelestialCyan
                    )
                    Text(
                        text = outlook.modelTag,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = CelestialCyan
                    )
                }

                Text(
                    text = "Generated at $generatedTime",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

@Composable
private fun ExecutiveSummaryCard(summary: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("executive_summary_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = VedicGold.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VedicGold
                        )
                    }
                }
                Text(
                    text = "Executive Market Synthesis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryLight.copy(alpha = 0.9f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PlanetaryCatalystsSection(catalysts: List<PlanetaryCatalyst>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = VedicGoldLight
            )
            Text(
                text = "Key Planetary Catalysts & Astrological Drivers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
        }

        catalysts.forEach { catalyst ->
            val biasColor = when (catalyst.sentimentBias.lowercase(Locale.US)) {
                "bullish", "positive" -> Color(0xFF10B981)
                "defensive", "bearish" -> Color(0xFFEF4444)
                else -> Color(0xFFF97316)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalyst_card_${catalyst.planetName.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Planet Symbol Avatar
                    Surface(
                        color = CosmicDeepNavy,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(biasColor)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = catalyst.symbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = biasColor
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${catalyst.planetName} (${catalyst.role})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            Surface(
                                color = biasColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = catalyst.sentimentBias,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = biasColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = catalyst.impactAnalysis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorSentimentSection(sectors: List<SectorSentimentOutlook>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sectors_sentiment_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = CelestialCyan
                )
                Text(
                    text = "Sector Rotation & Planetary Alignment",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            sectors.forEach { sector ->
                val isFavored = sector.stance.contains("Favored", ignoreCase = true) || sector.stance.contains("Accumulate", ignoreCase = true)
                val isCautious = sector.stance.contains("Pressure", ignoreCase = true) || sector.stance.contains("Hedge", ignoreCase = true) || sector.stance.contains("Avoid", ignoreCase = true)
                val statusColor = if (isFavored) Color(0xFF10B981) else if (isCautious) Color(0xFFEF4444) else Color(0xFF94A3B8)

                Surface(
                    color = CosmicSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sector.sectorName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            Text(
                                text = sector.stance,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Governing: ${sector.planetaryDriver}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = VedicGoldLight
                            )

                            Text(
                                text = sector.guidance,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
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
private fun TradingWindowsSection(windows: List<AstroTradingWindow>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trading_windows_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = VedicGold
                )
                Text(
                    text = "Key Intraday Astrological Timing Windows",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            windows.forEach { window ->
                val badgeColor = if (window.isCaution) Color(0xFFF97316) else VedicGold

                Surface(
                    color = CosmicSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(badgeColor.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (window.isCaution) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = window.windowName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )

                                Text(
                                    text = window.timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }

                            Text(
                                text = window.expectedImpact,
                                style = MaterialTheme.typography.bodySmall,
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
private fun ActionableStrategyCard(strategy: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("actionable_strategy_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(VedicGold.copy(alpha = 0.6f))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = VedicGold
                )
                Text(
                    text = "Tactical Trading Rules & Actionable Posture",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VedicGoldLight
                )
            }

            Text(
                text = strategy,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryLight,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AstrologicalContextInspectorCard(
    contextSnapshot: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("context_inspector_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DataObject,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CelestialCyan
                    )
                    Text(
                        text = "Calculated Context Transmitted to Gemini",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondaryLight
                )
            }

            AnimatedVisibility(visible = expanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = contextSnapshot.ifBlank { "Calculated context payload ready for next Gemini execution." },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CelestialCyan.copy(alpha = 0.9f),
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlookLoadingState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outlook_loading_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = VedicGold,
                modifier = Modifier.size(44.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "Synthesizing Planetary Ephemeris with Gemini...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryLight,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Transmitting Navagraha Nakshatra positions, SBC Vedhas, and Panchang pillars via Firebase GenAI SDK",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OutlookErrorCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outlook_error_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Notice on Firebase GenAI SDK Connection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = VedicGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Gemini Request", color = VedicGold)
            }
        }
    }
}
