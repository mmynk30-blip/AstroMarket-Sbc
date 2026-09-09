package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.AlertNotificationEntity
import com.example.data.AppSettingsEntity
import com.example.engine.SbcNotificationManager
import com.example.model.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SbcAlertCenterDialog(
    currentSettings: AppSettingsEntity,
    shiftSummary: SbcCycleShiftSummary?,
    alertHistory: List<AlertNotificationEntity>,
    customThresholds: List<com.example.data.MarketIntensityThresholdEntity> = emptyList(),
    onDismiss: () -> Unit,
    onUpdateAlertSettings: (
        enabled: Boolean,
        thresholdDelta: Float,
        bullishThreshold: Float,
        bearishThreshold: Float,
        alertOnBullish: Boolean,
        alertOnBearish: Boolean,
        regimeFlip: Boolean,
        abhijit: Boolean,
        rahu: Boolean,
        harmonics: Boolean
    ) -> Unit,
    onAddThreshold: ((label: String, score: Float, condition: String, asset: String, description: String) -> Unit)? = null,
    onToggleThreshold: ((id: Int, isEnabled: Boolean) -> Unit)? = null,
    onDeleteThreshold: ((id: Int) -> Unit)? = null,
    onResetThresholds: (() -> Unit)? = null,
    onSendTestAlert: () -> Unit,
    onTriggerShiftAlert: (SbcIntensityShift) -> Unit,
    onClearHistory: () -> Unit
) {
    val context = LocalContext.current

    // Check notification permission state
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                SbcNotificationManager.areNotificationsEnabled(context)
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Editable state synced with Room AppSettingsEntity
    var alertsEnabled by remember { mutableStateOf(currentSettings.alertsEnabled) }
    var thresholdDelta by remember { mutableFloatStateOf(currentSettings.alertThresholdDelta) }
    var alertBullishThreshold by remember { mutableFloatStateOf(currentSettings.alertBullishThreshold) }
    var alertBearishThreshold by remember { mutableFloatStateOf(currentSettings.alertBearishThreshold) }
    var alertOnBullish by remember { mutableStateOf(currentSettings.alertOnBullishThreshold) }
    var alertOnBearish by remember { mutableStateOf(currentSettings.alertOnBearishThreshold) }
    var alertOnRegimeFlip by remember { mutableStateOf(currentSettings.alertOnRegimeFlip) }
    var alertOnAbhijit by remember { mutableStateOf(currentSettings.alertOnAbhijitMuhurta) }
    var alertOnRahu by remember { mutableStateOf(currentSettings.alertOnRahuKaal) }
    var alertOnHarmonics by remember { mutableStateOf(currentSettings.alertOnHarmonics) }

    fun commitSettings(
        enabled: Boolean = alertsEnabled,
        delta: Float = thresholdDelta,
        bullish: Float = alertBullishThreshold,
        bearish: Float = alertBearishThreshold,
        onBullish: Boolean = alertOnBullish,
        onBearish: Boolean = alertOnBearish,
        flip: Boolean = alertOnRegimeFlip,
        abhijit: Boolean = alertOnAbhijit,
        rahu: Boolean = alertOnRahu,
        harmonics: Boolean = alertOnHarmonics
    ) {
        onUpdateAlertSettings(enabled, delta, bullish, bearish, onBullish, onBearish, flip, abhijit, rahu, harmonics)
    }

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Detected Shifts, 1: Alert Preferences, 2: History

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = CosmicSurface,
            tonalElevation = 8.dp,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = CosmicSurfaceElevated,
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (alertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = "Alert System",
                                    tint = if (alertsEnabled) VedicGold else TextSecondaryLight
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "SBC Intensity Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "Sarvatobhadra Chakra Local Notification Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = VedicGoldLight
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Permission Banner if not granted
                if (!hasPermission) {
                    Surface(
                        color = BearishRubyDark.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BearishRuby))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Permission Alert",
                                    tint = BearishRuby,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Notifications Disabled",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "Enable system permission to receive market intensity shift alerts in real-time.",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BearishRuby),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Section Tabs (Shifts, Settings, History)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabItems = listOf(
                        Triple(0, "Detected Shifts", shiftSummary?.shifts?.size ?: 0),
                        Triple(1, "Alert Rules", null),
                        Triple(2, "Alert Log", alertHistory.size)
                    )

                    tabItems.forEach { (index, title, badgeCount) ->
                        val selected = selectedSection == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (selected) CosmicSurface else Color.Transparent,
                            onClick = { selectedSection = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) VedicGold else TextSecondaryLight
                                )
                                if (badgeCount != null && badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = if (selected) VedicGold else TextSecondaryLight.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = badgeCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) CosmicSurface else TextPrimaryLight,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Views
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedSection) {
                        0 -> DetectedShiftsList(
                            summary = shiftSummary,
                            onTriggerShiftAlert = onTriggerShiftAlert
                        )
                        1 -> AlertRulesConfiguration(
                            alertsEnabled = alertsEnabled,
                            onAlertsEnabledChange = {
                                alertsEnabled = it
                                commitSettings(enabled = it)
                            },
                            thresholdDelta = thresholdDelta,
                            onThresholdDeltaChange = {
                                thresholdDelta = it
                                commitSettings(delta = it)
                            },
                            alertBullishThreshold = alertBullishThreshold,
                            onAlertBullishThresholdChange = {
                                alertBullishThreshold = it
                                commitSettings(bullish = it)
                            },
                            alertBearishThreshold = alertBearishThreshold,
                            onAlertBearishThresholdChange = {
                                alertBearishThreshold = it
                                commitSettings(bearish = it)
                            },
                            alertOnBullish = alertOnBullish,
                            onAlertOnBullishChange = {
                                alertOnBullish = it
                                commitSettings(onBullish = it)
                            },
                            alertOnBearish = alertOnBearish,
                            onAlertOnBearishChange = {
                                alertOnBearish = it
                                commitSettings(onBearish = it)
                            },
                            alertOnRegimeFlip = alertOnRegimeFlip,
                            onAlertOnRegimeFlipChange = {
                                alertOnRegimeFlip = it
                                commitSettings(flip = it)
                            },
                            alertOnAbhijit = alertOnAbhijit,
                            onAlertOnAbhijitChange = {
                                alertOnAbhijit = it
                                commitSettings(abhijit = it)
                            },
                            alertOnRahu = alertOnRahu,
                            onAlertOnRahuChange = {
                                alertOnRahu = it
                                commitSettings(rahu = it)
                            },
                            alertOnHarmonics = alertOnHarmonics,
                            onAlertOnHarmonicsChange = {
                                alertOnHarmonics = it
                                commitSettings(harmonics = it)
                            },
                            customThresholds = customThresholds,
                            onAddThreshold = onAddThreshold,
                            onToggleThreshold = onToggleThreshold,
                            onDeleteThreshold = onDeleteThreshold,
                            onResetThresholds = onResetThresholds,
                            onSendTestAlert = onSendTestAlert
                        )
                        2 -> AlertHistoryList(
                            history = alertHistory,
                            onClearHistory = onClearHistory
                        )
                    }
                }

                // Footer Quick Action
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onSendTestAlert,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VedicGold),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VedicGold)),
                        modifier = Modifier.testTag("test_alert_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Notification", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = VedicGold, contentColor = CosmicDeepNavy)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectedShiftsList(
    summary: SbcCycleShiftSummary?,
    onTriggerShiftAlert: (SbcIntensityShift) -> Unit
) {
    if (summary == null || summary.shifts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BullishEmerald,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Significant Shifts at Current Threshold",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "Market intensity flows within steady baseline limits. Adjust threshold in 'Alert Rules' to detect smaller intraday moves.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cycle Volatility Profile: ${summary.cycleVolatilityRisk}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VedicGold
                        )
                        Text(
                            text = "${summary.totalShiftsDetected} significant intensity shifts detected across 24 hours",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }

                    summary.activeShift?.let { active ->
                        Surface(
                            color = active.shiftType.indicatorColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(active.shiftType.indicatorColor))
                        ) {
                            Text(
                                text = "ACTIVE NOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = active.shiftType.indicatorColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        items(summary.shifts) { shift ->
            ShiftCard(
                shift = shift,
                onSendAlert = { onTriggerShiftAlert(shift) }
            )
        }
    }
}

