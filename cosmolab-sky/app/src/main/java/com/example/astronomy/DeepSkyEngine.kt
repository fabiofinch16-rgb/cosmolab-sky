package com.example.astronomy

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10
import kotlin.math.max

/**
 * DeepSkyEngine & Target Recommendation System.
 * Evaluates observation targets (Planets & Deep Sky Objects) using AstronomyEngine and OpticsEngine.
 */
object DeepSkyEngine {

    /**
     * Evaluate a single DeepSkyObject for a given observation context.
     */
    fun evaluateDso(
        dso: DeepSkyObject,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        equipment: TelescopeEquipment = TelescopeEquipment(),
        bortle: BortleScale = BortleScale.BORTLE_4,
        mode: ObservationMode = ObservationMode.TELESCOPE,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0
    ): TelescopeTargetEvaluation {
        val astronomyEngine = AstronomyEngine
        val pos = astronomyEngine.calculateEquatorialPosition(
            raDeg = dso.raDeg,
            decDeg = dso.declinationDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude
        )

        val lunarInfo = evaluateLunarInterference(dso, calendar, latitude, longitude)
        val solarProximity = astronomyEngine.calculateSolarProximity(
            targetRaDeg = dso.raDeg,
            targetDecDeg = dso.declinationDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            targetName = dso.commonName.ifEmpty { dso.messierNgc }
        )

        return ObservationAnalysisEngine.analyzeDsoObservation(
            dso = dso,
            altitudeDeg = pos.altitudeDeg,
            sunAltitudeDeg = pos.sunAltitudeDeg,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle,
            lunarInfo = lunarInfo,
            solarProximityInfo = solarProximity
        )
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )

    private fun calculateBortleScore(
        type: DeepSkyType,
        mag: Double,
        surfBright: Double?,
        bortle: BortleScale
    ): Double {
        val level = bortle.level
        return when (type) {
            DeepSkyType.DOUBLE_STAR -> 25.0 - (level - 1) * 0.5 // Virtually unaffected
            DeepSkyType.OPEN_CLUSTER -> 25.0 - (level - 1) * 1.5 // Lightly affected
            DeepSkyType.GLOBULAR_CLUSTER -> 25.0 - (level - 1) * 2.0 // Moderately affected
            DeepSkyType.PLANETARY_NEBULA -> {
                // High surface brightness helps in polluted skies
                val brightPenalty = if ((surfBright ?: 12.0) < 11.0) 1.5 else 2.5
                max(0.0, 25.0 - (level - 1) * brightPenalty)
            }
            DeepSkyType.NEBULA -> max(0.0, 25.0 - (level - 1) * 3.2) // Strongly affected
            DeepSkyType.GALAXY -> max(0.0, 25.0 - (level - 1) * 3.5) // Highly affected by light pollution
        }
    }

    private fun calculateEquipmentScore(
        type: DeepSkyType,
        exitPupilMm: Double,
        mag: Double,
        limitingMag: Double
    ): Double {
        var score = 15.0

        // Brightness margin
        val margin = limitingMag - mag
        if (margin > 3.0) score += 5.0
        else if (margin > 1.0) score += 3.0
        else if (margin < 0.0) score -= 5.0

        // Exit pupil optimization per object type
        when (type) {
            DeepSkyType.NEBULA, DeepSkyType.GALAXY, DeepSkyType.OPEN_CLUSTER -> {
                // Wide exit pupil (2.5mm - 6mm) preferred
                if (exitPupilMm >= 2.5 && exitPupilMm <= 6.0) score += 5.0
                else if (exitPupilMm < 1.5) score -= 3.0
            }
            DeepSkyType.GLOBULAR_CLUSTER, DeepSkyType.PLANETARY_NEBULA -> {
                // Medium exit pupil (1.0mm - 3.0mm) preferred
                if (exitPupilMm >= 1.0 && exitPupilMm <= 3.0) score += 5.0
            }
            DeepSkyType.DOUBLE_STAR -> {
                // High magnification / smaller exit pupil (0.8mm - 2.0mm) preferred
                if (exitPupilMm >= 0.8 && exitPupilMm <= 2.0) score += 5.0
            }
        }

        return score.coerceIn(0.0, 20.0)
    }

    private fun generateQualityMessage(
        dso: DeepSkyObject,
        quality: ObservationQuality,
        altDeg: Double,
        bortle: BortleScale,
        exitPupilMm: Double
    ): String {
        if (altDeg <= 0.0) return "${dso.commonName.ifEmpty { dso.messierNgc }} está atualmente abaixo do horizonte."
        if (altDeg < 15.0) return "Posição baixa no horizonte. Interferência da turbulência atmosférica."

        return when (quality) {
            ObservationQuality.IDEAL -> "Condições ideais! Objeto em excelente altura com céu propício."
            ObservationQuality.EXCELLENT -> "Excelente oportunidade de observação com seu equipamento atual."
            ObservationQuality.GOOD -> "Boas condições para localizar e contemplar ${dso.commonName.ifEmpty { dso.id }}."
            ObservationQuality.DIFFICULT -> if (bortle.level >= 7) "A poluição luminosa (Bortle ${bortle.level}) reduz significativamente o contraste." else "Visibilidade limitada pelo crepúsculo ou altitude."
            ObservationQuality.UNAVAILABLE -> "Condições inadequadas para observação neste momento."
        }
    }

