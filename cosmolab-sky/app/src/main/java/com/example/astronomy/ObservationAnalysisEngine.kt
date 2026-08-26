package com.example.astronomy

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Classification of magnification suitability for a specific celestial target.
 */
enum class MagnificationSuitability(val label: String, val shortLabel: String) {
    INSUFFICIENT("Ampliação Insuficiente", "Insuficiente"),
    OPTIMAL("Ampliação Ideal", "Ideal"),
    HIGH("Ampliação Alta", "Alta"),
    EXCESSIVE("Ampliação Excessiva", "Excessiva")
}

/**
 * Configurable weights for observation quality scoring.
 */
object QualityWeights {
    const val ALTITUDE_WEIGHT = 0.25
    const val MAGNIFICATION_WEIGHT = 0.20
    const val OBJECT_SPECIFIC_WEIGHT = 0.20
    const val APERTURE_WEIGHT = 0.15
    const val SKY_WEIGHT = 0.10
    const val EXIT_PUPIL_WEIGHT = 0.05
    const val MOON_WEIGHT = 0.05
}

/**
 * Individual factor subscores (0..100) evaluated for an observation context.
 */
data class ObservationQualityFactors(
    val altitude: Double,
    val equipment: Double,
    val magnification: Double,
    val aperture: Double,
    val sky: Double,
    val moon: Double,
    val objectSpecific: Double,
    val exitPupil: Double,
    val sun: Double
)

/**
 * Internal levels corresponding to quality score ranges:
 * 0..20: Muito difícil
 * 21..40: Difícil
 * 41..60: Regular
 * 61..80: Bom
 * 81..100: Excelente
 */
enum class ObservationQualityLevel(
    val label: String,
    val minScore: Double,
    val maxScore: Double
) {
    VERY_DIFFICULT("Muito difícil", 0.0, 20.0),
    DIFFICULT("Difícil", 20.1, 40.0),
    FAIR("Regular", 40.1, 60.0),
    GOOD("Bom", 60.1, 80.0),
    EXCELLENT("Excelente", 80.1, 100.0);

    companion object {
        fun fromScore(score: Double): ObservationQualityLevel {
            return when {
                score <= 20.0 -> VERY_DIFFICULT
                score <= 40.0 -> DIFFICULT
                score <= 60.0 -> FAIR
                score <= 80.0 -> GOOD
                else -> EXCELLENT
            }
        }
    }
}

/**
 * Detailed observational quality breakdown structure.
 */
data class ObservationQualityBreakdown(
    val score: Double, // 0..100
    val level: ObservationQualityLevel,
    val levelLabel: String,
    val quality: ObservationQuality,
    val qualityReason: String = "",
    val factors: ObservationQualityFactors,
    val visibleFeatures: List<TargetObservableFeature>,
    val unresolvableFeatures: List<TargetObservableFeature>,
    val recommendation: String,
    val dynamicDescription: String,
    val limitations: List<String>
)

/**
 * Comprehensive analysis result from the Astronomical Analysis Engine.
 */
data class ObservationAnalysisResult(
    val targetId: String,
    val mode: ObservationMode,
    val level: Int, // 1 to 5
    val levelLabel: String,
    val magnificationSuitability: MagnificationSuitability,
    val perceivedSizeText: String,
    val resolvableFeatures: List<TargetObservableFeature>,
    val unresolvableFeatures: List<TargetObservableFeature>,
    val dynamicDescription: String,
    val qualityMessage: String,
    val score: Double,
    val opticalSummary: String,
    val quality: ObservationQuality,
    val qualityReason: String = "",
    val qualityBreakdown: ObservationQualityBreakdown? = null
)

/**
 * Deterministic Astronomical & Optical Analysis Engine for CosmoLab Sky.
 * Operates purely locally without generative AI or API calls.
 */
object ObservationAnalysisEngine {

