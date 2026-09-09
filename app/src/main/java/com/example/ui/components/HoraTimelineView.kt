package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HoraTimelineView(
    horas: List<HoraPeriod>,
    currentHour: Int,
    modifier: Modifier = Modifier
) {
    var showOnlyMarketHours by remember { mutableStateOf(true) }

    val displayedHoras = remember(horas, showOnlyMarketHours) {
        if (showOnlyMarketHours) horas.filter { it.isMarketHours } else horas
    }

    val currentActiveHora = remember(horas) {
        horas.find { it.isCurrentHora } ?: horas.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Spotlight on Current Active Hora
        item {
            currentActiveHora?.let { activeHora ->
                CurrentHoraSpotlightCard(hora = activeHora)
            }
        }

        // Filter toggle
        item {
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
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = VedicGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Intraday Hora Cycle Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                FilterChip(
                    selected = showOnlyMarketHours,
                    onClick = { showOnlyMarketHours = !showOnlyMarketHours },
                    label = {
                        Text(
                            text = if (showOnlyMarketHours) "Market Session" else "Full 24h",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CosmicSurfaceElevated,
                        selectedLabelColor = VedicGoldLight
                    )
                )
            }
        }

        // Hora cards
        items(displayedHoras, key = { it.horaIndex }) { hora ->
            HoraCard(hora = hora)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrentHoraSpotlightCard(hora: HoraPeriod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(VedicGold)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BullishEmerald)
                    )
                    Text(
                        text = "CURRENT ACTIVE HORA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                }

                Surface(
                    color = CosmicDeepNavy,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${hora.startTime} - ${hora.endTime}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CosmicDeepNavy,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(hora.planet.displayColor))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = hora.planet.symbol,
                                fontSize = 24.sp,
                                color = hora.planet.displayColor
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "${hora.planet.englishName} Hora (${hora.planet.sanskritName})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = hora.planet.marketRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                Surface(
                    color = Color(hora.marketBias.badgeColorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = hora.marketBias.title,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(hora.marketBias.badgeColorHex)
                    )
                }
            }

            // Power Score Meter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Intraday Power Meter",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "${if (hora.powerScore >= 0) "+" else ""}${hora.powerScore} Power",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hora.powerScore >= 0) BullishEmeraldLight else BearishRubyLight
                    )
                }
                LinearProgressIndicator(
                    progress = { ((hora.powerScore + 100) / 200f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (hora.powerScore >= 0) BullishEmerald else BearishRuby,
                    trackColor = CosmicDeepNavy
                )
            }

            // Trading Directive Quote
            Surface(
                color = CosmicDeepNavy,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = VedicGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = hora.tradingTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight
                    )
                }
            }

            // Favorable Sectors
            if (hora.favorableSectors.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hora.favorableSectors.forEach { sector ->
                        Surface(
                            color = BullishContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "↑ $sector",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BullishEmeraldLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HoraCard(hora: HoraPeriod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hora.isCurrentHora) VedicGold else CosmicCardBorder
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CosmicDeepNavy,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = hora.planet.symbol,
                                fontSize = 18.sp,
                                color = hora.planet.displayColor
                            )
                        }
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${hora.startTime} - ${hora.endTime}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            if (hora.isCurrentHora) {
                                Surface(
                                    color = VedicGoldDark,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "NOW",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGoldLight
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${hora.planet.englishName} (${hora.planet.sanskritName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = VedicGoldLight
                        )
                    }
                }

                Surface(
                    color = Color(hora.marketBias.badgeColorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = hora.marketBias.title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(hora.marketBias.badgeColorHex)
                    )
                }
            }

            Text(
                text = hora.tradingTip,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )

            // Favorable vs Cautious chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                hora.favorableSectors.take(3).forEach { sec ->
                    Surface(
                        color = CosmicDeepNavy,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "✓ $sec",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = BullishEmeraldLight
                        )
                    }
                }
                hora.cautiousSectors.take(2).forEach { sec ->
                    Surface(
                        color = CosmicDeepNavy,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "⚠ $sec",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = BearishRubyLight
                        )
                    }
                }
            }
        }
    }
}
