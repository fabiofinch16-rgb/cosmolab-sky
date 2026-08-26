package com.example.astronomy

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

data class MeteorShowerEvaluation(
    val shower: MeteorShower,
    val isCurrentlyActive: Boolean,
    val daysToPeak: Double,
    val isPeakDay: Boolean,
    val peakDateStr: String,
    val activityPeriodStr: String,

    // Radiant position at current selected time
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val directionLabel: String,
    val heightLabel: String,
    val isRadiantAboveHorizon: Boolean,
    val sunAltitudeDeg: Double,
    val skyConditionLabel: String,

    // Night Window (strictly calculated from astronomical positions)
    val bestWindowStr: String,
    val windowStartTimeStr: String,
    val windowEndTimeStr: String,
    val bestMomentTimeStr: String,
    val bestPeriodDescription: String,
    val maxRadiantAltitudeDeg: Double,
    val darkSkyDurationHours: Double,
    val hasObservableNightWindow: Boolean,

    // Moon info & impact
    val moonIlluminationPercent: Int,
    val moonPhaseName: String,
    val moonAltitudeDeg: Double,
    val isMoonAboveHorizon: Boolean,
    val moonInterferenceDescription: String,
    val moonInterferencePenalty: Double,

    // Rates & Bortle
    val peakZhr: Int,
    val effectiveExpectedZhr: Int,
    val bortleImpactDescription: String,

    // Quality & Score
    val quality: ObservationQuality,
    val qualityReason: String,
    val qualityMessage: String,
    val score: Double,
    val nightPotentialQuality: ObservationQuality,
    val nightPotentialScore: Double,

    // Advice
    val observationMethod: String = "👁️ A olho nu",
    val practicalAdvice: String
) {
    val displayStatus: String
        get() = when (quality) {
            ObservationQuality.UNAVAILABLE -> if (qualityReason.isNotEmpty()) "Indisponível — $qualityReason" else "Indisponível"
            ObservationQuality.DIFFICULT -> "Difícil"
            else -> quality.label
        }

    fun toTelescopeTargetEvaluation(): TelescopeTargetEvaluation {
        return TelescopeTargetEvaluation(
            target = CelestialTarget.MeteorShowerTarget(shower),
            altitudeDeg = altitudeDeg,
            azimuthDeg = azimuthDeg,
            directionLabel = directionLabel,
            heightLabel = heightLabel,
            isAboveHorizon = isRadiantAboveHorizon,
            sunAltitudeDeg = sunAltitudeDeg,
            quality = quality,
            qualityReason = qualityReason,
            qualityMessage = qualityMessage,
            score = score,
            opticalSummary = "👁️ A olho nu • Campo visual 180°",
            detectionCapability = "Taxa estimada no local: ~$effectiveExpectedZhr meteoros/h (${shower.velocityKmS} km/s).",
            bestWindowStr = bestWindowStr,
            maxAltitudeWindowDeg = maxRadiantAltitudeDeg,
            apertureSuitability = "Visão direta a olho nu (Telescópios não são recomendados)",
            bortleImpactMessage = bortleImpactDescription,
            rightAscensionDeg = shower.radiantRaDeg,
            declinationDeg = shower.radiantDecDeg
        )
    }
}

object MeteorShowerEngine {

