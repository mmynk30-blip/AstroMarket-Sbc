package com.example.engine

import com.example.model.TimingWindow
import java.util.Calendar
import kotlin.math.*

data class SolarEphemeris(
    val sunriseTime: String,
    val sunsetTime: String,
    val solarNoonTime: String,
    val daylightMinutes: Int,
    val sunriseMinutesFromMidnight: Int,
    val sunsetMinutesFromMidnight: Int,
    val rahuKaalWindow: TimingWindow,
    val yamagandaWindow: TimingWindow,
    val gulikaWindow: TimingWindow,
    val abhijitMuhurtaWindow: TimingWindow
) {
    val daylightHoursFormatted: String
        get() {
            val h = daylightMinutes / 60
            val m = daylightMinutes % 60
            return "${h}h ${m}m"
        }
}

object SolarEphemerisEngine {

    /**
     * Computes high-precision local solar ephemeris and Ashtama Bhaga timing windows
     */
    fun calculate(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double
    ): SolarEphemeris {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        // Solar Declination (degrees)
        val radPerDeg = Math.PI / 180.0
        val declination = 23.45 * sin((360.0 / 365.0 * (284 + dayOfYear)) * radPerDeg)

        // Equation of Time (minutes)
        val bDeg = (360.0 / 365.0) * (dayOfYear - 81)
        val bRad = bDeg * radPerDeg
        val eot = 9.87 * sin(2 * bRad) - 7.53 * cos(bRad) - 1.5 * sin(bRad)

        // Time correction for longitude vs timezone meridian (minutes)
        val standardMeridian = timezoneOffsetHours * 15.0
        val timeCorrection = 4.0 * (longitude - standardMeridian) + eot

        // Solar Noon in minutes from local midnight
        val solarNoonMinutes = (720.0 - timeCorrection).roundToInt()

        // Solar zenith angle at sunrise/sunset with atmospheric refraction (~90.833°)
        val latRad = latitude * radPerDeg
        val decRad = declination * radPerDeg
        val cosZenith = cos(90.833 * radPerDeg)

        val cosHourAngle = (cosZenith - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCos = cosHourAngle.coerceIn(-1.0, 1.0)
        val hourAngleDeg = acos(clampedCos) * (180.0 / Math.PI)

        val halfDaylightMinutes = (hourAngleDeg * 4.0).roundToInt()

        val sunriseMinutes = (solarNoonMinutes - halfDaylightMinutes).coerceIn(0, 1439)
        val sunsetMinutes = (solarNoonMinutes + halfDaylightMinutes).coerceIn(0, 1439)
        val daylightMinutes = (sunsetMinutes - sunriseMinutes).coerceAtLeast(60)

        // Ashtama Bhaga: divide daylight duration into 8 equal parts
        val oneBhaga = daylightMinutes / 8

        fun formatMinutesToTime(mins: Int): String {
            val normalized = (mins % 1440 + 1440) % 1440
            val h = normalized / 60
            val m = normalized % 60
            return String.format("%02d:%02d", h, m)
        }

        // Rahu Kaal Ashtama Bhaga index (0-indexed)
        val rahuBhagaIndex = when (dayOfWeek) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 6
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 3
            else -> 2 // Saturday
        }

        val rahuStart = sunriseMinutes + rahuBhagaIndex * oneBhaga
        val rahuEnd = rahuStart + oneBhaga

        // Yamaganda Ashtama Bhaga index
        val yamaBhagaIndex = when (dayOfWeek) {
            Calendar.SUNDAY -> 4
            Calendar.MONDAY -> 3
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 1
            Calendar.THURSDAY -> 0
            Calendar.FRIDAY -> 6
            else -> 5 // Saturday
        }

        val yamaStart = sunriseMinutes + yamaBhagaIndex * oneBhaga
        val yamaEnd = yamaStart + oneBhaga

        // Gulika Kaal Ashtama Bhaga index
        val gulikaBhagaIndex = when (dayOfWeek) {
            Calendar.SUNDAY -> 6
            Calendar.MONDAY -> 5
            Calendar.TUESDAY -> 4
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 2
            Calendar.FRIDAY -> 1
            else -> 0 // Saturday
        }

        val gulikaStart = sunriseMinutes + gulikaBhagaIndex * oneBhaga
        val gulikaEnd = gulikaStart + oneBhaga

        // Abhijit Muhurta: 1 Muhurta (48 minutes) centered exactly around Solar Noon
        val abhijitStart = solarNoonMinutes - 24
        val abhijitEnd = solarNoonMinutes + 24

        val isWednesday = dayOfWeek == Calendar.WEDNESDAY

        val rahuWindow = TimingWindow(
            name = "Rahu Kaal",
            startTime = formatMinutesToTime(rahuStart),
            endTime = formatMinutesToTime(rahuEnd),
            isAuspicious = false,
            tradingImpact = "High whipsaw volatility risk. Avoid breakout long entries.",
            cautionLevel = "Extreme Caution"
        )

        val yamaWindow = TimingWindow(
            name = "Yamaganda Kaal",
            startTime = formatMinutesToTime(yamaStart),
            endTime = formatMinutesToTime(yamaEnd),
            isAuspicious = false,
            tradingImpact = "Stagnant momentum and false directional breaks. Tighten stops.",
            cautionLevel = "Moderate Caution"
        )

        val gulikaWindow = TimingWindow(
            name = "Gulika Kaal",
            startTime = formatMinutesToTime(gulikaStart),
            endTime = formatMinutesToTime(gulikaEnd),
            isAuspicious = true,
            tradingImpact = "Saturnine consolidation; favors accumulation and algorithmic scaling.",
            cautionLevel = "Neutral/Strategic"
        )

        val abhijitWindow = TimingWindow(
            name = "Abhijit Muhurta",
            startTime = formatMinutesToTime(abhijitStart),
            endTime = formatMinutesToTime(abhijitEnd),
            isAuspicious = !isWednesday,
            tradingImpact = if (isWednesday) {
                "Wednesday afflicted Abhijit. Exercise moderate caution around midday turn."
            } else {
                "Golden midday solar peak. High-probability trend acceleration or sharp reversal."
            },
            cautionLevel = if (isWednesday) "Afflicted Muhurta" else "Golden Hour"
        )

        return SolarEphemeris(
            sunriseTime = formatMinutesToTime(sunriseMinutes),
            sunsetTime = formatMinutesToTime(sunsetMinutes),
            solarNoonTime = formatMinutesToTime(solarNoonMinutes),
            daylightMinutes = daylightMinutes,
            sunriseMinutesFromMidnight = sunriseMinutes,
            sunsetMinutesFromMidnight = sunsetMinutes,
            rahuKaalWindow = rahuWindow,
            yamagandaWindow = yamaWindow,
            gulikaWindow = gulikaWindow,
            abhijitMuhurtaWindow = abhijitWindow
        )
    }
}
