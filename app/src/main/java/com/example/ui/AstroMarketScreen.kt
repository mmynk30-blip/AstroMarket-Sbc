package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketAsset
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AstroMarketViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroMarketScreen(
    viewModel: AstroMarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.settingsSavedMessage) {
        uiState.settingsSavedMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSettingsSnackbarMessage()
        }
    }

    // Settings Dialog
    if (uiState.showSettingsDialog) {
        AstroSettingsDialog(
            currentSettings = uiState.appSettings,
            selectedCalendar = uiState.selectedCalendar,
            onDismiss = { viewModel.toggleSettingsDialog(false) },
            onApplyPreset = { preset, ayanamsa ->
                viewModel.applyTradingHubPreset(preset, ayanamsa)
            },
            onApplyCustom = { name, lat, lon, tz, ayanamsa ->
                viewModel.applyCustomLocation(name, lat, lon, tz, ayanamsa)
            },
            onResetDefaults = {
                viewModel.resetSettingsToDefault()
            }
        )
    }

    // Date Picker Dialog trigger
    if (uiState.showDatePicker) {
        val currentCal = uiState.selectedCalendar
        val datePickerDialog = remember(uiState.showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newCal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    viewModel.setDate(newCal)
                    viewModel.toggleDatePicker(false)
                },
                currentCal.get(Calendar.YEAR),
                currentCal.get(Calendar.MONTH),
                currentCal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnDismissListener {
                    viewModel.toggleDatePicker(false)
                }
            }
        }
        DisposableEffect(Unit) {
            datePickerDialog.show()
            onDispose {
                datePickerDialog.dismiss()
            }
        }
    }

    // Guide Dialog
    if (uiState.showGuideDialog) {
        AstroGuideDialog(onDismiss = { viewModel.toggleGuideDialog(false) })
    }

    // SBC Market Intensity Alert Center Dialog
    if (uiState.showAlertCenterDialog) {
        SbcAlertCenterDialog(
            currentSettings = uiState.appSettings,
            shiftSummary = uiState.sbcShiftSummary,
            alertHistory = uiState.alertHistory,
            customThresholds = uiState.marketIntensityThresholds,
            onDismiss = { viewModel.toggleAlertCenterDialog(false) },
            onUpdateAlertSettings = { enabled, thresholdDelta, bullish, bearish, onBull, onBear, regimeFlip, abhijit, rahu, harmonics ->
                viewModel.updateAlertSettings(
                    enabled = enabled,
                    thresholdDelta = thresholdDelta,
                    bullishThreshold = bullish,
                    bearishThreshold = bearish,
                    alertOnBullish = onBull,
                    alertOnBearish = onBear,
                    regimeFlip = regimeFlip,
                    abhijit = abhijit,
                    rahu = rahu,
                    harmonics = harmonics
                )
            },
            onAddThreshold = { label, score, condition, asset, description ->
                viewModel.addIntensityThreshold(label, score, condition, asset, description)
            },
            onToggleThreshold = { id, isEnabled ->
                viewModel.toggleIntensityThreshold(id, isEnabled)
            },
            onDeleteThreshold = { id ->
                viewModel.deleteIntensityThreshold(id)
            },
            onResetThresholds = {
                viewModel.resetIntensityThresholdsToDefault()
            },
            onSendTestAlert = { viewModel.triggerTestNotification() },
            onTriggerShiftAlert = { shift -> viewModel.triggerShiftNotification(shift) },
            onClearHistory = { viewModel.clearAlertHistory() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CosmicDeepNavy,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CosmicSurfaceElevated,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "ॐ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VedicGold
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "AstroMarket SBC",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "Sarvatobhadra • Hora • Panchang",
                                style = MaterialTheme.typography.labelSmall,
                                color = VedicGoldLight
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleAlertCenterDialog(true) },
                        modifier = Modifier.testTag("open_alert_center_button")
                    ) {
                        val shiftCount = uiState.sbcShiftSummary?.shifts?.size ?: 0
                        BadgedBox(
                            badge = {
                                if (shiftCount > 0 && uiState.appSettings.alertsEnabled) {
                                    Badge(
                                        containerColor = VedicGold,
                                        contentColor = CosmicDeepNavy
                                    ) {
                                        Text(
                                            text = shiftCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.appSettings.alertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "SBC Intensity Alerts",
                                tint = if (uiState.appSettings.alertsEnabled) VedicGold else TextSecondaryLight
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleSettingsDialog(true) },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Panchang & Ayanamsa Settings",
                            tint = VedicGold
                        )
                    }
                    IconButton(onClick = { viewModel.toggleDatePicker(true) }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = VedicGoldLight
                        )
                    }
                    IconButton(onClick = { viewModel.toggleGuideDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Astro Guide",
                            tint = CelestialCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CosmicSurface,
                windowInsets = WindowInsets.navigationBars
            ) {
                val tabs = listOf(
                    Triple(0, "Outlook", Icons.Default.AutoAwesome),
                    Triple(1, "Cycles", Icons.Default.Timeline),
                    Triple(2, "SBC Chakra", Icons.Default.GridOn),
                    Triple(3, "Hora", Icons.Default.AccessTime),
                    Triple(4, "Panchang", Icons.Default.WbSunny)
                )

                tabs.forEach { (index, title, icon) ->
                    val selected = uiState.selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (selected) VedicGold else TextSecondaryLight
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                color = if (selected) VedicGoldLight else TextSecondaryLight,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CosmicSurfaceElevated
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Date Controls & Day Lord Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CosmicSurface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.advanceDay(-1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = TextSecondaryLight
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.jumpToToday() },
                                colors = ButtonDefaults.textButtonColors(contentColor = VedicGold)
                            ) {
                                Text(text = "Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { viewModel.jumpToTomorrow() },
                                colors = ButtonDefaults.textButtonColors(contentColor = CelestialCyan)
                            ) {
                                Text(text = "Tomorrow", fontSize = 12.sp)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.advanceDay(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = TextSecondaryLight
                            )
                        }
                    }

                    // Selected Date Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormat.format(uiState.selectedCalendar.time),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        uiState.panchangDetails?.let { p ->
                            Text(
                                text = "Day Lord: ${p.dayLord.englishName} (${p.dayLord.symbol})",
                                style = MaterialTheme.typography.labelSmall,
                                color = p.dayLord.displayColor
                            )
                        }
                    }
                }
            }

            // Market Asset Chips
            val assetScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(assetScroll)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarketAsset.entries.forEach { asset ->
                    val isSelected = uiState.selectedAsset == asset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectAsset(asset) },
                        label = {
                            Text(
                                text = asset.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = VedicGold
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CosmicSurface,
                            selectedContainerColor = CosmicSurfaceElevated,
                            selectedLabelColor = VedicGoldLight,
                            labelColor = TextSecondaryLight
                        )
                    )
                }
            }

            // Active or Upcoming Sarvatobhadra Chakra Intensity Shift Banner
            uiState.activeSignificantShift?.let { activeShift ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .clickable { viewModel.toggleAlertCenterDialog(true) },
                    color = activeShift.shiftType.indicatorColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(activeShift.shiftType.indicatorColor))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = activeShift.shiftType.indicatorColor,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = activeShift.shiftType.iconSymbol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicDeepNavy
                                    )
                                }
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activeShift.alertTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    if (activeShift.isCurrentHourActive) {
                                        Surface(
                                            color = activeShift.shiftType.indicatorColor,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CosmicDeepNavy,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${activeShift.timeRange} • ${activeShift.horaLord.englishName} Lord • Tap to open Alert Center",
                                    fontSize = 10.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Alert Center",
                            tint = activeShift.shiftType.indicatorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Active Tab Content with Cross-Fade Animation
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        DailyOutlookDashboard(
                            outlookState = uiState.geminiOutlookState,
                            selectedAsset = uiState.selectedAsset,
                            selectedDateString = dateFormat.format(uiState.selectedCalendar.time),
                            onRefreshOutlook = { viewModel.refreshGeminiOutlook() },
                            trendReport = uiState.trend24HourReport,
                            selectedTrendPoint = uiState.selectedTrendPoint,
                            sbcState = uiState.sbcState,
                            panchangDetails = uiState.panchangDetails,
                            onNavigateToCycles = { viewModel.selectTab(1) }
                        )
                    }
                    1 -> {
                        uiState.dailyPrediction?.let { pred ->
                            TimeCycleForecastView(
                                prediction = pred,
                                trendReport = uiState.trend24HourReport,
                                selectedTrendPoint = uiState.selectedTrendPoint,
                                sbcState = uiState.sbcState,
                                panchang = uiState.panchangDetails,
                                onSelectTrendPoint = { viewModel.selectTrendPoint(it) },
                                isRolling24Hours = uiState.isRolling24Hours,
                                onToggleRolling = { viewModel.toggleRolling24Hours(it) },
                                onNavigateToOutlook = { viewModel.selectTab(0) }
                            )
                        } ?: LoadingIndicator()
                    }
                    2 -> {
                        uiState.sbcState?.let { sbc ->
                            SarvatobhadraChakraView(
                                sbcState = sbc,
                                selectedCell = uiState.selectedCell,
                                onSelectCell = { viewModel.selectCell(it) }
                            )
                        } ?: LoadingIndicator()
                    }
                    3 -> {
                        HoraTimelineView(
                            horas = uiState.horas,
                            currentHour = uiState.currentHour
                        )
                    }
                    4 -> {
                        uiState.panchangDetails?.let { panchang ->
                            PanchangView(
                                panchang = panchang,
                                onOpenSettings = { viewModel.toggleSettingsDialog(true) }
                            )
                        } ?: LoadingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VedicGold)
    }
}