@Composable
private fun ShiftCard(
    shift: SbcIntensityShift,
    onSendAlert: () -> Unit
) {
    Surface(
        color = CosmicSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = if (shift.isCurrentHourActive) {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(shift.shiftType.indicatorColor))
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    Surface(
                        color = shift.shiftType.indicatorColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = shift.shiftType.shortLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = shift.shiftType.indicatorColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = shift.timeRange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight
                    )

                    if (shift.isCurrentHourActive) {
                        Surface(
                            color = BullishEmerald,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                // Delta Score Chip
                val sign = if (shift.intensityDelta >= 0) "+" else ""
                val deltaStr = String.format(Locale.US, "%s%.1f pts", sign, shift.intensityDelta)
                Surface(
                    color = if (shift.intensityDelta >= 0) BullishEmerald.copy(alpha = 0.2f) else BearishRuby.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = deltaStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (shift.intensityDelta >= 0) BullishEmerald else BearishRuby,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Transition Line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "Intensity: %.1f", shift.previousIntensity),
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextSecondaryLight,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.1f", shift.newIntensity),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (shift.newIntensity >= 0) BullishEmerald else BearishRuby
                )
                Text(
                    text = "• Lord: ${shift.horaLord.englishName} (${shift.horaLord.symbol})",
                    fontSize = 11.sp,
                    color = VedicGoldLight
                )
            }

            // Guidance & Sector Impact
            Text(
                text = shift.actionableGuidance,
                fontSize = 11.sp,
                color = TextSecondaryLight,
                lineHeight = 16.sp
            )

            if (shift.favoredSectors.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Favored:", fontSize = 10.sp, color = BullishEmerald, fontWeight = FontWeight.Bold)
                    Text(
                        text = shift.favoredSectors.take(3).joinToString(", "),
                        fontSize = 10.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            // Notification Trigger Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSendAlert,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = VedicGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push Alert Now", fontSize = 11.sp, color = VedicGold)
                }
            }
        }
    }
}