    private fun generateDetectionCapability(
        dso: DeepSkyObject,
        equipment: TelescopeEquipment,
        bortle: BortleScale,
        opt: OpticsResult
    ): String {
        val ap = equipment.apertureMm
        return when (dso.type) {
            DeepSkyType.NEBULA -> {
                if (bortle.level <= 4) "Potencialmente favorável para perceber nebulosidade difusa e filamentos com filtro ${dso.bestFilter ?: "UHC"}."
                else "Adequado para reconhecer a região central da nebulosa."
            }
            DeepSkyType.PLANETARY_NEBULA -> "Adequado para observar o disco planetário/anel luminescente com $opt"
            DeepSkyType.OPEN_CLUSTER -> "Excelente definição estelar. Decenas de estrelas individuais resolvidas em campo amplo."
            DeepSkyType.GLOBULAR_CLUSTER -> {
                if (ap >= 114.0) "Potencial para resolver a periferia do aglomerado em estrelas pontuais."
                else "Aparece como um núcleo denso e brilhante rodeado por halo suave."
            }
            DeepSkyType.GALAXY -> {
                if (bortle.level <= 4) "Permite identificar o núcleo galáctico e halo difuso alongado."
                else "Núcleo galáctico detectável como uma mancha difusa compacta."
            }
            DeepSkyType.DOUBLE_STAR -> "Separação de ${dso.separationArcsec ?: 2.0}\" resolvida com clareza. Contraste cromático visível."
        }
    }

    private fun getBortleImpactText(type: DeepSkyType, bortle: BortleScale): String {
        return when (type) {
            DeepSkyType.DOUBLE_STAR -> "Impacto mínimo da poluição luminosa."
            DeepSkyType.OPEN_CLUSTER -> "Aglomerado visível mesmo em céus moderadamente urbanos."
            DeepSkyType.GLOBULAR_CLUSTER -> "Núcleo resistente à poluição; detalhes periféricos favorecidos em céus escuros."
            DeepSkyType.PLANETARY_NEBULA -> "Brilho superficial concentrado minimiza a interferência da poluição."
            DeepSkyType.NEBULA -> if (bortle.level <= 4) "Excelente contraste do gás contra o fundo escuro." else "Requer filtro nebuloso (UHC/OIII) devido à poluição."
            DeepSkyType.GALAXY -> if (bortle.level <= 4) "Visibilidade do halo e contraste muito favorecidos." else "Poluição reduz o contraste dos braços e do halo difuso."
        }
    }

    fun evaluateLunarInterference(
        dso: DeepSkyObject,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): LunarInterferenceInfo {
        val moonPos = AstronomyEngine.calculateMoonPosition(calendar, latitude, longitude)
        val moonDirLabel = AstronomyEngine.convertAzimuthToDirection(moonPos.azimuthDeg)

        val angularSepDeg = AstronomyEngine.calculateAngularSeparationDeg(
            ra1Deg = moonPos.rightAscensionDeg,
            dec1Deg = moonPos.declinationDeg,
            ra2Deg = dso.raDeg,
            dec2Deg = dso.declinationDeg
        )

        val isMoonAboveHorizon = moonPos.altitudeDeg > 0.0

        if (!isMoonAboveHorizon) {
            return LunarInterferenceInfo(
                moonAltitudeDeg = moonPos.altitudeDeg,
                moonAzimuthDeg = moonPos.azimuthDeg,
                moonDirectionLabel = moonDirLabel,
                moonIlluminationPercent = moonPos.illuminationPercent,
                moonPhaseName = moonPos.phaseName,
                isMoonAboveHorizon = false,
                angularSeparationDeg = angularSepDeg,
                interferenceLevelLabel = "Nenhuma",
                interferenceQualityLabel = "🟢 Excelente (Lua abaixo do horizonte)",
                penaltyScore = 0.0
            )
        }

        val penalty = calculateLunarPenalty(
            dso = dso,
            moonAltitudeDeg = moonPos.altitudeDeg,
            moonIlluminationPercent = moonPos.illuminationPercent,
            angularSeparationDeg = angularSepDeg
        )

        val (levelLabel, qualityLabel) = when {
            penalty < 2.0 -> Pair("Nenhuma", "🟢 Excelente (Sem influência relevante)")
            penalty < 6.0 -> Pair("Baixa", "🟢 Muito bom (Influência baixa)")
            penalty < 11.0 -> Pair("Moderada", "🔵 Bom (Influência perceptível)")
            penalty < 16.0 -> Pair("Aceitável", "🟡 Aceitável (Interferência moderada)")
            penalty < 21.0 -> Pair("Forte", "🟠 Difícil (Forte interferência)")
            else -> Pair("Muito Forte", "🔴 Ruim (Lua prejudica gravemente o alvo)")
        }

        return LunarInterferenceInfo(
            moonAltitudeDeg = moonPos.altitudeDeg,
            moonAzimuthDeg = moonPos.azimuthDeg,
            moonDirectionLabel = moonDirLabel,
            moonIlluminationPercent = moonPos.illuminationPercent,
            moonPhaseName = moonPos.phaseName,
            isMoonAboveHorizon = true,
            angularSeparationDeg = angularSepDeg,
            interferenceLevelLabel = levelLabel,
            interferenceQualityLabel = qualityLabel,
            penaltyScore = penalty
        )
    }

    fun calculateLunarPenalty(
        dso: DeepSkyObject,
        moonAltitudeDeg: Double,
        moonIlluminationPercent: Int,
        angularSeparationDeg: Double
    ): Double {
        if (moonAltitudeDeg <= 0.0) return 0.0 // Moon below horizon

        val illumFactor = moonIlluminationPercent / 100.0
        val altFactor = (moonAltitudeDeg / 90.0).coerceIn(0.0, 1.0)

        val sepFactor = when {
            angularSeparationDeg < 15.0 -> 1.0
            angularSeparationDeg < 30.0 -> 0.8
            angularSeparationDeg < 60.0 -> 0.5
            angularSeparationDeg < 90.0 -> 0.25
            else -> 0.12
        }

        val sensitivity = when (dso.type) {
            DeepSkyType.GALAXY, DeepSkyType.NEBULA -> 1.4
            DeepSkyType.PLANETARY_NEBULA -> 0.95
            DeepSkyType.GLOBULAR_CLUSTER -> 0.75
            DeepSkyType.OPEN_CLUSTER -> 0.45
            DeepSkyType.DOUBLE_STAR -> 0.15
        }

        val sbFactor = dso.surfaceBrightness?.let { sb ->
            when {
                sb >= 14.0 -> 1.2
                sb >= 13.0 -> 1.1
                sb <= 11.5 -> 0.85
                else -> 1.0
            }
        } ?: 1.0

        return (25.0 * illumFactor * altFactor * sepFactor * sensitivity * sbFactor).coerceIn(0.0, 25.0)
    }

