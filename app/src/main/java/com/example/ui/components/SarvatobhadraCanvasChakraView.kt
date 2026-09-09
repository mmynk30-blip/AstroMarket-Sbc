package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SarvatobhadraEngine
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.min

/**
 * Filter mode for Vedha aspect rays drawn on the Canvas.
 */
enum class SbcRayFilterMode(val label: String) {
    ALL("All Rays"),
    BENEFIC_ONLY("Benefics Only"),
    MALEFIC_ONLY("Malefics Only"),
    SELECTED_ONLY("Selected Only"),
    HIDDEN("Hide Rays")
}

/**
 * High-performance visual component using Canvas to render the authentic
 * 81-square (9x9) Sarvatobhadra Chakra layout with mapped stock market tickers.
 */
@Composable
fun SarvatobhadraCanvasChakra(
    sbcState: SarvatobhadraState,
    tickerMappings: List<StockTickerNakshatraMapping>,
    selectedCell: SbCell? = null,
    selectedNakshatraName: String?,
    highlightedTickerSymbol: String? = null,
    onSelectNakshatra: (String) -> Unit,
    onSelectCell: (SbCell?) -> Unit,
    modifier: Modifier = Modifier
) {
    var rayFilterMode by remember { mutableStateOf(SbcRayFilterMode.ALL) }
    var showTickerBadges by remember { mutableStateOf(true) }
    var showPlanetGlyphs by remember { mutableStateOf(true) }

    val textMeasurer = rememberTextMeasurer()

    // Map nakshatras to ticker count
    val nakshatraTickerCount = remember(tickerMappings) {
        tickerMappings.groupBy { it.nakshatraName }.mapValues { it.value.size }
    }

    // Identify nakshatra of currently highlighted search ticker
    val targetHighlightedNakshatra = remember(highlightedTickerSymbol, tickerMappings) {
        if (!highlightedTickerSymbol.isNullOrBlank()) {
            tickerMappings.find { it.tickerSymbol.equals(highlightedTickerSymbol, ignoreCase = true) }?.nakshatraName
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Toolbar with Ray Mode & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessAuto,
                    contentDescription = null,
                    tint = VedicGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "SBC Mandala Canvas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            // Quick Ray Filter Dropdown / Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (rayFilterMode != SbcRayFilterMode.HIDDEN) CosmicSurfaceElevated else CosmicDeepNavy,
                    border = BorderStroke(
                        1.dp,
                        if (rayFilterMode != SbcRayFilterMode.HIDDEN) VedicGold.copy(alpha = 0.5f) else CosmicCardBorder
                    ),
                    modifier = Modifier.clickable {
                        rayFilterMode = when (rayFilterMode) {
                            SbcRayFilterMode.ALL -> SbcRayFilterMode.BENEFIC_ONLY
                            SbcRayFilterMode.BENEFIC_ONLY -> SbcRayFilterMode.MALEFIC_ONLY
                            SbcRayFilterMode.MALEFIC_ONLY -> SbcRayFilterMode.SELECTED_ONLY
                            SbcRayFilterMode.SELECTED_ONLY -> SbcRayFilterMode.HIDDEN
                            SbcRayFilterMode.HIDDEN -> SbcRayFilterMode.ALL
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = CelestialCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = rayFilterMode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CelestialCyan
                        )
                    }
                }

                // Ticker badge toggle
                IconButton(
                    onClick = { showTickerBadges = !showTickerBadges },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (showTickerBadges) Icons.Default.Label else Icons.Default.LabelOff,
                        contentDescription = "Toggle Ticker Badges",
                        tint = if (showTickerBadges) VedicGold else TextSecondaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // The Custom Canvas Drawing Component
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicDeepNavy)
                .padding(4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(sbcState, tickerMappings) {
                        detectTapGestures { offset ->
                            val mandalaDimension = min(size.width.toFloat(), size.height.toFloat()) * 0.98f
                            val originX = (size.width - mandalaDimension) / 2f
                            val originY = (size.height - mandalaDimension) / 2f
                            val cellSize = mandalaDimension / 9f

                            if (offset.x in originX..(originX + mandalaDimension) &&
                                offset.y in originY..(originY + mandalaDimension)
                            ) {
                                val col = ((offset.x - originX) / cellSize).toInt().coerceIn(0, 8)
                                val row = ((offset.y - originY) / cellSize).toInt().coerceIn(0, 8)

                                val clickedCell = sbcState.grid.getOrNull(row)?.getOrNull(col)
                                clickedCell?.let { cell ->
                                    onSelectCell(cell)
                                    val nakName = getNakshatraNameForCell(row, col)
                                    if (nakName != null) {
                                        onSelectNakshatra(nakName)
                                    }
                                }
                            }
                        }
                    }
            ) {
                drawSarvatobhadraMandala(
                    sbcState = sbcState,
                    tickerCountMap = nakshatraTickerCount,
                    selectedCell = selectedCell,
                    selectedNakshatra = selectedNakshatraName,
                    highlightedNakshatra = targetHighlightedNakshatra,
                    rayFilterMode = rayFilterMode,
                    showTickerBadges = showTickerBadges,
                    showPlanetGlyphs = showPlanetGlyphs,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Canvas Footer Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CanvasLegendDot(color = VedicGold, label = "Planet Sthiti")
                CanvasLegendDot(color = BullishEmeraldLight, label = "Benefic Ray")
                CanvasLegendDot(color = BearishRubyLight, label = "Malefic Ray")
                CanvasLegendDot(color = CelestialCyan, label = "Mapped Stock")
            }

            Text(
                text = "Tap any cell to inspect",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * DrawScope rendering pipeline for the 9x9 Sarvatobhadra Chakra Mandala.
 */
private fun DrawScope.drawSarvatobhadraMandala(
    sbcState: SarvatobhadraState,
    tickerCountMap: Map<String, Int>,
    selectedCell: SbCell?,
    selectedNakshatra: String?,
    highlightedNakshatra: String?,
    rayFilterMode: SbcRayFilterMode,
    showTickerBadges: Boolean,
    showPlanetGlyphs: Boolean,
    textMeasurer: TextMeasurer
) {
    val mandalaDimension = min(size.width, size.height) * 0.98f
    val originX = (size.width - mandalaDimension) / 2f
    val originY = (size.height - mandalaDimension) / 2f
    val cellSize = mandalaDimension / 9f

    // 1. Draw Outer Mandala Border Frame
    drawRoundRect(
        color = CosmicCardBorder,
        topLeft = Offset(originX, originY),
        size = Size(mandalaDimension, mandalaDimension),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2.5f)
    )
    drawRoundRect(
        color = VedicGold.copy(alpha = 0.35f),
        topLeft = Offset(originX + 2f, originY + 2f),
        size = Size(mandalaDimension - 4f, mandalaDimension - 4f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1f)
    )

    // Precompute cell centers for ray rendering
    val cellCenters = Array(9) { r ->
        Array(9) { c ->
            Offset(
                x = originX + (c + 0.5f) * cellSize,
                y = originY + (r + 0.5f) * cellSize
            )
        }
    }

    // 2. Draw Cells Background & Borders
    for (r in 0..8) {
        for (c in 0..8) {
            val cell = sbcState.grid.getOrNull(r)?.getOrNull(c)
            val nakName = getNakshatraNameForCell(r, c)
            val isPerimeter = (r == 0 || r == 8 || c == 0 || c == 8)
            val isCorner = (r == 0 && c == 0) || (r == 0 && c == 8) || (r == 8 && c == 0) || (r == 8 && c == 8)
            val isCenter = (r == 4 && c == 4)

            val isSelected = (selectedCell != null && selectedCell.row == r && selectedCell.col == c) ||
                    (selectedCell == null && nakName != null && nakName.equals(selectedNakshatra, ignoreCase = true))
            val isHighlighted = nakName != null && nakName.equals(highlightedNakshatra, ignoreCase = true)

            val cellTopLeft = Offset(originX + c * cellSize, originY + r * cellSize)
            val cellRectSize = Size(cellSize - 1.5f, cellSize - 1.5f)

            // Determine Background Color
            val bgColor = when {
                isHighlighted -> Color(0xFF1E3A5F)
                isSelected -> Color(0xFF372C1E)
                cell != null && cell.occupyingPlanets.isNotEmpty() -> Color(0xFF281E3C)
                isCenter -> Color(0xFF332715)
                isCorner -> Color(0xFF151D2F)
                isPerimeter -> CosmicSurfaceElevated
                (r in 2..6 && c in 2..6) -> Color(0xFF0D1424)
                else -> CosmicSurface
            }

            drawRoundRect(
                color = bgColor,
                topLeft = cellTopLeft,
                size = cellRectSize,
                cornerRadius = CornerRadius(3f, 3f)
            )

            // Dynamic Affliction / Blessing Tints
            if (cell != null && cell.isAfflicted) {
                drawRoundRect(
                    color = BearishRuby.copy(alpha = 0.12f),
                    topLeft = cellTopLeft,
                    size = cellRectSize,
                    cornerRadius = CornerRadius(3f, 3f)
                )
            } else if (cell != null && cell.isBlessed) {
                drawRoundRect(
                    color = BullishEmerald.copy(alpha = 0.12f),
                    topLeft = cellTopLeft,
                    size = cellRectSize,
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }

            // Cell Border
            val borderColor = when {
                isHighlighted -> CelestialCyan
                isSelected -> VedicGold
                cell != null && cell.occupyingPlanets.isNotEmpty() -> VedicGoldLight.copy(alpha = 0.7f)
                isCenter -> VedicGold.copy(alpha = 0.6f)
                isPerimeter -> Color(0xFF283656)
                else -> Color(0xFF1B243B)
            }
            val borderWidth = if (isSelected || isHighlighted) 2f else 0.8f

            drawRoundRect(
                color = borderColor,
                topLeft = cellTopLeft,
                size = cellRectSize,
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(width = borderWidth)
            )

            // 3. Draw Cell Content & Typography
            drawCellContent(
                cell = cell,
                row = r,
                col = c,
                nakName = nakName,
                cellTopLeft = cellTopLeft,
                cellSize = cellSize,
                tickerCount = if (nakName != null) tickerCountMap[nakName] ?: 0 else 0,
                showTickerBadges = showTickerBadges,
                showPlanetGlyphs = showPlanetGlyphs,
                textMeasurer = textMeasurer,
                isCenter = isCenter,
                isCorner = isCorner
            )
        }
    }

    // 4. Draw Vedha Aspect Rays across the Mandala
    if (rayFilterMode != SbcRayFilterMode.HIDDEN) {
        drawVedhaRays(
            sbcState = sbcState,
            cellCenters = cellCenters,
            selectedNakshatra = selectedNakshatra,
            rayFilterMode = rayFilterMode
        )
    }

    // 5. Draw Decorative Inner Sanctum Concentric Ring for Center Bindu
    val centerOffset = cellCenters[4][4]
    drawCircle(
        color = VedicGold.copy(alpha = 0.35f),
        radius = cellSize * 0.42f,
        center = centerOffset,
        style = Stroke(width = 1.2f)
    )
    drawCircle(
        color = VedicGold,
        radius = cellSize * 0.08f,
        center = centerOffset
    )
}

/**
 * Renders text, planetary occupants, and stock ticker badges within a specific cell.
 */
private fun DrawScope.drawCellContent(
    cell: SbCell?,
    row: Int,
    col: Int,
    nakName: String?,
    cellTopLeft: Offset,
    cellSize: Float,
    tickerCount: Int,
    showTickerBadges: Boolean,
    showPlanetGlyphs: Boolean,
    textMeasurer: TextMeasurer,
    isCenter: Boolean,
    isCorner: Boolean
) {
    val occupants = cell?.occupyingPlanets ?: emptyList()
    val hasOccupants = showPlanetGlyphs && occupants.isNotEmpty()

    // 1. Draw Occupying Planet Glyphs (Top portion of cell)
    if (hasOccupants) {
        val glyphString = occupants.take(2).joinToString(" ") { it.symbol }
        val glyphResult = textMeasurer.measure(
            text = AnnotatedString(glyphString),
            style = TextStyle(
                color = VedicGoldLight,
                fontSize = (cellSize * 0.22f).toSp(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        drawText(
            textLayoutResult = glyphResult,
            topLeft = Offset(
                x = cellTopLeft.x + (cellSize - glyphResult.size.width) / 2f,
                y = cellTopLeft.y + (cellSize * 0.10f)
            )
        )
    }

    // 2. Corner Cells: Draw Devanagari Vowel on top, English Roman letter below
    if (isCorner) {
        val (hindiVowel, englishVowel) = getCornerVowelPair(row, col)
        val hindiResult = textMeasurer.measure(
            text = AnnotatedString(hindiVowel),
            style = TextStyle(
                color = CelestialCyan,
                fontSize = (cellSize * 0.24f).toSp(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        val englishResult = textMeasurer.measure(
            text = AnnotatedString(englishVowel),
            style = TextStyle(
                color = TextSecondaryLight,
                fontSize = (cellSize * 0.15f).toSp(),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
        drawText(
            textLayoutResult = hindiResult,
            topLeft = Offset(
                x = cellTopLeft.x + (cellSize - hindiResult.size.width) / 2f,
                y = cellTopLeft.y + cellSize * 0.16f
            )
        )
        drawText(
            textLayoutResult = englishResult,
            topLeft = Offset(
                x = cellTopLeft.x + (cellSize - englishResult.size.width) / 2f,
                y = cellTopLeft.y + cellSize * 0.54f
            )
        )
        return
    }

    // 3. Center Cell: Sacred "ॐ" Bindu
    if (isCenter) {
        val binduResult = textMeasurer.measure(
            text = AnnotatedString("ॐ"),
            style = TextStyle(
                color = VedicGold,
                fontSize = (cellSize * 0.36f).toSp(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        drawText(
            textLayoutResult = binduResult,
            topLeft = Offset(
                x = cellTopLeft.x + (cellSize - binduResult.size.width) / 2f,
                y = cellTopLeft.y + (cellSize - binduResult.size.height) / 2f
            )
        )
        return
    }

    // 4. Perimeter Nakshatras & Inner Cells
    val primaryLabel = when {
        nakName != null -> SarvatobhadraEngine.getNakshatraAbbr(nakName)
        cell != null && cell.label.isNotBlank() -> cell.label
        else -> getInnerSymbolForCell(row, col)
    }

    val labelColor = when {
        nakName != null -> TextPrimaryLight
        cell?.cellType == SbCell.CellType.INNER_RASI -> TextPrimaryLight.copy(alpha = 0.9f)
        cell?.cellType == SbCell.CellType.INNER_TITHI_VARA -> VedicGoldLight.copy(alpha = 0.85f)
        else -> TextMutedLight
    }

    val targetFontSize = if (hasOccupants) {
        (cellSize * 0.14f).toSp()
    } else {
        if (nakName != null) (cellSize * 0.18f).toSp() else (cellSize * 0.16f).toSp()
    }

    var labelResult = textMeasurer.measure(
        text = AnnotatedString(primaryLabel),
        style = TextStyle(
            color = labelColor,
            fontSize = targetFontSize,
            fontWeight = if (nakName != null) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    )

    // Safety constraint: strictly ensure text does not overflow cell width
    val maxAllowedWidth = cellSize * 0.84f
    if (labelResult.size.width > maxAllowedWidth && labelResult.size.width > 0) {
        val scaleDownRatio = maxAllowedWidth / labelResult.size.width.toFloat()
        labelResult = textMeasurer.measure(
            text = AnnotatedString(primaryLabel),
            style = TextStyle(
                color = labelColor,
                fontSize = (targetFontSize.value * scaleDownRatio).sp,
                fontWeight = if (nakName != null) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }

    val labelOffsetY = if (hasOccupants) {
        cellTopLeft.y + cellSize * 0.54f
    } else {
        cellTopLeft.y + (cellSize - labelResult.size.height) / 2f
    }

    drawText(
        textLayoutResult = labelResult,
        topLeft = Offset(
            x = cellTopLeft.x + (cellSize - labelResult.size.width) / 2f,
            y = labelOffsetY
        )
    )

    // 5. Stock Market Ticker Count Badge (Top-Right of Nakshatra Cell)
    if (showTickerBadges && nakName != null && tickerCount > 0) {
        val badgeRadius = cellSize * 0.13f
        val badgeCenter = Offset(
            x = cellTopLeft.x + cellSize - badgeRadius - 2f,
            y = cellTopLeft.y + badgeRadius + 2f
        )

        // Draw Badge Background Pill
        drawCircle(
            color = CelestialCyan,
            radius = badgeRadius,
            center = badgeCenter
        )

        // Draw Ticker Count inside Badge
        val countTextResult = textMeasurer.measure(
            text = AnnotatedString("$tickerCount"),
            style = TextStyle(
                color = CosmicDeepNavy,
                fontSize = (badgeRadius * 1.15f).toSp(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        drawText(
            textLayoutResult = countTextResult,
            topLeft = Offset(
                x = badgeCenter.x - countTextResult.size.width / 2f,
                y = badgeCenter.y - countTextResult.size.height / 2f
            )
        )
    }
}

/**
 * Draws Front (Samukha) and Diagonal (Dakshina / Vama) Vedha aspect rays across the Canvas.
 */
private fun DrawScope.drawVedhaRays(
    sbcState: SarvatobhadraState,
    cellCenters: Array<Array<Offset>>,
    selectedNakshatra: String?,
    rayFilterMode: SbcRayFilterMode
) {
    sbcState.activeVedhas.forEach { vedha ->
        val isBenefic = vedha.isBenefic

        // Filter rays according to user preference
        if (rayFilterMode == SbcRayFilterMode.BENEFIC_ONLY && !isBenefic) return@forEach
        if (rayFilterMode == SbcRayFilterMode.MALEFIC_ONLY && isBenefic) return@forEach

        val planetNakIndex = sbcState.planetaryPositions[vedha.planet] ?: return@forEach
        val sourceCoords = getCoordsForNakshatraIndex(planetNakIndex) ?: return@forEach
        val targetCoords = getCoordsForNakshatraName(vedha.targetNakshatra) ?: return@forEach

        val isDirectlyConnectedToSelected = selectedNakshatra != null &&
                (vedha.targetNakshatra.equals(selectedNakshatra, ignoreCase = true) ||
                        SarvatobhadraEngine.SBC_NAKSHATRAS[planetNakIndex].equals(selectedNakshatra, ignoreCase = true))

        if (rayFilterMode == SbcRayFilterMode.SELECTED_ONLY && !isDirectlyConnectedToSelected) {
            return@forEach
        }

        val start = cellCenters[sourceCoords.first][sourceCoords.second]
        val end = cellCenters[targetCoords.first][targetCoords.second]

        val rayColor = if (isBenefic) {
            if (isDirectlyConnectedToSelected) BullishEmeraldLight else BullishEmerald.copy(alpha = 0.45f)
        } else {
            if (isDirectlyConnectedToSelected) BearishRubyLight else BearishRuby.copy(alpha = 0.40f)
        }

        val strokeWidth = if (isDirectlyConnectedToSelected) 3.5f else 1.5f

        if (isBenefic) {
            // Smooth solid luminous line for benefic Vedha
            drawLine(
                color = rayColor,
                start = start,
                end = end,
                strokeWidth = strokeWidth
            )
        } else {
            // Dashed line for malefic affliction Vedha
            drawLine(
                color = rayColor,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
        }
    }
}

/**
 * Returns Nakshatra name for perimeter coordinates in the standard 9x9 Sarvatobhadra layout.
 */
fun getNakshatraNameForCell(row: Int, col: Int): String? {
    return when {
        // Top Row (0, 1..7): Krittika to Ashlesha
        row == 0 && col in 1..7 -> SarvatobhadraEngine.SBC_NAKSHATRAS.getOrNull(col - 1)

        // Right Column (1..7, 8): Magha to Vishakha
        col == 8 && row in 1..7 -> SarvatobhadraEngine.SBC_NAKSHATRAS.getOrNull(6 + row)

        // Bottom Row (8, 7 down to 1): Anuradha to Shravana
        row == 8 && col in 1..7 -> SarvatobhadraEngine.SBC_NAKSHATRAS.getOrNull(21 - col)

        // Left Column (7 down to 1, 0): Dhanishta to Bharani
        col == 0 && row in 1..7 -> SarvatobhadraEngine.SBC_NAKSHATRAS.getOrNull(28 - row)

        else -> null
    }
}

/**
 * Maps Nakshatra index (0..27) to (row, col) on the 9x9 perimeter.
 */
private fun getCoordsForNakshatraIndex(index: Int): Pair<Int, Int>? {
    return when (index) {
        in 0..6 -> Pair(0, index + 1)
        in 7..13 -> Pair(index - 6, 8)
        in 14..20 -> Pair(8, 21 - index)
        in 21..27 -> Pair(28 - index, 0)
        else -> null
    }
}

/**
 * Maps Nakshatra name to perimeter (row, col).
 */
private fun getCoordsForNakshatraName(name: String): Pair<Int, Int>? {
    val index = SarvatobhadraEngine.SBC_NAKSHATRAS.indexOfFirst { it.equals(name, ignoreCase = true) }
    if (index == -1) return null
    return getCoordsForNakshatraIndex(index)
}

/**
 * 4 Corner Vowels (Swaras) in Sarvatobhadra Mandala (Devanagari, Roman)
 */
private fun getCornerVowelPair(row: Int, col: Int): Pair<String, String> {
    return when {
        row == 0 && col == 0 -> Pair("अ", "A")
        row == 0 && col == 8 -> Pair("आ", "Aa")
        row == 8 && col == 8 -> Pair("इ", "I")
        row == 8 && col == 0 -> Pair("ई", "Ee")
        else -> Pair("", "")
    }
}

/**
 * Inner tier symbols (Consonants / Rasis / Varas)
 */
private fun getInnerSymbolForCell(row: Int, col: Int): String {
    // 12 Rasis in tier 3
    if (row == 2 && col in 2..6) return listOf("Ari", "Tau", "Gem", "Can", "Leo")[col - 2]
    if (row == 6 && col in 2..6) return listOf("Pis", "Aqu", "Cap", "Sag", "Sco")[col - 2]
    if (col == 6 && row in 3..5) return listOf("Vir", "Lib", "Sco")[row - 3]
    if (col == 2 && row in 3..5) return listOf("Pis", "Aqu", "Cap")[row - 3]

    // Consonants in tier 2
    if (row == 1) return listOf("क", "ख", "ग", "घ", "ङ", "च", "छ")[col - 1]
    if (row == 7) return listOf("प", "फ", "ब", "भ", "म", "य", "र")[col - 1]
    if (col == 1) return listOf("ल", "व", "श", "ष", "स")[row - 2]
    if (col == 7) return listOf("ज", "झ", "ञ", "ट", "ठ")[row - 2]

    return "•"
}

@Composable
private fun CanvasLegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = TextSecondaryLight
        )
    }
}