@Composable
private fun AlertRulesConfiguration(
    alertsEnabled: Boolean,
    onAlertsEnabledChange: (Boolean) -> Unit,
    thresholdDelta: Float,
    onThresholdDeltaChange: (Float) -> Unit,
    alertBullishThreshold: Float,
    onAlertBullishThresholdChange: (Float) -> Unit,
    alertBearishThreshold: Float,
    onAlertBearishThresholdChange: (Float) -> Unit,
    alertOnBullish: Boolean,
    onAlertOnBullishChange: (Boolean) -> Unit,
    alertOnBearish: Boolean,
    onAlertOnBearishChange: (Boolean) -> Unit,
    alertOnRegimeFlip: Boolean,
    onAlertOnRegimeFlipChange: (Boolean) -> Unit,
    alertOnAbhijit: Boolean,
    onAlertOnAbhijitChange: (Boolean) -> Unit,
    alertOnRahu: Boolean,
    onAlertOnRahuChange: (Boolean) -> Unit,
    alertOnHarmonics: Boolean,
    onAlertOnHarmonicsChange: (Boolean) -> Unit,
    customThresholds: List<com.example.data.MarketIntensityThresholdEntity> = emptyList(),
    onAddThreshold: ((label: String, score: Float, condition: String, asset: String, description: String) -> Unit)? = null,
    onToggleThreshold: ((id: Int, isEnabled: Boolean) -> Unit)? = null,
    onDeleteThreshold: ((id: Int) -> Unit)? = null,
    onResetThresholds: (() -> Unit)? = null,
    onSendTestAlert: () -> Unit
) {
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newRuleLabel by remember { mutableStateOf("") }
    var newRuleScore by remember { mutableFloatStateOf(50f) }
    var newRuleCondition by remember { mutableStateOf(com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE) }
    var newRuleAsset by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Master Switch Card
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable SBC Intensity Alerts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Triggers notifications whenever market intensity crosses your stored Room thresholds.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                    Switch(
                        checked = alertsEnabled,
                        onCheckedChange = onAlertsEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VedicGold,
                            checkedTrackColor = CosmicSurface
                        )
                    )
                }
            }
        }

        item {
            // User-Defined Bullish Market Intensity Score Threshold (Room Persisted)
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bullish Intensity Threshold",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = BullishEmerald.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Room Persisted",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BullishEmeraldLight,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Alert when market intensity score crosses above this level.",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                        Switch(
                            checked = alertOnBullish,
                            onCheckedChange = onAlertOnBullishChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BullishEmeraldLight,
                                checkedTrackColor = CosmicSurface
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trigger Level:",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                        Text(
                            text = "≥ +${alertBullishThreshold.toInt()} pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BullishEmeraldLight
                        )
                    }

                    Slider(
                        value = alertBullishThreshold,
                        onValueChange = onAlertBullishThresholdChange,
                        valueRange = 15f..80f,
                        steps = 12,
                        enabled = alertOnBullish,
                        colors = SliderDefaults.colors(
                            thumbColor = BullishEmeraldLight,
                            activeTrackColor = BullishEmerald,
                            inactiveTrackColor = CosmicSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val bullPresets = listOf(
                            Pair("+30 pts (Moderate)", 30f),
                            Pair("+45 pts (Strong)", 45f),
                            Pair("+60 pts (Zenith)", 60f)
                        )
                        bullPresets.forEach { (label, value) ->
                            val isSelected = (alertBullishThreshold == value)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onAlertBullishThresholdChange(value) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BullishEmerald,
                                    selectedLabelColor = CosmicDeepNavy
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            // User-Defined Bearish Market Intensity Score Threshold (Room Persisted)
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bearish Intensity Threshold",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = BearishRuby.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Room Persisted",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BearishRubyLight,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Alert when market intensity plunges below this affliction score.",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                        Switch(
                            checked = alertOnBearish,
                            onCheckedChange = onAlertOnBearishChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BearishRubyLight,
                                checkedTrackColor = CosmicSurface
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trigger Level:",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                        Text(
                            text = "≤ ${alertBearishThreshold.toInt()} pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BearishRubyLight
                        )
                    }

                    Slider(
                        value = alertBearishThreshold,
                        onValueChange = onAlertBearishThresholdChange,
                        valueRange = -80f..-15f,
                        steps = 12,
                        enabled = alertOnBearish,
                        colors = SliderDefaults.colors(
                            thumbColor = BearishRubyLight,
                            activeTrackColor = BearishRuby,
                            inactiveTrackColor = CosmicSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val bearPresets = listOf(
                            Pair("-30 pts (Mild)", -30f),
                            Pair("-45 pts (Heavy)", -45f),
                            Pair("-60 pts (Severe)", -60f)
                        )
                        bearPresets.forEach { (label, value) ->
                            val isSelected = (alertBearishThreshold == value)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onAlertBearishThresholdChange(value) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BearishRuby,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            // Intraday Shift Magnitude Sensitivity Slider & Preset Chips
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Intraday Shift Sensitivity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Δ ${thresholdDelta.toInt()} pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = VedicGold
                        )
                    }

                    Text(
                        text = "Minimum 1-hour change in predicted intensity to trigger a shift notification.",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )

                    Slider(
                        value = thresholdDelta,
                        onValueChange = onThresholdDeltaChange,
                        valueRange = 10f..50f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = VedicGold,
                            activeTrackColor = VedicGold,
                            inactiveTrackColor = CosmicSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Pair("15 pts (Sensitive)", 15f),
                            Pair("25 pts (Balanced)", 25f),
                            Pair("35 pts (Major)", 35f)
                        )
                        presets.forEach { (label, value) ->
                            val isSelected = (thresholdDelta == value)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onThresholdDeltaChange(value) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VedicGold,
                                    selectedLabelColor = CosmicDeepNavy
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            // Persistent Custom Intensity Rules in Room Database
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Saved Intensity Rules (Room DB)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = VedicGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${customThresholds.size} rules",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VedicGold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Stored in Room and remembered between app launches",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }

                        IconButton(
                            onClick = { showAddRuleDialog = !showAddRuleDialog },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (showAddRuleDialog) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Add Rule",
                                tint = VedicGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Add Rule Expandable Section
                    if (showAddRuleDialog) {
                        Surface(
                            color = CosmicSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Create New Intensity Threshold Rule",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VedicGold
                                )

                                OutlinedTextField(
                                    value = newRuleLabel,
                                    onValueChange = { newRuleLabel = it },
                                    label = { Text("Rule Name", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. Bank Nifty Zenith Breakout", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = newRuleCondition == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE,
                                        onClick = {
                                            newRuleCondition = com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE
                                            if (newRuleScore < 0f) newRuleScore = 45f
                                        },
                                        label = { Text("≥ Bullish Score", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BullishEmerald,
                                            selectedLabelColor = CosmicDeepNavy
                                        )
                                    )
                                    FilterChip(
                                        selected = newRuleCondition == com.example.data.MarketIntensityThresholdEntity.CONDITION_LTE,
                                        onClick = {
                                            newRuleCondition = com.example.data.MarketIntensityThresholdEntity.CONDITION_LTE
                                            if (newRuleScore > 0f) newRuleScore = -45f
                                        },
                                        label = { Text("≤ Bearish Score", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BearishRuby,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Threshold Score:", fontSize = 11.sp, color = TextSecondaryLight)
                                    Text(
                                        text = "${if (newRuleScore >= 0) "+${newRuleScore.toInt()}" else "${newRuleScore.toInt()}"} pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (newRuleScore >= 0) BullishEmeraldLight else BearishRubyLight
                                    )
                                }

                                Slider(
                                    value = newRuleScore,
                                    onValueChange = { newRuleScore = it },
                                    valueRange = if (newRuleCondition == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE) 10f..90f else -90f..-10f,
                                    steps = 15,
                                    colors = SliderDefaults.colors(
                                        thumbColor = VedicGold,
                                        activeTrackColor = VedicGold,
                                        inactiveTrackColor = CosmicSurfaceElevated
                                    )
                                )

                                Button(
                                    onClick = {
                                        val label = newRuleLabel.ifBlank {
                                            if (newRuleCondition == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE)
                                                "Bullish Threshold (+${newRuleScore.toInt()})"
                                            else
                                                "Bearish Threshold (${newRuleScore.toInt()})"
                                        }
                                        onAddThreshold?.invoke(
                                            label,
                                            newRuleScore,
                                            newRuleCondition,
                                            newRuleAsset,
                                            "Persisted rule triggering alert when intensity ${if (newRuleCondition == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE) "≥" else "≤"} ${newRuleScore.toInt()}"
                                        )
                                        newRuleLabel = ""
                                        showAddRuleDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = VedicGold)
                                ) {
                                    Text("Save Rule to Room DB", fontSize = 12.sp, color = CosmicDeepNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Render Custom Threshold List from Room
                    if (customThresholds.isEmpty()) {
                        Text(
                            text = "No custom rules saved yet. Default thresholds active.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    } else {
                        customThresholds.forEach { rule ->
                            Surface(
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = if (rule.isBullish) BullishEmerald.copy(alpha = 0.2f) else BearishRuby.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${if (rule.conditionType == com.example.data.MarketIntensityThresholdEntity.CONDITION_GTE) "≥" else "≤"} ${if (rule.thresholdScore >= 0) "+${rule.thresholdScore.toInt()}" else "${rule.thresholdScore.toInt()}"} pts",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rule.isBullish) BullishEmeraldLight else BearishRubyLight,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = rule.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (rule.isEnabled) TextPrimaryLight else TextSecondaryLight
                                            )
                                        }

                                        Text(
                                            text = "${rule.assetSymbol} • ${rule.description}",
                                            fontSize = 10.sp,
                                            color = TextSecondaryLight,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = rule.isEnabled,
                                            onCheckedChange = { isChecked ->
                                                onToggleThreshold?.invoke(rule.id, isChecked)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = VedicGold,
                                                checkedTrackColor = CosmicSurfaceElevated
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )

                                        IconButton(
                                            onClick = { onDeleteThreshold?.invoke(rule.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Rule",
                                                tint = TextSecondaryLight.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reset to Defaults Button
                    OutlinedButton(
                        onClick = { onResetThresholds?.invoke() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryLight)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Intensity Rules to Defaults", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            // Specific Trigger Conditions
            Surface(
                color = CosmicSurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Specific Cycle Triggers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimaryLight
                    )

                    RuleToggleRow(
                        title = "Regime Reversal Flips",
                        subtitle = "Alert when polarity changes from negative to positive or vice-versa",
                        checked = alertOnRegimeFlip,
                        onCheckedChange = onAlertOnRegimeFlipChange
                    )

                    HorizontalDivider(color = CosmicSurface)

                    RuleToggleRow(
                        title = "Abhijit Muhurta Zenith Peak",
                        subtitle = "Alert during auspicious mid-day solar peak window (+38 pts boost)",
                        checked = alertOnAbhijit,
                        onCheckedChange = onAlertOnAbhijitChange
                    )

                    HorizontalDivider(color = CosmicSurface)

                    RuleToggleRow(
                        title = "Rahu Kaal Shadow Window",
                        subtitle = "Alert on entry into the high volatility / false breakout window (-36 pts)",
                        checked = alertOnRahu,
                        onCheckedChange = onAlertOnRahuChange
                    )

                    HorizontalDivider(color = CosmicSurface)

                    RuleToggleRow(
                        title = "Harmonic Turning Cycles",
                        subtitle = "Alert on planetary hora inflection points commanding market order flow",
                        checked = alertOnHarmonics,
                        onCheckedChange = onAlertOnHarmonicsChange
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryLight)
            Text(text = subtitle, fontSize = 10.sp, color = TextSecondaryLight)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VedicGold,
                checkedTrackColor = CosmicSurface
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun AlertHistoryList(
    history: List<AlertNotificationEntity>,
    onClearHistory: () -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TextSecondaryLight,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = "No Alert History Yet",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "Triggered notifications will be logged here for audit and review.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${history.size} Dispatched Notifications",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VedicGold
            )
            TextButton(
                onClick = onClearHistory,
                colors = ButtonDefaults.textButtonColors(contentColor = BearishRuby)
            ) {
                Text("Clear History", fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { record ->
                Surface(
                    color = CosmicSurfaceElevated,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = record.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = record.timeRange,
                                fontSize = 10.sp,
                                color = VedicGoldLight
                            )
                        }

                        Text(
                            text = record.message,
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )

                        Text(
                            text = "• Guidance: ${record.guidance}",
                            fontSize = 10.sp,
                            color = VedicGoldLight
                        )
                    }
                }
            }
        }
    }
}
