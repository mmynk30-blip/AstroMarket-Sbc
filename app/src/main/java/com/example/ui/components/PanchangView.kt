package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun PanchangView(
    panchang: PanchangDetails,
    onOpenSettings: () -> Unit = {},
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
        // Location & Ayanamsa Ephemeris Header Strip
        PanchangLocationAyanamsaBanner(
            panchang = panchang,
            onOpenSettings = onOpenSettings
        )

        // Sentiment & Macro Panchang Banner
        PanchangMacroBanner(panchang = panchang)

        // The 5 Limbs of Panchang
        Text(
            text = "The 5 Limbs of Financial Time (Panchang)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )

        // Tithi Card
        LimbCard(
            title = "1. Tithi (Lunar Day)",
            value = "${panchang.tithiName} (#${panchang.tithiNumber})",
            badge = panchang.tithiCategory.title,
            badgeColor = when (panchang.tithiCategory) {
                TithiCategory.NANDA -> BullishEmerald
                TithiCategory.JAYA -> VedicGold
                TithiCategory.BHADRA -> CelestialCyan
                TithiCategory.PURNA -> CelestialViolet
                TithiCategory.RIKTA -> BearishRuby
            },
            subtitle = panchang.paksha,
            description = panchang.tithiCategory.financialSignificance,
            icon = Icons.Default.Brightness3
        )

        // Vaara (Day Lord) Card
        LimbCard(
            title = "2. Vaara (Solar Day)",
            value = "${panchang.dayOfWeek} (Lord: ${panchang.dayLord.englishName})",
            badge = panchang.dayLord.symbol,
            badgeColor = panchang.dayLord.displayColor,
            subtitle = "Sanskrit: ${panchang.dayLord.sanskritName}",
            description = "Market Tone: ${panchang.dayLord.marketRole}. Macro policy backdrop influenced by ${panchang.dayLord.englishName}.",
            icon = Icons.Default.WbSunny
        )

        // Nakshatra Card
        LimbCard(
            title = "3. Nakshatra (Constellation)",
            value = "${panchang.nakshatraName} (Lord: ${panchang.nakshatraLord.englishName})",
            badge = panchang.nakshatraNature.title.split(" ").first(),
            badgeColor = when (panchang.nakshatraNature) {
                NakshatraNature.KSHIPRA -> CelestialCyan
                NakshatraNature.UGRA -> BearishRuby
                NakshatraNature.STHIRA -> BullishEmerald
                NakshatraNature.CHARA -> VedicGold
                NakshatraNature.MRIDU -> CelestialViolet
                NakshatraNature.MISHRA -> VolatileAmber
            },
            subtitle = "Characteristic: ${panchang.nakshatraNature.title}",
            description = panchang.nakshatraNature.marketBehavior,
            icon = Icons.Default.AutoAwesome
        )

        // Yoga Card
        LimbCard(
            title = "4. Yoga (Sun-Moon Angle)",
            value = panchang.yogaName,
            badge = if (panchang.isYogaAuspicious) "Auspicious Flow" else "Volatile Alert",
            badgeColor = if (panchang.isYogaAuspicious) BullishEmerald else BearishRuby,
            subtitle = if (panchang.isYogaAuspicious) "Positive Astro Angular Harmony" else "Inauspicious Friction Zone",
            description = panchang.yogaMarketEffect,
            icon = Icons.Default.CompareArrows
        )

        // Karana Card
        LimbCard(
            title = "5. Karana (Half-Tithi)",
            value = panchang.karanaName,
            badge = if (panchang.isVishtiKarana) "Bhadra Warning" else "Stable Karana",
            badgeColor = if (panchang.isVishtiKarana) BearishRuby else CelestialCyan,
            subtitle = if (panchang.isVishtiKarana) "Vishti (Bhadra) Active" else "Standard Execution",
            description = if (panchang.isVishtiKarana) {
                "Vishti Karana induces market turbulence, false breakouts, and stop-loss hunting. Avoid fresh breakout longs."
            } else {
                "Orderly execution environment with reliable bid-ask spread depth."
            },
            icon = Icons.Default.Timeline
        )

        // Celestial Sign Alignments
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Moon Sign (Chandra Rasi)", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = panchang.moonRasi, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MoonSilver)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(CosmicCardBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Sun Sign (Surya Rasi)", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = panchang.sunRasi, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VedicGold)
                }
            }
        }

        // Auspicious & Inauspicious Timing Windows
        Text(
            text = "Crucial Astrological Timing Windows",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )

        TimingWindowCard(window = panchang.abhijitMuhurta)
        TimingWindowCard(window = panchang.rahuKaal)
        TimingWindowCard(window = panchang.yamagandaKaal)
        TimingWindowCard(window = panchang.gulikaKaal)
    }
}