    /**
     * Search for the best observation time window for a DeepSkyObject in a 24h scan around night.
     */
    private fun findDsoBestWindow(
        dso: DeepSkyObject,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        equipment: TelescopeEquipment? = null,
        bortle: BortleScale? = null
    ): Pair<String, Double> {
        val scanCal = calendar.clone() as Calendar
        scanCal.set(Calendar.HOUR_OF_DAY, 12)
        scanCal.set(Calendar.MINUTE, 0)
        scanCal.set(Calendar.SECOND, 0)

        val astronomy = AstronomyEngine
        val defaultEquip = equipment ?: TelescopeEquipment(114.0, 900.0, 25.0, 52.0)
        val defaultBortle = bortle ?: BortleScale.BORTLE_4

        val validPoints = mutableListOf<Calendar>()
        var maxAlt = -90.0

        for (m in 0 until 96) {
            val pointCal = scanCal.clone() as Calendar
            pointCal.add(Calendar.MINUTE, m * 15)

            val pos = astronomy.calculateEquatorialPosition(
                raDeg = dso.raDeg,
                decDeg = dso.declinationDeg,
                calendar = pointCal,
                latitude = latitude,
                longitude = longitude
            )

            if (pos.altitudeDeg > maxAlt && pos.sunAltitudeDeg <= 0.0) {
                maxAlt = pos.altitudeDeg
            }

            if (pos.sunAltitudeDeg <= -6.0 && pos.altitudeDeg >= 15.0) {
                val moonPos = astronomy.calculateMoonPosition(pointCal, latitude, longitude)
                val sep = astronomy.calculateAngularSeparationDeg(
                    moonPos.rightAscensionDeg, moonPos.declinationDeg,
                    dso.raDeg, dso.declinationDeg
                )
                val penalty = calculateLunarPenalty(dso, moonPos.altitudeDeg, moonPos.illuminationPercent, sep)

                val altScore = when {
                    pos.altitudeDeg < 15.0 -> (pos.altitudeDeg / 15.0) * 10.0
                    pos.altitudeDeg < 45.0 -> 10.0 + ((pos.altitudeDeg - 15.0) / 30.0) * 12.0
                    else -> 22.0 + ((pos.altitudeDeg - 45.0) / 45.0) * 8.0
                }
                val darkScore = when {
                    pos.sunAltitudeDeg <= -18.0 -> 25.0
                    pos.sunAltitudeDeg <= -12.0 -> 20.0
                    else -> 10.0
                }
                val bortleScore = calculateBortleScore(dso.type, dso.apparentMagnitude, dso.surfaceBrightness, defaultBortle)
                val score = (altScore + darkScore + bortleScore + 15.0 - penalty).coerceIn(0.0, 100.0)

                if (score >= 40.0) {
                    validPoints.add(pointCal)
                }
            }
        }

        if (validPoints.isNotEmpty()) {
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                timeZone = calendar.timeZone
            }
            val firstTime = fmt.format(validPoints.first().time)
            val lastTime = fmt.format(validPoints.last().time)
            val windowStr = if (validPoints.size == 1) "$firstTime" else "$firstTime – $lastTime"
            return Pair(windowStr, if (maxAlt > -90.0) maxAlt else 15.0)
        }

