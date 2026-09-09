package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AyanamsaSystem

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val locationName: String = "Mumbai",
    val marketLabel: String = "NSE / BSE (Dalal Street)",
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val timezoneOffsetHours: Double = 5.5,
    val timezoneId: String = "Asia/Kolkata",
    val ayanamsaSystemCode: String = AyanamsaSystem.LAHIRI.code,
    val isCustomLocation: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),

    // Sarvatobhadra Chakra Market Intensity Alert Settings
    val alertsEnabled: Boolean = true,
    val alertThresholdDelta: Float = 25f,
    val alertBullishThreshold: Float = 45f,
    val alertBearishThreshold: Float = -45f,
    val alertOnBullishThreshold: Boolean = true,
    val alertOnBearishThreshold: Boolean = true,
    val alertOnRegimeFlip: Boolean = true,
    val alertOnAbhijitMuhurta: Boolean = true,
    val alertOnRahuKaal: Boolean = true,
    val alertOnHarmonics: Boolean = true,
    val lastTriggeredAlertHash: String = ""
)
