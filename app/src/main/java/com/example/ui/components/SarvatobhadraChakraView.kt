package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun SarvatobhadraChakraView(
    sbcState: SarvatobhadraState,
    selectedCell: SbCell?,
    onSelectCell: (SbCell?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var visualMode by remember { mutableIntStateOf(0) } // 0: Canvas Mandala, 1: 9x9 Grid Matrix
    var tickerMappings by remember {
        mutableStateOf(SarvatobhadraTickerRepository.DEFAULT_TICKER_MAPPINGS)
    }
    var selectedNakshatraName by remember {
        mutableStateOf<String?>(
            selectedCell?.let { getNakshatraNameForCell(it.row, it.col) } ?: "Krittika"
        )
    }
    var highlightedTickerSymbol by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Overview Banner
        SbcHeaderBanner(sbcState = sbcState)

        // View Mode Selector (Canvas Mandala vs Grid Matrix)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CosmicSurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CosmicCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Canvas Mandala Tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { visualMode = 0 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (visualMode == 0) VedicGoldDark else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessAuto,
                            contentDescription = null,
                            tint = if (visualMode == 0) VedicGoldLight else TextSecondaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Canvas Mandala View",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (visualMode == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (visualMode == 0) VedicGoldLight else TextSecondaryLight
                        )
                    }
                }

                // Grid Matrix Tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { visualMode = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (visualMode == 1) VedicGoldDark else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = if (visualMode == 1) VedicGoldLight else TextSecondaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Grid Matrix View",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (visualMode == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (visualMode == 1) VedicGoldLight else TextSecondaryLight
                        )
                    }
                }
            }
        }

        // Mode 0: Custom Canvas Sarvatobhadra Chakra Visualizer
        if (visualMode == 0) {
            SarvatobhadraCanvasChakra(
                sbcState = sbcState,
                tickerMappings = tickerMappings,
                selectedCell = selectedCell,
                selectedNakshatraName = selectedNakshatraName,
                highlightedTickerSymbol = highlightedTickerSymbol,
                onSelectNakshatra = { nakName ->
                    selectedNakshatraName = nakName
                },
                onSelectCell = { cell ->
                    onSelectCell(cell)
                    cell?.let {
                        val nakName = getNakshatraNameForCell(it.row, it.col)
                        if (nakName != null) {
                            selectedNakshatraName = nakName
                        }
                    }
                }
            )
        } else {
            // Mode 1: 9x9 Sarvatobhadra Chakra Visualizer Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = VedicGold
                            )
                            Text(
                                text = "81-Square Mandala Grid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                        Text(
                            text = "Tap cell to inspect",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                    }

                    Text(
                        text = "Perimeter: 28 Nakshatras | Inner: Swaras, Varnas & Rasis | Center: Brahma Bindu",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )

                    // The 9x9 Grid with horizontal scroll for smaller screens
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicDeepNavy)
                            .padding(4.dp)
                    ) {
                        val gridHorizontalScroll = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(gridHorizontalScroll)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                sbcState.grid.forEachIndexed { rIndex, rowCells ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        rowCells.forEachIndexed { cIndex, cell ->
                                            val isSelected = selectedCell?.row == rIndex && selectedCell.col == cIndex
                                            SbcGridCellView(
                                                cell = cell,
                                                isSelected = isSelected,
                                                onClick = {
                                                    onSelectCell(cell)
                                                    val nakName = getNakshatraNameForCell(cell.row, cell.col)
                                                    if (nakName != null) {
                                                        selectedNakshatraName = nakName
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Grid Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendItem(color = VedicGold, text = "Planet Occupant")
                        LegendItem(color = BullishEmerald, text = "Benefic Vedha")
                        LegendItem(color = BearishRuby, text = "Malefic Vedha")
                        LegendItem(color = CelestialCyan, text = "Nakshatra")
                    }
                }
            }
        }

        // Stock Market Ticker Nakshatra Mapping & Management Component
        StockTickerNakshatraPanel(
            sbcState = sbcState,
            tickerMappings = tickerMappings,
            selectedNakshatraName = selectedNakshatraName,
            onSelectNakshatra = { nakName ->
                selectedNakshatraName = nakName
            },
            onAddTickerMapping = { newMapping ->
                tickerMappings = tickerMappings + newMapping
            },
            onRemoveTickerMapping = { mappingToRemove ->
                tickerMappings = tickerMappings.filterNot {
                    it.tickerSymbol.equals(mappingToRemove.tickerSymbol, ignoreCase = true)
                }
            },
            onHighlightTicker = { ticker ->
                highlightedTickerSymbol = ticker
                if (ticker != null) {
                    val matching = tickerMappings.find { it.tickerSymbol.equals(ticker, ignoreCase = true) }
                    matching?.let { selectedNakshatraName = it.nakshatraName }
                }
            }
        )

        // Selected Cell Detail Card
        AnimatedVisibility(visible = selectedCell != null) {
            selectedCell?.let { cell ->
                SelectedCellDetailsCard(
                    cell = cell,
                    onDismiss = { onSelectCell(null) }
                )
            }
        }

        // Active Vedhas Impact Matrix
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder)),
            shape = RoundedCornerShape(16.dp)
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = null,
                            tint = CelestialCyan
                        )
                        Text(
                            text = "Active Vedha Rays (Aspects)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                    Surface(
                        color = CosmicSurfaceElevated,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${sbcState.activeVedhas.size} Active Rays",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CelestialCyan
                        )
                    }
                }

                Text(
                    text = "Planets cast Front (Samukha), Right (Dakshina), and Left (Vama) rays across the SBC matrix, affecting corresponding stock market sectors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )

                // List of top active Vedhas
                sbcState.activeVedhas.take(8).forEach { vedha ->
                    VedhaRayRow(vedha = vedha)
                }
            }
        }
    }
}

