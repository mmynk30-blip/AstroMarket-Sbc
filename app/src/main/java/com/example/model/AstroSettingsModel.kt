package com.example.model

/**
 * Supported Ayanamsa Systems for Sidereal vs Tropical calculation
 */
enum class AyanamsaSystem(
    val code: String,
    val displayName: String,
    val baseValueDegrees: Double,
    val description: String,
    val recommendedUse: String
) {
    LAHIRI(
        code = "LAHIRI",
        displayName = "Lahiri (Chitra Paksha)",
        baseValueDegrees = 24.2305, // ~24° 13' 50" in modern epoch
        description = "Official Indian Government Calendar Reform Committee standard. Aligns Spica (Chitra) at 180°.",
        recommendedUse = "Recommended default for Sarvatobhadra Chakra, Indian equities, and classical Panchang."
    ),
    KP(
        code = "KP",
        displayName = "Krishnamurti Paddhati (KP)",
        baseValueDegrees = 24.1166, // ~24° 07' 00"
        description = "Calculated using Newcomb's solar motion with KP star positions and sub-lord divisions.",
        recommendedUse = "Preferred for high-frequency sub-nakshatra turning points and intraday trading."
    ),
    RAMAN(
        code = "RAMAN",
        displayName = "B.V. Raman",
        baseValueDegrees = 22.7042, // ~22° 42' 15"
        description = "Formulated by Dr. B.V. Raman based on traditional Surya Siddhanta benchmarks.",
        recommendedUse = "Favored for commodity markets, bullion cycles, and macro harmonic turns."
    ),
    SAYANA(
        code = "SAYANA",
        displayName = "Sayana (Tropical / 0°)",
        baseValueDegrees = 0.0,
        description = "Western Tropical Zodiac using the Vernal Equinox as 0° Aries without sidereal precession offset.",
        recommendedUse = "Standard for W.D. Gann seasonal cycles, planetary aspects, and Bradley Siderograph."
    ),
    FAGAN_BRADLEY(
        code = "FAGAN_BRADLEY",
        displayName = "Fagan-Bradley",
        baseValueDegrees = 24.8450, // ~24° 50' 42"
        description = "Western Sidereal Zodiac benchmark anchored to the fixed star Spica at 29° Virgo.",
        recommendedUse = "Standard for Western sidereal financial market studies."
    ),
    YUKTESHWAR(
        code = "YUKTESHWAR",
        displayName = "Sri Yukteshwar",
        baseValueDegrees = 21.8210, // ~21° 49' 16"
        description = "Derived from Swami Sri Yukteshwar's Holy Science cosmic equinoctial yuga model.",
        recommendedUse = "Useful for multi-year secular macro cycles and structural market trends."
    );

    fun formatDegreesMinutesSeconds(): String {
        val totalSeconds = (baseValueDegrees * 3600).toInt()
        val deg = totalSeconds / 3600
        val min = (kotlin.math.abs(totalSeconds) % 3600) / 60
        val sec = kotlin.math.abs(totalSeconds) % 60
        return String.format("%02d° %02d' %02d\"", deg, min, sec)
    }
}

/**
 * Major financial and astronomical trading hub presets
 */
data class TradingHubPreset(
    val id: String,
    val name: String,
    val marketLabel: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double,
    val timezoneId: String,
    val countryFlag: String
)

object TradingHubPresets {
    val PRESETS = listOf(
        TradingHubPreset(
            id = "MUMBAI",
            name = "Mumbai",
            marketLabel = "NSE / BSE (Dalal Street)",
            latitude = 19.0760,
            longitude = 72.8777,
            timezoneOffsetHours = 5.5,
            timezoneId = "Asia/Kolkata",
            countryFlag = "🇮🇳"
        ),
        TradingHubPreset(
            id = "DELHI",
            name = "New Delhi",
            marketLabel = "National Standard IST",
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneOffsetHours = 5.5,
            timezoneId = "Asia/Kolkata",
            countryFlag = "🇮🇳"
        ),
        TradingHubPreset(
            id = "NEW_YORK",
            name = "New York",
            marketLabel = "NYSE / NASDAQ (Wall Street)",
            latitude = 40.7128,
            longitude = -74.0060,
            timezoneOffsetHours = -4.0, // EDT (standard summer offset)
            timezoneId = "America/New_York",
            countryFlag = "🇺🇸"
        ),
        TradingHubPreset(
            id = "LONDON",
            name = "London",
            marketLabel = "LSE / Global Bullion",
            latitude = 51.5074,
            longitude = -0.1278,
            timezoneOffsetHours = 1.0, // BST
            timezoneId = "Europe/London",
            countryFlag = "🇬🇧"
        ),
        TradingHubPreset(
            id = "TOKYO",
            name = "Tokyo",
            marketLabel = "TSE / Nikkei",
            latitude = 35.6762,
            longitude = 139.6503,
            timezoneOffsetHours = 9.0,
            timezoneId = "Asia/Tokyo",
            countryFlag = "🇯🇵"
        ),
        TradingHubPreset(
            id = "SINGAPORE",
            name = "Singapore",
            marketLabel = "SGX Financial Centre",
            latitude = 1.3521,
            longitude = 103.8198,
            timezoneOffsetHours = 8.0,
            timezoneId = "Asia/Singapore",
            countryFlag = "🇸🇬"
        ),
        TradingHubPreset(
            id = "DUBAI",
            name = "Dubai",
            marketLabel = "DFM / Energy & Gold Hub",
            latitude = 25.2048,
            longitude = 55.2708,
            timezoneOffsetHours = 4.0,
            timezoneId = "Asia/Dubai",
            countryFlag = "🇦🇪"
        )
    )

    val DEFAULT = PRESETS.first()
}
