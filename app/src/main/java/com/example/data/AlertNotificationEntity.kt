package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history")
data class AlertNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val timeRange: String,
    val clockHour: Int,
    val assetSymbol: String,
    val shiftType: String,
    val intensityDelta: Float,
    val previousIntensity: Float,
    val newIntensity: Float,
    val horaLord: String,
    val title: String,
    val message: String,
    val guidance: String
)