@Composable
private fun PanchangMacroBanner(panchang: PanchangDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = panchang.dateString,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                    Text(
                        text = "Vedic Panchang & Timing Cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                val score = panchang.marketSentimentScore
                Surface(
                    color = if (score >= 0) BullishContainer else BearishContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${if (score >= 0) "+" else ""}$score Sentiment",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 0) BullishEmeraldLight else BearishRubyLight
                    )
                }
            }

            // Sentiment Score bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vedic Financial Health Gauge",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = if (panchang.marketSentimentScore >= 30) "Favorable Bullish Wind"
                        else if (panchang.marketSentimentScore >= 0) "Balanced / Cautious Long"
                        else "Defensive / Volatile Drag",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (panchang.marketSentimentScore >= 0) BullishEmeraldLight else BearishRubyLight
                    )
                }
                LinearProgressIndicator(
                    progress = { ((panchang.marketSentimentScore + 100) / 200f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (panchang.marketSentimentScore >= 0) BullishEmerald else BearishRuby,
                    trackColor = CosmicDeepNavy
                )
            }
        }
    }
}

@Composable
private fun LimbCard(
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    subtitle: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CosmicCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    Icon(imageVector = icon, contentDescription = null, tint = VedicGold, modifier = Modifier.size(18.dp))
                    Text(text = title, style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = VedicGoldLight
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )
        }
    }
}

@Composable
private fun TimingWindowCard(window: TimingWindow) {
    val isGolden = window.cautionLevel == "Golden Hour"
    val isRisk = window.cautionLevel == "High Risk"

    val containerColor = when {
        isGolden -> Color(0xFF1E2F23)
        isRisk -> Color(0xFF331B22)
        else -> CosmicSurface
    }

    val borderColor = when {
        isGolden -> BullishEmerald
        isRisk -> BearishRuby
        else -> CosmicCardBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isGolden) BullishEmerald.copy(alpha = 0.2f) else if (isRisk) BearishRuby.copy(alpha = 0.2f) else CosmicSurfaceElevated,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isGolden) Icons.Default.Stars else if (isRisk) Icons.Default.Warning else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (isGolden) BullishEmerald else if (isRisk) BearishRuby else CelestialCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = window.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Surface(
                            color = if (isGolden) BullishEmerald.copy(alpha = 0.2f) else if (isRisk) BearishRuby.copy(alpha = 0.2f) else CosmicDeepNavy,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = window.cautionLevel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGolden) BullishEmeraldLight else if (isRisk) BearishRubyLight else CelestialCyan
                            )
                        }
                    }

                    Text(
                        text = "${window.startTime} - ${window.endTime}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGoldLight
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = window.tradingImpact,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun PanchangLocationAyanamsaBanner(
    panchang: PanchangDetails,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = CosmicSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSettings),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CelestialCyan.copy(alpha = 0.35f))
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CelestialCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = panchang.locationName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Surface(
                    color = VedicGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(VedicGold.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = VedicGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VedicGold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sunrise: ${panchang.sunriseTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = VedicGoldLight
                    )
                    Text(
                        text = "Sunset: ${panchang.sunsetTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = VedicGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${panchang.ayanamsaSystem.displayName} (${panchang.ayanamsaFormatted})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = VedicGoldLight
                    )
                }
            }
        }
    }
}
