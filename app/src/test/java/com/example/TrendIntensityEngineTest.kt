package com.example

import com.example.engine.HoraEngine
import com.example.engine.PanchangEngine
import com.example.engine.SarvatobhadraEngine
import com.example.engine.TrendIntensityEngine
import com.example.model.MarketAsset
import com.example.model.TrendMarkerType
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class TrendIntensityEngineTest {

    @Test
    fun test24HourTrendReportGeneration() {
        val calendar = Calendar.getInstance()
        val sbc = SarvatobhadraEngine.calculateSbcState(calendar)
        val panchang = PanchangEngine.calculatePanchang(calendar)
        val horas = HoraEngine.calculateDailyHoras(calendar)

        val report = TrendIntensityEngine.calculate24HourTrend(
            asset = MarketAsset.NIFTY_50,
            calendar = calendar,
            panchang = panchang,
            horas = horas,
            sbcState = sbc,
            currentHour = 10,
            isRolling24Hours = false
        )

        assertNotNull(report)
        assertEquals(24, report.points.size)

        // Verify intensity scores within bound [-100, 100]
        report.points.forEach { pt ->
            assertTrue("Intensity score out of range: ${pt.intensityScore}", pt.intensityScore in -100f..100f)
            assertTrue("Hora power out of range: ${pt.horaPower}", pt.horaPower in -100f..100f)
            assertNotNull(pt.horaPlanet)
            assertNotNull(pt.actionableGuidance)
            assertNotNull(pt.dominantInfluence)
        }

        // Verify key milestones are recognized
        val abhijitPoint = report.points.find { it.specialMarkerType == TrendMarkerType.ABHIJIT_MUHURTA }
        assertNotNull("Abhijit Muhurta point should be detected", abhijitPoint)

        val rahuPoint = report.points.find { it.specialMarkerType == TrendMarkerType.RAHU_KAAL }
        assertNotNull("Rahu Kaal point should be detected", rahuPoint)

        // Verify summary calculations
        assertNotNull(report.peakBullishPoint)
        assertNotNull(report.peakBearishPoint)
        assertTrue(report.peakBullishPoint.intensityScore >= report.peakBearishPoint.intensityScore)
        assertEquals(24, report.bullishHoursCount + report.bearishHoursCount + report.neutralHoursCount)
    }

    @Test
    fun testRolling24HoursStartsAtCurrentHour() {
        val calendar = Calendar.getInstance()
        val sbc = SarvatobhadraEngine.calculateSbcState(calendar)
        val panchang = PanchangEngine.calculatePanchang(calendar)
        val horas = HoraEngine.calculateDailyHoras(calendar)
        val currentHour = 14

        val report = TrendIntensityEngine.calculate24HourTrend(
            asset = MarketAsset.BANK_NIFTY,
            calendar = calendar,
            panchang = panchang,
            horas = horas,
            sbcState = sbc,
            currentHour = currentHour,
            isRolling24Hours = true
        )

        assertEquals(24, report.points.size)
        assertEquals(currentHour, report.points.first().hourOfDay)
    }
}
