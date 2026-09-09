package com.example.engine

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.MarketAsset
import com.example.model.Planet
import com.example.model.SbcIntensityShift
import com.example.model.SbcShiftType

object SbcNotificationManager {

    const val CHANNEL_ID = "sbc_market_intensity_channel"
    const val CHANNEL_NAME = "SBC Market Intensity Alerts"
    const val CHANNEL_DESC = "Notifications for significant market intensity shifts in the Sarvatobhadra Chakra cycle"

    private const val BASE_NOTIFICATION_ID = 7000

    /**
     * Initializes the Android Notification Channel (Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#F59E0B") // Vedic Gold
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether local notifications are fully enabled and permitted by the system.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            return permissionStatus == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Dispatches a local notification for a detected Sarvatobhadra Chakra intensity shift.
     * Returns true if notification was posted, false if permission was absent.
     */
    fun postShiftAlert(
        context: Context,
        shift: SbcIntensityShift
    ): Boolean {
        createNotificationChannel(context)

        if (!areNotificationsEnabled(context)) {
            return false
        }

        val notificationManager = NotificationManagerCompat.from(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", 1) // Switch to Cycles & 24-Hour Trend tab
            putExtra("alert_hour", shift.clockHour)
            putExtra("asset_name", shift.asset.name)
            putExtra("shift_type", shift.shiftType.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            shift.clockHour + BASE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val colorInt = when (shift.shiftType) {
            SbcShiftType.BULLISH_SURGE, SbcShiftType.REGIME_FLIP_BULLISH, SbcShiftType.BULLISH_THRESHOLD_BREACH ->
                android.graphics.Color.parseColor("#10B981") // Emerald Green
            SbcShiftType.BEARISH_DIVE, SbcShiftType.REGIME_FLIP_BEARISH, SbcShiftType.BEARISH_THRESHOLD_BREACH ->
                android.graphics.Color.parseColor("#EF4444") // Bearish Red
            SbcShiftType.ABHIJIT_MUHURTA_PEAK ->
                android.graphics.Color.parseColor("#F59E0B") // Vedic Gold
            SbcShiftType.RAHU_KAAL_VOLATILITY ->
                android.graphics.Color.parseColor("#F43F5E") // Crimson Rose
            SbcShiftType.HARMONIC_INFLECTION ->
                android.graphics.Color.parseColor("#06B6D4") // Cyan
        }

        val bigText = buildString {
            append(shift.alertMessage)
            append("\n\n")
            append("• Astrological Context: ${shift.detailedAnalysis}\n")
            append("• Actionable Guidance: ${shift.actionableGuidance}")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sbc_alert)
            .setContentTitle(shift.alertTitle)
            .setContentText(shift.alertMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(shift.alertTitle)
                    .setSummaryText("Sarvatobhadra Cycle Alert")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(colorInt)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250))

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            return false
        }

        val notificationId = BASE_NOTIFICATION_ID + shift.clockHour
        notificationManager.notify(notificationId, builder.build())
        return true
    }

    /**
     * Sends an immediate test notification demonstrating the SBC intensity alert system.
     */
    fun sendTestAlert(
        context: Context,
        asset: MarketAsset = MarketAsset.NIFTY_50
    ): Boolean {
        createNotificationChannel(context)

        if (!areNotificationsEnabled(context)) {
            return false
        }

        val testShift = SbcIntensityShift(
            id = "test_alert_${System.currentTimeMillis()}",
            hourIndex = 11,
            clockHour = 11,
            timeRange = "11:48 - 12:38",
            shiftType = SbcShiftType.ABHIJIT_MUHURTA_PEAK,
            previousIntensity = 14.5f,
            newIntensity = 68.2f,
            intensityDelta = 53.7f,
            horaLord = Planet.JUPITER,
            horaMarketBias = com.example.model.HoraMarketBias.STRONG_BULLISH,
            asset = asset,
            favoredSectors = listOf("Banking & NBFC", "Mega-Cap Tech", "Gold"),
            cautiousSectors = listOf("High-Debt Energy"),
            alertTitle = "★ Abhijit Muhurta Zenith Surge (+53.7 pts)",
            alertMessage = "${asset.displayName}: Predicted intensity shifts to +68.2 in Abhijit Muhurta window under Jupiter Lordship.",
            detailedAnalysis = "Sarvatobhadra Chakra registers a major harmonic inflection. Benefic rays from Jupiter and Sun converge during the solar mid-day zenith.",
            actionableGuidance = "Favorable window for institutional liquidity accumulation. Trailing stops recommended on momentum long contracts.",
            isCurrentHourActive = true,
            isUpcoming = false
        )

        return postShiftAlert(context, testShift)
    }
}
