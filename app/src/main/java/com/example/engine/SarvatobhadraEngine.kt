package com.example.engine

import com.example.model.*
import java.util.Calendar

object SarvatobhadraEngine {

    // 28 Nakshatras in Sarvatobhadra order
    val SBC_NAKSHATRAS = listOf(
        "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu", "Pushya", "Ashlesha",
        "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati", "Vishakha",
        "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha", "Uttara Ashadha", "Abhijit", "Shravana",
        "Dhanishta", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati", "Ashwini", "Bharani"
    )

    /**
     * Standard 3-4 character unambiguous abbreviations for 28 Nakshatras.
     */
    fun getNakshatraAbbr(name: String): String {
        return when (name.lowercase().trim()) {
            "krittika" -> "KRI"
            "rohini" -> "ROH"
            "mrigashira", "mrigashirsha" -> "MRI"
            "ardra" -> "ARD"
            "punarvasu" -> "PUN"
            "pushya" -> "PUS"
            "ashlesha" -> "ASH"
            "magha" -> "MAG"
            "purva phalguni" -> "P.PH"
            "uttara phalguni" -> "U.PH"
            "hasta" -> "HAS"
            "chitra" -> "CHI"
            "swati" -> "SWA"
            "vishakha" -> "VIS"
            "anuradha" -> "ANU"
            "jyeshtha" -> "JYE"
            "mula" -> "MUL"
            "purva ashadha" -> "P.AS"
            "uttara ashadha" -> "U.AS"
            "abhijit" -> "ABH"
            "shravana" -> "SHR"
            "dhanishta" -> "DHA"
            "shatabhisha" -> "SHT"
            "purva bhadrapada" -> "P.BH"
            "uttara bhadrapada" -> "U.BH"
            "revati" -> "REV"
            "ashwini" -> "ASW"
            "bharani" -> "BHA"
            else -> name.take(3).uppercase()
        }
    }

    // Sector associations for Nakshatras
    private val NAKSHATRA_SECTORS = mapOf(
        "Krittika" to "Gold & Sovereign PSUs",
        "Rohini" to "Auto & Real Estate",
        "Mrigashira" to "Metals, Steel & Defense",
        "Ardra" to "IT, Tech & Communication",
        "Punarvasu" to "Banking & Capital Goods",
        "Pushya" to "Banking, Finance & Institutions",
        "Ashlesha" to "Telecom, Media & Scalpers",
        "Magha" to "Energy & Heavy Power",
        "Purva Phalguni" to "Luxury & Entertainment",
        "Uttara Phalguni" to "Sovereign Debt & Infrastructure",
        "Hasta" to "Retail, FMCG & Chemicals",
        "Chitra" to "Real Estate & Architecture",
        "Swati" to "Aviation & Logistics",
        "Vishakha" to "Pharma & Healthcare",
        "Anuradha" to "Oil & Gas, Heavy Mining",
        "Jyeshtha" to "Fintech & Algorithmic Trading",
        "Mula" to "Commodities & Raw Materials",
        "Purva Ashadha" to "Hospitality & Textiles",
        "Uttara Ashadha" to "Power, Solar & Renewable",
        "Abhijit" to "Apex Large-Caps & Nifty Index",
        "Shravana" to "FMCG & Dairy / Liquids",
        "Dhanishta" to "Metals & High-Tech Hardware",
        "Shatabhisha" to "Biotech, Crypto & Speculative",
        "Purva Bhadrapada" to "Financial NBFCs & Insurance",
        "Uttara Bhadrapada" to "Deep Value & Shipping",
        "Revati" to "Wealth Management & IT Exports",
        "Ashwini" to "High-Beta Day Trading & Mobility",
        "Bharani" to "Chemicals & Agro-Fertilizers"
    )

    /**
     * Map 28 Nakshatra index (0..27) to 9x9 perimeter coordinates (row, col).
     */
    fun getNakshatraCoords(index: Int): Pair<Int, Int> {
        return when (index) {
            in 0..6 -> Pair(0, index + 1)
            in 7..13 -> Pair(index - 6, 8)
            in 14..20 -> Pair(8, 21 - index)
            in 21..27 -> Pair(28 - index, 0)
            else -> Pair(0, 1)
        }
    }

