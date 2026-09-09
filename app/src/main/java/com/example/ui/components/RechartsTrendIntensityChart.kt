package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

/**
 * Filter mode for the Recharts interactive tooltip inspection.
 */
enum class TooltipInfluenceMode(val label: String) {
    OVERVIEW("Overview"),
    PANCHANG("Panchang Influence"),
    HORA("Hora Influence")
}

/**
 * Native Jetpack Compose Recharts-styled 24-Hour Trend Intensity Data Visualization.
 * Plots predicted market trend intensity over the next 24 hours based on Panchang and Hora calculations.
 * Features cubic Bézier area splines, dual-tone bullish/bearish gradients, Cartesian grid,
 * zero reference line, milestone reference dots, and an interactive scrubbing tooltip cursor.
 */
@Composable
fun RechartsTrendIntensityChart(
    report: Trend24HourReport,
    selectedPoint: TrendDataPoint?,
    onSelectPoint: (TrendDataPoint) -> Unit,
    isRolling24Hours: Boolean,
    onToggleRolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Recharts Series Visibility States
    var showAreaFill by remember { mutableStateOf(true) }
    var showHoraCurve by remember { mutableStateOf(true) }
    var showPanchangLine by remember { mutableStateOf(false) }
    var showMilestones by remember { mutableStateOf(true) }

    // Interactive Tooltip Influence Mode (Overview, Panchang Influence, Hora Influence)
    var tooltipInfluenceMode by remember { mutableStateOf(TooltipInfluenceMode.OVERVIEW) }

    val activePoint = selectedPoint ?: report.points.find { it.isCurrentHour } ?: report.points.first()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(CosmicCardBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Recharts Header & Title Bar
            RechartsHeader(
                regime = report.primaryRegime,
                isRolling = isRolling24Hours,
                onToggleRolling = onToggleRolling
            )

            // 24-Hour KPI Summary Chips
            SummaryKpiRow(report = report)

            // Recharts Series Filter Chips (Area, Hora, Panchang, Markers)
            SeriesFilterRow(
                showAreaFill = showAreaFill,
                onToggleArea = { showAreaFill = !showAreaFill },
                showHoraCurve = showHoraCurve,
                onToggleHora = { showHoraCurve = !showHoraCurve },
                showPanchangLine = showPanchangLine,
                onTogglePanchang = { showPanchangLine = !showPanchangLine },
                showMilestones = showMilestones,
                onToggleMilestones = { showMilestones = !showMilestones }
            )

            // The Recharts Interactive Canvas with Hover & Floating Tooltips
            RechartsCanvas(
                points = report.points,
                activePoint = activePoint,
                onPointScrubbed = onSelectPoint,
                showAreaFill = showAreaFill,
                showHoraCurve = showHoraCurve,
                showPanchangLine = showPanchangLine,
                showMilestones = showMilestones,
                influenceMode = tooltipInfluenceMode,
                onSelectInfluenceMode = { tooltipInfluenceMode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )

            // Recharts Dynamic Glassmorphism Interactive Tooltip Inspector
            RechartsTooltipCard(
                point = activePoint,
                selectedInfluenceMode = tooltipInfluenceMode,
                onSelectInfluenceMode = { tooltipInfluenceMode = it }
            )

            // Quick 24-Hour Hour Selector Strip
            HourSelectorStrip(
                points = report.points,
                activePoint = activePoint,
                onSelectPoint = onSelectPoint
            )

            // Recharts Chart Legend
            RechartsLegend(
                showAreaFill = showAreaFill,
                showHoraCurve = showHoraCurve,
                showPanchangLine = showPanchangLine,
                showMilestones = showMilestones
            )
        }
    }
}