    /**
     * Evaluates a specific meteor shower for the given observation context.
     */
    fun evaluateShower(
        shower: MeteorShower,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        bortle: BortleScale
    ): MeteorShowerEvaluation? {
        val isActive = shower.isActive(calendar)
        if (!isActive) return null

        val astronomyEngine = AstronomyEngine
        val pos = astronomyEngine.calculateEquatorialPosition(
            raDeg = shower.radiantRaDeg,
            decDeg = shower.radiantDecDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude
        )

        val daysFromPeak = shower.daysFromPeak(calendar)
        val isPeak = abs(daysFromPeak) <= 1.0

        // Half width in days for Gaussian activity decay
        val halfWidthDays = when (shower.id) {
            "quadrantids" -> 1.2
            "geminids" -> 2.8
            "perseids" -> 3.2
            "eta_aquariids", "southern_delta_aquariids" -> 4.5
            else -> 2.5
        }
        val activityProfile = exp(-0.5 * (abs(daysFromPeak) / halfWidthDays).pow(2.0)).coerceIn(0.1, 1.0)

        // Night observation window scanning (strictly astronomical: dark sky + radiant above horizon)
        val nightAnalysis = analyzeNightObservationWindow(
            shower = shower,
            baseCalendar = calendar,
            latitude = latitude,
            longitude = longitude
        )

        // Moon context
        val moonObs = astronomyEngine.analyzeMoonObservation(calendar, latitude, longitude)
        val moonIllum = moonObs.illuminationPercent

        // Moon interference penalty calculation
        val moonPenalty: Double
        val moonInterferenceDesc: String

        if (nightAnalysis.moonUpDuringWindow) {
            val illumFraction = moonIllum / 100.0
            moonPenalty = (illumFraction * 24.0).coerceIn(2.0, 24.0)
            moonInterferenceDesc = if (moonIllum > 70) {
                "Lua brilhante (${moonIllum}% ilum.) no céu durante o período útil, reduzindo a visibilidade de meteoros tênues."
            } else if (moonIllum > 30) {
                "Lua moderada (${moonIllum}% ilum.) presente, porém meteoros médios e bólidos continuam visíveis."
            } else {
                "Lua fina (${moonIllum}% ilum.) com interferência luminosa mínima no campo."
            }
        } else {
            moonPenalty = 0.0
            moonInterferenceDesc = if (moonIllum > 50) {
                "Excelente: a Lua (${moonIllum}%) se põe antes do ápice do radiante (ou nasce depois), garantindo janela de céu totalmente escuro."
            } else {
                "Ótimo: céu escuro sem interferência significativa da Lua."
            }
        }

        // Bortle reduction factor
        val bortleFactor = when (bortle.level) {
            1 -> 1.0
            2 -> 0.95
            3 -> 0.85
            4 -> 0.70
            5 -> 0.55
            6 -> 0.38
            7 -> 0.22
            8 -> 0.12
            else -> 0.05
        }

        val bortleImpactDesc = when (bortle.level) {
            1, 2 -> "Céu pristino (${bortle.displayLabel}): condições ideais para registrar quase a totalidade da taxa teórica ZHR."
            3, 4 -> "Céu rural/suburbano (${bortle.displayLabel}): boa escuridão, permitindo ver meteoros tênues e rastros ionizados."
            5, 6 -> "Céu suburbano (${bortle.displayLabel}): poluição luminosa ofusca meteoros de magnitude 4 a 6. Apenas meteoros médios e bólidos são visíveis."
            else -> "Céu urbano (${bortle.displayLabel}): forte poluição luminosa. Visíveis apenas bólidos luminosos excepcionais (fireballs)."
        }

        // Effective expected meteors per hour during optimal window
        val effectiveZhr = (shower.peakZhr * activityProfile * bortleFactor * (1.0 - (moonPenalty / 40.0)))
            .roundToInt()
            .coerceAtLeast(if (nightAnalysis.maxRadiantAlt > 5.0) 1 else 0)

        // Radiant height score for night potential (0..30)
        val altitudeScore = when {
            nightAnalysis.maxRadiantAlt <= 0.0 -> 0.0
            nightAnalysis.maxRadiantAlt < 15.0 -> (nightAnalysis.maxRadiantAlt / 15.0) * 12.0
            nightAnalysis.maxRadiantAlt < 35.0 -> 12.0 + ((nightAnalysis.maxRadiantAlt - 15.0) / 20.0) * 12.0
            else -> 24.0 + min(6.0, ((nightAnalysis.maxRadiantAlt - 35.0) / 30.0) * 6.0)
        }

        // Activity score (0..35)
        val activityScore = (activityProfile * 25.0 + (min(shower.peakZhr.toDouble(), 120.0) / 120.0) * 10.0)

        // Dark window score (0..20)
        val windowScore = (min(nightAnalysis.darkSkyHours, 5.0) / 5.0) * 20.0

        // Sky quality score (0..15)
        val skyScore = ((10.0 - bortle.level) / 9.0) * 15.0

        // Night potential score calculation (0..100)
        var potentialNightScore = (activityScore + altitudeScore + windowScore + skyScore - moonPenalty).coerceIn(0.0, 100.0)
        if (nightAnalysis.maxRadiantAlt <= 0.0 || !nightAnalysis.hasObservableWindow) {
            potentialNightScore = 0.0
        }

        val nightPotentialQuality = when {
            nightAnalysis.maxRadiantAlt <= 0.0 || !nightAnalysis.hasObservableWindow -> ObservationQuality.UNAVAILABLE
            potentialNightScore >= 72.0 -> ObservationQuality.IDEAL
            potentialNightScore >= 52.0 -> ObservationQuality.EXCELLENT
            potentialNightScore >= 32.0 -> ObservationQuality.GOOD
            potentialNightScore >= 15.0 -> ObservationQuality.DIFFICULT
            else -> ObservationQuality.UNAVAILABLE
        }

        // -------------------------------------------------------------
        // INSTANTANEOUS OBSERVABILITY CLASSIFICATION (Right Now at Calendar)
        // -------------------------------------------------------------
        val currentSunAlt = pos.sunAltitudeDeg
        val currentRadAlt = pos.altitudeDeg

        val skyConditionLabel = when {
            currentSunAlt > 0.0 -> "☀️ Dia (Sol a ${String.format(Locale.US, "%.0f°", currentSunAlt)})"
            currentSunAlt > -6.0 -> "🌅 Crepúsculo Civil (Céu claro)"
            currentSunAlt > -12.0 -> "🌆 Crepúsculo Náutico"
            currentSunAlt > -18.0 -> "🌌 Crepúsculo Astronômico"
            else -> "🌌 Céu Noturno Escuro"
        }

        val quality: ObservationQuality
        val qualityReason: String
        val qualityMessage: String
        val instantScore: Double

        when {
            nightAnalysis.maxRadiantAlt <= 0.0 || !nightAnalysis.hasObservableWindow -> {
                quality = ObservationQuality.UNAVAILABLE
                qualityReason = "Radiante abaixo do horizonte nesta latitude"
                qualityMessage = "O radiante não atinge altitude observável durante a noite nesta localização."
                instantScore = 0.0
            }
            currentSunAlt > -6.0 -> {
                // Daytime or civil twilight: MANDATORY clear sky rule
                quality = ObservationQuality.UNAVAILABLE
                qualityReason = "Céu claro"
                qualityMessage = "Sol acima do horizonte / céu claro. A observação a olho nu só é viável à noite durante a janela calculada (${nightAnalysis.windowStr})."
                instantScore = min(potentialNightScore * 0.15, 15.0)
            }
            currentRadAlt <= 0.0 -> {
                // Night, but radiant is currently below horizon
                quality = ObservationQuality.UNAVAILABLE
                qualityReason = "Abaixo do horizonte"
                qualityMessage = "O radiante está abaixo do horizonte no momento. A observação estará aberta na janela ${nightAnalysis.windowStr}."
                instantScore = min(potentialNightScore * 0.20, 20.0)
            }
            currentSunAlt > -12.0 -> {
                // Nautical twilight: sky still has residual light
                quality = ObservationQuality.DIFFICULT
                qualityReason = "Crepúsculo"
                qualityMessage = "Céu em crepúsculo. A claridade residual ofusca meteoros de magnitude moderada. Aguarde o céu totalmente escuro."
                instantScore = min(potentialNightScore * 0.45, 38.0)
            }
            currentRadAlt < 15.0 -> {
                // Dark sky, but radiant is low
                quality = ObservationQuality.DIFFICULT
                qualityReason = "Radiante baixo"
                qualityMessage = "Radiante baixo no horizonte (${String.format(Locale.US, "%.1f°", currentRadAlt)}). A taxa aumentará conforme o radiante se eleva."
                instantScore = min(potentialNightScore * 0.60, 48.0)
            }
            else -> {
                // Truly dark sky (Sun <= -12°) AND radiant is well placed (Altitude >= 15°)
                quality = nightPotentialQuality
                qualityReason = when {
                    bortle.level >= 8 && moonPenalty > 15.0 -> "Forte poluição luminosa e interferência lunar"
                    abs(daysFromPeak) > 7.0 -> "Longe da data do pico de atividade"
                    else -> ""
                }
                qualityMessage = when (quality) {
                    ObservationQuality.IDEAL -> "Excelente oportunidade agora! Céu escuro e radiante em posição muito favorável."
                    ObservationQuality.EXCELLENT -> "Muito bom para observação a olho nu neste momento."
                    ObservationQuality.GOOD -> "Boa visibilidade atual. Olhe para áreas abertas do céu a 45° do radiante."
                    ObservationQuality.DIFFICULT -> "Condições difíceis devido à poluição urbana ou interferência lunar."
                    ObservationQuality.UNAVAILABLE -> "Indisponível no momento para observação nesta localidade."
                }
                instantScore = potentialNightScore
            }
        }

        val practicalAdvice = "Procure uma área ampla e aberta com vista desimpedida do céu, longe de postes e luzes diretas. " +
                "Deite-se confortavelmente em uma cadeira reclinável e evite olhar fixamente para o radiante (${shower.radiantConstellation}); " +
                "os meteoros com trilhas mais longas cruzam regiões a 30°–60° de distância. Não utilize telescópios ou binóculos."

        return MeteorShowerEvaluation(
            shower = shower,
            isCurrentlyActive = true,
            daysToPeak = daysFromPeak,
            isPeakDay = isPeak,
            peakDateStr = shower.peakDateStr,
            activityPeriodStr = shower.activityPeriodStr,
            altitudeDeg = pos.altitudeDeg,
            azimuthDeg = pos.azimuthDeg,
            directionLabel = astronomyEngine.convertAzimuthToDirection(pos.azimuthDeg),
            heightLabel = astronomyEngine.convertAltitudeToHeightLabel(pos.altitudeDeg),
            isRadiantAboveHorizon = pos.altitudeDeg > 0.0,
            sunAltitudeDeg = pos.sunAltitudeDeg,
            skyConditionLabel = skyConditionLabel,
            bestWindowStr = nightAnalysis.windowStr,
            windowStartTimeStr = nightAnalysis.startTimeStr,
            windowEndTimeStr = nightAnalysis.endTimeStr,
            bestMomentTimeStr = nightAnalysis.bestMomentTimeStr,
            bestPeriodDescription = nightAnalysis.bestPeriodDesc,
            maxRadiantAltitudeDeg = nightAnalysis.maxRadiantAlt,
            darkSkyDurationHours = nightAnalysis.darkSkyHours,
            hasObservableNightWindow = nightAnalysis.hasObservableWindow,
            moonIlluminationPercent = moonIllum,
            moonPhaseName = moonObs.phaseName,
            moonAltitudeDeg = moonObs.altitudeDeg,
            isMoonAboveHorizon = moonObs.altitudeDeg > 0.0,
            moonInterferenceDescription = moonInterferenceDesc,
            moonInterferencePenalty = moonPenalty,
            peakZhr = shower.peakZhr,
            effectiveExpectedZhr = effectiveZhr,
            bortleImpactDescription = bortleImpactDesc,
            quality = quality,
            qualityReason = qualityReason,
            qualityMessage = qualityMessage,
            score = instantScore,
            nightPotentialQuality = nightPotentialQuality,
            nightPotentialScore = potentialNightScore,
            practicalAdvice = practicalAdvice
        )
    }

