package com.example.astronomy

import java.util.Calendar

/**
 * Observation mode chosen by the user (Naked eye, Binocular, Telescope).
 */
enum class ObservationMode(
    val title: String,
    val icon: String,
    val buttonLabel: String
) {
    NAKED_EYE("Olho nu", "👁️", "👁️ Olho nu"),
    BINOCULAR("Binóculo", "🔭", "🔭 Binóculo"),
    TELESCOPE("Telescópio", "🔭", "🔭 Telescópio")
}

/**
 * Unified sealed class representing any object observable with a telescope.
 */
sealed class CelestialTarget {
    data class PlanetTarget(val planet: Planet) : CelestialTarget()
    data class DeepSkyTarget(val dso: DeepSkyObject) : CelestialTarget()
    object MoonTarget : CelestialTarget()
    data class MeteorShowerTarget(val shower: MeteorShower) : CelestialTarget()

    val id: String
        get() = when (this) {
            is PlanetTarget -> planet.id
            is DeepSkyTarget -> dso.id
            is MoonTarget -> "moon"
            is MeteorShowerTarget -> shower.id
        }

    val title: String
        get() = when (this) {
            is PlanetTarget -> planet.portugueseName
            is DeepSkyTarget -> dso.commonName.ifEmpty { dso.messierNgc }
            is MoonTarget -> "Lua"
            is MeteorShowerTarget -> shower.portugueseName
        }

    val subtitle: String
        get() = when (this) {
            is PlanetTarget -> "Planeta ${planet.symbol}"
            is DeepSkyTarget -> "${dso.messierNgc} • ${dso.constellation}"
            is MoonTarget -> "Satélite Natural 🌙"
            is MeteorShowerTarget -> "Chuva de Meteoros 🌠"
        }

    val typeLabel: String
        get() = when (this) {
            is PlanetTarget -> "Planeta"
            is DeepSkyTarget -> dso.type.portugueseName
            is MoonTarget -> "Satélite"
            is MeteorShowerTarget -> "Chuva de Meteoros"
        }

    val typeSymbol: String
        get() = when (this) {
            is PlanetTarget -> planet.symbol
            is DeepSkyTarget -> dso.type.symbol
            is MoonTarget -> "🌙"
            is MeteorShowerTarget -> "🌠"
        }
}

data class LunarInterferenceInfo(
    val moonAltitudeDeg: Double,
    val moonAzimuthDeg: Double,
    val moonDirectionLabel: String,
    val moonIlluminationPercent: Int,
    val moonPhaseName: String,
    val isMoonAboveHorizon: Boolean,
    val angularSeparationDeg: Double,
    val interferenceLevelLabel: String,
    val interferenceQualityLabel: String,
    val penaltyScore: Double
)

/**
 * Detailed telescope observation evaluation for a target at a given context.
 */
data class TelescopeTargetEvaluation(
    val target: CelestialTarget,
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val directionLabel: String,
    val heightLabel: String,
    val isAboveHorizon: Boolean,
    val sunAltitudeDeg: Double,
    val quality: ObservationQuality,
    val qualityReason: String = "",
    val qualityMessage: String,
    val score: Double,                      // 0..100
    val opticalSummary: String,            // e.g. "90× • Pupila 1.27 mm • FOV 0.58°"
    val detectionCapability: String,       // e.g. "Estruturas e detalhes estelares visíveis"
    val bestWindowStr: String,             // e.g. "21:30 - 01:15"
    val maxAltitudeWindowDeg: Double,      // max altitude in observation window
    val apertureSuitability: String,       // e.g. "Excelente para abertura de 114 mm"
    val bortleImpactMessage: String,       // e.g. "Bortle 4: céus rurais/suburbanos favorecem nebulosas"
    val lunarInfo: LunarInterferenceInfo? = null,
    val rightAscensionDeg: Double = 0.0,
    val declinationDeg: Double = 0.0,
    val qualityBreakdown: ObservationQualityBreakdown? = null,
    val daytimeSafetyWarning: String? = null,
    val solarProximityInfo: SolarProximityInfo? = null
) {
    val displayStatus: String
        get() = when (quality) {
            ObservationQuality.UNAVAILABLE -> if (qualityReason.isNotEmpty()) "Indisponível — $qualityReason" else "Indisponível"
            ObservationQuality.DIFFICULT -> "Difícil"
            else -> quality.label
        }
}