@Composable
private fun RechartsHeader(
    regime: String,
    isRolling: Boolean,
    onToggleRolling: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = VedicGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Predicted Trend Intensity (24h)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }
            Text(
                text = "Panchang & Hora Time-Cycle Harmonic Spline",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )
        }

        // Rolling vs Vedic Horizon Toggle
        Surface(
            color = CosmicDeepNavy,
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(VedicGold.copy(alpha = 0.4f)))
        ) {
            Row(modifier = Modifier.padding(2.dp)) {
                Surface(
                    color = if (!isRolling) VedicGold else Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onToggleRolling(false) }
                ) {
                    Text(
                        text = "Vedic (06h)",
                        fontSize = 11.sp,
                        fontWeight = if (!isRolling) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isRolling) CosmicDeepNavy else TextSecondaryLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = if (isRolling) VedicGold else Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onToggleRolling(true) }
                ) {
                    Text(
                        text = "Rolling 24h",
                        fontSize = 11.sp,
                        fontWeight = if (isRolling) FontWeight.Bold else FontWeight.Normal,
                        color = if (isRolling) CosmicDeepNavy else TextSecondaryLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryKpiRow(report: Trend24HourReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Peak Bullish KPI
        Surface(
            modifier = Modifier.weight(1f),
            color = CosmicDeepNavy,
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BullishEmerald.copy(alpha = 0.3f)))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Peak Bullish",
                    style = MaterialTheme.typography.labelSmall,
                    color = BullishEmeraldLight,
                    fontSize = 10.sp
                )
                Text(
                    text = "${report.peakBullishPoint.timeLabel} (${String.format("+%.0f", report.peakBullishPoint.intensityScore)})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BullishEmerald
                )
                Text(
                    text = report.peakBullishPoint.horaPlanet.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = TextSecondaryLight
                )
            }
        }

        // Peak Caution KPI
        Surface(
            modifier = Modifier.weight(1f),
            color = CosmicDeepNavy,
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BearishRuby.copy(alpha = 0.3f)))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Peak Caution",
                    style = MaterialTheme.typography.labelSmall,
                    color = BearishRubyLight,
                    fontSize = 10.sp
                )
                Text(
                    text = "${report.peakBearishPoint.timeLabel} (${String.format("%.0f", report.peakBearishPoint.intensityScore)})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BearishRuby
                )
                Text(
                    text = report.peakBearishPoint.horaPlanet.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = TextSecondaryLight
                )
            }
        }

        // 24h Trend Ratio KPI
        Surface(
            modifier = Modifier.weight(1f),
            color = CosmicDeepNavy,
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CelestialCyan.copy(alpha = 0.3f)))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "24h Ratio",
                    style = MaterialTheme.typography.labelSmall,
                    color = CelestialCyan,
                    fontSize = 10.sp
                )
                Text(
                    text = "${report.bullishHoursCount}B / ${report.bearishHoursCount}S",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "${report.neutralHoursCount} Neutral",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

@Composable
private fun SeriesFilterRow(
    showAreaFill: Boolean,
    onToggleArea: () -> Unit,
    showHoraCurve: Boolean,
    onToggleHora: () -> Unit,
    showPanchangLine: Boolean,
    onTogglePanchang: () -> Unit,
    showMilestones: Boolean,
    onToggleMilestones: () -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = showAreaFill,
            onClick = onToggleArea,
            label = { Text("Intensity Area", fontSize = 11.sp) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BullishEmerald)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BullishEmerald.copy(alpha = 0.2f),
                selectedLabelColor = BullishEmeraldLight,
                containerColor = CosmicDeepNavy,
                labelColor = TextSecondaryLight
            )
        )

        FilterChip(
            selected = showHoraCurve,
            onClick = onToggleHora,
            label = { Text("Hora Power", fontSize = 11.sp) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VedicGold)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = VedicGold.copy(alpha = 0.2f),
                selectedLabelColor = VedicGoldLight,
                containerColor = CosmicDeepNavy,
                labelColor = TextSecondaryLight
            )
        )

        FilterChip(
            selected = showPanchangLine,
            onClick = onTogglePanchang,
            label = { Text("Panchang Base", fontSize = 11.sp) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CelestialCyan)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CelestialCyan.copy(alpha = 0.2f),
                selectedLabelColor = CelestialCyan,
                containerColor = CosmicDeepNavy,
                labelColor = TextSecondaryLight
            )
        )

        FilterChip(
            selected = showMilestones,
            onClick = onToggleMilestones,
            label = { Text("Vedic Markers", fontSize = 11.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = VedicGold,
                    modifier = Modifier.size(12.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CosmicSurfaceElevated,
                selectedLabelColor = VedicGold,
                containerColor = CosmicDeepNavy,
                labelColor = TextSecondaryLight
            )
        )
    }
}

/**
 * Core Canvas that renders the Recharts Spline AreaChart with Cartesian grid,
 * dual gradients, reference lines, milestone dots, and interactive touch scrubbing.
 */