    private data class NightWindowAnalysis(
        val windowStr: String,
        val startTimeStr: String,
        val endTimeStr: String,
        val bestMomentTimeStr: String,
        val bestPeriodDesc: String,
        val maxRadiantAlt: Double,
        val darkSkyHours: Double,
        val moonUpDuringWindow: Boolean,
        val hasObservableWindow: Boolean
    )

    private fun analyzeNightObservationWindow(
        shower: MeteorShower,
        baseCalendar: Calendar,
        latitude: Double,
        longitude: Double
    ): NightWindowAnalysis {
        val astronomyEngine = AstronomyEngine
        val tz = baseCalendar.timeZone

        // Anchor start to 17:00 of the corresponding night
        val startCal = Calendar.getInstance(tz).apply {
            timeInMillis = baseCalendar.timeInMillis
            if (get(Calendar.HOUR_OF_DAY) < 12) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var maxDarkAlt = -90.0
        var bestMomentCal: Calendar? = null
        val darkObservablePoints = mutableListOf<Calendar>()
        val twilightObservablePoints = mutableListOf<Calendar>()
        var moonCountInWindow = 0
        var darkPointsCount = 0

        val timeFmt = SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply {
            timeZone = tz
        }

        // Scan 14 hours in 10-minute steps (from 17:00 to 07:00 next day = 84 steps)
        for (step in 0..84) {
            val pointCal = (startCal.clone() as Calendar).apply {
                add(Calendar.MINUTE, step * 10)
            }

            val radPos = astronomyEngine.calculateEquatorialPosition(
                raDeg = shower.radiantRaDeg,
                decDeg = shower.radiantDecDeg,
                calendar = pointCal,
                latitude = latitude,
                longitude = longitude
            )

            val sunAlt = radPos.sunAltitudeDeg
            val radAlt = radPos.altitudeDeg

            val isDark = sunAlt <= -12.0
            val isTwilight = sunAlt in -12.0..-6.0

            if (isDark) {
                darkPointsCount++
                if (radAlt > 0.0) {
                    darkObservablePoints.add(pointCal)
                    if (radAlt > maxDarkAlt) {
                        maxDarkAlt = radAlt
                        bestMomentCal = pointCal
                    }
                    val moonPos = astronomyEngine.analyzeMoonObservation(pointCal, latitude, longitude)
                    if (moonPos.altitudeDeg > 5.0 && moonPos.illuminationPercent > 25) {
                        moonCountInWindow++
                    }
                }
            } else if (isTwilight && radAlt > 0.0) {
                twilightObservablePoints.add(pointCal)
            }
        }

        val (windowStr, startTimeStr, endTimeStr, bestMomentStr, periodDesc, hasObs) = when {
            darkObservablePoints.isNotEmpty() -> {
                val first = timeFmt.format(darkObservablePoints.first().time)
                val last = timeFmt.format(darkObservablePoints.last().time)
                val bestM = bestMomentCal?.let { timeFmt.format(it.time) } ?: first
                val wStr = if (first == last) first else "$first – $last"

                val startHour = darkObservablePoints.first().get(Calendar.HOUR_OF_DAY)
                val endHour = darkObservablePoints.last().get(Calendar.HOUR_OF_DAY)
                val periodName = when {
                    startHour >= 17 && endHour <= 23 -> "Início da noite ($first às $last)"
                    startHour in 0..5 -> "Madrugada ($first às $last)"
                    startHour >= 21 || endHour in 0..4 -> "Meio da noite / Madrugada ($first às $last)"
                    else -> "Ao longo da noite ($first às $last)"
                }
                val desc = "$periodName, com melhor ápice do radiante às $bestM (altitude de ${String.format(Locale.US, "%.1f", maxDarkAlt)}°)."

                NightWindowTuple(wStr, first, last, bestM, desc, true)
            }
            twilightObservablePoints.isNotEmpty() -> {
                val first = timeFmt.format(twilightObservablePoints.first().time)
                val last = timeFmt.format(twilightObservablePoints.last().time)
                val wStr = if (first == last) first else "$first – $last"
                NightWindowTuple(wStr, first, last, first, "Janela crepuscular restrita ($first às $last).", true)
            }
            else -> {
                NightWindowTuple("Não observável hoje", "--:--", "--:--", "--:--", "Radiante não atinge altitude observável durante a noite nesta localização.", false)
            }
        }

        val moonUp = darkObservablePoints.isNotEmpty() && (moonCountInWindow.toDouble() / darkObservablePoints.size) > 0.5
        val darkHours = (darkPointsCount * 10.0) / 60.0

        return NightWindowAnalysis(
            windowStr = windowStr,
            startTimeStr = startTimeStr,
            endTimeStr = endTimeStr,
            bestMomentTimeStr = bestMomentStr,
            bestPeriodDesc = periodDesc,
            maxRadiantAlt = if (maxDarkAlt > -90.0) maxDarkAlt else 0.0,
            darkSkyHours = darkHours,
            moonUpDuringWindow = moonUp,
            hasObservableWindow = hasObs
        )
    }

    private data class NightWindowTuple(
        val windowStr: String,
        val startTimeStr: String,
        val endTimeStr: String,
        val bestMomentTimeStr: String,
        val periodDesc: String,
        val hasObs: Boolean
    )
}