@Composable
private fun SbcHeaderBanner(sbcState: SarvatobhadraState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
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
                        text = "Sarvatobhadra Chakra",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                    Text(
                        text = "Universal Auspicious Chart of Financial Astrology",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                // Net Score Badge
                val isPositive = sbcState.netVedhaScore >= 0
                Surface(
                    color = if (isPositive) BullishContainer else BearishContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) BullishEmeraldLight else BearishRubyLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${sbcState.netVedhaScore} Net",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) BullishEmeraldLight else BearishRubyLight
                        )
                    }
                }
            }

            // Bullish vs Bearish Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bullish Vedha Power: ${sbcState.bullishVedhaScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = BullishEmeraldLight
                    )
                    Text(
                        text = "Malefic Drag: ${sbcState.bearishVedhaScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = BearishRubyLight
                    )
                }
                LinearProgressIndicator(
                    progress = { sbcState.bullishVedhaScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BullishEmerald,
                    trackColor = BearishRuby,
                )
            }

            // Key Influences
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CosmicSurface,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Benefic Shield",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BullishEmerald
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sbcState.primaryBeneficInfluence,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryLight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = CosmicSurface,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Malefic Drag",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BearishRuby
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sbcState.primaryMaleficPressure,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryLight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SbcGridCellView(
    cell: SbCell,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasOccupant = cell.occupyingPlanets.isNotEmpty()
    val isAfflicted = cell.isAfflicted
    val isBlessed = cell.isBlessed

    val bgColor = when {
        isSelected -> VedicGoldDark
        hasOccupant -> Color(0xFF2E234A)
        cell.cellType == SbCell.CellType.CENTER_BINDU -> Color(0xFF3B2E1E)
        cell.cellType == SbCell.CellType.NAKSHATRA_PERIMETER -> CosmicSurfaceElevated
        cell.cellType == SbCell.CellType.CORNER_SWARA -> Color(0xFF1E2638)
        else -> CosmicSurface
    }

    val borderColor = when {
        isSelected -> VedicGold
        hasOccupant -> VedicGold
        isAfflicted -> BearishRuby
        isBlessed -> BullishEmerald
        cell.cellType == SbCell.CellType.NAKSHATRA_PERIMETER -> CosmicCardBorder
        else -> Color(0xFF1E2842)
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = if (isSelected || hasOccupant) 1.5.dp else 0.8.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasOccupant) {
                // Show planetary symbols
                val planetSymbols = cell.occupyingPlanets.take(2).joinToString("") { it.symbol }
                Text(
                    text = planetSymbols,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VedicGoldLight
                )
            } else {
                Text(
                    text = cell.label.take(4),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = if (cell.cellType == SbCell.CellType.NAKSHATRA_PERIMETER) FontWeight.Bold else FontWeight.Normal,
                    color = if (cell.cellType == SbCell.CellType.CENTER_BINDU) VedicGold else TextPrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            if (cell.subLabel.isNotEmpty() && !hasOccupant) {
                Text(
                    text = cell.subLabel.take(3),
                    fontSize = 7.sp,
                    color = TextSecondaryLight,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SelectedCellDetailsCard(
    cell: SbCell,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold)),
        shape = RoundedCornerShape(14.dp)
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
                        color = VedicGoldDark,
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${cell.row},${cell.col}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VedicGoldLight
                            )
                        }
                    }

                    Column {
                        Text(
                            text = cell.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Category: ${cell.cellType.name.replace("_", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (cell.occupyingPlanets.isNotEmpty()) {
                Text(
                    text = "Occupying Planets: ${cell.occupyingPlanets.joinToString(", ") { "${it.englishName} (${it.symbol})" }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = VedicGold
                )
            }

            if (cell.activeVedhas.isNotEmpty()) {
                Text(
                    text = "Incoming Vedha Aspects:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CelestialCyan
                )
                cell.activeVedhas.forEach { vedha ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${vedha.planet.englishName} (${vedha.vedhaType.title})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (vedha.isBenefic) BullishEmeraldLight else BearishRubyLight
                        )
                        Text(
                            text = vedha.marketEffect,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }
            } else {
                Text(
                    text = "No direct planetary affliction or blessing on this square today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

@Composable
private fun VedhaRayRow(vedha: VedhaImpact) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CosmicDeepNavy,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (vedha.isBenefic) BullishEmerald.copy(alpha = 0.4f) else BearishRuby.copy(alpha = 0.4f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (vedha.isBenefic) BullishContainer else BearishContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = vedha.planet.symbol,
                            fontSize = 18.sp,
                            color = if (vedha.isBenefic) BullishEmeraldLight else BearishRubyLight
                        )
                    }
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${vedha.planet.englishName} → ${vedha.targetNakshatra}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "(${vedha.vedhaType.name})",
                            style = MaterialTheme.typography.labelSmall,
                            color = CelestialCyan
                        )
                    }
                    Text(
                        text = "Sector: ${vedha.targetSector}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VedicGoldLight
                    )
                    Text(
                        text = vedha.marketEffect,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Surface(
                color = if (vedha.isBenefic) BullishEmerald.copy(alpha = 0.15f) else BearishRuby.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (vedha.isBenefic) "+${vedha.impactScore}" else "${vedha.impactScore}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (vedha.isBenefic) BullishEmeraldLight else BearishRubyLight
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = TextSecondaryLight
        )
    }
}