@Composable
private fun RechartsCanvas(
    points: List<TrendDataPoint>,
    activePoint: TrendDataPoint,
    onPointScrubbed: (TrendDataPoint) -> Unit,
    showAreaFill: Boolean,
    showHoraCurve: Boolean,
    showPanchangLine: Boolean,
    showMilestones: Boolean,
    influenceMode: TooltipInfluenceMode,
    onSelectInfluenceMode: (TooltipInfluenceMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicDeepNavy)
            .pointerInput(points) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val position = change.position
                        val chartLeft = 54f
                        val chartRight = size.width - 24f
                        val chartWidth = chartRight - chartLeft
                        if (chartWidth > 0 && points.size > 1) {
                            val stepX = chartWidth / (points.size - 1).toFloat()
                            if (position.x in (chartLeft - 15f)..(chartRight + 15f)) {
                                val index = (((position.x - chartLeft) / stepX) + 0.5f)
                                    .toInt()
                                    .coerceIn(0, points.size - 1)
                                onPointScrubbed(points[index])
                            }
                        }
                    }
                }
            }
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val pointWidth = (size.width - 90f) / (points.size - 1).coerceAtLeast(1)
                    val chartLeft = 54f
                    val index = ((offset.x - chartLeft) / pointWidth)
                        .toInt()
                        .coerceIn(0, points.size - 1)
                    onPointScrubbed(points[index])
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chartLeft = 54f
            val chartRight = size.width - 24f
            val chartTop = 20f
            val chartBottom = size.height - 30f

            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            if (points.isEmpty() || chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            val stepX = chartWidth / (points.size - 1).toFloat()
            val zeroY = chartTop + chartHeight * 0.5f // 0 is exactly in the middle (-100 to +100)

            fun mapY(score: Float): Float {
                // score is -100 to +100
                val normalized = (score + 100f) / 200f
                return chartBottom - (normalized * chartHeight)
            }

            // 1. Cartesian Grid & Y-Axis Labels
            val yLevels = listOf(
                Pair(80f, "+80 Bull"),
                Pair(40f, "+40"),
                Pair(0f, "0 Neutral"),
                Pair(-40f, "-40"),
                Pair(-80f, "-80 Bear")
            )

            val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            yLevels.forEach { (level, label) ->
                val y = mapY(level)
                val isZero = level == 0f

                // Horizontal gridline
                drawLine(
                    color = if (isZero) VedicGold.copy(alpha = 0.6f) else CosmicCardBorder.copy(alpha = 0.5f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = if (isZero) 1.5f else 1f,
                    pathEffect = if (isZero) null else gridPathEffect
                )

                // Y-Axis label
                val textLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = if (isZero) VedicGoldLight else TextSecondaryLight
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(chartLeft - textLayout.size.width - 6f, y - textLayout.size.height / 2f)
                )
            }

            // 2. Reference Threshold Lines (+60 Overbought, -60 Oversold)
            val overboughtY = mapY(60f)
            val oversoldY = mapY(-60f)
            val thresholdPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            drawLine(
                color = BullishEmerald.copy(alpha = 0.4f),
                start = Offset(chartLeft, overboughtY),
                end = Offset(chartRight, overboughtY),
                strokeWidth = 1f,
                pathEffect = thresholdPathEffect
            )
            drawLine(
                color = BearishRuby.copy(alpha = 0.4f),
                start = Offset(chartLeft, oversoldY),
                end = Offset(chartRight, oversoldY),
                strokeWidth = 1f,
                pathEffect = thresholdPathEffect
            )

            // 3. X-Axis Time Labels & subtle vertical grid
            val tickInterval = 3 // show every 3rd hour
            points.forEachIndexed { i, point ->
                val x = chartLeft + i * stepX
                if (i % tickInterval == 0 || i == points.size - 1) {
                    // Vertical subtle gridline
                    drawLine(
                        color = CosmicCardBorder.copy(alpha = 0.25f),
                        start = Offset(x, chartTop),
                        end = Offset(x, chartBottom),
                        strokeWidth = 0.8f,
                        pathEffect = gridPathEffect
                    )

                    val textLayout = textMeasurer.measure(
                        text = point.timeLabel,
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = if (point.isCurrentHour) VedicGold else TextSecondaryLight,
                            fontWeight = if (point.isCurrentHour) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(x - textLayout.size.width / 2f, chartBottom + 6f)
                    )
                }
            }

            // 4. Compute Cubic Bézier Spline Curves for Intensity
            val intensityPoints = points.mapIndexed { i, pt ->
                Offset(chartLeft + i * stepX, mapY(pt.intensityScore))
            }

            val intensityPath = Path()
            buildCubicSplinePath(intensityPath, intensityPoints)

            // Area Gradient Fill (Above & Below Zero line)
            if (showAreaFill && intensityPoints.isNotEmpty()) {
                val filledPath = Path().apply {
                    addPath(intensityPath)
                    lineTo(chartRight, zeroY)
                    lineTo(chartLeft, zeroY)
                    close()
                }

                // Bullish area fill above zero
                clipRect(left = chartLeft, top = chartTop, right = chartRight, bottom = zeroY) {
                    drawPath(
                        path = filledPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BullishEmerald.copy(alpha = 0.45f),
                                BullishEmerald.copy(alpha = 0.05f)
                            ),
                            startY = chartTop,
                            endY = zeroY
                        )
                    )
                }

                // Bearish area fill below zero
                clipRect(left = chartLeft, top = zeroY, right = chartRight, bottom = chartBottom) {
                    drawPath(
                        path = filledPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BearishRuby.copy(alpha = 0.05f),
                                BearishRuby.copy(alpha = 0.45f)
                            ),
                            startY = zeroY,
                            endY = chartBottom
                        )
                    )
                }
            }

            // 5. Optional Secondary Line: Panchang Sentiment (Cyan Dotted)
            if (showPanchangLine) {
                val panchangPoints = points.mapIndexed { i, pt ->
                    Offset(chartLeft + i * stepX, mapY(pt.panchangFactor))
                }
                val panchangPath = Path()
                buildCubicSplinePath(panchangPath, panchangPoints)
                drawPath(
                    path = panchangPath,
                    color = CelestialCyan.copy(alpha = 0.7f),
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                )
            }

            // 6. Optional Secondary Line: Hora Planetary Power (Dashed Gold Curve)
            if (showHoraCurve) {
                val horaPoints = points.mapIndexed { i, pt ->
                    Offset(chartLeft + i * stepX, mapY(pt.horaPower))
                }
                val horaPath = Path()
                buildCubicSplinePath(horaPath, horaPoints)
                drawPath(
                    path = horaPath,
                    color = VedicGoldLight.copy(alpha = 0.8f),
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )
            }

            // 7. Primary Trend Intensity Stroke
            drawPath(
                path = intensityPath,
                brush = Brush.horizontalGradient(
                    colors = points.map { pt ->
                        if (pt.intensityScore >= 0f) BullishEmerald else BearishRuby
                    },
                    startX = chartLeft,
                    endX = chartRight
                ),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 8. Milestone Reference Dots
            if (showMilestones) {
                points.forEachIndexed { i, pt ->
                    val ptOffset = intensityPoints[i]
                    when (pt.specialMarkerType) {
                        TrendMarkerType.ABHIJIT_MUHURTA -> {
                            // Golden glowing star dot
                            drawCircle(color = VedicGold.copy(alpha = 0.35f), radius = 10f, center = ptOffset)
                            drawCircle(color = VedicGold, radius = 5f, center = ptOffset)
                            drawCircle(color = Color.White, radius = 2f, center = ptOffset)
                        }
                        TrendMarkerType.RAHU_KAAL -> {
                            // Ruby hazard warning dot
                            drawCircle(color = BearishRuby.copy(alpha = 0.35f), radius = 9f, center = ptOffset)
                            drawCircle(color = BearishRuby, radius = 5f, center = ptOffset)
                        }
                        TrendMarkerType.MARKET_OPEN -> {
                            drawCircle(color = CelestialCyan, radius = 5f, center = ptOffset)
                        }
                        TrendMarkerType.MARKET_CLOSE -> {
                            drawCircle(color = Color(0xFFA855F7), radius = 5f, center = ptOffset)
                        }
                        TrendMarkerType.HARMONIC_REVERSAL -> {
                            drawCircle(color = Color(0xFFA855F7).copy(alpha = 0.3f), radius = 8f, center = ptOffset)
                            drawCircle(color = Color(0xFFA855F7), radius = 4f, center = ptOffset)
                        }
                        else -> Unit
                    }
                }
            }

            // 9. Active Scrubber / Tooltip Cursor Guide Line
            val activeIndex = points.indexOfFirst { it.hourIndex == activePoint.hourIndex }
                .takeIf { it >= 0 } ?: 0
            val activeOffset = intensityPoints[activeIndex]

            // Vertical Recharts cursor line
            drawLine(
                color = VedicGold.copy(alpha = 0.85f),
                start = Offset(activeOffset.x, chartTop),
                end = Offset(activeOffset.x, chartBottom),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
            )

            // Horizontal Recharts crosshair to Y-axis
            drawLine(
                color = if (activePoint.intensityScore >= 0f) BullishEmerald.copy(alpha = 0.35f) else BearishRuby.copy(alpha = 0.35f),
                start = Offset(chartLeft, activeOffset.y),
                end = Offset(activeOffset.x, activeOffset.y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            // Glowing Active Point
            drawCircle(
                color = VedicGold.copy(alpha = 0.3f),
                radius = 12f,
                center = activeOffset
            )
            drawCircle(
                color = if (activePoint.intensityScore >= 0f) BullishEmerald else BearishRuby,
                radius = 6f,
                center = activeOffset
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = activeOffset
            )
        }

        // Floating Recharts Tooltip Overlay positioned relative to active point
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val chartLeft = 54f
        val chartRight = widthPx - 24f
        val chartTop = 20f
        val chartBottom = heightPx - 30f
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        if (points.isNotEmpty() && chartWidth > 0 && chartHeight > 0) {
            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1).toFloat()
            val activeIndex = points.indexOfFirst { it.hourIndex == activePoint.hourIndex }.coerceAtLeast(0)
            val activeXPx = chartLeft + activeIndex * stepX
            val normalized = (activePoint.intensityScore + 100f) / 200f
            val activeYPx = chartBottom - (normalized * chartHeight)

            val activeXDp = with(density) { activeXPx.toDp() }
            val activeYDp = with(density) { activeYPx.toDp() }

            val tooltipWidthDp = 196.dp
            val isRightHalf = activeXPx > (chartLeft + chartWidth * 0.52f)
            val tooltipXDp = if (isRightHalf) {
                (activeXDp - tooltipWidthDp - 8.dp).coerceAtLeast(6.dp)
            } else {
                (activeXDp + 10.dp).coerceAtMost(maxWidth - tooltipWidthDp - 6.dp)
            }
            val tooltipYDp = (activeYDp - 48.dp).coerceIn(4.dp, (maxHeight - 114.dp).coerceAtLeast(4.dp))

            FloatingRechartsTooltip(
                point = activePoint,
                influenceMode = influenceMode,
                onCycleMode = {
                    val next = when (influenceMode) {
                        TooltipInfluenceMode.OVERVIEW -> TooltipInfluenceMode.PANCHANG
                        TooltipInfluenceMode.PANCHANG -> TooltipInfluenceMode.HORA
                        TooltipInfluenceMode.HORA -> TooltipInfluenceMode.OVERVIEW
                    }
                    onSelectInfluenceMode(next)
                },
                modifier = Modifier
                    .offset(x = tooltipXDp, y = tooltipYDp)
                    .width(tooltipWidthDp)
            )
        }
    }
}

