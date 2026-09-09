package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SarvatobhadraEngine
import com.example.model.*
import com.example.ui.theme.*

/**
 * Interactive control panel and manager for mapping stock market tickers
 * to Lunar Mansions (Nakshatras) in the Sarvatobhadra Chakra.
 */
@Composable
fun StockTickerNakshatraPanel(
    sbcState: SarvatobhadraState,
    tickerMappings: List<StockTickerNakshatraMapping>,
    selectedNakshatraName: String?,
    onSelectNakshatra: (String) -> Unit,
    onAddTickerMapping: (StockTickerNakshatraMapping) -> Unit,
    onRemoveTickerMapping: (StockTickerNakshatraMapping) -> Unit,
    onHighlightTicker: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddTickerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Selected Nakshatra Tickers, 1: Directory of All 28 Mansions

    // Filtered tickers based on search query
    val searchResults = remember(searchQuery, tickerMappings) {
        if (searchQuery.isBlank()) emptyList()
        else tickerMappings.filter {
            it.tickerSymbol.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.companyName.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.nakshatraName.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.sector.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    // Tickers mapped to currently selected nakshatra (defaults to Krittika or first with tickers)
    val activeNakshatra = selectedNakshatraName ?: "Krittika"
    val mappedTickersForActiveNakshatra = remember(activeNakshatra, tickerMappings) {
        tickerMappings.filter { it.nakshatraName.equals(activeNakshatra, ignoreCase = true) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Title and Add Ticker Action
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
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = VedicGold
                    )
                    Text(
                        text = "Stock Ticker Mapping",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Button(
                    onClick = { showAddTickerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = VedicGoldDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Map New Ticker",
                        tint = VedicGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Map Ticker",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VedicGoldLight
                    )
                }
            }

            // Search Bar for Stock Tickers
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isNotBlank()) {
                        val firstMatch = searchResults.firstOrNull()
                        onHighlightTicker(firstMatch?.tickerSymbol)
                    } else {
                        onHighlightTicker(null)
                    }
                },
                placeholder = {
                    Text(
                        text = "Search ticker (e.g. RELIANCE, NIFTY, TCS, INFY)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedLight
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CelestialCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onHighlightTicker(null)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VedicGold,
                    unfocusedBorderColor = CosmicCardBorder,
                    focusedContainerColor = CosmicSurfaceElevated,
                    unfocusedContainerColor = CosmicSurfaceElevated,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Instant Search Result Chips
            AnimatedVisibility(visible = searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Found in SBC (${searchResults.size} matches):",
                        style = MaterialTheme.typography.labelSmall,
                        color = CelestialCyan
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchResults) { result ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CosmicDeepNavy,
                                border = BorderStroke(1.dp, VedicGold.copy(alpha = 0.6f)),
                                modifier = Modifier.clickable {
                                    onSelectNakshatra(result.nakshatraName)
                                    onHighlightTicker(result.tickerSymbol)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = result.tickerSymbol,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGoldLight
                                    )
                                    Text(
                                        text = "→ ${result.nakshatraName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CelestialCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Lunar Mansion Selector Carousel
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Lunar Mansions (28 Nakshatras):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SarvatobhadraEngine.SBC_NAKSHATRAS) { nakName ->
                        val isSelected = nakName.equals(activeNakshatra, ignoreCase = true)
                        val count = tickerMappings.count { it.nakshatraName.equals(nakName, ignoreCase = true) }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) VedicGoldDark else CosmicSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) VedicGold else CosmicCardBorder
                            ),
                            modifier = Modifier.clickable {
                                onSelectNakshatra(nakName)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = nakName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) VedicGoldLight else TextPrimaryLight
                                )
                                if (count > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) VedicGoldLight else CelestialCyan.copy(alpha = 0.2f),
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$count",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) CosmicDeepNavy else CelestialCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Mansion Detail Banner
            ActiveNakshatraHeader(
                nakshatraName = activeNakshatra,
                sbcState = sbcState,
                mappedTickerCount = mappedTickersForActiveNakshatra.size
            )

            // Mapped Stock Tickers List
            if (mappedTickersForActiveNakshatra.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mappedTickersForActiveNakshatra.forEach { mapping ->
                        val posture = remember(mapping, sbcState) {
                            SarvatobhadraTickerRepository.evaluateTickerPosture(mapping, sbcState)
                        }
                        MappedTickerItemRow(
                            posture = posture,
                            onRemove = { onRemoveTickerMapping(mapping) }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CosmicDeepNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Addchart,
                            contentDescription = null,
                            tint = TextMutedLight,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "No stock tickers currently mapped to $activeNakshatra.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                        TextButton(onClick = { showAddTickerDialog = true }) {
                            Text("Map a stock ticker to $activeNakshatra", color = VedicGold)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Map Custom Stock Ticker to Lunar Mansion
    if (showAddTickerDialog) {
        AddStockTickerMappingDialog(
            defaultNakshatra = activeNakshatra,
            onDismiss = { showAddTickerDialog = false },
            onConfirm = { newMapping ->
                onAddTickerMapping(newMapping)
                onSelectNakshatra(newMapping.nakshatraName)
                showAddTickerDialog = false
            }
        )
    }
}

/**
 * Header card summarizing astrological qualities and vedha conditions of the selected Lunar Mansion.
 */
@Composable
private fun ActiveNakshatraHeader(
    nakshatraName: String,
    sbcState: SarvatobhadraState,
    mappedTickerCount: Int
) {
    val nakIndex = SarvatobhadraEngine.SBC_NAKSHATRAS.indexOf(nakshatraName)
    val occupyingPlanets = sbcState.planetaryPositions
        .filter { it.value == nakIndex }
        .map { it.key }

    val incomingVedhas = sbcState.activeVedhas
        .filter { it.targetNakshatra.equals(nakshatraName, ignoreCase = true) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CosmicSurfaceElevated,
        border = BorderStroke(1.dp, VedicGold.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nakshatraName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGoldLight
                    )
                    Surface(
                        color = CosmicDeepNavy,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$mappedTickerCount stocks mapped",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CelestialCyan
                        )
                    }
                }

                if (occupyingPlanets.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transit:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                        occupyingPlanets.forEach { planet ->
                            Surface(
                                shape = CircleShape,
                                color = VedicGoldDark,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = planet.symbol,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGoldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary of active Vedhas impacting this Lunar Mansion
            if (incomingVedhas.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    incomingVedhas.take(3).forEach { vedha ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (vedha.isBenefic) BullishContainer else BearishContainer
                        ) {
                            Text(
                                text = "${vedha.planet.englishName} (${vedha.vedhaType.name})",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (vedha.isBenefic) BullishEmeraldLight else BearishRubyLight
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single stock ticker mapping item row displaying its evaluated posture.
 */
@Composable
private fun MappedTickerItemRow(
    posture: StockTickerAstroPosture,
    onRemove: () -> Unit
) {
    val mapping = posture.mapping
    val isPositive = posture.sentimentScore >= 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CosmicDeepNavy,
        border = BorderStroke(
            1.dp,
            if (isPositive) BullishEmerald.copy(alpha = 0.3f) else BearishRuby.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mapping.tickerSymbol,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Surface(
                        color = CosmicSurfaceElevated,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = mapping.exchange,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextSecondaryLight
                        )
                    }
                    if (mapping.isCustomUserMapped) {
                        Surface(
                            color = VedicGoldDark.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Custom",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = VedicGoldLight
                            )
                        }
                    }
                }

                Text(
                    text = "${mapping.companyName} • ${mapping.sector}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = posture.summaryText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isPositive) BullishEmeraldLight else BearishRubyLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sentiment Score Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = posture.stance.containerColor
                ) {
                    Text(
                        text = "${if (posture.sentimentScore > 0) "+" else ""}${posture.sentimentScore} Net",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = posture.stance.badgeColor
                    )
                }

                Text(
                    text = posture.stance.label.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = posture.stance.badgeColor
                )
            }

            if (mapping.isCustomUserMapped) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove Ticker",
                        tint = BearishRubyLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to add / map a new stock ticker to any of the 28 Lunar Mansions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStockTickerMappingDialog(
    defaultNakshatra: String,
    onDismiss: () -> Unit,
    onConfirm: (StockTickerNakshatraMapping) -> Unit
) {
    var tickerSymbol by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var selectedNakshatra by remember { mutableStateOf(defaultNakshatra) }
    var sector by remember { mutableStateOf("") }
    var exchange by remember { mutableStateOf("NSE") }

    var expandedNakshatraDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Addchart,
                    contentDescription = null,
                    tint = VedicGold
                )
                Text(
                    text = "Map Stock to Nakshatra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Map any global or domestic stock market ticker to its lunar mansion to trace planetary Vedhas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )

                // Ticker Symbol
                OutlinedTextField(
                    value = tickerSymbol,
                    onValueChange = { tickerSymbol = it.uppercase() },
                    label = { Text("Ticker Symbol (e.g. NVDA, ZOMATO)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name (e.g. Nvidia Corp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Lunar Mansion Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedNakshatraDropdown,
                    onExpandedChange = { expandedNakshatraDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedNakshatra,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lunar Mansion (Nakshatra)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNakshatraDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedNakshatraDropdown,
                        onDismissRequest = { expandedNakshatraDropdown = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        SarvatobhadraEngine.SBC_NAKSHATRAS.forEach { nak ->
                            DropdownMenuItem(
                                text = { Text(nak) },
                                onClick = {
                                    selectedNakshatra = nak
                                    expandedNakshatraDropdown = false
                                }
                            )
                        }
                    }
                }

                // Sector
                OutlinedTextField(
                    value = sector,
                    onValueChange = { sector = it },
                    label = { Text("Sector / Industry (e.g. AI Hardware)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Exchange Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("NSE", "BSE", "NASDAQ", "NYSE", "MCX").forEach { exch ->
                        FilterChip(
                            selected = exchange == exch,
                            onClick = { exchange = exch },
                            label = { Text(exch, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tickerSymbol.isNotBlank()) {
                        onConfirm(
                            StockTickerNakshatraMapping(
                                tickerSymbol = tickerSymbol.trim(),
                                companyName = companyName.ifBlank { tickerSymbol.trim() },
                                nakshatraName = selectedNakshatra,
                                sector = sector.ifBlank { "General Equities" },
                                exchange = exchange,
                                isCustomUserMapped = true
                            )
                        )
                    }
                },
                enabled = tickerSymbol.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VedicGold)
            ) {
                Text("Confirm Mapping", color = CosmicDeepNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryLight)
            }
        },
        containerColor = CosmicSurfaceElevated
    )
}
