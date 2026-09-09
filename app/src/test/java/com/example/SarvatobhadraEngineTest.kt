package com.example

import com.example.engine.HoraEngine
import com.example.engine.PanchangEngine
import com.example.engine.SarvatobhadraEngine
import com.example.engine.TimeCycleEngine
import com.example.model.MarketAsset
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class SarvatobhadraEngineTest {

    @Test
    fun testSarvatobhadraGridStructure() {
        val calendar = Calendar.getInstance()
        val sbcState = SarvatobhadraEngine.calculateSbcState(calendar)

        // Verify 9x9 grid size
        assertEquals(9, sbcState.grid.size)
        sbcState.grid.forEach { row ->
            assertEquals(9, row.size)
        }

        // Verify active Vedhas exist
        assertTrue(sbcState.activeVedhas.isNotEmpty())
        assertTrue(sbcState.planetaryPositions.isNotEmpty())
    }

    @Test
    fun testPanchangLimbs() {
        val calendar = Calendar.getInstance()
        val panchang = PanchangEngine.calculatePanchang(calendar)

        assertNotNull(panchang.tithiName)
        assertNotNull(panchang.nakshatraName)
        assertNotNull(panchang.yogaName)
        assertNotNull(panchang.karanaName)
        assertNotNull(panchang.dayLord)
        assertNotNull(panchang.rahuKaal)
        assertNotNull(panchang.abhijitMuhurta)
    }

    @Test
    fun testHoraCycleCount() {
        val calendar = Calendar.getInstance()
        val horas = HoraEngine.calculateDailyHoras(calendar)

        assertEquals(24, horas.size)
        val marketHoras = horas.filter { it.isMarketHours }
        assertTrue(marketHoras.isNotEmpty())
    }

    @Test
    fun testTimeCyclePrediction() {
        val calendar = Calendar.getInstance()
        val sbc = SarvatobhadraEngine.calculateSbcState(calendar)
        val panchang = PanchangEngine.calculatePanchang(calendar)
        val horas = HoraEngine.calculateDailyHoras(calendar)

        val prediction = TimeCycleEngine.generateDailyPrediction(
            asset = MarketAsset.NIFTY_50,
            calendar = calendar,
            sbcState = sbc,
            panchang = panchang,
            horas = horas
        )

        assertNotNull(prediction)
        assertTrue(prediction.timeCycles.isNotEmpty())
        assertTrue(prediction.sectorRankings.isNotEmpty())
        assertTrue(prediction.keyReversalTimes.isNotEmpty())
    }

    @Test
    fun testStockTickerNakshatraMappings() {
        val mappings = com.example.model.SarvatobhadraTickerRepository.DEFAULT_TICKER_MAPPINGS
        assertTrue("Expected rich ticker catalog", mappings.size >= 28)

        // Verify key benchmark and mega-caps are mapped
        val symbols = mappings.map { it.tickerSymbol }
        assertTrue(symbols.contains("RELIANCE"))
        assertTrue(symbols.contains("NIFTY"))
        assertTrue(symbols.contains("HDFCBANK"))
        assertTrue(symbols.contains("INFY"))
        assertTrue(symbols.contains("NVDA"))

        // Verify all 28 Nakshatras are covered
        val mappedNakshatras = mappings.map { it.nakshatraName }.toSet()
        SarvatobhadraEngine.SBC_NAKSHATRAS.forEach { nakName ->
            assertTrue("Nakshatra $nakName should have at least one mapped ticker", mappedNakshatras.contains(nakName))
        }

        // Test posture evaluation for a ticker under current SBC state
        val calendar = Calendar.getInstance()
        val sbc = SarvatobhadraEngine.calculateSbcState(calendar)
        val relianceMapping = mappings.first { it.tickerSymbol == "RELIANCE" }
        val posture = com.example.model.SarvatobhadraTickerRepository.evaluateTickerPosture(relianceMapping, sbc)

        assertNotNull(posture)
        assertEquals("RELIANCE", posture.mapping.tickerSymbol)
        assertTrue(posture.sentimentScore in -100..100)
        assertNotNull(posture.stance)
        assertTrue(posture.summaryText.isNotEmpty())
    }
}