/**
 * Builds a smooth Monotone cubic Bézier spline through the given data points.
 */
private fun buildCubicSplinePath(path: Path, points: List<Offset>) {
    if (points.isEmpty()) return
    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return

    for (i in 0 until points.size - 1) {
        val p0 = points[max(i - 1, 0)]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[min(i + 2, points.size - 1)]

        val cp1X = p1.x + (p2.x - p0.x) / 6f
        val cp1Y = p1.y + (p2.y - p0.y) / 6f

        val cp2X = p2.x - (p3.x - p1.x) / 6f
        val cp2Y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2.x, p2.y)
    }
}

/**
 * Compact floating Recharts tooltip box anchored directly above or next to the active data point on the canvas.
 */
@Composable
private fun FloatingRechartsTooltip(
    point: TrendDataPoint,
    influenceMode: TooltipInfluenceMode,
    onCycleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBullish = point.intensityScore >= 0f
    val accentColor = if (isBullish) BullishEmerald else BearishRuby

    Surface(
        modifier = modifier.clickable { onCycleMode() },
        color = CosmicDeepNavy.copy(alpha = 0.95f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.85f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Header Row: Time & Intensity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = point.timeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VedicGoldLight
                    )
                    point.specialMarker?.let {
                        Text(
                            text = when (point.specialMarkerType) {
                                TrendMarkerType.ABHIJIT_MUHURTA -> "★ Abhijit"
                                TrendMarkerType.RAHU_KAAL -> "⚠ Rahu"
                                TrendMarkerType.MARKET_OPEN -> "▲ Open"
                                TrendMarkerType.MARKET_CLOSE -> "▼ Close"
                                else -> "◆ Reversal"
                            },
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (point.specialMarkerType) {
                                TrendMarkerType.ABHIJIT_MUHURTA -> VedicGold
                                TrendMarkerType.RAHU_KAAL -> BearishRubyLight
                                else -> CelestialCyan
                            }
                        )
                    }
                }

                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = String.format("%s%.0f", if (isBullish) "+" else "", point.intensityScore),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 1.dp),
                color = CosmicCardBorder.copy(alpha = 0.6f),
                thickness = 0.5.dp
            )

            // Dynamic Content based on influenceMode
            when (influenceMode) {
                TooltipInfluenceMode.OVERVIEW -> {
                    // Panchang Influence Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🕉️", fontSize = 9.sp)
                        Text(
                            text = "${point.tithiName.ifEmpty { "Tithi" }} (${point.tithiCategory.title.substringBefore(" ")})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = CelestialCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Hora Influence Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = point.horaPlanet.symbol, fontSize = 11.sp, color = point.horaPlanet.displayColor)
                        Text(
                            text = "${point.horaPlanet.englishName} Hora (${point.horaBias.title.substringBefore(" ")})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = VedicGoldLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TooltipInfluenceMode.PANCHANG -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕉️ PANCHANG",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan
                        )
                        Text(
                            text = "${if (point.panchangContribution >= 0) "+" else ""}${point.panchangContribution.toInt()} pts",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan
                        )
                    }
                    Text(
                        text = "Tithi: ${point.tithiName} (${point.tithiCategory.marketBias})",
                        fontSize = 8.5.sp,
                        color = TextPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Nakshatra: ${point.nakshatraName} • ${point.timingWindowName}",
                        fontSize = 8.sp,
                        color = TextSecondaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TooltipInfluenceMode.HORA -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪐 HORA: ${point.horaPlanet.symbol} ${point.horaPlanet.englishName}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = point.horaPlanet.displayColor
                        )
                        Text(
                            text = "${if (point.horaPower >= 0) "+" else ""}${point.horaPower.toInt()} pts",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = VedicGoldLight
                        )
                    }
                    Text(
                        text = point.horaBias.title,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(point.horaBias.badgeColorHex)
                    )
                    if (point.horaFavorableSectors.isNotEmpty()) {
                        Text(
                            text = "Favors: ${point.horaFavorableSectors.take(2).joinToString(", ")}",
                            fontSize = 8.sp,
                            color = BullishEmeraldLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Mode indicator footer hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (influenceMode) {
                        TooltipInfluenceMode.OVERVIEW -> "Mode: Overview"
                        TooltipInfluenceMode.PANCHANG -> "Mode: Panchang"
                        TooltipInfluenceMode.HORA -> "Mode: Hora Lord"
                    },
                    fontSize = 7.5.sp,
                    color = TextSecondaryLight
                )
                Text(
                    text = "Tap to cycle ↻",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedicGoldLight
                )
            }
        }
    }
}

