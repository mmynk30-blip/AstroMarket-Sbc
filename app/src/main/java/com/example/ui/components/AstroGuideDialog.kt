package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AstroGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vedic Financial Astrology Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryLight
                        )
                    }
                }

                HorizontalDivider(color = CosmicCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GuideSection(
                        title = "1. Sarvatobhadra Chakra (SBC)",
                        content = "• The Sarvatobhadra Chakra is an ancient 9x9 mandala containing 81 squares.\n" +
                                "• Outermost perimeter maps the 28 Vedic Nakshatras (including Abhijit, the 28th constellation between Sagittarius & Capricorn).\n" +
                                "• Inner rings house 16 Swaras (vowels), 32 Varnas (consonants matching company initials like 'R' for Reliance, 'T' for Tata), and 12 Rasis.\n" +
                                "• Planets transiting the perimeter cast 3 kinds of Vedhas (rays):\n" +
                                "   1. Front Vedha (Samukha): Straight line across the grid hitting the opposite sector.\n" +
                                "   2. Right Vedha (Dakshina): 90-degree right diagonal aspect.\n" +
                                "   3. Left Vedha (Vama): 90-degree left diagonal aspect.\n" +
                                "• Benefic planets (Jupiter, Venus, Mercury, waxing Moon) casting Vedhas induce bullish momentum and buying volume.\n" +
                                "• Malefic planets (Saturn, Mars, Rahu, Ketu, Sun) casting Vedhas cause price corrections, resistance, and sudden market sell-offs."
                    )

                    GuideSection(
                        title = "2. The Hora System (Planetary Hours)",
                        content = "• Every solar day is divided into 24 Horas (planetary hours) starting from local sunrise (~06:00 AM).\n" +
                                "• The 1st Hora of the day is always ruled by the Day Lord (e.g., Sunday = Sun, Monday = Moon, Tuesday = Mars, etc.).\n" +
                                "• Horas then strictly rotate according to the Chaldean order: Sun → Venus → Mercury → Moon → Saturn → Jupiter → Mars.\n" +
                                "• Jupiter Hora is the Golden Hour: High institutional buying in BankNifty & broad index.\n" +
                                "• Saturn Hora creates market drag: High selling pressure in metals, coal, and heavy industry.\n" +
                                "• Mercury Hora fuels algorithmic trading: Fast scalping moves in IT, Fintech & tech shares.\n" +
                                "• Mars Hora fuels volatility: Explosive breakout spikes, defense & short squeezes."
                    )

                    GuideSection(
                        title = "3. Panchang Timing & Muhurta",
                        content = "• Panchang consists of 5 limbs: Tithi (Lunar day), Vaara (Solar weekday), Nakshatra, Yoga, and Karana.\n" +
                                "• Tithi categories:\n" +
                                "   - Nanda & Jaya: Auspicious for initiating fresh bullish positions.\n" +
                                "   - Rikta (4th, 9th, 14th): 'Empty hands' — sharp stop-loss hunting & high risk.\n" +
                                "• Inauspicious vs Auspicious Timing Windows:\n" +
                                "   - Rahu Kaal (90 min daily): Known for sudden fake breakouts and intraday traps.\n" +
                                "   - Abhijit Muhurta (~11:48 - 12:38 PM): The golden solar apex window, frequently acting as a market trend reversal pivot or recovery bounce."
                    )

                    GuideSection(
                        title = "4. Intraday Harmonic Time Cycles",
                        content = "• Markets do not move randomly; they oscillate in harmonic mathematical time cycles.\n" +
                                "• By combining the active Hora Lord, the Moon's transit degree, and Sarvatobhadra Vedha angles, turning point windows (pivots) are projected.\n" +
                                "• Use these pivot times (e.g. 10:24 AM, 11:18 AM, 13:15 PM) to look for candlestick exhaustion patterns, double tops/bottoms, or trend breakouts with disciplined stop-losses."
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CosmicSurfaceElevated,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VedicGoldLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                lineHeight = 18.sp
            )
        }
    }
}