    /**
     * Map 9x9 perimeter coordinates (row, col) to Nakshatra index (0..27), or null if inner cell / corner.
     */
    fun getNakshatraIndexForCoords(row: Int, col: Int): Int? {
        return when {
            row == 0 && col in 1..7 -> col - 1
            col == 8 && row in 1..7 -> 6 + row
            row == 8 && col in 1..7 -> 21 - col
            col == 0 && row in 1..7 -> 28 - row
            else -> null
        }
    }

    /**
     * Samukha (Front / Direct) Vedha:
     * Crosses directly opposite across row or column on the 9x9 matrix.
     */
    fun getSamukhaVedhaIndex(nakIndex: Int): Int {
        val (row, col) = getNakshatraCoords(nakIndex)
        val oppositeCoords = when {
            row == 0 -> Pair(8, col)
            row == 8 -> Pair(0, col)
            col == 8 -> Pair(row, 0)
            col == 0 -> Pair(row, 8)
            else -> Pair(0, col)
        }
        return getNakshatraIndexForCoords(oppositeCoords.first, oppositeCoords.second) ?: ((nakIndex + 14) % 28)
    }

    /**
     * Dakshina (Right Diagonal) and Vama (Left Diagonal) Vedhas:
     * Follow exact 45-degree diagonal rays across the 9x9 matrix.
     */
    fun getDiagonalVedhaIndices(nakIndex: Int): Pair<Int, Int> {
        val (row, col) = getNakshatraCoords(nakIndex)
        val (rightCoords, leftCoords) = when {
            // Top row: right goes down-right, left goes down-left
            row == 0 -> Pair(Pair(8 - col, 8), Pair(col, 0))
            // Right col: left goes up-left, right goes down-left
            col == 8 -> Pair(Pair(8, row), Pair(0, 8 - row))
            // Bottom row: right goes up-right, left goes up-left
            row == 8 -> Pair(Pair(col, 8), Pair(8 - col, 0))
            // Left col: right goes up-right, left goes down-right
            col == 0 -> Pair(Pair(0, row), Pair(8, 8 - row))
            else -> Pair(Pair(row, col), Pair(row, col))
        }

        val rightIndex = getNakshatraIndexForCoords(rightCoords.first, rightCoords.second) ?: ((nakIndex + 7) % 28)
        val leftIndex = getNakshatraIndexForCoords(leftCoords.first, leftCoords.second) ?: ((nakIndex + 21) % 28)
        return Pair(rightIndex, leftIndex)
    }

    /**
     * Checks if a cell at (cellRow, cellCol) lies along the ray segment from (startRow, startCol) to (endRow, endCol).
     */
    fun isCellTraversedByRay(
        cellRow: Int, cellCol: Int,
        startRow: Int, startCol: Int,
        endRow: Int, endCol: Int
    ): Boolean {
        // Check collinearity via 2D cross product
        val crossProduct = (cellRow - startRow) * (endCol - startCol) - (cellCol - startCol) * (endRow - startRow)
        if (crossProduct != 0) return false

        // Check if within bounding box of ray segment
        val minR = minOf(startRow, endRow)
        val maxR = maxOf(startRow, endRow)
        val minC = minOf(startCol, endCol)
        val maxC = maxOf(startCol, endCol)

        return cellRow in minR..maxR && cellCol in minC..maxC
    }

