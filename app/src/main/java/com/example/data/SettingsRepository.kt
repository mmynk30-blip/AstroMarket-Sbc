package com.example.data

import com.example.model.AyanamsaSystem
import com.example.model.TradingHubPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dao: SettingsDao,
    private val alertHistoryDao: AlertHistoryDao? = null,
    private val thresholdDao: MarketIntensityThresholdDao? = null
) {

    val settingsFlow: Flow<AppSettingsEntity> = dao.getSettings().map { entity ->
        entity ?: AppSettingsEntity()
    }

    val alertHistoryFlow: Flow<List<AlertNotificationEntity>> =
        alertHistoryDao?.getAllAlertsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    val thresholdRulesFlow: Flow<List<MarketIntensityThresholdEntity>> =
        thresholdDao?.getAllThresholdsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getSettings(): AppSettingsEntity {
        return dao.getSettingsDirect() ?: AppSettingsEntity().also {
            dao.saveSettings(it)
        }
    }

    suspend fun applyPreset(preset: TradingHubPreset, ayanamsa: AyanamsaSystem) {
        val current = getSettings()
        val updated = current.copy(
            id = 1,
            locationName = preset.name,
            marketLabel = preset.marketLabel,
            latitude = preset.latitude,
            longitude = preset.longitude,
            timezoneOffsetHours = preset.timezoneOffsetHours,
            timezoneId = preset.timezoneId,
            ayanamsaSystemCode = ayanamsa.code,
            isCustomLocation = false,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        dao.saveSettings(updated)
    }

    suspend fun applyCustomLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double,
        ayanamsa: AyanamsaSystem
    ) {
        val current = getSettings()
        val updated = current.copy(
            id = 1,
            locationName = name.ifBlank { "Custom Location" },
            marketLabel = "Custom Astro Coordinates",
            latitude = latitude,
            longitude = longitude,
            timezoneOffsetHours = timezoneOffset,
            timezoneId = "Custom (UTC${if (timezoneOffset >= 0) "+$timezoneOffset" else "$timezoneOffset"})",
            ayanamsaSystemCode = ayanamsa.code,
            isCustomLocation = true,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        dao.saveSettings(updated)
    }

    suspend fun updateAyanamsa(ayanamsa: AyanamsaSystem) {
        val current = getSettings()
        dao.saveSettings(current.copy(ayanamsaSystemCode = ayanamsa.code, lastUpdatedTimestamp = System.currentTimeMillis()))
    }

    suspend fun updateAlertSettings(
        alertsEnabled: Boolean,
        alertThresholdDelta: Float,
        alertBullishThreshold: Float,
        alertBearishThreshold: Float,
        alertOnBullishThreshold: Boolean,
        alertOnBearishThreshold: Boolean,
        alertOnRegimeFlip: Boolean,
        alertOnAbhijitMuhurta: Boolean,
        alertOnRahuKaal: Boolean,
        alertOnHarmonics: Boolean
    ) {
        val current = getSettings()
        val updated = current.copy(
            alertsEnabled = alertsEnabled,
            alertThresholdDelta = alertThresholdDelta,
            alertBullishThreshold = alertBullishThreshold,
            alertBearishThreshold = alertBearishThreshold,
            alertOnBullishThreshold = alertOnBullishThreshold,
            alertOnBearishThreshold = alertOnBearishThreshold,
            alertOnRegimeFlip = alertOnRegimeFlip,
            alertOnAbhijitMuhurta = alertOnAbhijitMuhurta,
            alertOnRahuKaal = alertOnRahuKaal,
            alertOnHarmonics = alertOnHarmonics,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        dao.saveSettings(updated)
    }

    suspend fun updateLastTriggeredAlertHash(hash: String) {
        val current = getSettings()
        dao.saveSettings(current.copy(lastTriggeredAlertHash = hash))
    }

    suspend fun recordAlertNotification(alert: AlertNotificationEntity) {
        alertHistoryDao?.insertAlert(alert)
    }

    suspend fun clearAlertHistory() {
        alertHistoryDao?.clearHistory()
    }

    // --- Market Intensity Threshold Rules Room Persistence ---

    suspend fun getEnabledThresholdRules(assetSymbol: String? = null): List<MarketIntensityThresholdEntity> {
        val dao = thresholdDao ?: return emptyList()
        return if (assetSymbol != null) {
            dao.getEnabledThresholdsForAsset(assetSymbol)
        } else {
            dao.getEnabledThresholds()
        }
    }

    suspend fun insertThreshold(threshold: MarketIntensityThresholdEntity): Long {
        return thresholdDao?.insertThreshold(threshold) ?: -1L
    }

    suspend fun updateThreshold(threshold: MarketIntensityThresholdEntity) {
        thresholdDao?.updateThreshold(threshold)
    }

    suspend fun setThresholdEnabled(id: Int, isEnabled: Boolean) {
        thresholdDao?.updateEnabledState(id, isEnabled)
    }

    suspend fun deleteThreshold(threshold: MarketIntensityThresholdEntity) {
        thresholdDao?.deleteThreshold(threshold)
    }

    suspend fun deleteThresholdById(id: Int) {
        thresholdDao?.deleteThresholdById(id)
    }

    suspend fun seedDefaultThresholdsIfEmpty() {
        val dao = thresholdDao ?: return
        if (dao.getThresholdsCount() == 0) {
            dao.insertAll(MarketIntensityThresholdEntity.createDefaultThresholds())
        }
    }

    suspend fun resetThresholdsToDefault() {
        val dao = thresholdDao ?: return
        dao.clearAllThresholds()
        dao.insertAll(MarketIntensityThresholdEntity.createDefaultThresholds())
    }

    suspend fun resetToDefaults() {
        dao.saveSettings(AppSettingsEntity())
        resetThresholdsToDefault()
    }
}
