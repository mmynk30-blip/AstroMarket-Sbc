package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlertNotificationEntity
import com.example.data.AppDatabase
import com.example.data.AppSettingsEntity
import com.example.data.SettingsRepository
import com.example.engine.FirebaseGenAiService
import com.example.engine.HoraEngine
import com.example.engine.PanchangEngine
import com.example.engine.SarvatobhadraEngine
import com.example.engine.SbcIntensityAlertEngine
import com.example.engine.SbcNotificationManager
import com.example.engine.TimeCycleEngine
import com.example.engine.TrendIntensityEngine
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AstroMarketUiState(
    val selectedCalendar: Calendar = Calendar.getInstance(),
    val selectedAsset: MarketAsset = MarketAsset.NIFTY_50,
    val selectedTab: Int = 0, // 0: Daily Outlook Dashboard, 1: Time Cycles & Recharts, 2: Sarvatobhadra Chakra, 3: Hora Hours, 4: Panchang
    val sbcState: SarvatobhadraState? = null,
    val panchangDetails: PanchangDetails? = null,
    val horas: List<HoraPeriod> = emptyList(),
    val dailyPrediction: DailyMarketPrediction? = null,
    val trend24HourReport: Trend24HourReport? = null,
    val isRolling24Hours: Boolean = false,
    val selectedTrendPoint: TrendDataPoint? = null,
    val selectedCell: SbCell? = null,
    val showGuideDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showAlertCenterDialog: Boolean = false,
    val appSettings: AppSettingsEntity = AppSettingsEntity(),
    val currentAyanamsa: AyanamsaSystem = AyanamsaSystem.LAHIRI,
    val settingsSavedMessage: String? = null,
    val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val geminiOutlookState: GeminiOutlookUiState = GeminiOutlookUiState.Loading,
    val sbcShiftSummary: SbcCycleShiftSummary? = null,
    val activeSignificantShift: SbcIntensityShift? = null,
    val alertHistory: List<AlertNotificationEntity> = emptyList(),
    val marketIntensityThresholds: List<com.example.data.MarketIntensityThresholdEntity> = emptyList(),
    val lastDispatchedNotificationMessage: String? = null
)

class AstroMarketViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(
        AppDatabase.getInstance(application).settingsDao(),
        AppDatabase.getInstance(application).alertHistoryDao(),
        AppDatabase.getInstance(application).marketIntensityThresholdDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AstroMarketUiState())
    val uiState: StateFlow<AstroMarketUiState> = _uiState.asStateFlow()

    init {
        // Initialize notification channel
        SbcNotificationManager.createNotificationChannel(application)

        // Seed default market intensity thresholds if Room database is fresh
        viewModelScope.launch {
            settingsRepository.seedDefaultThresholdsIfEmpty()
        }

        // Observe persistent user settings from Room database
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val ayanamsa = try {
                    AyanamsaSystem.valueOf(settings.ayanamsaSystemCode)
                } catch (e: Exception) {
                    AyanamsaSystem.LAHIRI
                }
                _uiState.update {
                    it.copy(
                        appSettings = settings,
                        currentAyanamsa = ayanamsa
                    )
                }
                refreshCalculations()
            }
        }

        // Observe alert history
        viewModelScope.launch {
            settingsRepository.alertHistoryFlow.collect { history ->
                _uiState.update { it.copy(alertHistory = history) }
            }
        }

        // Observe persistent user-defined market intensity thresholds from Room
        viewModelScope.launch {
            settingsRepository.thresholdRulesFlow.collect { rules ->
                _uiState.update { it.copy(marketIntensityThresholds = rules) }
                refreshCalculations()
            }
        }
    }

    fun setDate(calendar: Calendar) {
        _uiState.update { it.copy(selectedCalendar = calendar) }
        refreshCalculations()
    }

    fun selectAsset(asset: MarketAsset) {
        _uiState.update { it.copy(selectedAsset = asset) }
        refreshCalculations()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectCell(cell: SbCell?) {
        _uiState.update { it.copy(selectedCell = cell) }
    }

    fun toggleRolling24Hours(isRolling: Boolean) {
        _uiState.update { it.copy(isRolling24Hours = isRolling) }
        refreshCalculations()
    }

    fun selectTrendPoint(point: TrendDataPoint?) {
        _uiState.update { it.copy(selectedTrendPoint = point) }
    }

    fun toggleGuideDialog(show: Boolean) {
        _uiState.update { it.copy(showGuideDialog = show) }
    }

    fun toggleAlertCenterDialog(show: Boolean) {
        _uiState.update { it.copy(showAlertCenterDialog = show) }
    }

    fun updateAlertSettings(
        enabled: Boolean,
        thresholdDelta: Float,
        bullishThreshold: Float = _uiState.value.appSettings.alertBullishThreshold,
        bearishThreshold: Float = _uiState.value.appSettings.alertBearishThreshold,
        alertOnBullish: Boolean = _uiState.value.appSettings.alertOnBullishThreshold,
        alertOnBearish: Boolean = _uiState.value.appSettings.alertOnBearishThreshold,
        regimeFlip: Boolean = _uiState.value.appSettings.alertOnRegimeFlip,
        abhijit: Boolean = _uiState.value.appSettings.alertOnAbhijitMuhurta,
        rahu: Boolean = _uiState.value.appSettings.alertOnRahuKaal,
        harmonics: Boolean = _uiState.value.appSettings.alertOnHarmonics
    ) {
        viewModelScope.launch {
            settingsRepository.updateAlertSettings(
                alertsEnabled = enabled,
                alertThresholdDelta = thresholdDelta,
                alertBullishThreshold = bullishThreshold,
                alertBearishThreshold = bearishThreshold,
                alertOnBullishThreshold = alertOnBullish,
                alertOnBearishThreshold = alertOnBearish,
                alertOnRegimeFlip = regimeFlip,
                alertOnAbhijitMuhurta = abhijit,
                alertOnRahuKaal = rahu,
                alertOnHarmonics = harmonics
            )
            _uiState.update {
                it.copy(
                    settingsSavedMessage = "Intensity thresholds saved to Room (Bull ≥ +${bullishThreshold.toInt()}, Bear ≤ ${bearishThreshold.toInt()})"
                )
            }
            refreshCalculations()
        }
    }

    fun addIntensityThreshold(
        label: String,
        score: Float,
        condition: String,
        assetSymbol: String = "ALL",
        description: String = ""
    ) {
        viewModelScope.launch {
            settingsRepository.insertThreshold(
                com.example.data.MarketIntensityThresholdEntity(
                    label = label.ifBlank { "Custom Threshold (${score.toInt()})" },
                    assetSymbol = assetSymbol,
                    conditionType = condition,
                    thresholdScore = score,
                    isEnabled = true,
                    description = description.ifBlank { "Alert when intensity $condition $score" }
                )
            )
            _uiState.update {
                it.copy(settingsSavedMessage = "Alert rule '$label' saved to Room database")
            }
        }
    }

    fun toggleIntensityThreshold(id: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThresholdEnabled(id, isEnabled)
        }
    }

    fun deleteIntensityThreshold(id: Int) {
        viewModelScope.launch {
            settingsRepository.deleteThresholdById(id)
            _uiState.update { it.copy(settingsSavedMessage = "Threshold rule removed") }
        }
    }

    fun resetIntensityThresholdsToDefault() {
        viewModelScope.launch {
            settingsRepository.resetThresholdsToDefault()
            _uiState.update { it.copy(settingsSavedMessage = "Threshold rules reset to default") }
        }
    }

    fun triggerTestNotification() {
        val state = _uiState.value
        val context = getApplication<Application>()
        val asset = state.selectedAsset
        val posted = SbcNotificationManager.sendTestAlert(context, asset)
        if (posted) {
            viewModelScope.launch {
                settingsRepository.recordAlertNotification(
                    AlertNotificationEntity(
                        dateFormatted = "Today (Live Test)",
                        timeRange = "11:48 - 12:38",
                        clockHour = 11,
                        assetSymbol = asset.displayName,
                        shiftType = SbcShiftType.ABHIJIT_MUHURTA_PEAK.title,
                        intensityDelta = 53.7f,
                        previousIntensity = 14.5f,
                        newIntensity = 68.2f,
                        horaLord = Planet.JUPITER.englishName,
                        title = "★ Abhijit Muhurta Zenith Surge (+53.7 pts)",
                        message = "${asset.displayName}: Predicted intensity shifts to +68.2 in Abhijit Muhurta window under Jupiter Lordship.",
                        guidance = "Favorable window for institutional liquidity accumulation. Trailing stops recommended on momentum long contracts."
                    )
                )
                _uiState.update {
                    it.copy(
                        settingsSavedMessage = "Test notification sent to device notification tray!",
                        lastDispatchedNotificationMessage = "Test notification sent successfully"
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(settingsSavedMessage = "Notification permission required. Tap 'Enable' in Alert Center.")
            }
        }
    }

    fun triggerShiftNotification(shift: SbcIntensityShift) {
        val context = getApplication<Application>()
        val posted = SbcNotificationManager.postShiftAlert(context, shift)
        if (posted) {
            viewModelScope.launch {
                settingsRepository.recordAlertNotification(
                    AlertNotificationEntity(
                        dateFormatted = _uiState.value.panchangDetails?.dateString ?: "Today",
                        timeRange = shift.timeRange,
                        clockHour = shift.clockHour,
                        assetSymbol = shift.asset.displayName,
                        shiftType = shift.shiftType.title,
                        intensityDelta = shift.intensityDelta,
                        previousIntensity = shift.previousIntensity,
                        newIntensity = shift.newIntensity,
                        horaLord = shift.horaLord.englishName,
                        title = shift.alertTitle,
                        message = shift.alertMessage,
                        guidance = shift.actionableGuidance
                    )
                )
                _uiState.update {
                    it.copy(
                        settingsSavedMessage = "Alert sent for ${shift.timeRange} (${shift.shiftType.title})"
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(settingsSavedMessage = "Notification permission required.")
            }
        }
    }

    fun clearAlertHistory() {
        viewModelScope.launch {
            settingsRepository.clearAlertHistory()
            _uiState.update { it.copy(settingsSavedMessage = "Alert history cleared") }
        }
    }

    fun toggleDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun applyTradingHubPreset(preset: TradingHubPreset, ayanamsa: AyanamsaSystem) {
        viewModelScope.launch {
            settingsRepository.applyPreset(preset, ayanamsa)
            _uiState.update {
                it.copy(
                    settingsSavedMessage = "Location set to ${preset.name} (${preset.marketLabel})",
                    showSettingsDialog = false
                )
            }
        }
    }

    fun applyCustomLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double,
        ayanamsa: AyanamsaSystem
    ) {
        viewModelScope.launch {
            settingsRepository.applyCustomLocation(name, latitude, longitude, timezoneOffset, ayanamsa)
            _uiState.update {
                it.copy(
                    settingsSavedMessage = "Custom location saved: $name ($latitude°, $longitude°)",
                    showSettingsDialog = false
                )
            }
        }
    }

    fun updateAyanamsa(ayanamsa: AyanamsaSystem) {
        viewModelScope.launch {
            settingsRepository.updateAyanamsa(ayanamsa)
            _uiState.update {
                it.copy(
                    settingsSavedMessage = "Ayanamsa updated to ${ayanamsa.displayName}"
                )
            }
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            _uiState.update {
                it.copy(
                    settingsSavedMessage = "Settings reset to Mumbai (NSE India) & Lahiri Ayanamsa"
                )
            }
        }
    }

    fun clearSettingsSnackbarMessage() {
        _uiState.update { it.copy(settingsSavedMessage = null) }
    }

    fun jumpToToday() {
        val today = Calendar.getInstance()
        setDate(today)
    }

    fun jumpToTomorrow() {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        setDate(tomorrow)
    }

    fun advanceDay(amount: Int) {
        val cal = (_uiState.value.selectedCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, amount)
        }
        setDate(cal)
    }

    private fun refreshCalculations() {
        viewModelScope.launch {
            val state = _uiState.value
            val cal = state.selectedCalendar
            val asset = state.selectedAsset
            val settings = state.appSettings
            val ayanamsa = state.currentAyanamsa
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val sbcState = SarvatobhadraEngine.calculateSbcState(cal, ayanamsa)
            val panchang = PanchangEngine.calculatePanchang(
                calendar = cal,
                latitude = settings.latitude,
                longitude = settings.longitude,
                timezoneOffsetHours = settings.timezoneOffsetHours,
                locationName = "${settings.locationName} (${settings.marketLabel})",
                ayanamsa = ayanamsa
            )

            val sunriseHour = panchang.sunriseTime.split(":").firstOrNull()?.toIntOrNull() ?: 6
            val horas = HoraEngine.calculateDailyHoras(cal, currentHour, sunriseHour)

            val prediction = TimeCycleEngine.generateDailyPrediction(
                asset = asset,
                calendar = cal,
                sbcState = sbcState,
                panchang = panchang,
                horas = horas
            )

            val trendReport = TrendIntensityEngine.calculate24HourTrend(
                asset = asset,
                calendar = cal,
                panchang = panchang,
                horas = horas,
                sbcState = sbcState,
                currentHour = currentHour,
                isRolling24Hours = state.isRolling24Hours
            )

            val currentPoint = trendReport.points.find { it.isCurrentHour } ?: trendReport.points.firstOrNull()

            // Analyze 24-hour cycle for significant Sarvatobhadra Chakra intensity shifts
            val shiftSummary = SbcIntensityAlertEngine.analyzeCycleShifts(
                asset = asset,
                panchang = panchang,
                sbcState = sbcState,
                trendReport = trendReport,
                currentHour = currentHour,
                thresholdDelta = settings.alertThresholdDelta,
                alertBullishThreshold = settings.alertBullishThreshold,
                alertBearishThreshold = settings.alertBearishThreshold,
                alertOnBullishThreshold = settings.alertOnBullishThreshold,
                alertOnBearishThreshold = settings.alertOnBearishThreshold,
                alertOnRegimeFlip = settings.alertOnRegimeFlip,
                alertOnAbhijit = settings.alertOnAbhijitMuhurta,
                alertOnRahuKaal = settings.alertOnRahuKaal,
                alertOnHarmonics = settings.alertOnHarmonics,
                customThresholdRules = state.marketIntensityThresholds
            )

            _uiState.update {
                it.copy(
                    sbcState = sbcState,
                    panchangDetails = panchang,
                    horas = horas,
                    dailyPrediction = prediction,
                    trend24HourReport = trendReport,
                    selectedTrendPoint = it.selectedTrendPoint ?: currentPoint,
                    currentHour = currentHour,
                    sbcShiftSummary = shiftSummary,
                    activeSignificantShift = shiftSummary.activeShift ?: shiftSummary.nextUpcomingShift
                )
            }

            // Check if there is an active significant shift that should trigger a local notification
            checkAndTriggerActiveShift(shiftSummary, asset, cal)

            // Automatically trigger or refresh Gemini Daily Market Sentiment summary
            refreshGeminiOutlook()
        }
    }

    private fun checkAndTriggerActiveShift(
        summary: SbcCycleShiftSummary,
        asset: MarketAsset,
        cal: Calendar
    ) {
        val settings = _uiState.value.appSettings
        if (!settings.alertsEnabled) return

        // Target current active shift or imminent shift in the next hour
        val targetShift = summary.activeShift ?: summary.nextUpcomingShift ?: return

        val shiftHash = "${cal.get(Calendar.DAY_OF_YEAR)}_${asset.name}_${targetShift.clockHour}_${targetShift.shiftType.name}"
        if (settings.lastTriggeredAlertHash == shiftHash) {
            return // Prevent duplicate notifications for the same cycle transition
        }

        val context = getApplication<Application>()
        val posted = SbcNotificationManager.postShiftAlert(context, targetShift)
        if (posted) {
            viewModelScope.launch {
                settingsRepository.updateLastTriggeredAlertHash(shiftHash)
                settingsRepository.recordAlertNotification(
                    AlertNotificationEntity(
                        dateFormatted = summary.dateString,
                        timeRange = targetShift.timeRange,
                        clockHour = targetShift.clockHour,
                        assetSymbol = asset.displayName,
                        shiftType = targetShift.shiftType.title,
                        intensityDelta = targetShift.intensityDelta,
                        previousIntensity = targetShift.previousIntensity,
                        newIntensity = targetShift.newIntensity,
                        horaLord = targetShift.horaLord.englishName,
                        title = targetShift.alertTitle,
                        message = targetShift.alertMessage,
                        guidance = targetShift.actionableGuidance
                    )
                )
                _uiState.update {
                    it.copy(
                        lastDispatchedNotificationMessage = "SBC Alert posted: ${targetShift.alertTitle}"
                    )
                }
            }
        }
    }

    fun refreshGeminiOutlook() {
        viewModelScope.launch {
            val state = _uiState.value
            val sbc = state.sbcState ?: return@launch
            val panchang = state.panchangDetails ?: return@launch
            val horas = state.horas
            val prediction = state.dailyPrediction
            val asset = state.selectedAsset
            val cal = state.selectedCalendar

            _uiState.update { it.copy(geminiOutlookState = GeminiOutlookUiState.Loading) }

            try {
                val outlook = FirebaseGenAiService.fetchDailyMarketSentiment(
                    context = getApplication(),
                    asset = asset,
                    calendar = cal,
                    sbcState = sbc,
                    panchang = panchang,
                    horas = horas,
                    prediction = prediction
                )
                _uiState.update { it.copy(geminiOutlookState = GeminiOutlookUiState.Success(outlook)) }
            } catch (e: Exception) {
                val fallback = FirebaseGenAiService.generateCalculatedVedicOutlook(
                    asset = asset,
                    sbcState = sbc,
                    panchang = panchang,
                    horas = horas,
                    prediction = prediction,
                    contextSnapshot = FirebaseGenAiService.buildAstrologicalContext(asset, cal, sbc, panchang, horas, prediction)
                )
                _uiState.update {
                    it.copy(
                        geminiOutlookState = GeminiOutlookUiState.Error(
                            message = "Firebase GenAI connection note: ${e.localizedMessage ?: "Calculated Vedic analysis active"}",
                            fallbackOutlook = fallback
                        )
                    )
                }
            }
        }
    }
}