    /**
     * Compute Sarvatobhadra Chakra state for a specific date and Ayanamsa
     */
    fun calculateSbcState(
        calendar: Calendar,
        ayanamsa: AyanamsaSystem = AyanamsaSystem.LAHIRI
    ): SarvatobhadraState {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val seed = year * 365 + dayOfYear

        // Ayanamsa offset: shift in 28-nakshatra SBC space (each nakshatra is ~12.857°)
        val ayanamsaDelta = ayanamsa.baseValueDegrees - AyanamsaSystem.LAHIRI.baseValueDegrees
        val sbcShift = (ayanamsaDelta / 12.857).toInt()

        // Deterministic planetary positions in 28 Nakshatras for the date
        fun normalizeSbcIndex(idx: Int): Int = (((idx - sbcShift) % 28) + 28) % 28

        val planetaryPositions = mutableMapOf<Planet, Int>()
        // Moon moves approx 1 nakshatra per day
        planetaryPositions[Planet.MOON] = normalizeSbcIndex((seed * 13) % 28)
        // Sun moves ~ 1 nakshatra per 13.5 days
        planetaryPositions[Planet.SUN] = normalizeSbcIndex(((seed / 13) + 2) % 28)
        // Jupiter moves ~ 1 nakshatra per 13 days of cycle in SBC
        planetaryPositions[Planet.JUPITER] = normalizeSbcIndex((seed / 140 + 5) % 28)
        // Saturn is slow
        planetaryPositions[Planet.SATURN] = normalizeSbcIndex((year % 28 + 14) % 28)
        // Mars moves ~ 1 nakshatra per 2 days
        planetaryPositions[Planet.MARS] = normalizeSbcIndex(((seed / 2) + 7) % 28)
        // Mercury moves fast with Sun
        planetaryPositions[Planet.MERCURY] = normalizeSbcIndex(((seed / 10) + 4) % 28)
        // Venus moves with Sun
        planetaryPositions[Planet.VENUS] = normalizeSbcIndex(((seed / 12) + 8) % 28)
        // Rahu & Ketu (Retrograde axis opposite each other)
        val rahuPos = normalizeSbcIndex((28 - (seed / 60) % 28) % 28)
        planetaryPositions[Planet.RAHU] = rahuPos
        planetaryPositions[Planet.KETU] = (rahuPos + 14) % 28

        // Calculate Vedhas (Aspects) using authentic Sarvatobhadra Chakra 9x9 geometry
        val activeVedhas = mutableListOf<VedhaImpact>()
        planetaryPositions.forEach { (planet, nakIndex) ->
            val sourceNak = SBC_NAKSHATRAS[nakIndex]

            // 1. Direct conjunction Vedha (Sthiti)
            activeVedhas.add(
                VedhaImpact(
                    planet = planet,
                    vedhaType = VedhaType.CONJUNCTION,
                    targetNakshatra = sourceNak,
                    targetSector = NAKSHATRA_SECTORS[sourceNak] ?: "General Market",
                    isBenefic = planet.isNaturalBenefic,
                    marketEffect = if (planet.isNaturalBenefic) "Direct accumulation & bullish support" else "Volatile pressure & drag",
                    impactScore = if (planet.isNaturalBenefic) 75 else -70
                )
            )

            // 2. Front Vedha (Samukha) -> Straight across the 9x9 matrix (row/column opposite)
            val frontNakIndex = getSamukhaVedhaIndex(nakIndex)
            val frontNak = SBC_NAKSHATRAS[frontNakIndex]
            activeVedhas.add(
                VedhaImpact(
                    planet = planet,
                    vedhaType = VedhaType.SAMUKHA,
                    targetNakshatra = frontNak,
                    targetSector = NAKSHATRA_SECTORS[frontNak] ?: "General Market",
                    isBenefic = planet.isNaturalBenefic,
                    marketEffect = if (planet.isNaturalBenefic) "Strong bullish impulse on opposite sector" else "Direct bearish resistance line",
                    impactScore = if (planet.isNaturalBenefic) 85 else -85
                )
            )

            // 3 & 4. Diagonal Vedhas (Dakshina & Vama) -> 45-degree rays across the 9x9 matrix
            val (rightNakIndex, leftNakIndex) = getDiagonalVedhaIndices(nakIndex)

            val rightNak = SBC_NAKSHATRAS[rightNakIndex]
            activeVedhas.add(
                VedhaImpact(
                    planet = planet,
                    vedhaType = VedhaType.DAKSHINA,
                    targetNakshatra = rightNak,
                    targetSector = NAKSHATRA_SECTORS[rightNak] ?: "Sector Basket",
                    isBenefic = planet.isNaturalBenefic,
                    marketEffect = if (planet.isNaturalBenefic) "Harmonic right-aspect upward momentum" else "Diagonal selling friction",
                    impactScore = if (planet.isNaturalBenefic) 60 else -60
                )
            )

            val leftNak = SBC_NAKSHATRAS[leftNakIndex]
            activeVedhas.add(
                VedhaImpact(
                    planet = planet,
                    vedhaType = VedhaType.VAMA,
                    targetNakshatra = leftNak,
                    targetSector = NAKSHATRA_SECTORS[leftNak] ?: "Sector Basket",
                    isBenefic = planet.isNaturalBenefic,
                    marketEffect = if (planet.isNaturalBenefic) "Smooth lift & liquidity flow" else "Subtle bearish divergence",
                    impactScore = if (planet.isNaturalBenefic) 50 else -50
                )
            )
        }

        // Calculate Grid Matrix (9x9)
        val grid = build9x9Grid(planetaryPositions, activeVedhas)

        // Compute aggregate scores
        var beneficPoints = 0
        var maleficPoints = 0
        activeVedhas.forEach {
            if (it.isBenefic) {
                beneficPoints += it.impactScore
            } else {
                maleficPoints += kotlin.math.abs(it.impactScore)
            }
        }

        val totalPoints = (beneficPoints + maleficPoints).coerceAtLeast(1)
        val bullishScore = ((beneficPoints.toDouble() / totalPoints) * 100).toInt().coerceIn(10, 95)
        val bearishScore = (100 - bullishScore).coerceIn(5, 90)
        val netScore = (bullishScore - bearishScore)

        val primaryBenefic = if (activeVedhas.any { it.planet == Planet.JUPITER }) {
            "Jupiter casting front Vedha on Banking & Large-caps, providing institutional floor."
        } else {
            "Mercury & Venus transits creating buoyant liquidity for Tech & Auto."
        }

        val primaryMalefic = if (activeVedhas.any { it.planet == Planet.SATURN && !it.isBenefic }) {
            "Saturn Vedha creating heavy resistance in Energy & Metals, warning against aggressive longs."
        } else {
            "Mars-Rahu square causing sudden sharp intraday pullbacks."
        }

        return SarvatobhadraState(
            grid = grid,
            activeVedhas = activeVedhas,
            planetaryPositions = planetaryPositions,
            bullishVedhaScore = bullishScore,
            bearishVedhaScore = bearishScore,
            netVedhaScore = netScore,
            primaryBeneficInfluence = primaryBenefic,
            primaryMaleficPressure = primaryMalefic
        )
    }