    fun analyzePlanetObservation(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale = BortleScale.BORTLE_4,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeObservationEvaluation {
        val result = evaluatePlanet(
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            magnitude = magnitude,
            distanceAU = distanceAU,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle
        )

        return TelescopeObservationEvaluation(
            planet = planet,
            equipment = equipment,
            magnification = when (mode) {
                ObservationMode.NAKED_EYE -> 1.0
                ObservationMode.BINOCULAR -> binocularMagnification
                ObservationMode.TELESCOPE -> equipment.magnification
            },
            exitPupilMm = when (mode) {
                ObservationMode.NAKED_EYE -> 0.0
                ObservationMode.BINOCULAR -> if (binocularMagnification > 0) binocularApertureMm / binocularMagnification else 5.0
                ObservationMode.TELESCOPE -> equipment.exitPupilMm
            },
            trueFovDeg = when (mode) {
                ObservationMode.NAKED_EYE -> null
                ObservationMode.BINOCULAR -> 50.0 / binocularMagnification
                ObservationMode.TELESCOPE -> equipment.trueFovDeg
            },
            planetAngularDiameterArcsec = OpticsEngine.getAngularDiameterArcsec(planet, distanceAU),
            perceivedAngularSizeMin = (OpticsEngine.getAngularDiameterArcsec(planet, distanceAU) * (if (mode == ObservationMode.TELESCOPE) equipment.magnification else if (mode == ObservationMode.BINOCULAR) binocularMagnification else 1.0)) / 60.0,
            detectionCapability = result.dynamicDescription,
            quality = result.quality,
            qualityReason = result.qualityReason,
            qualityMessage = result.qualityMessage,
            score = result.score,
            opticalSummary = result.opticalSummary,
            qualityBreakdown = result.qualityBreakdown,
            solarProximityInfo = solarProximityInfo
        )
    }

    fun analyzeDsoObservation(
        dso: DeepSkyObject,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale = BortleScale.BORTLE_4,
        lunarInfo: LunarInterferenceInfo? = null,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeTargetEvaluation {
        val result = evaluateDso(
            dso = dso,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle,
            lunarInfo = lunarInfo
        )

        val directionLabel = AstronomyEngine.convertAzimuthToDirection((dso.raDeg).coerceIn(0.0, 360.0))
        val heightLabel = AstronomyEngine.convertAltitudeToHeightLabel(altitudeDeg)
        val isAbove = altitudeDeg > 0.0

        return TelescopeTargetEvaluation(
            target = CelestialTarget.DeepSkyTarget(dso),
            altitudeDeg = altitudeDeg,
            azimuthDeg = (dso.rightAscensionHours * 15.0) % 360.0,
            directionLabel = directionLabel,
            heightLabel = heightLabel,
            isAboveHorizon = isAbove,
            sunAltitudeDeg = sunAltitudeDeg,
            quality = result.quality,
            qualityReason = result.qualityReason,
            qualityMessage = result.qualityMessage,
            score = result.score,
            opticalSummary = result.opticalSummary,
            detectionCapability = result.dynamicDescription,
            bestWindowStr = if (isAbove) "Toda a noite (Melhor na culminação)" else "Abaixo do horizonte",
            maxAltitudeWindowDeg = max(0.0, altitudeDeg),
            apertureSuitability = when (mode) {
                ObservationMode.NAKED_EYE -> "Visão direta a olho nu"
                ObservationMode.BINOCULAR -> "Binóculo ${binocularApertureMm.toInt()} mm"
                ObservationMode.TELESCOPE -> "Abertura ${equipment.apertureMm.toInt()} mm (limite ~${String.format("%.1f", 2.7 + 5.0 * kotlin.math.log10(equipment.apertureMm))} mag)"
            },
            bortleImpactMessage = "Céu Classe ${bortle.level} (${bortle.shortName}): ${bortle.description}",
            lunarInfo = lunarInfo,
            rightAscensionDeg = dso.raDeg,
            declinationDeg = dso.declinationDeg,
            qualityBreakdown = result.qualityBreakdown,
            solarProximityInfo = solarProximityInfo
        )
    }

    /**
     * Central observation quality calculation engine.
     * Evaluates all 13 observational variables to return a score from 0 to 100,
     * subfactor scores, resolvable features, contextual recommendation, and dynamic description.
     */
    fun calculateObservationQuality(
        targetName: String,
        targetId: String,
        isPlanet: Boolean,
        planet: Planet? = null,
        dso: DeepSkyObject? = null,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale = BortleScale.BORTLE_4,
        lunarInfo: LunarInterferenceInfo? = null,
        apparentDiameterArcsec: Double = 0.0,
        magnitude: Double = 0.0
    ): ObservationQualityBreakdown {
        val profile = if (isPlanet && planet != null) {
            AstronomicalKnowledgeBase.getProfileForPlanet(planet)
        } else if (dso != null) {
            AstronomicalKnowledgeBase.getProfileForDso(dso)
        } else {
            ObjectObservationalProfile(
                targetId = targetId,
                isPlanet = false,
                features = emptyList()
            )
        }

        val effectiveAperture = when (mode) {
            ObservationMode.NAKED_EYE -> 6.0
            ObservationMode.BINOCULAR -> binocularApertureMm
            ObservationMode.TELESCOPE -> equipment.apertureMm
        }

        val effectiveMag = when (mode) {
            ObservationMode.NAKED_EYE -> 1.0
            ObservationMode.BINOCULAR -> binocularMagnification
            ObservationMode.TELESCOPE -> equipment.magnification
        }

        val exitPupil = when (mode) {
            ObservationMode.NAKED_EYE -> 6.0
            ObservationMode.BINOCULAR -> if (binocularMagnification > 0) binocularApertureMm / binocularMagnification else 5.0
            ObservationMode.TELESCOPE -> equipment.exitPupilMm
        }

        val dawesLimit = 116.0 / max(1.0, effectiveAperture)

        val resolvable = mutableListOf<TargetObservableFeature>()
        val unresolvable = mutableListOf<TargetObservableFeature>()

        val moonPenalty = lunarInfo?.penaltyScore ?: 0.0
        val isAbove = altitudeDeg > 0.0
        val isNight = sunAltitudeDeg <= -2.0

        for (feature in profile.features) {
            val isModeSupported = when (mode) {
                ObservationMode.NAKED_EYE -> feature.visibleNakedEye
                ObservationMode.BINOCULAR -> feature.visibleBinocular
                ObservationMode.TELESCOPE -> feature.visibleTelescope
            }

            val meetsAperture = effectiveAperture >= feature.minApertureMm
            val meetsMag = effectiveMag >= feature.minMagnification
            val meetsResolution = dawesLimit <= feature.requiredResolutionArcsec || mode == ObservationMode.NAKED_EYE || mode == ObservationMode.BINOCULAR
            val meetsAltitude = altitudeDeg >= feature.minAltitudeDeg

            // Atmospheric turbulence check: fine details require altitude >= 20°
            val atmosphereOk = !(feature.requiredResolutionArcsec < 1.3 && altitudeDeg < 20.0)

            val bortleOk = !feature.sensitiveToBortle || bortle.level <= 6
            val moonOk = !feature.sensitiveToMoon || moonPenalty < 15.0

            if (isAbove && isNight && isModeSupported && meetsAperture && meetsMag && meetsResolution && meetsAltitude && atmosphereOk && bortleOk && moonOk) {
                resolvable.add(feature)
            } else {
                unresolvable.add(feature)
            }
        }

        val magSuitability = when (mode) {
            ObservationMode.NAKED_EYE -> MagnificationSuitability.OPTIMAL
            ObservationMode.BINOCULAR -> MagnificationSuitability.OPTIMAL
            ObservationMode.TELESCOPE -> {
                if (effectiveMag > equipment.maxUsefulMagnification || exitPupil < 0.5) {
                    MagnificationSuitability.EXCESSIVE
                } else if (effectiveMag < profile.minUsefulMagnification) {
                    MagnificationSuitability.INSUFFICIENT
                } else if (effectiveMag > profile.maxOptimalMagnification) {
                    MagnificationSuitability.HIGH
                } else {
                    MagnificationSuitability.OPTIMAL
                }
            }
        }

        // --- SUBFACTOR CALCULATION (0..100) ---

        // 1. Altitude & Atmospheric turbulence
        val altitudeScore = when {
            altitudeDeg <= 0.0 -> 0.0
            altitudeDeg <= 10.0 -> (altitudeDeg / 10.0) * 20.0
            altitudeDeg <= 20.0 -> 20.0 + ((altitudeDeg - 10.0) / 10.0) * 20.0
            altitudeDeg <= 30.0 -> 40.0 + ((altitudeDeg - 20.0) / 10.0) * 20.0
            altitudeDeg <= 45.0 -> 60.0 + ((altitudeDeg - 30.0) / 15.0) * 20.0
            altitudeDeg <= 60.0 -> 80.0 + ((altitudeDeg - 45.0) / 15.0) * 12.0
            else -> 92.0 + min(8.0, ((altitudeDeg - 60.0) / 30.0) * 8.0)
        }

        // 2. Sun / Night illumination
        val sunScore = when {
            sunAltitudeDeg <= -18.0 -> 100.0
            sunAltitudeDeg <= -12.0 -> 80.0 + ((-12.0 - sunAltitudeDeg) / 6.0) * 20.0
            sunAltitudeDeg <= -6.0 -> 40.0 + ((-6.0 - sunAltitudeDeg) / 6.0) * 40.0
            sunAltitudeDeg <= 0.0 -> 10.0 + ((0.0 - sunAltitudeDeg) / 6.0) * 30.0
            else -> 0.0
        }

        // 3. Aperture & Resolution power
        val apertureScore = when (mode) {
            ObservationMode.NAKED_EYE -> 50.0
            ObservationMode.BINOCULAR -> min(100.0, 50.0 + (binocularApertureMm / 80.0) * 50.0)
            ObservationMode.TELESCOPE -> min(100.0, 30.0 + (equipment.apertureMm / 300.0) * 70.0)
        }

        // 4. Magnification Appropriateness
        val magnificationScore = when (mode) {
            ObservationMode.NAKED_EYE -> 100.0
            ObservationMode.BINOCULAR -> 95.0
            ObservationMode.TELESCOPE -> {
                val maxUseful = equipment.maxUsefulMagnification
                val minUseful = profile.minUsefulMagnification
                val maxOpt = profile.maxOptimalMagnification

                when {
                    effectiveMag > maxUseful -> max(10.0, 100.0 - (effectiveMag - maxUseful) * 1.5)
                    effectiveMag < minUseful -> max(20.0, 50.0 + (effectiveMag / minUseful) * 40.0)
                    effectiveMag in minUseful..maxOpt -> 100.0 - (abs(effectiveMag - (minUseful + maxOpt) / 2.0) / max(1.0, maxOpt - minUseful)) * 15.0
                    else -> max(40.0, 85.0 - ((effectiveMag - maxOpt) / max(1.0, maxUseful - maxOpt)) * 45.0)
                }
            }
        }

        // 5. Exit Pupil Usability
        val exitPupilScore = if (isPlanet) {
            when {
                exitPupil in profile.idealExitPupilMinMm..profile.idealExitPupilMaxMm -> 100.0
                exitPupil in 0.5..profile.idealExitPupilMinMm -> 80.0
                exitPupil in profile.idealExitPupilMaxMm..4.0 -> 75.0
                exitPupil < 0.5 -> 30.0
                else -> 40.0
            }
        } else {
            when {
                exitPupil in profile.idealExitPupilMinMm..profile.idealExitPupilMaxMm -> 100.0
                exitPupil in 1.0..profile.idealExitPupilMinMm -> 80.0
                exitPupil in profile.idealExitPupilMaxMm..6.0 -> 75.0
                exitPupil < 0.6 -> 30.0
                else -> 50.0
            }
        }

        // 6. Sky Darkness (Bortle)
        val rawSkyScore = when (bortle) {
            BortleScale.BORTLE_1, BortleScale.BORTLE_2 -> 100.0
            BortleScale.BORTLE_3 -> 90.0
            BortleScale.BORTLE_4 -> 80.0
            BortleScale.BORTLE_5 -> 65.0
            BortleScale.BORTLE_6 -> 50.0
            BortleScale.BORTLE_7 -> 35.0
            BortleScale.BORTLE_8, BortleScale.BORTLE_9 -> 20.0
        }
        val skyScore = if (isPlanet) (75.0 + rawSkyScore * 0.25) else rawSkyScore

        // 7. Lunar Interference
        val moonScore = if (lunarInfo == null || !lunarInfo.isMoonAboveHorizon) {
            100.0
        } else if (isPlanet) {
            max(60.0, 100.0 - lunarInfo.penaltyScore * 1.0)
        } else {
            max(0.0, 100.0 - lunarInfo.penaltyScore * 2.5)
        }

        // 8. Object Specific Features Resolved Ratio
        val objectSpecificScore = if (profile.features.isEmpty()) {
            50.0
        } else {
            (resolvable.size.toDouble() / profile.features.size.toDouble()) * 100.0
        }

        val factors = ObservationQualityFactors(
            altitude = altitudeScore,
            equipment = apertureScore,
            magnification = magnificationScore,
            aperture = apertureScore,
            sky = skyScore,
            moon = moonScore,
            objectSpecific = objectSpecificScore,
            exitPupil = exitPupilScore,
            sun = sunScore
        )

        // Weighted raw total
        val rawWeightedScore = (
            altitudeScore * QualityWeights.ALTITUDE_WEIGHT +
            magnificationScore * QualityWeights.MAGNIFICATION_WEIGHT +
            objectSpecificScore * QualityWeights.OBJECT_SPECIFIC_WEIGHT +
            apertureScore * QualityWeights.APERTURE_WEIGHT +
            skyScore * QualityWeights.SKY_WEIGHT +
            exitPupilScore * QualityWeights.EXIT_PUPIL_WEIGHT +
            moonScore * QualityWeights.MOON_WEIGHT
        )

        var finalScore = rawWeightedScore

        // PHYSICAL CONSTRAINT CAPS (Atmospheric turbulence, horizon, twilight)
        if (altitudeDeg <= 0.0) {
            finalScore = 0.0
        } else if (altitudeDeg < 10.0) {
            finalScore = min(finalScore, 20.0)
        } else if (altitudeDeg < 20.0) {
            finalScore = min(finalScore, 48.0)
        } else if (altitudeDeg < 30.0) {
            finalScore = min(finalScore, 70.0)
        }

        if (sunAltitudeDeg > 0.0) {
            finalScore = min(finalScore, 15.0)
        } else if (sunAltitudeDeg > -6.0) {
            finalScore = min(finalScore, 35.0)
        }

        if (mode == ObservationMode.TELESCOPE) {
            if (effectiveMag > equipment.maxUsefulMagnification) {
                finalScore *= 0.65
            } else if (exitPupil < 0.5) {
                finalScore *= 0.80
            }
        }

        finalScore = finalScore.coerceIn(0.0, 100.0)

        val level = ObservationQualityLevel.fromScore(finalScore)

        val isVenusDaytime = (isPlanet && planet == Planet.VENUS && altitudeDeg > 15.0)

        val (qualityEnum, qualityReason) = when {
            altitudeDeg <= 0.0 -> Pair(ObservationQuality.UNAVAILABLE, "Abaixo do horizonte")
            sunAltitudeDeg > -6.0 && !isVenusDaytime -> Pair(ObservationQuality.UNAVAILABLE, "Céu claro")
            finalScore >= 80.1 -> Pair(ObservationQuality.IDEAL, "")
            finalScore >= 60.1 -> Pair(ObservationQuality.EXCELLENT, "")
            finalScore >= 40.1 -> Pair(ObservationQuality.GOOD, "")
            else -> {
                val reason = when {
                    sunAltitudeDeg > -12.0 -> "Crepúsculo"
                    altitudeDeg < 15.0 -> "Altitude baixa"
                    else -> "Condições desfavoráveis"
                }
                Pair(ObservationQuality.DIFFICULT, reason)
            }
        }

        // GENERATE LIMITATIONS LIST
        val limitations = mutableListOf<String>()

        if (altitudeDeg <= 0.0) {
            limitations.add("O astro está abaixo do horizonte no momento.")
        } else if (altitudeDeg < 10.0) {
            limitations.add("Altitude muito baixa (${String.format("%.0f", altitudeDeg)}°): turbulência atmosférica extrema e refração causam sério desfoque.")
        } else if (altitudeDeg < 20.0) {
            limitations.add("Baixa altitude no horizonte (${String.format("%.0f", altitudeDeg)}°): a turbulência do ar prejudica a resolução de detalhes finos.")
        } else if (altitudeDeg < 30.0) {
            limitations.add("Altitude moderada (${String.format("%.0f", altitudeDeg)}°): a estabilidade atmosférica pode oscilar.")
        }

        if (mode == ObservationMode.TELESCOPE) {
            if (effectiveMag > equipment.maxUsefulMagnification) {
                limitations.add("Ampliação de ${effectiveMag.toInt()}× ultrapassa o limite útil (${equipment.maxUsefulMagnification.toInt()}×) para a abertura de ${equipment.apertureMm.toInt()} mm.")
            } else if (effectiveMag < profile.minUsefulMagnification) {
                limitations.add("Ampliação de ${effectiveMag.toInt()}× é muito baixa para revelar detalhes no disco deste objeto.")
            }

            if (exitPupil < 0.5) {
                limitations.add("Pupila de saída muito reduzida (${String.format("%.2f", exitPupil)} mm): imagem fraca e propensa a moscas volantes.")
            }
        }

        if (sunAltitudeDeg > -6.0) {
            limitations.add("Crepúsculo/Iluminação solar reduz o contraste do céu.")
        }

        if (lunarInfo != null && lunarInfo.isMoonAboveHorizon && lunarInfo.penaltyScore > 10.0) {
            limitations.add("O luar (${lunarInfo.moonIlluminationPercent}% iluminado a ${String.format("%.0f", lunarInfo.angularSeparationDeg)}°) clareia o fundo do céu.")
        }

        if (bortle.level >= 6 && profile.features.any { it.sensitiveToBortle }) {
            limitations.add("Poluição luminosa da classe Bortle ${bortle.level} ofusca detalhes difusos externos.")
        }

        // GENERATE CONTEXTUAL RECOMMENDATION
        val recommendation = when {
            altitudeDeg <= 0.0 -> "Aguarde o astro nascer no horizonte para realizar a observação."
            altitudeDeg < 25.0 -> "Espere o astro atingir maior altitude (acima de 30°) para reduzir a turbulência atmosférica e observar detalhes mais finos."
            mode == ObservationMode.TELESCOPE && effectiveMag > equipment.maxUsefulMagnification -> "A ampliação atual (${effectiveMag.toInt()}×) está excessiva. Reduzir o aumento usando uma ocular de maior mm produzirá uma imagem mais nítida e brilhante."
            mode == ObservationMode.TELESCOPE && effectiveMag < profile.minUsefulMagnification -> "Sua ampliação atual (${effectiveMag.toInt()}×) é baixa para este objeto. Utilize uma ocular com menor distância focal para aproximar a imagem."
            mode == ObservationMode.TELESCOPE && exitPupil < 0.6 -> "Reduzir a ampliação aumentará a pupila de saída, proporcionando uma imagem mais confortável e iluminada."
            bortle.level >= 6 && profile.features.any { it.sensitiveToBortle } -> "Seu equipamento tem potencial para mais detalhes, mas a poluição luminosa local está limitando o contraste. Se possível, observe em um local com céu mais escuro."
            lunarInfo != null && lunarInfo.isMoonAboveHorizon && lunarInfo.penaltyScore > 15.0 -> "O luar está reduzindo o contraste de fundo. Tente observar quando a Lua estiver abaixo do horizonte."
            finalScore >= 80.0 -> "A ampliação e as condições atmosféricas estão favoráveis! Excelente momento para examinar detalhes finos do astro."
            else -> "Configuração óptica equilibrada para as condições atuais do céu."
        }

        // GENERATE DYNAMIC PORTUGUESE DESCRIPTION
        val dynamicDesc = generateDynamicSentence(
            targetName = targetName,
            isPlanet = isPlanet,
            planet = planet,
            dso = dso,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            effectiveMag = effectiveMag,
            exitPupil = exitPupil,
            altitudeDeg = altitudeDeg,
            score = finalScore,
            level = level,
            magSuitability = magSuitability,
            resolvable = resolvable,
            unresolvable = unresolvable,
            bortle = bortle
        )

        return ObservationQualityBreakdown(
            score = finalScore,
            level = level,
            levelLabel = level.label,
            quality = qualityEnum,
            qualityReason = qualityReason,
            factors = factors,
            visibleFeatures = resolvable,
            unresolvableFeatures = unresolvable,
            recommendation = recommendation,
            dynamicDescription = dynamicDesc,
            limitations = limitations
        )
    }

    private fun generateDynamicSentence(
        targetName: String,
        isPlanet: Boolean,
        planet: Planet?,
        dso: DeepSkyObject?,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double,
        binocularMagnification: Double,
        effectiveMag: Double,
        exitPupil: Double,
        altitudeDeg: Double,
        score: Double,
        level: ObservationQualityLevel,
        magSuitability: MagnificationSuitability,
        resolvable: List<TargetObservableFeature>,
        unresolvable: List<TargetObservableFeature>,
        bortle: BortleScale
    ): String {
        val sb = StringBuilder()

        when (mode) {
            ObservationMode.NAKED_EYE -> {
                sb.append("A olho nu, $targetName ")
                if (isPlanet && planet != null) {
                    when (planet) {
                        Planet.SATURN -> sb.append("aparece como um ponto brilhante e amarelado. Os anéis de Saturno não podem ser resolvidos a olho nu, pois exigem ao menos 30× de aumento óptico.")
                        Planet.JUPITER -> sb.append("brilha intensamente no céu. Suas faixas e luas galileanas exigem auxílio óptico.")
                        Planet.MARS -> sb.append("exibe seu tom avermelhado característico. Detalhes de superfície dependem de telescópio.")
                        Planet.VENUS -> sb.append("desponta como um farol brilhante. Suas fases exigem binóculo ou telescópio.")
                        Planet.MERCURY -> sb.append("é um ponto brilhante e esquivo visível próximo ao horizonte no crepúsculo.")
                        Planet.URANUS -> sb.append("é uma estrela tênue no limite da percepção visual em céus perfeitamente escuros.")
                        Planet.NEPTUNE -> sb.append("é invisível a olho nu, necessitando de instrumentos para localização.")
                    }
                } else if (dso != null) {
                    if (dso.apparentMagnitude <= 4.5 && altitudeDeg > 15.0) {
                        sb.append("pode ser identificado como uma mancha suave e nebulosa sob céus escuros.")
                    } else {
                        sb.append("(magnitude ${String.format("%.1f", dso.apparentMagnitude)}) não é visível a olho nu. Requer binóculo ou telescópio.")
                    }
                }
            }

            ObservationMode.BINOCULAR -> {
                sb.append("Com seu binóculo de ${binocularApertureMm.toInt()} mm (${binocularMagnification.toInt()}×, pupila de saída ${String.format("%.1f", exitPupil)} mm), $targetName ")
                if (resolvable.isNotEmpty()) {
                    sb.append("revela: ")
                    sb.append(resolvable.joinToString("; ") { it.descriptionWhenResolved })
                    sb.append(". ")
                } else {
                    sb.append("aparece como um brilho sutil sem estrutura definida. ")
                }
                if (unresolvable.any { it.visibleTelescope }) {
                    sb.append("Detalhes mais finos de alta resolução exigem a ampliação e abertura de um telescópio.")
                }
            }

            ObservationMode.TELESCOPE -> {
                sb.append("Com seu telescópio de ${equipment.apertureMm.toInt()} mm e ampliação de ${effectiveMag.toInt()}× (pupila de saída ${String.format("%.1f", exitPupil)} mm), ")

                if (isPlanet && planet != null) {
                    sb.append("$targetName ")
                    if (resolvable.isNotEmpty()) {
                        val resolvedNames = resolvable.joinToString(", ") { it.name }
                        if (resolvable.any { it.difficulty == FeatureDifficulty.DEMANDING || it.difficulty == FeatureDifficulty.CHALLENGING }) {
                            sb.append("apresenta potencial para revelar estruturas delicadas, como $resolvedNames. ")
                        } else {
                            sb.append("mostra $resolvedNames. ")
                        }
                    } else {
                        sb.append("aparece como um pequeno disco de luz. ")
                    }

                    if (altitudeDeg < 20.0) {
                        sb.append("A baixa altitude atual (${String.format("%.0f", altitudeDeg)}°) reduz a nitidez e a estabilidade da imagem, dificultando a resolução de detalhes mais finos.")
                    } else if (altitudeDeg > 50.0) {
                        sb.append("A alta altitude (${String.format("%.0f", altitudeDeg)}°) favorece a estabilidade atmosférica e a nitidez nesta observação.")
                    } else {
                        sb.append("A altitude atual de ${String.format("%.0f", altitudeDeg)}° proporciona uma observação equilibrada.")
                    }
                } else if (dso != null) {
                    sb.append("${dso.commonName.ifEmpty { dso.messierNgc }} ")
                    if (resolvable.isNotEmpty()) {
                        sb.append("permite observar: ")
                        sb.append(resolvable.joinToString(" • ") { it.descriptionWhenResolved })
                        sb.append(". ")
                    } else {
                        sb.append("permanece como uma mancha difusa tênue sob a configuração atual. ")
                    }

                    if (bortle.level >= 6 && AstronomicalKnowledgeBase.getProfileForDso(dso).features.any { it.sensitiveToBortle }) {
                        sb.append("A poluição luminosa local atenua o contraste das bordas externas.")
                    } else if (altitudeDeg < 20.0) {
                        sb.append("A baixa altitude reduz o brilho e contraste de fundo.")
                    } else {
                        sb.append("As condições do céu favorecem uma boa visão do objeto.")
                    }
                }
            }
        }

        return sb.toString().trim()
    }

    private fun evaluatePlanet(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double,
        binocularMagnification: Double,
        bortle: BortleScale
    ): ObservationAnalysisResult {
        val angularDiameterArcsec = OpticsEngine.getAngularDiameterArcsec(planet, distanceAU)

        val breakdown = calculateObservationQuality(
            targetName = planet.portugueseName,
            targetId = planet.id,
            isPlanet = true,
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle,
            apparentDiameterArcsec = angularDiameterArcsec,
            magnitude = magnitude
        )

        val effectiveMag = when (mode) {
            ObservationMode.NAKED_EYE -> 1.0
            ObservationMode.BINOCULAR -> binocularMagnification
            ObservationMode.TELESCOPE -> equipment.magnification
        }

        val exitPupil = when (mode) {
            ObservationMode.NAKED_EYE -> 6.0
            ObservationMode.BINOCULAR -> if (binocularMagnification > 0) binocularApertureMm / binocularMagnification else 5.0
            ObservationMode.TELESCOPE -> equipment.exitPupilMm
        }

        val profile = AstronomicalKnowledgeBase.getProfileForPlanet(planet)

        val magSuitability = when (mode) {
            ObservationMode.NAKED_EYE -> MagnificationSuitability.OPTIMAL
            ObservationMode.BINOCULAR -> MagnificationSuitability.OPTIMAL
            ObservationMode.TELESCOPE -> {
                when {
                    effectiveMag < profile.minUsefulMagnification -> MagnificationSuitability.INSUFFICIENT
                    exitPupil < 0.5 || effectiveMag > equipment.maxUsefulMagnification -> MagnificationSuitability.EXCESSIVE
                    effectiveMag > profile.maxOptimalMagnification -> MagnificationSuitability.HIGH
                    else -> MagnificationSuitability.OPTIMAL
                }
            }
        }

        val perceivedArcmin = (angularDiameterArcsec * effectiveMag) / 60.0
        val perceivedSizeText = when {
            perceivedArcmin >= 15.0 -> "Tamanho aparente amplo (${String.format("%.1f", perceivedArcmin)}'), ocupando área marcante no campo visual."
            perceivedArcmin >= 5.0 -> "Tamanho aparente bem definido (${String.format("%.1f", perceivedArcmin)}'), permitindo separar estruturas claramente."
            perceivedArcmin >= 1.5 -> "Tamanho aparente compacto (${String.format("%.1f", perceivedArcmin)}'), exigindo atenção aos detalhes centrados."
            else -> "Aparece muito compacto (${String.format("%.1f", perceivedArcmin)}'), próximo a um ponto estelar."
        }

        val opticalSummary = when (mode) {
            ObservationMode.NAKED_EYE -> "Visão direta (A olho nu)"
            ObservationMode.BINOCULAR -> "Binóculo ${binocularApertureMm.toInt()}×${binocularMagnification.toInt()} • Pupila ${String.format("%.1f", exitPupil)} mm"
            ObservationMode.TELESCOPE -> "${equipment.apertureMm.toInt()}mm / ${equipment.focalLengthMm.toInt()}mm • ${String.format("%.0f", effectiveMag)}× • Pupila ${String.format("%.2f", exitPupil)} mm"
        }

        return ObservationAnalysisResult(
            targetId = planet.id,
            mode = mode,
            level = breakdown.level.ordinal + 1,
            levelLabel = "Nível ${breakdown.level.ordinal + 1} • ${breakdown.level.label}",
            magnificationSuitability = magSuitability,
            perceivedSizeText = perceivedSizeText,
            resolvableFeatures = breakdown.visibleFeatures,
            unresolvableFeatures = breakdown.unresolvableFeatures,
            dynamicDescription = breakdown.dynamicDescription,
            qualityMessage = breakdown.recommendation,
            score = breakdown.score,
            opticalSummary = opticalSummary,
            quality = breakdown.quality,
            qualityBreakdown = breakdown
        )
    }

    private fun evaluateDso(
        dso: DeepSkyObject,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        mode: ObservationMode,
        equipment: TelescopeEquipment,
        binocularApertureMm: Double,
        binocularMagnification: Double,
        bortle: BortleScale,
        lunarInfo: LunarInterferenceInfo?
    ): ObservationAnalysisResult {
        val breakdown = calculateObservationQuality(
            targetName = dso.commonName.ifEmpty { dso.messierNgc },
            targetId = dso.id,
            isPlanet = false,
            dso = dso,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            mode = mode,
            equipment = equipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle,
            lunarInfo = lunarInfo,
            apparentDiameterArcsec = dso.angularSizeArcmin * 60.0,
            magnitude = dso.apparentMagnitude
        )

        val effectiveMag = when (mode) {
            ObservationMode.NAKED_EYE -> 1.0
            ObservationMode.BINOCULAR -> binocularMagnification
            ObservationMode.TELESCOPE -> equipment.magnification
        }

        val exitPupil = when (mode) {
            ObservationMode.NAKED_EYE -> 6.0
            ObservationMode.BINOCULAR -> if (binocularMagnification > 0) binocularApertureMm / binocularMagnification else 5.0
            ObservationMode.TELESCOPE -> equipment.exitPupilMm
        }

        val profile = AstronomicalKnowledgeBase.getProfileForDso(dso)

        val magSuitability = when (mode) {
            ObservationMode.NAKED_EYE -> MagnificationSuitability.OPTIMAL
            ObservationMode.BINOCULAR -> MagnificationSuitability.OPTIMAL
            ObservationMode.TELESCOPE -> {
                if (profile.isWideObject && effectiveMag > 80.0) {
                    MagnificationSuitability.HIGH
                } else if (effectiveMag < profile.minUsefulMagnification) {
                    MagnificationSuitability.INSUFFICIENT
                } else if (exitPupil in profile.idealExitPupilMinMm..profile.idealExitPupilMaxMm) {
                    MagnificationSuitability.OPTIMAL
                } else if (exitPupil < 0.6) {
                    MagnificationSuitability.EXCESSIVE
                } else {
                    MagnificationSuitability.OPTIMAL
                }
            }
        }

        val perceivedSizeText = "Tamanho angular de ${String.format("%.0f", dso.angularSizeArcmin)}' em campo visual de ${String.format("%.1f", equipment.trueFovDeg ?: 1.0)}°."

        val opticalSummary = when (mode) {
            ObservationMode.NAKED_EYE -> "Visão direta (A olho nu)"
            ObservationMode.BINOCULAR -> "Binóculo ${binocularApertureMm.toInt()}×${binocularMagnification.toInt()} • Pupila ${String.format("%.1f", exitPupil)} mm"
            ObservationMode.TELESCOPE -> "${equipment.apertureMm.toInt()}mm / ${equipment.focalLengthMm.toInt()}mm • ${String.format("%.0f", effectiveMag)}× • Pupila ${String.format("%.1f", exitPupil)} mm"
        }

        return ObservationAnalysisResult(
            targetId = dso.id,
            mode = mode,
            level = breakdown.level.ordinal + 1,
            levelLabel = "Nível ${breakdown.level.ordinal + 1} • ${breakdown.level.label}",
            magnificationSuitability = magSuitability,
            perceivedSizeText = perceivedSizeText,
            resolvableFeatures = breakdown.visibleFeatures,
            unresolvableFeatures = breakdown.unresolvableFeatures,
            dynamicDescription = breakdown.dynamicDescription,
            qualityMessage = breakdown.recommendation,
            score = breakdown.score,
            opticalSummary = opticalSummary,
            quality = breakdown.quality,
            qualityBreakdown = breakdown
        )
    }
}