        return Pair("Pouco favorável hoje", if (maxAlt > -90.0) maxAlt else 0.0)
    }

    /**
     * Top 20 Telescope Recommendations Engine.
     * Evaluates all planets AND deep sky catalog objects, returning the top 20 targets sorted by quality and astronomical suitability.
     */
    fun getTop20TelescopeRecommendations(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        equipment: TelescopeEquipment,
        bortle: BortleScale
    ): List<TelescopeTargetEvaluation> {
        val allEvaluations = mutableListOf<TelescopeTargetEvaluation>()

        // 1. Evaluate Telescope Planets
        Planet.telescopicPlanets.forEach { planet ->
            val pos = AstronomyEngine.calculatePosition(planet, calendar, latitude, longitude)
            val planetEval = OpticsEngine.evaluateTelescopicObservation(
                planet = planet,
                altitudeDeg = pos.altitudeDeg,
                sunAltitudeDeg = pos.sunAltitudeDeg,
                magnitude = pos.magnitude,
                distanceAU = pos.distanceAU,
                equipment = equipment
            )

            val opt = OpticsEngine.evaluateOptics(
                apertureMm = equipment.apertureMm,
                focalLengthMm = equipment.focalLengthMm,
                eyepieceFocalLengthMm = equipment.eyepieceFocalLengthMm,
                eyepieceApparentFovDeg = equipment.eyepieceApparentFovDeg ?: 52.0,
                objectAngularDiameterArcsec = optDiameterForPlanet(planet, pos.distanceAU)
            )

            val windows = AstronomyEngine.findBestObservationWindows(planet, calendar, latitude, longitude, "Baixo", true)
            val windowStr = if (windows.isNotEmpty()) "${windows.first().startTimeStr} – ${windows.first().endTimeStr}" else "Durante a noite"
            val maxWindowAlt = windows.maxOfOrNull { it.maxAltitudeDeg } ?: pos.altitudeDeg

            val solarProximity = AstronomyEngine.calculateSolarProximity(
                targetRaDeg = pos.rightAscensionDeg,
                targetDecDeg = pos.declinationDeg,
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                targetName = planet.portugueseName
            )

            allEvaluations.add(
                TelescopeTargetEvaluation(
                    target = CelestialTarget.PlanetTarget(planet),
                    altitudeDeg = pos.altitudeDeg,
                    azimuthDeg = pos.azimuthDeg,
                    directionLabel = AstronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
                    heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
                    isAboveHorizon = pos.altitudeDeg > 0.0,
                    sunAltitudeDeg = pos.sunAltitudeDeg,
                    quality = planetEval.quality,
                    qualityMessage = planetEval.qualityMessage,
                    score = planetEval.score,
                    opticalSummary = "${String.format("%.0f", opt.magnification)}× • Pupila ${String.format("%.1f", opt.exitPupilMm)} mm • Campo ${String.format("%.2f", opt.realFovDeg)}°",
                    detectionCapability = planetEval.detectionCapability,
                    bestWindowStr = windowStr,
                    maxAltitudeWindowDeg = maxWindowAlt,
                    apertureSuitability = "Abertura ${equipment.apertureMm.toInt()} mm",
                    bortleImpactMessage = "Planetas são brilhantes e não sofrem com poluição luminosa.",
                    solarProximityInfo = solarProximity
                )
            )
        }

        // 2. Evaluate All Deep Sky Catalog Objects
        DeepSkyCatalog.objects.forEach { dso ->
            val dsoEval = evaluateDso(dso, calendar, latitude, longitude, equipment, bortle)
            allEvaluations.add(dsoEval)
        }

        // 3. Evaluate Moon Target (Daytime or Nighttime)
        val moonEval = evaluateMoonTarget(
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            mode = ObservationMode.TELESCOPE,
            equipment = equipment,
            bortle = bortle
        )
        if (moonEval != null) {
            allEvaluations.add(moonEval)
        }

        // 4. Evaluate Active Meteor Showers
        MeteorShowerCatalog.getActiveShowers(calendar).forEach { shower ->
            val showerEval = MeteorShowerEngine.evaluateShower(shower, calendar, latitude, longitude, bortle)
            if (showerEval != null && showerEval.isCurrentlyActive && showerEval.maxRadiantAltitudeDeg > 0.0) {
                allEvaluations.add(showerEval.toTelescopeTargetEvaluation())
            }
        }

        // 3. Filter candidates above horizon (or close) and sort by score (Ensure Sun is NEVER recommended)
        val candidateList = allEvaluations
            .filter { (it.isAboveHorizon || it.altitudeDeg >= -5.0) && it.target.id != "sun" && !it.target.title.equals("Sol", ignoreCase = true) }
            .sortedByDescending { it.score }

        // 4. Apply smart category diversity balancing (ensure no single category dominates all 20 spots if other high-quality types exist)
        val selectedTop20 = mutableListOf<TelescopeTargetEvaluation>()
        val categoryCount = mutableMapOf<String, Int>()

        for (eval in candidateList) {
            val catKey = eval.target.typeLabel
            val currentCount = categoryCount.getOrDefault(catKey, 0)

            // Allow up to 6 objects per category in Top 20 unless running out of candidates
            if (currentCount < 6 || candidateList.size - selectedTop20.size <= 20 - selectedTop20.size) {
                selectedTop20.add(eval)
                categoryCount[catKey] = currentCount + 1
            }

            if (selectedTop20.size >= 20) break
        }

        // If less than 20 due to category capping, fill remaining best candidates
        if (selectedTop20.size < 20) {
            for (eval in candidateList) {
                if (!selectedTop20.contains(eval)) {
                    selectedTop20.add(eval)
                    if (selectedTop20.size >= 20) break
                }
            }
        }

        return selectedTop20.take(20)
    }

    /**
     * Unified recommendation engine for Naked Eye, Binocular, and Telescope modes.
     */
    fun getTop20RecommendationsForMode(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        mode: ObservationMode,
        telescopeEquipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale
    ): List<TelescopeTargetEvaluation> {
        return when (mode) {
            ObservationMode.TELESCOPE -> getTop20TelescopeRecommendations(
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                equipment = telescopeEquipment,
                bortle = bortle
            )
            ObservationMode.NAKED_EYE -> getNakedEyeRecommendations(
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                bortle = bortle
            )
            ObservationMode.BINOCULAR -> getBinocularRecommendations(
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                apertureMm = binocularApertureMm,
                magnification = binocularMagnification,
                bortle = bortle
            )
        }
    }

    private fun getNakedEyeRecommendations(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        bortle: BortleScale
    ): List<TelescopeTargetEvaluation> {
        val evaluations = mutableListOf<TelescopeTargetEvaluation>()

        // 1. Naked eye planets
        Planet.nakedEyePlanets.forEach { planet ->
            val pos = AstronomyEngine.calculatePosition(planet, calendar, latitude, longitude)
            val isAbove = pos.altitudeDeg > 0.0
            val isVisible = isAbove && pos.altitudeDeg >= 3.0 && pos.sunAltitudeDeg <= -2.0

            val score = if (isVisible) {
                (pos.altitudeDeg * 0.8 + (if (pos.sunAltitudeDeg <= -12.0) 30.0 else 15.0)).coerceIn(20.0, 100.0)
            } else if (isAbove) {
                (pos.altitudeDeg * 0.3).coerceIn(0.0, 15.0)
            } else 0.0

            val quality = when {
                !isAbove -> ObservationQuality.UNAVAILABLE
                score >= 70.0 -> ObservationQuality.IDEAL
                score >= 50.0 -> ObservationQuality.EXCELLENT
                score >= 30.0 -> ObservationQuality.GOOD
                else -> ObservationQuality.DIFFICULT
            }

            val windows = AstronomyEngine.findBestObservationWindows(planet, calendar, latitude, longitude, "Baixo", false)
            val windowStr = if (windows.isNotEmpty()) "${windows.first().startTimeStr} – ${windows.first().endTimeStr}" else "Durante a noite"
            val maxAlt = windows.maxOfOrNull { it.maxAltitudeDeg } ?: pos.altitudeDeg

            val solarProximity = AstronomyEngine.calculateSolarProximity(
                targetRaDeg = pos.rightAscensionDeg,
                targetDecDeg = pos.declinationDeg,
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                targetName = planet.portugueseName
            )

            evaluations.add(
                TelescopeTargetEvaluation(
                    target = CelestialTarget.PlanetTarget(planet),
                    altitudeDeg = pos.altitudeDeg,
                    azimuthDeg = pos.azimuthDeg,
                    directionLabel = AstronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
                    heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
                    isAboveHorizon = isAbove,
                    sunAltitudeDeg = pos.sunAltitudeDeg,
                    quality = quality,
                    qualityMessage = if (isVisible) "Brilhante e facilmente observável a olho nu no céu." else "Visibilidade limitada pela altura ou luminosidade do céu.",
                    score = score,
                    opticalSummary = "Visão direta (a olho nu)",
                    detectionCapability = "Ponto muito brilhante destacado no céu noturno.",
                    bestWindowStr = windowStr,
                    maxAltitudeWindowDeg = maxAlt,
                    apertureSuitability = "Visão direta (a olho nu)",
                    bortleImpactMessage = "Planetas são muito brilhantes e podem ser vistos a olho nu mesmo em áreas urbanas.",
                    solarProximityInfo = solarProximity
                )
            )
        }

        // 2. Naked eye Deep Sky Objects
        val limitingMag = when (bortle.level) {
            1, 2, 3 -> 6.2
            4, 5 -> 5.5
            6, 7 -> 4.5
            else -> 3.8
        }

        DeepSkyCatalog.objects.forEach { dso ->
            val pos = AstronomyEngine.calculateEquatorialPosition(dso.raDeg, dso.declinationDeg, calendar, latitude, longitude)
            val isAbove = pos.altitudeDeg > 0.0
            val isNakedEyeCandidate = isAbove && pos.altitudeDeg >= 5.0 && pos.sunAltitudeDeg <= -6.0 && dso.apparentMagnitude <= limitingMag

            if (isNakedEyeCandidate) {
                val altScore = (pos.altitudeDeg / 90.0) * 40.0
                val magBonus = (limitingMag - dso.apparentMagnitude) * 10.0
                val totalScore = (altScore + magBonus + 30.0).coerceIn(10.0, 95.0)

                val quality = when {
                    totalScore >= 75.0 -> ObservationQuality.IDEAL
                    totalScore >= 55.0 -> ObservationQuality.EXCELLENT
                    totalScore >= 35.0 -> ObservationQuality.GOOD
                    else -> ObservationQuality.DIFFICULT
                }

                val bestWindow = findDsoBestWindow(dso, calendar, latitude, longitude, null, bortle)

                val solarProximity = AstronomyEngine.calculateSolarProximity(
                    targetRaDeg = dso.raDeg,
                    targetDecDeg = dso.declinationDeg,
                    calendar = calendar,
                    latitude = latitude,
                    longitude = longitude,
                    targetName = dso.commonName.ifEmpty { dso.messierNgc }
                )

                evaluations.add(
                    TelescopeTargetEvaluation(
                        target = CelestialTarget.DeepSkyTarget(dso),
                        altitudeDeg = pos.altitudeDeg,
                        azimuthDeg = pos.azimuthDeg,
                        directionLabel = AstronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
                        heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
                        isAboveHorizon = isAbove,
                        sunAltitudeDeg = pos.sunAltitudeDeg,
                        quality = quality,
                        qualityMessage = "Objeto brilhante identificável a olho nu sob céu de classe ${bortle.displayLabel}.",
                        score = totalScore,
                        opticalSummary = "Visão direta (a olho nu)",
                        detectionCapability = "Aparece como uma mancha difusa ou aglomerado estelar visível sem instrumentos.",
                        bestWindowStr = bestWindow.first,
                        maxAltitudeWindowDeg = bestWindow.second,
                        apertureSuitability = "Visão direta (a olho nu)",
                        bortleImpactMessage = "Céu ${bortle.shortName}: objetos a olho nu exigem boa transparência e céu escuro.",
                        solarProximityInfo = solarProximity
                    )
                )
            }
        }

        // Evaluate Moon for Naked Eye
        val moonEval = evaluateMoonTarget(
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            mode = ObservationMode.NAKED_EYE,
            equipment = TelescopeEquipment(),
            bortle = bortle
        )
        if (moonEval != null) {
            evaluations.add(moonEval)
        }

        // Evaluate Active Meteor Showers for Naked Eye
        MeteorShowerCatalog.getActiveShowers(calendar).forEach { shower ->
            val showerEval = MeteorShowerEngine.evaluateShower(shower, calendar, latitude, longitude, bortle)
            if (showerEval != null && showerEval.isCurrentlyActive && showerEval.maxRadiantAltitudeDeg > 0.0) {
                evaluations.add(showerEval.toTelescopeTargetEvaluation())
            }
        }

        return evaluations.filter { it.target.id != "sun" && !it.target.title.equals("Sol", ignoreCase = true) }.sortedByDescending { it.score }
    }

    private fun getBinocularRecommendations(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        apertureMm: Double,
        magnification: Double,
        bortle: BortleScale
    ): List<TelescopeTargetEvaluation> {
        val evaluations = mutableListOf<TelescopeTargetEvaluation>()

        // 1. Planets for Binoculars
        Planet.entries.forEach { planet ->
            val pos = AstronomyEngine.calculatePosition(planet, calendar, latitude, longitude)
            val isAbove = pos.altitudeDeg > 0.0
            val isVisible = isAbove && pos.altitudeDeg >= 3.0 && pos.sunAltitudeDeg <= -2.0

            val score = if (isVisible) {
                (pos.altitudeDeg * 0.7 + (if (pos.sunAltitudeDeg <= -12.0) 35.0 else 15.0)).coerceIn(20.0, 100.0)
            } else if (isAbove) {
                (pos.altitudeDeg * 0.3).coerceIn(0.0, 15.0)
            } else 0.0

            val quality = when {
                !isAbove -> ObservationQuality.UNAVAILABLE
                score >= 75.0 -> ObservationQuality.IDEAL
                score >= 55.0 -> ObservationQuality.EXCELLENT
                score >= 35.0 -> ObservationQuality.GOOD
                else -> ObservationQuality.DIFFICULT
            }

            val windows = AstronomyEngine.findBestObservationWindows(planet, calendar, latitude, longitude, "Baixo", false)
            val windowStr = if (windows.isNotEmpty()) "${windows.first().startTimeStr} – ${windows.first().endTimeStr}" else "Durante a noite"
            val maxAlt = windows.maxOfOrNull { it.maxAltitudeDeg } ?: pos.altitudeDeg

            val binocularDesc = when (planet) {
                Planet.JUPITER -> "Disco planetário e as 4 principais luas galileanas visíveis como pontos alinhados."
                Planet.SATURN -> "Objeto ovalado nítido em binóculos de ${magnification.toInt()}x."
                Planet.VENUS -> "Fase do planeta (crescente/cheia) perceptível com binóculo estabilizado."
                Planet.MARS -> "Ponto avermelhado brilhante e muito nítido em campo amplo."
                else -> "Visível como ponto estelar no binóculo."
            }

            val solarProximity = AstronomyEngine.calculateSolarProximity(
                targetRaDeg = pos.rightAscensionDeg,
                targetDecDeg = pos.declinationDeg,
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                targetName = planet.portugueseName
            )

            evaluations.add(
                TelescopeTargetEvaluation(
                    target = CelestialTarget.PlanetTarget(planet),
                    altitudeDeg = pos.altitudeDeg,
                    azimuthDeg = pos.azimuthDeg,
                    directionLabel = AstronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
                    heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
                    isAboveHorizon = isAbove,
                    sunAltitudeDeg = pos.sunAltitudeDeg,
                    quality = quality,
                    qualityMessage = "Excelente para localização em campo amplo com binóculo de ${apertureMm.toInt()} mm.",
                    score = score,
                    opticalSummary = "${magnification.toInt()}× • Objetiva ${apertureMm.toInt()} mm • Campo amplo",
                    detectionCapability = binocularDesc,
                    bestWindowStr = windowStr,
                    maxAltitudeWindowDeg = maxAlt,
                    apertureSuitability = "Binóculo ${apertureMm.toInt()} mm / ${magnification.toInt()}x",
                    bortleImpactMessage = "Planetas são ideais para binóculos mesmo sob poluição luminosa.",
                    solarProximityInfo = solarProximity
                )
            )
        }

        // 2. Binocular DSOs
        val limitingMag = when (bortle.level) {
            1, 2, 3 -> 9.5
            4, 5 -> 8.8
            6, 7 -> 8.0
            else -> 7.2
        }

        DeepSkyCatalog.objects.forEach { dso ->
            val pos = AstronomyEngine.calculateEquatorialPosition(dso.raDeg, dso.declinationDeg, calendar, latitude, longitude)
            val isAbove = pos.altitudeDeg > 0.0
            val isBinocularCandidate = isAbove && pos.altitudeDeg >= 4.0 && pos.sunAltitudeDeg <= -6.0 && dso.apparentMagnitude <= limitingMag

            if (isBinocularCandidate) {
                val altScore = (pos.altitudeDeg / 90.0) * 35.0
                val magBonus = (limitingMag - dso.apparentMagnitude) * 8.0
                val totalScore = (altScore + magBonus + 25.0).coerceIn(10.0, 98.0)

                val quality = when {
                    totalScore >= 75.0 -> ObservationQuality.IDEAL
                    totalScore >= 55.0 -> ObservationQuality.EXCELLENT
                    totalScore >= 35.0 -> ObservationQuality.GOOD
                    else -> ObservationQuality.DIFFICULT
                }

                val bestWindow = findDsoBestWindow(dso, calendar, latitude, longitude, null, bortle)

                val binocularDsoDesc = when (dso.type) {
                    DeepSkyType.OPEN_CLUSTER -> "Espetacular visão em campo amplo. Dezenas de estrelas reluzentes agrupadas."
                    DeepSkyType.NEBULA -> "Nebulosidade suave bem definida contra o fundo estelar no campo do binóculo."
                    DeepSkyType.GALAXY -> "Mancha ovalada difusa luminescente destacada no campo visual amplo."
                    DeepSkyType.GLOBULAR_CLUSTER -> "Aparece como uma estrela desfocada ou bolinha difusa brilhante."
                    DeepSkyType.PLANETARY_NEBULA -> "Ponto ou pequeno disco compacto distinguível com binóculo."
                    DeepSkyType.DOUBLE_STAR -> "Estrela dupla bem separada com belo contraste visual no binóculo."
                }

                val solarProximity = AstronomyEngine.calculateSolarProximity(
                    targetRaDeg = dso.raDeg,
                    targetDecDeg = dso.declinationDeg,
                    calendar = calendar,
                    latitude = latitude,
                    longitude = longitude,
                    targetName = dso.commonName.ifEmpty { dso.messierNgc }
                )

                evaluations.add(
                    TelescopeTargetEvaluation(
                        target = CelestialTarget.DeepSkyTarget(dso),
                        altitudeDeg = pos.altitudeDeg,
                        azimuthDeg = pos.azimuthDeg,
                        directionLabel = AstronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
                        heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
                        isAboveHorizon = isAbove,
                        sunAltitudeDeg = pos.sunAltitudeDeg,
                        quality = quality,
                        qualityMessage = "Muito favorável para binóculos sob céu de classe ${bortle.displayLabel}.",
                        score = totalScore,
                        opticalSummary = "${magnification.toInt()}× • Objetiva ${apertureMm.toInt()} mm • Campo amplo",
                        detectionCapability = binocularDsoDesc,
                        bestWindowStr = bestWindow.first,
                        maxAltitudeWindowDeg = bestWindow.second,
                        apertureSuitability = "Binóculo ${apertureMm.toInt()} mm / ${magnification.toInt()}x",
                        bortleImpactMessage = "Céu ${bortle.shortName}: binóculos oferecem imersão única em aglomerados e nebulosas grandes.",
                        solarProximityInfo = solarProximity
                    )
                )
            }
        }

        // Evaluate Moon for Binoculars
        val moonEval = evaluateMoonTarget(
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            mode = ObservationMode.BINOCULAR,
            equipment = TelescopeEquipment(),
            binocularApertureMm = apertureMm,
            binocularMagnification = magnification,
            bortle = bortle
        )
        if (moonEval != null) {
            evaluations.add(moonEval)
        }

        // Evaluate Active Meteor Showers for Binoculars
        MeteorShowerCatalog.getActiveShowers(calendar).forEach { shower ->
            val showerEval = MeteorShowerEngine.evaluateShower(shower, calendar, latitude, longitude, bortle)
            if (showerEval != null && showerEval.isCurrentlyActive && showerEval.maxRadiantAltitudeDeg > 0.0) {
                evaluations.add(showerEval.toTelescopeTargetEvaluation())
            }
        }

        return evaluations.filter { it.target.id != "sun" && !it.target.title.equals("Sol", ignoreCase = true) }.sortedByDescending { it.score }
    }

    private fun evaluateMoonTarget(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale
    ): TelescopeTargetEvaluation? {
        val moonPos = AstronomyEngine.calculateMoonPosition(calendar, latitude, longitude)
        val isAbove = moonPos.altitudeDeg > 0.0
        
        // Rule 1 & 2: Moon MUST be above horizon to be recommended
        if (!isAbove) return null

        val sunPos = AstronomyEngine.calculateSunPosition(calendar, latitude, longitude)
        val sunAltDeg = sunPos.altitudeDeg
        val isDaytime = sunAltDeg > -2.0

        val angularSeparationDeg = AstronomyEngine.calculateMoonSunAngularSeparationDeg(calendar, latitude, longitude)

        // Rule 2, 3 & 4:
        // - If daytime and angular separation < 60.0° (MIN_SAFE_SOLAR_ELONGATION_DEG), DO NOT RECOMMEND Moon.
        // - If night (Sol abaixo do horizonte, isDaytime == false), the 60° rule is DISREGARDED.
        if (isDaytime && angularSeparationDeg < AstronomyEngine.MIN_SAFE_SOLAR_ELONGATION_DEG) {
            return null
        }

        // Score evaluation based on altitude, phase, illumination %, solar separation, mode and equipment
        val altitudeFactor = (moonPos.altitudeDeg / 90.0).coerceIn(0.0, 1.0)
        val phaseContrastBonus = when {
            moonPos.illuminationPercent in 20..80 -> 20.0 // Craters & shadows sharply visible
            moonPos.illuminationPercent > 95 -> 10.0 // Full moon, high glare
            else -> 12.0
        }

        val baseScore = if (isDaytime) {
            val separationFactor = (angularSeparationDeg / 180.0).coerceIn(0.0, 1.0)
            (altitudeFactor * 45.0 + separationFactor * 35.0 + phaseContrastBonus).coerceIn(25.0, 95.0)
        } else {
            (altitudeFactor * 65.0 + phaseContrastBonus + 15.0).coerceIn(35.0, 100.0)
        }

        val quality = when {
            !isAbove -> ObservationQuality.UNAVAILABLE
            baseScore >= 75.0 -> ObservationQuality.IDEAL
            baseScore >= 55.0 -> ObservationQuality.EXCELLENT
            baseScore >= 35.0 -> ObservationQuality.GOOD
            else -> ObservationQuality.DIFFICULT
        }

        val (curiosityText, observationTip) = getLunarPhaseCuriosity(moonPos.phaseName, moonPos.illuminationPercent, isDaytime, mode)

        val qualityMessage = if (isDaytime) {
            "🌙 A Lua está visível durante o dia (${moonPos.phaseName}, ${moonPos.illuminationPercent}% iluminada) com separação angular segura de ${String.format("%.0f", angularSeparationDeg)}° do Sol.\n\n$curiosityText"
        } else {
            "🌙 ${moonPos.phaseName} com ${moonPos.illuminationPercent}% de iluminação no céu noturno.\n\n$curiosityText"
        }

        val daytimeSafetyWarning = if (isDaytime) {
            when (mode) {
                ObservationMode.TELESCOPE, ObservationMode.BINOCULAR ->
                    "☀️ ATENÇÃO AO OBSERVAR A LUA DURANTE O DIA\n\nA Lua pode ser observada durante o dia, mas nunca aponte binóculos ou telescópios para uma região próxima ao Sol.\n\nNunca olhe para o Sol através de instrumentos ópticos sem proteção solar apropriada."
                ObservationMode.NAKED_EYE ->
                    "☀️ A Lua está visível durante o dia. Nunca olhe diretamente para o Sol."
            }
        } else null

        val opticalSummary = when (mode) {
            ObservationMode.NAKED_EYE -> "Visão direta (a olho nu)"
            ObservationMode.BINOCULAR -> "${binocularMagnification.toInt()}× • Objetiva ${binocularApertureMm.toInt()} mm • Campo amplo"
            ObservationMode.TELESCOPE -> {
                val mag = equipment.magnification
                val pupil = equipment.exitPupilMm
                "${mag.toInt()}× • Pupila ${String.format("%.1f", pupil)} mm • Visão detalhada"
            }
        }

        val detectionCapability = "$observationTip\n\nFeições de destaque: Mares lunares, cristas, vales e crateras de impacto ao longo do exterminador."

        val windows = AstronomyEngine.findBestObservationWindows(Planet.JUPITER, calendar, latitude, longitude, "Baixo", false)
        val windowStr = if (isDaytime) "Durante o dia (${String.format("%.0f", moonPos.altitudeDeg)}° alt)" else if (windows.isNotEmpty()) "${windows.first().startTimeStr} – ${windows.first().endTimeStr}" else "Durante a noite"

        val solarProximity = AstronomyEngine.calculateSolarProximityForMoon(
            calendar = calendar,
            latitude = latitude,
            longitude = longitude
        )

        return TelescopeTargetEvaluation(
            target = CelestialTarget.MoonTarget,
            altitudeDeg = moonPos.altitudeDeg,
            azimuthDeg = moonPos.azimuthDeg,
            directionLabel = AstronomyEngine.convertAzimuthToDirection(moonPos.azimuthDeg),
            heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(moonPos.altitudeDeg),
            isAboveHorizon = isAbove,
            sunAltitudeDeg = sunAltDeg,
            quality = quality,
            qualityMessage = qualityMessage,
            score = baseScore,
            opticalSummary = opticalSummary,
            detectionCapability = detectionCapability,
            bestWindowStr = windowStr,
            maxAltitudeWindowDeg = moonPos.altitudeDeg,
            apertureSuitability = if (mode == ObservationMode.TELESCOPE) "Abertura ${equipment.apertureMm.toInt()} mm" else "Apropriado para ${mode.title}",
            bortleImpactMessage = "A Lua é o objeto mais brilhante e não sofre impacto da poluição luminosa.",
            daytimeSafetyWarning = daytimeSafetyWarning,
            solarProximityInfo = solarProximity
        )
    }

    private fun getLunarPhaseCuriosity(
        phaseName: String,
        illumPercent: Int,
        isDaytime: Boolean,
        mode: ObservationMode
    ): Pair<String, String> {
        val curiosity = when {
            phaseName.contains("Nova", ignoreCase = true) || illumPercent < 5 ->
                "Na fase de Lua Nova ($illumPercent%), a face voltada para a Terra recebe iluminação solar indireta. É o momento ideal para observar a 'Luz Cinzenta' (Earthshine), que é o brilho da Terra refletido nos mares lunares."
            phaseName.contains("Crescente", ignoreCase = true) && !phaseName.contains("Quarto", ignoreCase = true) && !phaseName.contains("Gibosa", ignoreCase = true) ->
                "Com $illumPercent% de iluminação, o sol incide obliquamente sobre a cratera Theophilus e o Mare Nectaris. As sombras longas projetadas ao longo da linha do exterminador revelam picos centrais e vales em relevo tridimensional."
            phaseName.contains("Quarto Crescente", ignoreCase = true) || (illumPercent in 45..55 && phaseName.contains("Crescente", ignoreCase = true)) ->
                "Na fase de Quarto Crescente ($illumPercent%), o exterminador cruza o centro do disco lunar. É a janela astronômica ideal para observar o trio de crateras Ptolemaeus, Alphonsus e Arzachel e a muralha Rupes Recta."
            phaseName.contains("Gibosa Crescente", ignoreCase = true) || (illumPercent in 56..94 && phaseName.contains("Crescente", ignoreCase = true)) ->
                "Na Gibosa Crescente ($illumPercent%), o Mare Imbrium (Mar das Chuvas) e a majestosa cratera Copernicus ganham destaque com paredes abruptas e terraciamento interno espetacular."
            phaseName.contains("Cheia", ignoreCase = true) || illumPercent >= 95 ->
                "Durante a Lua Cheia ($illumPercent%), a iluminação vertical destaca os sistemas de raios brilhantes emergindo de crateras jovens como Tycho e Copernicus, estendendo-se por milhares de quilômetros."
            phaseName.contains("Gibosa Minguante", ignoreCase = true) || (illumPercent in 56..94 && phaseName.contains("Minguante", ignoreCase = true)) ->
                "A Gibosa Minguante ($illumPercent%) ilumina o hemisfério ocidental lunar, revelando o vasto Oceanus Procellarum e a brilhante cratera Aristarchus, o ponto de maior albedo da superfície lunar."
            phaseName.contains("Quarto Minguante", ignoreCase = true) || (illumPercent in 45..55 && phaseName.contains("Minguante", ignoreCase = true)) ->
                "No Quarto Minguante ($illumPercent%), visível na segunda metade da noite e madrugada, os feixes solares iluminam a cratera Gassendi e as ranhuras rimae com alto contraste."
            else ->
                "Com $illumPercent% de superfície iluminada na fase $phaseName, a linha do exterminador avança diariamente revelando novas crateras de impacto, cristas dorsais e bacias vulcânicas."
        }

        val observationTip = if (isDaytime) {
            "🌙 No céu diurno, a luz azul espalhada pela atmosfera atua como um filtro natural de atenuação, permitindo estudar detalhes lunares sem fadiga ocular pelo alto brilho."
        } else {
            when (mode) {
                ObservationMode.TELESCOPE -> "🔭 Dica: Use oculares de médio e alto aumento para explorar fendas, domos e picos centrais ao longo da borda de sombra."
                ObservationMode.BINOCULAR -> "🔍 Dica: Apoie os cotovelos ou use um tripé para apreciar a percepção tridimensional dos mares e continentes lunares."
                ObservationMode.NAKED_EYE -> "👁️ Dica: A olho nu, identifique o contraste entre as regiões escuras (maria vulcânicos) e claras (continentes anortosíticos)."
            }
        }

        return Pair(curiosity, observationTip)
    }

    private fun optDiameterForPlanet(planet: Planet, distanceAU: Double): Double {
        val baseSizeArcsec = when (planet) {
            Planet.JUPITER -> 40.0
            Planet.SATURN -> 18.0
            Planet.MARS -> 10.0
            Planet.VENUS -> 25.0
            Planet.MERCURY -> 7.0
            Planet.URANUS -> 3.6
            Planet.NEPTUNE -> 2.3
        }
        return baseSizeArcsec / distanceAU
    }
}