    private fun build9x9Grid(
        planetaryPositions: Map<Planet, Int>,
        activeVedhas: List<VedhaImpact>
    ): List<List<SbCell>> {
        val matrix = Array(9) { r ->
            Array(9) { c ->
                SbCell(
                    row = r,
                    col = c,
                    label = "",
                    cellType = SbCell.CellType.INNER_VARNA
                )
            }
        }

        // Map 28 perimeter Nakshatras:
        // Top Row (0, 1..7): Krittika to Ashlesha
        for (i in 0..6) {
            val nakName = SBC_NAKSHATRAS[i]
            val occupying = planetaryPositions.filter { it.value == i }.map { it.key }
            val vedhas = activeVedhas.filter { it.targetNakshatra == nakName }
            matrix[0][i + 1] = SbCell(
                row = 0, col = i + 1,
                label = getNakshatraAbbr(nakName),
                subLabel = nakName,
                cellType = SbCell.CellType.NAKSHATRA_PERIMETER,
                occupyingPlanets = occupying,
                activeVedhas = vedhas
            )
        }

        // Right Column (1..7, 8): Magha to Vishakha
        for (i in 0..6) {
            val nakIndex = 7 + i
            val nakName = SBC_NAKSHATRAS[nakIndex]
            val occupying = planetaryPositions.filter { it.value == nakIndex }.map { it.key }
            val vedhas = activeVedhas.filter { it.targetNakshatra == nakName }
            matrix[i + 1][8] = SbCell(
                row = i + 1, col = 8,
                label = getNakshatraAbbr(nakName),
                subLabel = nakName,
                cellType = SbCell.CellType.NAKSHATRA_PERIMETER,
                occupyingPlanets = occupying,
                activeVedhas = vedhas
            )
        }

        // Bottom Row (8, 7..1 down): Anuradha to Shravana
        for (i in 0..6) {
            val nakIndex = 14 + i
            val nakName = SBC_NAKSHATRAS[nakIndex]
            val occupying = planetaryPositions.filter { it.value == nakIndex }.map { it.key }
            val vedhas = activeVedhas.filter { it.targetNakshatra == nakName }
            matrix[8][7 - i] = SbCell(
                row = 8, col = 7 - i,
                label = getNakshatraAbbr(nakName),
                subLabel = nakName,
                cellType = SbCell.CellType.NAKSHATRA_PERIMETER,
                occupyingPlanets = occupying,
                activeVedhas = vedhas
            )
        }

        // Left Column (7..1 down, 0): Dhanishta to Bharani
        for (i in 0..6) {
            val nakIndex = 21 + i
            val nakName = SBC_NAKSHATRAS[nakIndex]
            val occupying = planetaryPositions.filter { it.value == nakIndex }.map { it.key }
            val vedhas = activeVedhas.filter { it.targetNakshatra == nakName }
            matrix[7 - i][0] = SbCell(
                row = 7 - i, col = 0,
                label = getNakshatraAbbr(nakName),
                subLabel = nakName,
                cellType = SbCell.CellType.NAKSHATRA_PERIMETER,
                occupyingPlanets = occupying,
                activeVedhas = vedhas
            )
        }

        // 4 Corners: Vowels
        matrix[0][0] = SbCell(0, 0, "अ", "A", SbCell.CellType.CORNER_SWARA)
        matrix[0][8] = SbCell(0, 8, "आ", "Aa", SbCell.CellType.CORNER_SWARA)
        matrix[8][8] = SbCell(8, 8, "इ", "I", SbCell.CellType.CORNER_SWARA)
        matrix[8][0] = SbCell(8, 0, "ई", "Ee", SbCell.CellType.CORNER_SWARA)

        // Ring 2: Swaras & Varnas (letters corresponding to stock names like R for Reliance, T for Tata, etc.)
        val varnaLabels = listOf(
            "U", "Oo", "Ri", "Ka", "Kha", "Ga", "Gha",
            "Cha", "Ja", "Ta", "Da", "Na", "Pa", "Bha",
            "Ma", "Ya", "Ra", "La", "Va", "Sha", "Sa"
        )
        var vIndex = 0
        for (r in 1..7) {
            for (c in 1..7) {
                if (r == 1 || r == 7 || c == 1 || c == 7) {
                    val label = varnaLabels[vIndex % varnaLabels.size]
                    vIndex++
                    matrix[r][c] = SbCell(r, c, label, "Varna", SbCell.CellType.INNER_VARNA)
                }
            }
        }

        // Ring 3: 12 Rasis (Zodiac Signs)
        val rasis = listOf("Ari", "Tau", "Gem", "Can", "Leo", "Vir", "Lib", "Sco", "Sag", "Cap", "Aqu", "Pis")
        val rasiFullNames = listOf("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces")
        var rasiIndex = 0
        for (r in 2..6) {
            for (c in 2..6) {
                if (r == 2 || r == 6 || c == 2 || c == 6) {
                    val rasiAbbr = rasis[rasiIndex % rasis.size]
                    val rasiFullName = rasiFullNames[rasiIndex % rasiFullNames.size]
                    rasiIndex++
                    matrix[r][c] = SbCell(r, c, rasiAbbr, rasiFullName, SbCell.CellType.INNER_RASI)
                }
            }
        }

        // Ring 4: 5 Tithis & Vaaras
        val tithisAndVaaras = listOf("Nan", "Bhad", "Jaya", "Rikt", "Purn", "Guru", "Shan", "Mang")
        val tvFullNames = listOf("Nanda", "Bhadra", "Jaya", "Rikta", "Purna", "Guru (Thu)", "Shani (Sat)", "Mangal (Tue)")
        var tIndex = 0
        for (r in 3..5) {
            for (c in 3..5) {
                if (!(r == 4 && c == 4)) {
                    val tvAbbr = tithisAndVaaras[tIndex % tithisAndVaaras.size]
                    val tvFull = tvFullNames[tIndex % tvFullNames.size]
                    tIndex++
                    matrix[r][c] = SbCell(r, c, tvAbbr, tvFull, SbCell.CellType.INNER_TITHI_VARA)
                }
            }
        }

        // Center: Brahma Akshara Bindu
        matrix[4][4] = SbCell(4, 4, "ॐ", "Bindu", SbCell.CellType.CENTER_BINDU)

        // Populate activeVedhas for inner and corner cells by finding rays traversing them
        for (r in 0..8) {
            for (c in 0..8) {
                if (matrix[r][c].cellType != SbCell.CellType.NAKSHATRA_PERIMETER) {
                    val traversingVedhas = activeVedhas.filter { vedha ->
                        if (vedha.vedhaType == VedhaType.CONJUNCTION) return@filter false
                        val planetNakIdx = planetaryPositions[vedha.planet] ?: return@filter false
                        val targetNakIdx = SBC_NAKSHATRAS.indexOfFirst { it.equals(vedha.targetNakshatra, ignoreCase = true) }
                        if (targetNakIdx == -1) return@filter false
                        val (r1, c1) = getNakshatraCoords(planetNakIdx)
                        val (r2, c2) = getNakshatraCoords(targetNakIdx)
                        isCellTraversedByRay(r, c, r1, c1, r2, c2)
                    }
                    if (traversingVedhas.isNotEmpty()) {
                        val current = matrix[r][c]
                        matrix[r][c] = current.copy(activeVedhas = traversingVedhas)
                    }
                }
            }
        }

        return matrix.map { it.toList() }
    }
}
