package com.example.astronomy

import kotlin.math.max
import kotlin.math.min

/**
 * Type of telescope mount for pointing guidance.
 */
enum class TelescopeMountType(
    val label: String,
    val shortName: String,
    val description: String
) {
    UNIVERSAL("Olho nu / Binóculo", "👁️ Olho nu / Binóculo", "Sem montagem: observe diretamente ou use um binóculo."),
    EQUATORIAL("Equatorial", "🔭 Equatorial", "Montagem com eixo inclinado para acompanhar o movimento do céu."),
    ALT_AZIMUTH("Altazimutal / Dobsoniana", "🔭 Altazimutal", "Movimente o telescópio para os lados e para cima/baixo.")
}

/**
 * User's telescope configuration model.
 */
data class TelescopeEquipment(
    val apertureMm: Double = 114.0,
    val focalLengthMm: Double = 900.0,
    val eyepieceFocalLengthMm: Double = 10.0,
    val eyepieceApparentFovDeg: Double? = 52.0,
    val mountType: TelescopeMountType = TelescopeMountType.UNIVERSAL
) {
    val magnification: Double
        get() = if (eyepieceFocalLengthMm > 0) focalLengthMm / eyepieceFocalLengthMm else 1.0

    val exitPupilMm: Double
        get() = if (magnification > 0) apertureMm / magnification else 0.0

    val focalRatio: Double
        get() = if (apertureMm > 0) focalLengthMm / apertureMm else 0.0

    val dawesLimitArcsec: Double
        get() = if (apertureMm > 0) 116.0 / apertureMm else 1.0

    val maxUsefulMagnification: Double
        get() = apertureMm * 2.0

    val minUsefulMagnification: Double
        get() = max(1.0, apertureMm / 7.0)

    val trueFovDeg: Double?
        get() = eyepieceApparentFovDeg?.let { fov ->
            if (magnification > 0) fov / magnification else null
        }
}

/**
 * Basic optics result container for magnification, exit pupil and FOV.
 */
data class OpticsResult(
    val magnification: Double,
    val exitPupilMm: Double,
    val realFovDeg: Double
)

/**
 * Result of evaluating a planet observation specifically for telescope use.
 */
data class TelescopeObservationEvaluation(
    val planet: Planet,
    val equipment: TelescopeEquipment,
    val magnification: Double,
    val exitPupilMm: Double,
    val trueFovDeg: Double?,
    val planetAngularDiameterArcsec: Double,
    val perceivedAngularSizeMin: Double, // perceived diameter in arcminutes (diameter * magnification / 60)
    val detectionCapability: String,     // e.g., "Disco bem visível com estruturas"
    val quality: ObservationQuality,
    val qualityReason: String = "",
    val qualityMessage: String,
    val score: Double,                  // 0..100 continuous score
    val opticalSummary: String,         // concise optical breakdown
    val qualityBreakdown: ObservationQualityBreakdown? = null,
    val solarProximityInfo: SolarProximityInfo? = null
) {
    val displayStatus: String
        get() = when (quality) {
            ObservationQuality.UNAVAILABLE -> if (qualityReason.isNotEmpty()) "Indisponível — $qualityReason" else "Indisponível"
            ObservationQuality.DIFFICULT -> "Difícil"
            else -> quality.label
        }
}

object OpticsEngine {

    /**
     * Compute basic optical metrics for a given aperture, focal length, eyepiece, and target size.
     */
    fun evaluateOptics(
        apertureMm: Double,
        focalLengthMm: Double,
        eyepieceFocalLengthMm: Double,
        eyepieceApparentFovDeg: Double = 52.0,
        objectAngularDiameterArcsec: Double = 0.0
    ): OpticsResult {
        val mag = if (eyepieceFocalLengthMm > 0) focalLengthMm / eyepieceFocalLengthMm else 1.0
        val exitPupil = if (mag > 0) apertureMm / mag else 0.0
        val fov = if (mag > 0) eyepieceApparentFovDeg / mag else 0.0
        return OpticsResult(magnification = mag, exitPupilMm = exitPupil, realFovDeg = fov)
    }

    /**
     * Compute approximate angular diameter of planet in arcseconds based on geocentric distance in AU.
     */
    fun getAngularDiameterArcsec(planet: Planet, distanceAU: Double): Double {
        val dist = max(0.1, distanceAU)
        return when (planet) {
            Planet.MERCURY -> 6.74 / dist
            Planet.VENUS -> 16.82 / dist
            Planet.MARS -> 9.36 / dist
            Planet.JUPITER -> 196.94 / dist
            Planet.SATURN -> 165.4 / dist  // Ring span included
            Planet.URANUS -> 70.0 / dist   // ~3.6"
            Planet.NEPTUNE -> 62.0 / dist  // ~2.3"
        }
    }

    /**
     * Evaluates planet observation conditions through a telescope.
     * Incorporates:
     * - Telescope aperture & resolution limit (Dawes limit)
     * - Magnification vs optimal range
     * - Exit pupil usability (ideal 1mm - 4mm for planetary)
     * - Planet apparent angular size vs resolved size
     * - Sky darkness & altitude
     */
    fun evaluateTelescopicObservation(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        equipment: TelescopeEquipment,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeObservationEvaluation {
        return ObservationAnalysisEngine.analyzePlanetObservation(
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            magnitude = magnitude,
            distanceAU = distanceAU,
            mode = ObservationMode.TELESCOPE,
            equipment = equipment,
            solarProximityInfo = solarProximityInfo
        )
    }

    /**
     * Mode-aware evaluation function dispatching between Naked Eye, Binocular, and Telescope.
     */
    fun evaluateObservationForMode(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        mode: ObservationMode,
        telescopeEquipment: TelescopeEquipment,
        binocularApertureMm: Double = 50.0,
        binocularMagnification: Double = 10.0,
        bortle: BortleScale = BortleScale.BORTLE_4,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeObservationEvaluation {
        return ObservationAnalysisEngine.analyzePlanetObservation(
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            magnitude = magnitude,
            distanceAU = distanceAU,
            mode = mode,
            equipment = telescopeEquipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortle,
            solarProximityInfo = solarProximityInfo
        )
    }

    fun evaluateNakedEyeObservation(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        bortle: BortleScale,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeObservationEvaluation {
        return ObservationAnalysisEngine.analyzePlanetObservation(
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            magnitude = magnitude,
            distanceAU = distanceAU,
            mode = ObservationMode.NAKED_EYE,
            equipment = TelescopeEquipment(),
            bortle = bortle,
            solarProximityInfo = solarProximityInfo
        )
    }

    fun evaluateBinocularObservation(
        planet: Planet,
        altitudeDeg: Double,
        sunAltitudeDeg: Double,
        magnitude: Double,
        distanceAU: Double,
        apertureMm: Double,
        magnification: Double,
        bortle: BortleScale,
        solarProximityInfo: SolarProximityInfo? = null
    ): TelescopeObservationEvaluation {
        return ObservationAnalysisEngine.analyzePlanetObservation(
            planet = planet,
            altitudeDeg = altitudeDeg,
            sunAltitudeDeg = sunAltitudeDeg,
            magnitude = magnitude,
            distanceAU = distanceAU,
            mode = ObservationMode.BINOCULAR,
            equipment = TelescopeEquipment(),
            binocularApertureMm = apertureMm,
            binocularMagnification = magnification,
            bortle = bortle,
            solarProximityInfo = solarProximityInfo
        )
    }
}
