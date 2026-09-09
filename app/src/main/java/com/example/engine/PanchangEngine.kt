package com.example.engine

import com.example.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object PanchangEngine {

    private val TITHI_NAMES = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
        "Pratipada (K)", "Dwitiya (K)", "Tritiya (K)", "Chaturthi (K)", "Panchami (K)",
        "Shashthi (K)", "Saptami (K)", "Ashtami (K)", "Navami (K)", "Dashami (K)",
        "Ekadashi (K)", "Dwadashi (K)", "Trayodashi (K)", "Chaturdashi (K)", "Amavasya"
    )

    private val NAKSHATRAS = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    private val YOGAS = listOf(
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda",
        "Sukarma", "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva",
        "Vyaghata", "Harshana", "Vajra", "Siddhi", "Vyatipata", "Variyan",
        "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha", "Shukla",
        "Brahma", "Indra", "Vaidhriti"
    )

    private val KARANAS = listOf(
        "Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)",
        "Shakuni", "Chatushpada", "Naga", "Kintughna"
    )

    private val ZODIAC_SIGNS = listOf(
        "Aries (Mesha)", "Taurus (Vrishabha)", "Gemini (Mithuna)", "Cancer (Karka)",
        "Leo (Simha)", "Virgo (Kanya)", "Libra (Tula)", "Scorpio (Vrishchika)",
        "Sagittarius (Dhanu)", "Capricorn (Makara)", "Aquarius (Kumbha)", "Pisces (Meena)"
    )

    fun calculatePanchang(
        calendar: Calendar,
        latitude: Double = 19.0760,
        longitude: Double = 72.8777,
        timezoneOffsetHours: Double = 5.5,
        locationName: String = "Mumbai (NSE)",
        ayanamsa: AyanamsaSystem = AyanamsaSystem.LAHIRI
    ): PanchangDetails {
        val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val epochDay = (year - 2000) * 365 + dayOfYear

        // High-precision Solar Ephemeris & Ashtama Bhaga timing for custom location
        val solarEphemeris = SolarEphemerisEngine.calculate(
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            timezoneOffsetHours = timezoneOffsetHours
        )

        val dayLord = when (dayOfWeekInt) {
            Calendar.SUNDAY -> Planet.SUN
            Calendar.MONDAY -> Planet.MOON
            Calendar.TUESDAY -> Planet.MARS
            Calendar.WEDNESDAY -> Planet.MERCURY
            Calendar.THURSDAY -> Planet.JUPITER
            Calendar.FRIDAY -> Planet.VENUS
            else -> Planet.SATURN
        }

        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayOfWeekName = dayNames[dayOfWeekInt - 1]

        // Ayanamsa delta relative to Lahiri benchmark
        val ayanamsaDelta = ayanamsa.baseValueDegrees - AyanamsaSystem.LAHIRI.baseValueDegrees
        val locationLongitudeOffset = (longitude - 72.8777) / 360.0

        // Approximate Tithi (lunar month ~ 29.53 days)
        val adjustedEpochDay = epochDay + locationLongitudeOffset
        val tithiIndex = (((adjustedEpochDay.toInt() % 30) + 30) % 30)
        val tithiNumber = tithiIndex + 1
        val tithiName = TITHI_NAMES[tithiIndex]
        val isShukla = tithiNumber <= 15
        val paksha = if (isShukla) "Shukla Paksha (Waxing)" else "Krishna Paksha (Waning)"

        // Tithi category: Nanda (1,6,11), Bhadra (2,7,12), Jaya (3,8,13), Rikta (4,9,14), Purna (5,10,15)
        val rem5 = (tithiNumber - 1) % 5
        val tithiCategory = when (rem5) {
            0 -> TithiCategory.NANDA
            1 -> TithiCategory.BHADRA
            2 -> TithiCategory.JAYA
            3 -> TithiCategory.RIKTA
            else -> TithiCategory.PURNA
        }

        // Nakshatra calculation with Ayanamsa precession offset (Each nakshatra = 13° 20' = 13.3333°)
        val rawNakshatraCoord = ((epochDay * 13.0) / 10.0 + 5.0) - (ayanamsaDelta / 13.3333) + locationLongitudeOffset
        val nakshatraIndex = (((rawNakshatraCoord.toInt() % 27) + 27) % 27)
        val nakshatraName = NAKSHATRAS[nakshatraIndex]
        val nakshatraLord = when (nakshatraIndex % 9) {
            0 -> Planet.KETU
            1 -> Planet.VENUS
            2 -> Planet.SUN
            3 -> Planet.MOON
            4 -> Planet.MARS
            5 -> Planet.RAHU
            6 -> Planet.JUPITER
            7 -> Planet.SATURN
            else -> Planet.MERCURY
        }

        val nakshatraNature = when (nakshatraIndex) {
            0, 7, 12 -> NakshatraNature.KSHIPRA // Ashwini, Pushya, Hasta
            1, 9, 10, 18, 24 -> NakshatraNature.UGRA // Bharani, Magha, Purva Phalguni, Purva Ashadha, Purva Bhadrapada
            3, 11, 20, 25 -> NakshatraNature.STHIRA // Rohini, Uttara Phalguni, Uttara Ashadha, Uttara Bhadrapada
            6, 14, 21, 22, 23 -> NakshatraNature.CHARA // Punarvasu, Swati, Shravana, Dhanishta, Shatabhisha
            4, 13, 16, 26 -> NakshatraNature.MRIDU // Mrigashira, Chitra, Anuradha, Revati
            else -> NakshatraNature.MISHRA
        }

        // Yoga calculation (Sum of Sun and Moon longitude)
        val yogaIndex = (((epochDay + (ayanamsaDelta / 26.6666).toInt()) % 27 + 27) % 27)
        val yogaName = YOGAS[yogaIndex]
        val isInauspiciousYoga = yogaName in listOf("Vyatipata", "Vaidhriti", "Vishkambha", "Atiganda", "Shula", "Ganda")
        val yogaMarketEffect = if (isInauspiciousYoga) {
            "Crash warning & stop-loss hunting risk. Institutional hedging recommended."
        } else {
            "Favorable astrological flow supporting disciplined dip accumulation."
        }

        // Karana calculation
        val karanaIndex = (epochDay * 2) % 11
        val karanaName = KARANAS[karanaIndex]
        val isVishti = karanaName.contains("Vishti")

        // Moon and Sun Rasi
        val moonRasi = ZODIAC_SIGNS[(nakshatraIndex * 4 / 9) % 12]
        val sunRasi = ZODIAC_SIGNS[(calendar.get(Calendar.MONTH) + 8) % 12]

        // Timings for Rahu Kaal, Yamaganda, Gulika, Abhijit from SolarEphemerisEngine
        val rahuKaal = solarEphemeris.rahuKaalWindow
        val yamagandaKaal = solarEphemeris.yamagandaWindow
        val gulikaKaal = solarEphemeris.gulikaWindow
        val abhijitMuhurta = solarEphemeris.abhijitMuhurtaWindow

        // Sentiment score computation
        var sentiment = 0
        sentiment += if (dayLord.isNaturalBenefic) 20 else -15
        sentiment += if (isShukla) 15 else -10
        sentiment += when (tithiCategory) {
            TithiCategory.NANDA -> 25
            TithiCategory.JAYA -> 20
            TithiCategory.BHADRA -> 5
            TithiCategory.PURNA -> 0
            TithiCategory.RIKTA -> -35
        }
        sentiment += if (isInauspiciousYoga) -30 else 20
        sentiment += if (isVishti) -25 else 10
        sentiment = sentiment.coerceIn(-90, 95)

        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

        return PanchangDetails(
            dateString = dateFormat.format(calendar.time),
            dayOfWeek = dayOfWeekName,
            dayLord = dayLord,
            tithiName = tithiName,
            tithiNumber = tithiNumber,
            tithiCategory = tithiCategory,
            paksha = paksha,
            nakshatraName = nakshatraName,
            nakshatraLord = nakshatraLord,
            nakshatraNature = nakshatraNature,
            yogaName = yogaName,
            isYogaAuspicious = !isInauspiciousYoga,
            yogaMarketEffect = yogaMarketEffect,
            karanaName = karanaName,
            isVishtiKarana = isVishti,
            rahuKaal = rahuKaal,
            yamagandaKaal = yamagandaKaal,
            gulikaKaal = gulikaKaal,
            abhijitMuhurta = abhijitMuhurta,
            moonRasi = moonRasi,
            sunRasi = sunRasi,
            marketSentimentScore = sentiment,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            sunriseTime = solarEphemeris.sunriseTime,
            sunsetTime = solarEphemeris.sunsetTime,
            ayanamsaSystem = ayanamsa,
            ayanamsaFormatted = ayanamsa.formatDegreesMinutesSeconds()
        )
    }

    private fun getRahuKaal(dayOfWeek: Int): TimingWindow {
        val (start, end) = when (dayOfWeek) {
            Calendar.MONDAY -> "07:30" to "09:00"
            Calendar.TUESDAY -> "15:00" to "16:30"
            Calendar.WEDNESDAY -> "12:00" to "13:30"
            Calendar.THURSDAY -> "13:30" to "15:00"
            Calendar.FRIDAY -> "10:30" to "12:00"
            Calendar.SATURDAY -> "09:00" to "10:30"
            else -> "16:30" to "18:00" // Sunday
        }
        return TimingWindow(
            name = "Rahu Kaal",
            startTime = start,
            endTime = end,
            isAuspicious = false,
            tradingImpact = "Heightened risk of false breakouts, sudden panic wicks, and option trap squeezes.",
            cautionLevel = "High Risk"
        )
    }

    private fun getYamaganda(dayOfWeek: Int): TimingWindow {
        val (start, end) = when (dayOfWeek) {
            Calendar.MONDAY -> "10:30" to "12:00"
            Calendar.TUESDAY -> "09:00" to "10:30"
            Calendar.WEDNESDAY -> "07:30" to "09:00"
            Calendar.THURSDAY -> "06:00" to "07:30"
            Calendar.FRIDAY -> "15:00" to "16:30"
            Calendar.SATURDAY -> "13:30" to "15:00"
            else -> "12:00" to "13:30" // Sunday
        }
        return TimingWindow(
            name = "Yamaganda",
            startTime = start,
            endTime = end,
            isAuspicious = false,
            tradingImpact = "Sudden momentum stalls. Liquidity contraction zone.",
            cautionLevel = "Caution"
        )
    }

    private fun getGulika(dayOfWeek: Int): TimingWindow {
        val (start, end) = when (dayOfWeek) {
            Calendar.MONDAY -> "13:30" to "15:00"
            Calendar.TUESDAY -> "12:00" to "13:30"
            Calendar.WEDNESDAY -> "10:30" to "12:00"
            Calendar.THURSDAY -> "09:00" to "10:30"
            Calendar.FRIDAY -> "07:30" to "09:00"
            Calendar.SATURDAY -> "06:00" to "07:30"
            else -> "15:00" to "16:30" // Sunday
        }
        return TimingWindow(
            name = "Gulika Kaal",
            startTime = start,
            endTime = end,
            isAuspicious = true,
            tradingImpact = "Subtle accumulation window. Often marks the base for the afternoon breakout.",
            cautionLevel = "Safe"
        )
    }
}