/**
 * Recharts Tooltip inspector card showing full interactive breakdown for the selected hour.
 * Supports interactive tab filtering between Overview, specific Panchang influence, and specific Hora influence.
 */
@Composable
private fun RechartsTooltipCard(
    point: TrendDataPoint,
    selectedInfluenceMode: TooltipInfluenceMode,
    onSelectInfluenceMode: (TooltipInfluenceMode) -> Unit
) {
    val isBullish = point.intensityScore >= 0f
    val accentColor = if (isBullish) BullishEmerald else BearishRuby

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CosmicDeepNavy,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(accentColor.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Time, Special Marker & Intensity Score
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
                        color = CosmicSurfaceElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = point.timeRange,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VedicGoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    point.specialMarker?.let { marker ->
                        Surface(
                            color = when (point.specialMarkerType) {
                                TrendMarkerType.ABHIJIT_MUHURTA -> VedicGold.copy(alpha = 0.2f)
                                TrendMarkerType.RAHU_KAAL -> BearishRuby.copy(alpha = 0.2f)
                                else -> CelestialCyan.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = marker,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (point.specialMarkerType) {
                                    TrendMarkerType.ABHIJIT_MUHURTA -> VedicGoldLight
                                    TrendMarkerType.RAHU_KAAL -> BearishRubyLight
                                    else -> CelestialCyan
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Trend Intensity Badge
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isBullish) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format("%s%.1f %s", if (isBullish) "+" else "", point.intensityScore, if (isBullish) "Bullish" else "Bearish"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            // Interactive Tooltip Influence Mode Tab Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TooltipInfluenceMode.values().forEach { mode ->
                    val isSelected = (mode == selectedInfluenceMode)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CosmicSurfaceElevated else CosmicDeepNavy,
                        border = if (isSelected) BorderStroke(1.dp, VedicGold) else BorderStroke(0.5.dp, CosmicCardBorder.copy(alpha = 0.5f)),
                        onClick = { onSelectInfluenceMode(mode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    TooltipInfluenceMode.OVERVIEW -> "⚡ Overview"
                                    TooltipInfluenceMode.PANCHANG -> "🕉️ Panchang"
                                    TooltipInfluenceMode.HORA -> "🪐 Hora Lord"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) VedicGoldLight else TextSecondaryLight
                            )
                        }
                    }
                }
            }

            // Mode Content Section
            when (selectedInfluenceMode) {
                TooltipInfluenceMode.OVERVIEW -> {
                    // Breakdown Grid (Hora Lord, Panchang Factor, Bias)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Active Hora Lord
                        Surface(
                            modifier = Modifier.weight(1.3f),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = point.horaPlanet.symbol,
                                    color = point.horaPlanet.displayColor,
                                    fontSize = 16.sp
                                )
                                Column {
                                    Text(
                                        text = "Hora: ${point.horaPlanet.englishName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = point.horaBias.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 9.sp,
                                        color = Color(point.horaBias.badgeColorHex)
                                    )
                                }
                            }
                        }

                        // Hora Base Power
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Hora Power", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondaryLight)
                                Text(
                                    text = String.format("%s%.0f pts", if (point.horaPower >= 0) "+" else "", point.horaPower),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VedicGoldLight
                                )
                            }
                        }

                        // Panchang Macro Sentiment
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Panchang", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondaryLight)
                                Text(
                                    text = String.format("%s%.0f pts", if (point.panchangFactor >= 0) "+" else "", point.panchangFactor),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CelestialCyan
                                )
                            }
                        }
                    }

                    // Dominant Influence Narrative & Actionable Tip
                    Text(
                        text = "Dominant Influence: ${point.dominantInfluence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        fontSize = 11.sp
                    )

                    Surface(
                        color = CosmicSurfaceElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = VedicGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = point.actionableGuidance,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                TooltipInfluenceMode.PANCHANG -> {
                    // Specific Panchang Influence View
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Specific Panchang Cosmic Factors Influencing This Hour",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan
                        )

                        // 1. Tithi Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tithi: ${point.tithiName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Surface(
                                        color = CelestialCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = point.tithiCategory.marketBias,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CelestialCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Category: ${point.tithiCategory.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextSecondaryLight
                                )
                                Text(
                                    text = point.tithiEffect,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextPrimaryLight
                                )
                            }
                        }

                        // 2. Nakshatra & Lord Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nakshatra: ${point.nakshatraName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "Lord: ${point.nakshatraLord.symbol} ${point.nakshatraLord.englishName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = point.nakshatraLord.displayColor
                                    )
                                }
                                Text(
                                    text = "Nature: ${point.nakshatraNature.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextSecondaryLight
                                )
                                Text(
                                    text = point.nakshatraEffect,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextPrimaryLight
                                )
                            }
                        }

                        // 3. Timing Window & Muhurta
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Muhurta Window: ${point.timingWindowName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGoldLight
                                    )
                                    Text(
                                        text = point.timingWindowImpact,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                                Surface(
                                    color = CelestialCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${if (point.panchangContribution >= 0) "+" else ""}${point.panchangContribution.toInt()} pts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CelestialCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // 4. Yoga & Karana Harmony
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "Yoga Harmony", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondaryLight)
                                    Text(text = point.yogaName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                    Text(text = point.yogaEffect, style = MaterialTheme.typography.bodySmall, fontSize = 9.5.sp, color = TextSecondaryLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "Karana Execution", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondaryLight)
                                    Text(text = point.karanaName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                    Text(
                                        text = if (point.isVishtiKarana) "⚠ Vishti / Bhadra High Caution" else "Auspicious execution liquidity",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 9.5.sp,
                                        color = if (point.isVishtiKarana) BearishRubyLight else BullishEmeraldLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                TooltipInfluenceMode.HORA -> {
                    // Specific Hora Influence View
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Hourly Planetary Lord & Intraday Cycle Influence",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VedicGoldLight
                        )

                        // 1. Planetary Lord Dignity Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = point.horaPlanet.displayColor.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = point.horaPlanet.symbol, fontSize = 20.sp, color = point.horaPlanet.displayColor)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${point.horaPlanet.englishName} (${point.horaPlanet.sanskritName})",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryLight
                                        )
                                        Surface(
                                            color = Color(point.horaBias.badgeColorHex).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = point.horaBias.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(point.horaBias.badgeColorHex),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Role: ${point.horaPlanet.marketRole}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = TextSecondaryLight
                                    )
                                    Text(
                                        text = "Element: ${point.horaElement} • Dhatu: ${point.horaDhatu}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 9.5.sp,
                                        color = VedicGoldLight
                                    )
                                }
                            }
                        }

                        // 2. Hora Power & Asset Ruler Resonance
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hora Power Contribution:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryLight
                                    )
                                    Text(
                                        text = String.format("%s%.0f / 100 points", if (point.horaPower >= 0) "+" else "", point.horaPower),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGoldLight
                                    )
                                }

                                if (point.isAssetRulerResonance) {
                                    Surface(
                                        color = VedicGold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, VedicGold.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "⭐ Key Asset Ruler Resonance: ${point.horaPlanet.englishName} rules this selected asset directly, intensifying intraday trend impact!",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = VedicGoldLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Favorable Sectors vs Cautious Sectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = "Favored Sectors", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = BullishEmeraldLight)
                                    Text(
                                        text = point.horaFavorableSectors.ifEmpty { listOf("General Market") }.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryLight
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = "Hedge / Caution Sectors", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = BearishRubyLight)
                                    Text(
                                        text = point.horaCautiousSectors.ifEmpty { listOf("None") }.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryLight
                                    )
                                }
                            }
                        }

                        // 4. Actionable Hora Trading Strategy Tip
                        Surface(
                            color = CosmicSurfaceElevated,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TipsAndUpdates,
                                    contentDescription = null,
                                    tint = VedicGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Hora Intraday Rule: ${point.horaTradingTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryLight,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact horizontal hour strip allowing quick tapping on any hour of the 24h series.
 */
@Composable
private fun HourSelectorStrip(
    points: List<TrendDataPoint>,
    activePoint: TrendDataPoint,
    onSelectPoint: (TrendDataPoint) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        points.forEach { pt ->
            val isSelected = pt.hourIndex == activePoint.hourIndex
            val isBullish = pt.intensityScore >= 0f
            val dotColor = if (isBullish) BullishEmerald else BearishRuby

            Surface(
                color = if (isSelected) CosmicSurfaceElevated else CosmicDeepNavy,
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) {
                    CardDefaults.outlinedCardBorder().copy(brush = SolidColor(VedicGold))
                } else if (pt.isCurrentHour) {
                    CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CelestialCyan.copy(alpha = 0.5f)))
                } else null,
                onClick = { onSelectPoint(pt) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = pt.timeLabel,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) VedicGoldLight else TextSecondaryLight
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )

                    Text(
                        text = pt.horaPlanet.symbol,
                        color = pt.horaPlanet.displayColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Recharts Legend mimicking Recharts `<Legend />` component.
 */
@Composable
private fun RechartsLegend(
    showAreaFill: Boolean,
    showHoraCurve: Boolean,
    showPanchangLine: Boolean,
    showMilestones: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            label = "Trend Intensity",
            color = BullishEmerald,
            shape = LegendShape.AREA,
            active = showAreaFill
        )

        LegendItem(
            label = "Hora Power",
            color = VedicGold,
            shape = LegendShape.DASHED_LINE,
            active = showHoraCurve
        )

        LegendItem(
            label = "Panchang Base",
            color = CelestialCyan,
            shape = LegendShape.DOTTED_LINE,
            active = showPanchangLine
        )

        LegendItem(
            label = "Milestone Marker",
            color = VedicGoldLight,
            shape = LegendShape.CIRCLE,
            active = showMilestones
        )
    }
}

private enum class LegendShape {
    AREA, DASHED_LINE, DOTTED_LINE, CIRCLE
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    shape: LegendShape,
    active: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (shape) {
            LegendShape.AREA -> {
                Box(
                    modifier = Modifier
                        .size(12.dp, 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (active) color else TextSecondaryLight.copy(alpha = 0.3f))
                )
            }
            LegendShape.DASHED_LINE -> {
                Box(
                    modifier = Modifier
                        .size(12.dp, 3.dp)
                        .background(if (active) color else TextSecondaryLight.copy(alpha = 0.3f))
                )
            }
            LegendShape.DOTTED_LINE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(if (active) color else TextSecondaryLight.copy(alpha = 0.3f))
                        )
                    }
                }
            }
            LegendShape.CIRCLE -> {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (active) color else TextSecondaryLight.copy(alpha = 0.3f))
                )
            }
        }

        Text(
            text = label,
            fontSize = 10.sp,
            color = if (active) TextPrimaryLight else TextSecondaryLight
        )
    }
}
