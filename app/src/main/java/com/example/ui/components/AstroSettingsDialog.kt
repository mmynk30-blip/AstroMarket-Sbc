package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppSettingsEntity
import com.example.engine.SolarEphemerisEngine
import com.example.model.*
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroSettingsDialog(
    currentSettings: AppSettingsEntity,
    selectedCalendar: Calendar,
    onDismiss: () -> Unit,
    onApplyPreset: (TradingHubPreset, AyanamsaSystem) -> Unit,
    onApplyCustom: (name: String, latitude: Double, longitude: Double, timezoneOffset: Double, AyanamsaSystem) -> Unit,
    onResetDefaults: () -> Unit
) {
    // Local editable state
    var selectedAyanamsa by remember {
        mutableStateOf(
            try {
                AyanamsaSystem.valueOf(currentSettings.ayanamsaSystemCode)
            } catch (e: Exception) {
                AyanamsaSystem.LAHIRI
            }
        )
    }

    var isCustomMode by remember { mutableStateOf(currentSettings.isCustomLocation) }
    var selectedPresetId by remember {
        mutableStateOf(
            TradingHubPresets.PRESETS.find {
                kotlin.math.abs(it.latitude - currentSettings.latitude) < 0.05 &&
                        kotlin.math.abs(it.longitude - currentSettings.longitude) < 0.05
            }?.id ?: "MUMBAI"
        )
    }

    var customCityName by remember { mutableStateOf(currentSettings.locationName) }
    var customLatText by remember { mutableStateOf(currentSettings.latitude.toString()) }
    var customLonText by remember { mutableStateOf(currentSettings.longitude.toString()) }
    var customTzOffset by remember { mutableStateOf(currentSettings.timezoneOffsetHours.toFloat()) }

    // Live preview coordinates
    val activeLat = if (isCustomMode) customLatText.toDoubleOrNull() ?: 19.0760 else {
        TradingHubPresets.PRESETS.find { it.id == selectedPresetId }?.latitude ?: 19.0760
    }
    val activeLon = if (isCustomMode) customLonText.toDoubleOrNull() ?: 72.8777 else {
        TradingHubPresets.PRESETS.find { it.id == selectedPresetId }?.longitude ?: 72.8777
    }
    val activeTz = if (isCustomMode) customTzOffset.toDouble() else {
        TradingHubPresets.PRESETS.find { it.id == selectedPresetId }?.timezoneOffsetHours ?: 5.5
    }

    // Dynamic ephemeris calculation
    val liveEphemeris = remember(selectedCalendar, activeLat, activeLon, activeTz) {
        SolarEphemerisEngine.calculate(
            calendar = selectedCalendar,
            latitude = activeLat,
            longitude = activeLon,
            timezoneOffsetHours = activeTz
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, VedicGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
            color = CosmicSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceElevated)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = VedicGold.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = VedicGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Astro & Panchang Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "Custom Location & Sidereal Ayanamsa",
                                style = MaterialTheme.typography.labelSmall,
                                color = VedicGoldLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = TextSecondaryLight
                        )
                    }
                }

                // Scrollable Content
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Section 1: Ayanamsa Selection
                    AyanamsaSection(
                        selectedAyanamsa = selectedAyanamsa,
                        onSelectAyanamsa = { selectedAyanamsa = it }
                    )

                    // Section 2: Location Selection
                    LocationSection(
                        isCustomMode = isCustomMode,
                        onToggleCustomMode = { isCustomMode = it },
                        selectedPresetId = selectedPresetId,
                        onSelectPreset = {
                            selectedPresetId = it
                            isCustomMode = false
                        },
                        customCityName = customCityName,
                        onCityNameChange = { customCityName = it },
                        customLatText = customLatText,
                        onLatChange = { customLatText = it },
                        customLonText = customLonText,
                        onLonChange = { customLonText = it },
                        customTzOffset = customTzOffset,
                        onTzOffsetChange = { customTzOffset = it }
                    )

                    // Section 3: Live Solar Ephemeris Preview
                    LiveEphemerisPreviewCard(
                        ephemeris = liveEphemeris,
                        lat = activeLat,
                        lon = activeLon,
                        tz = activeTz
                    )
                }

                // Footer Actions
                Surface(
                    color = CosmicSurfaceElevated,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                onResetDefaults()
                                selectedAyanamsa = AyanamsaSystem.LAHIRI
                                isCustomMode = false
                                selectedPresetId = "MUMBAI"
                                customCityName = "Mumbai"
                                customLatText = "19.0760"
                                customLonText = "72.8777"
                                customTzOffset = 5.5f
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reset_defaults_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondaryLight
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Reset", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = {
                                if (isCustomMode) {
                                    val lat = customLatText.toDoubleOrNull() ?: 19.0760
                                    val lon = customLonText.toDoubleOrNull() ?: 72.8777
                                    onApplyCustom(
                                        customCityName.ifBlank { "Custom Location" },
                                        lat,
                                        lon,
                                        customTzOffset.toDouble(),
                                        selectedAyanamsa
                                    )
                                } else {
                                    val preset = TradingHubPresets.PRESETS.find { it.id == selectedPresetId }
                                        ?: TradingHubPresets.DEFAULT
                                    onApplyPreset(preset, selectedAyanamsa)
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .testTag("apply_settings_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VedicGold,
                                contentColor = CosmicDeepNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save & Apply Settings",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AyanamsaSection(
    selectedAyanamsa: AyanamsaSystem,
    onSelectAyanamsa: (AyanamsaSystem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = VedicGold,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "1. Sidereal Ayanamsa System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
        }

        Text(
            text = "Select the angular precession offset for zodiac cusp boundaries, Sarvatobhadra entry cells, and intraday Nakshatra transit moments.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )

        // Readout of currently selected Ayanamsa
        Surface(
            color = CosmicSurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold.copy(alpha = 0.4f)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active Ephemeris Offset:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "${selectedAyanamsa.displayName} • ${selectedAyanamsa.formatDegreesMinutesSeconds()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = VedicGold
                    )
                }
                Surface(
                    color = VedicGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedAyanamsa == AyanamsaSystem.LAHIRI) "Official Standard" else "${selectedAyanamsa.baseValueDegrees}° Base",
                        style = MaterialTheme.typography.labelSmall,
                        color = VedicGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Ayanamsa System Options List
        AyanamsaSystem.values().forEach { sys ->
            val isSelected = sys == selectedAyanamsa
            Surface(
                color = if (isSelected) CosmicSurfaceElevated else CosmicSurfaceElevated.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAyanamsa(sys) }
                    .testTag("ayanamsa_card_${sys.code}"),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isSelected) VedicGold else Color.White.copy(alpha = 0.08f)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectAyanamsa(sys) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = VedicGold,
                            unselectedColor = TextSecondaryLight
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = sys.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) VedicGold else TextPrimaryLight
                            )
                            Text(
                                text = sys.formatDegreesMinutesSeconds(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CelestialCyan else TextSecondaryLight
                            )
                        }

                        Text(
                            text = sys.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )

                        Text(
                            text = "Market Target: ${sys.recommendedUse}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) VedicGoldLight else Color.LightGray.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    isCustomMode: Boolean,
    onToggleCustomMode: (Boolean) -> Unit,
    selectedPresetId: String,
    onSelectPreset: (String) -> Unit,
    customCityName: String,
    onCityNameChange: (String) -> Unit,
    customLatText: String,
    onLatChange: (String) -> Unit,
    customLonText: String,
    onLonChange: (String) -> Unit,
    customTzOffset: Float,
    onTzOffsetChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = CelestialCyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "2. Geographic Location Coordinates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
        }

        Text(
            text = "Local latitude and longitude govern astronomical solar noon, local sunrise, sunset, Ashtama Bhaga divisions (Rahu Kaal, Yamaganda), and Abhijit Muhurta.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )

        // Mode Switcher: Presets vs Custom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicSurfaceElevated)
                .padding(4.dp)
        ) {
            TabButton(
                title = "Financial Trading Hubs",
                icon = Icons.Default.AccountBalance,
                isSelected = !isCustomMode,
                onClick = { onToggleCustomMode(false) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "Custom GPS Coordinates",
                icon = Icons.Default.EditLocation,
                isSelected = isCustomMode,
                onClick = { onToggleCustomMode(true) },
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedContent(
            targetState = isCustomMode,
            label = "LocationModeTransition"
        ) { isCustom ->
            if (!isCustom) {
                // Trading Hub Presets Carousel
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Primary Market Exchange Location:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(TradingHubPresets.PRESETS) { preset ->
                            val isSelected = preset.id == selectedPresetId
                            Surface(
                                color = if (isSelected) CosmicSurfaceElevated else CosmicSurfaceElevated.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { onSelectPreset(preset.id) }
                                    .testTag("preset_chip_${preset.id}"),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) CelestialCyan else Color.White.copy(alpha = 0.08f)
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = preset.countryFlag, fontSize = 20.sp)
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = CelestialCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CelestialCyan else TextPrimaryLight
                                    )

                                    Text(
                                        text = preset.marketLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryLight,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = "${preset.latitude}° N, ${preset.longitude}° E",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VedicGoldLight,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Custom Coordinates Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceElevated, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customCityName,
                        onValueChange = onCityNameChange,
                        label = { Text("Location / City Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_city_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelestialCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CelestialCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customLatText,
                            onValueChange = onLatChange,
                            label = { Text("Latitude (-90 to +90)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_lat_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CelestialCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        OutlinedTextField(
                            value = customLonText,
                            onValueChange = onLonChange,
                            label = { Text("Longitude (-180 to +180)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_lon_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CelestialCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UTC Timezone Offset:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryLight
                            )
                            Text(
                                text = "UTC ${if (customTzOffset >= 0) "+$customTzOffset" else "$customTzOffset"} hours",
                                style = MaterialTheme.typography.labelSmall,
                                color = CelestialCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = customTzOffset,
                            onValueChange = onTzOffsetChange,
                            valueRange = -12f..14f,
                            steps = 51,
                            colors = SliderDefaults.colors(
                                thumbColor = CelestialCyan,
                                activeTrackColor = CelestialCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("custom_tz_slider")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveEphemerisPreviewCard(
    ephemeris: com.example.engine.SolarEphemeris,
    lat: Double,
    lon: Double,
    tz: Double
) {
    Surface(
        color = CosmicSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold.copy(alpha = 0.35f)))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = VedicGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Live Astronomical Solar Ephemeris Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VedicGoldLight
                )
            }

            Text(
                text = "Computed for coordinates: ${String.format("%.4f", lat)}° Lat, ${String.format("%.4f", lon)}° Lon (UTC ${if (tz >= 0) "+$tz" else "$tz"}h)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight,
                fontSize = 11.sp
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EphemerisPill(label = "Sunrise", value = ephemeris.sunriseTime, icon = Icons.Default.WbTwilight, color = VedicGold)
                EphemerisPill(label = "Local Noon", value = ephemeris.solarNoonTime, icon = Icons.Default.WbSunny, color = BullishEmerald)
                EphemerisPill(label = "Sunset", value = ephemeris.sunsetTime, icon = Icons.Default.Bedtime, color = BearishRuby)
                EphemerisPill(label = "Daylight", value = ephemeris.daylightHoursFormatted, icon = Icons.Default.Timer, color = CelestialCyan)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // Calculated critical timing windows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = BullishEmerald.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BullishEmerald.copy(alpha = 0.3f)))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Abhijit Muhurta (Peak)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BullishEmerald
                        )
                        Text(
                            text = "${ephemeris.abhijitMuhurtaWindow.startTime} - ${ephemeris.abhijitMuhurtaWindow.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }

                Surface(
                    color = BearishRuby.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BearishRuby.copy(alpha = 0.3f)))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Rahu Kaal (Caution)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BearishRuby
                        )
                        Text(
                            text = "${ephemeris.rahuKaalWindow.startTime} - ${ephemeris.rahuKaalWindow.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EphemerisPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) VedicGold.copy(alpha = 0.18f) else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold)) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) VedicGold else TextSecondaryLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) VedicGold else TextSecondaryLight,
                maxLines = 1
            )
        }
    }
}
