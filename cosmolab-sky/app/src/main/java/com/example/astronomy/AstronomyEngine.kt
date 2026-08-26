package com.example.astronomy

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Planet enum with physical and orbital characteristics.
 */
enum class Planet(
    val id: String,
    val portugueseName: String,
    val symbol: String,
    val hexColor: String,
    val orbitOrder: Int,
    val description: String
) {
    MERCURY("mercury", "Mercúrio", "☿", "#9E9E9E", 1, "Planeta rochoso mais próximo do Sol. Rápido e desafiador."),
    VENUS("venus", "Vênus", "♀", "#FFD54F", 2, "O objeto mais brilhante do céu noturno depois da Lua. 'Estrela da Alva'."),
    MARS("mars", "Marte", "♂", "#FF7043", 3, "O Planeta Vermelho, conhecido pelo seu tom avermelhado característico."),
    JUPITER("jupiter", "Júpiter", "♃", "#FFB74D", 4, "O gigante gasoso com bandas atmosféricas e a Grande Mancha Vermelha."),
    SATURN("saturn", "Saturno", "♄", "#FFE082", 5, "Famoso por seu espetacular sistema de anéis visível ao telescópio."),
    URANUS("uranus", "Urano", "♅", "#4DD0E1", 6, "Gigante gelado de tom azul-esverdeado suave."),
    NEPTUNE("neptune", "Netuno", "♆", "#5C6BC0", 7, "O planeta mais distante do Sistema Solar, azul profundo.");

    companion object {
        fun fromId(id: String): Planet = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SATURN

        val nakedEyePlanets = listOf(MERCURY, VENUS, MARS, JUPITER, SATURN)
        val telescopicPlanets = entries.toList()
    }
}

/**
 * Quality rating for observation conditions.
 */
enum class ObservationQuality(
    val label: String,
    val emoji: String,
    val colorHex: String,
    val stars: Int
) {
    IDEAL("Ideal", "🔵", "#38BDF8", 5),
    EXCELLENT("Excelente", "🟢", "#4ADE80", 5),
    GOOD("Bom", "🟡", "#FACC15", 4),
    DIFFICULT("Difícil", "🟠", "#FB923C", 2),
    UNAVAILABLE("Indisponível", "🔴", "#F87171", 1)
}

enum class SkyCondition(
    val label: String,
    val icon: String,
    val description: String,
    val isDark: Boolean
) {
    DAYTIME(
        label = "Dia",
        icon = "☀️",
        description = "Céu claro",
        isDark = false
    ),
    CIVIL_TWILIGHT(
        label = "Crepúsculo civil",
        icon = "🌅",
        description = "Crepúsculo civil",
        isDark = false
    ),
    NAUTICAL_TWILIGHT(
        label = "Crepúsculo náutico",
        icon = "🌆",
        description = "Crepúsculo náutico",
        isDark = false
    ),
    ASTRONOMICAL_TWILIGHT(
        label = "Crepúsculo astronômico",
        icon = "🌌",
        description = "Crepúsculo astronômico",
        isDark = true
    ),
    ASTRONOMICAL_NIGHT(
        label = "Noite astronômica",
        icon = "🌙",
        description = "Céu escuro",
        isDark = true
    );

    val displayString: String
        get() = "$icon $label"

    companion object {
        fun fromSunAltitude(sunAltitudeDeg: Double): SkyCondition {
            return when {
                sunAltitudeDeg > 0.0 -> DAYTIME
                sunAltitudeDeg > -6.0 -> CIVIL_TWILIGHT
                sunAltitudeDeg > -12.0 -> NAUTICAL_TWILIGHT
                sunAltitudeDeg > -18.0 -> ASTRONOMICAL_TWILIGHT
                else -> ASTRONOMICAL_NIGHT
            }
        }
    }
}

/**
 * Informações do Alerta de Proximidade Solar (< 60° em relação ao Sol).
 */
data class SolarProximityInfo(
    val angularSeparationDeg: Double,
    val isSunAboveHorizon: Boolean,
    val isWarningActive: Boolean,
    val targetName: String = "O objeto",
    val warningTitle: String = "⚠️ ATENÇÃO: PRÓXIMO AO SOL",
    val formattedSeparation: String = String.format(Locale("pt", "BR"), "%.1f°", angularSeparationDeg),
    val warningMessage: String = "$targetName está a ${String.format(Locale("pt", "BR"), "%.1f°", angularSeparationDeg)} do Sol. O brilho do céu pode dificultar a observação.",
    val safetyGuidance: String = "Nunca aponte um telescópio, binóculo ou instrumento óptico para o Sol ou para uma região próxima ao Sol. A luz solar concentrada pelo instrumento pode causar lesões oculares graves."
) {
    val showAlert: Boolean get() = isWarningActive
}

/**
 * Computed astronomical data for a given planet at a specific moment and location.
 */
data class PlanetObservation(
    val planet: Planet,
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val distanceAU: Double,
    val magnitude: Double,
    val isAboveHorizon: Boolean,
    val directionLabel: String,       // e.g. "Leste", "Sudeste"
    val heightLabel: String,          // e.g. "Alto no céu", "Bem posicionado"
    val quality: ObservationQuality,
    val qualityReason: String = "",
    val qualityMessage: String,
    val sunAltitudeDeg: Double,
    val isSkyDark: Boolean,
    val riseTimeStr: String,          // e.g. "21:24"
    val transitTimeStr: String,       // e.g. "03:18"
    val setTimeStr: String,           // e.g. "09:12"
    val isNextDayRise: Boolean = false,
    val isNextDaySet: Boolean = false,
    val score: Double,                 // numeric score for ranking
    val rightAscensionDeg: Double = 0.0,
    val declinationDeg: Double = 0.0,
    val solarProximityInfo: SolarProximityInfo? = null
) {
    val skyCondition: SkyCondition
        get() = SkyCondition.fromSunAltitude(sunAltitudeDeg)

    val displayStatus: String
        get() = when (quality) {
            ObservationQuality.UNAVAILABLE -> if (qualityReason.isNotEmpty()) "Indisponível — $qualityReason" else "Indisponível"
            ObservationQuality.DIFFICULT -> "Difícil"
            else -> quality.label
        }
}

/**
 * Represents a favorable observation time window.
 */
data class ObservationWindow(
    val planet: Planet,
    val startTimeStr: String,
    val endTimeStr: String,
    val startCal: Calendar,
    val endCal: Calendar,
    val quality: ObservationQuality,
    val description: String,
    val startDirection: String,
    val peakDirection: String,
    val heightLabel: String,
    val maxAltitudeDeg: Double
)

/**
 * Astronomy Engine providing accurate topocentric calculations for the 7 naked-eye/amateur planets.
 */
object AstronomyEngine {

    const val MIN_SAFE_SOLAR_ELONGATION_DEG = 60.0

    /**
     * Calcula a proximidade angular ao Sol para qualquer objeto celeste (RA, Dec).
     * Regra: Alerta ativo apenas quando o Sol estiver acima do horizonte E separação angular < 60°.
     */
    fun calculateSolarProximity(
        targetRaDeg: Double,
        targetDecDeg: Double,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        targetName: String = "O objeto"
    ): SolarProximityInfo {
        val sunPos = calculateSunPosition(calendar, latitude, longitude)
        val angularSeparationDeg = calculateAngularSeparationDeg(
            targetRaDeg, targetDecDeg,
            sunPos.rightAscensionDeg, sunPos.declinationDeg
        )
        val isSunAboveHorizon = sunPos.altitudeDeg > 0.0
        val isWarningActive = isSunAboveHorizon && (angularSeparationDeg < MIN_SAFE_SOLAR_ELONGATION_DEG)
        val formattedSep = String.format(Locale("pt", "BR"), "%.1f°", angularSeparationDeg)

        return SolarProximityInfo(
            angularSeparationDeg = angularSeparationDeg,
            isSunAboveHorizon = isSunAboveHorizon,
            isWarningActive = isWarningActive,
            targetName = targetName,
            warningTitle = "⚠️ ATENÇÃO: PRÓXIMO AO SOL",
            formattedSeparation = formattedSep,
            warningMessage = "$targetName está a $formattedSep do Sol. O brilho do céu pode dificultar a observação.",
            safetyGuidance = "Nunca aponte um telescópio, binóculo ou instrumento óptico para o Sol ou para uma região próxima ao Sol. A luz solar concentrada pelo instrumento pode causar lesões oculares graves."
        )
    }

    /**
     * Calcula a proximidade solar a partir de coordenadas horizontais (Alt, Az).
     */
    fun calculateAltAzAngularSeparationDeg(
        alt1Deg: Double, az1Deg: Double,
        alt2Deg: Double, az2Deg: Double
    ): Double {
        val alt1Rad = Math.toRadians(alt1Deg)
        val alt2Rad = Math.toRadians(alt2Deg)
        val dAzRad = Math.toRadians(az1Deg - az2Deg)
        val cosD = (Math.sin(alt1Rad) * Math.sin(alt2Rad)) + (Math.cos(alt1Rad) * Math.cos(alt2Rad) * Math.cos(dAzRad))
        val clampedCosD = cosD.coerceIn(-1.0, 1.0)
        return Math.toDegrees(Math.acos(clampedCosD))
    }

    /**
     * Calcula a proximidade solar para qualquer objeto com base em Azimute e Altitude.
     */
    fun calculateSolarProximityFromAltAz(
        targetAltDeg: Double,
        targetAzDeg: Double,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        targetName: String = "O objeto"
    ): SolarProximityInfo {
        val sunPos = calculateSunPosition(calendar, latitude, longitude)
        val angularSeparationDeg = calculateAltAzAngularSeparationDeg(
            targetAltDeg, targetAzDeg,
            sunPos.altitudeDeg, sunPos.azimuthDeg
        )
        val isSunAboveHorizon = sunPos.altitudeDeg > 0.0
        val isWarningActive = isSunAboveHorizon && (angularSeparationDeg < MIN_SAFE_SOLAR_ELONGATION_DEG)
        val formattedSep = String.format(Locale("pt", "BR"), "%.1f°", angularSeparationDeg)

        return SolarProximityInfo(
            angularSeparationDeg = angularSeparationDeg,
            isSunAboveHorizon = isSunAboveHorizon,
            isWarningActive = isWarningActive,
            targetName = targetName,
            warningTitle = "⚠️ ATENÇÃO: PRÓXIMO AO SOL",
            formattedSeparation = formattedSep,
            warningMessage = "$targetName está a $formattedSep do Sol. O brilho do céu pode dificultar a observação.",
            safetyGuidance = "Nunca aponte um telescópio, binóculo ou instrumento óptico para o Sol ou para uma região próxima ao Sol. A luz solar concentrada pelo instrumento pode causar lesões oculares graves."
        )
    }

    /**
     * Calcula a proximidade solar para um planeta.
     */
    fun calculateSolarProximityForPlanet(
        planet: Planet,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): SolarProximityInfo {
        val topo = calculatePosition(planet, calendar, latitude, longitude)
        return calculateSolarProximity(
            targetRaDeg = topo.rightAscensionDeg,
            targetDecDeg = topo.declinationDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            targetName = planet.portugueseName
        )
    }

    /**
     * Calcula a proximidade solar para a Lua.
     */
    fun calculateSolarProximityForMoon(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): SolarProximityInfo {
        val moonPos = calculateMoonPosition(calendar, latitude, longitude)
        return calculateSolarProximity(
            targetRaDeg = moonPos.rightAscensionDeg,
            targetDecDeg = moonPos.declinationDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            targetName = "A Lua"
        )
    }

    /**
     * Calculates the angular separation in degrees between the Moon and the Sun
     * for a given calendar date and observer location.
     */
    fun calculateMoonSunAngularSeparationDeg(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): Double {
        val moonPos = calculateMoonPosition(calendar, latitude, longitude)
        val sunPos = calculateSunPosition(calendar, latitude, longitude)
        return calculateAngularSeparationDeg(
            moonPos.rightAscensionDeg, moonPos.declinationDeg,
            sunPos.rightAscensionDeg, sunPos.declinationDeg
        )
    }

    /**
     * Compute Julian Date from Calendar.
     * Converts to UTC first to calculate exact Universal Time Julian Date.
     */
    fun getJulianDate(calendar: Calendar): Double {
        return 2440587.5 + (calendar.timeInMillis.toDouble() / 86400000.0)
    }

    /**
     * Orbital Elements for planets (Keplerian elements relative to J2000.0 epoch).
     * N, i, w, a, e, M
     */
    private data class OrbitalElements(
        val N0: Double, val Nd: Double, // Longitude of ascending node
        val i0: Double, val id: Double, // Inclination
        val w0: Double, val wd: Double, // Argument of perihelion
        val a0: Double, val ad: Double, // Semi-major axis (AU)
        val e0: Double, val ed: Double, // Eccentricity
        val M0: Double, val Md: Double  // Mean anomaly
    )

    private val planetElements = mapOf(
        Planet.MERCURY to OrbitalElements(48.3313, 3.24587E-5, 7.0047, 5.00E-8, 29.1241, 1.01444E-5, 0.387098, 0.0, 0.205635, 5.59E-10, 168.6562, 4.0923344368),
        Planet.VENUS to OrbitalElements(76.6799, 2.46590E-5, 3.3946, 2.75E-8, 54.8910, 1.38374E-5, 0.723330, 0.0, 0.006773, -1.30E-9, 48.0052, 1.6021302244),
        Planet.MARS to OrbitalElements(49.5574, 2.11081E-5, 1.8497, -1.78E-8, 286.5016, 2.92961E-5, 1.523688, 0.0, 0.093405, 2.51E-9, 18.6021, 0.5240207766),
        Planet.JUPITER to OrbitalElements(100.4542, 2.76854E-5, 1.3030, -1.557E-7, 273.8777, 1.64505E-5, 5.20256, 0.0, 0.048498, 4.469E-9, 19.8950, 0.0830853001),
        Planet.SATURN to OrbitalElements(113.6655, 2.38980E-5, 2.4886, -1.081E-7, 339.3939, 2.97661E-5, 9.55475, 0.0, 0.055546, -9.499E-9, 316.9670, 0.0334442282),
        Planet.URANUS to OrbitalElements(74.0005, 1.3978E-5, 0.7733, 1.9E-8, 96.6612, 3.0565E-5, 19.18171, -1.55E-8, 0.047318, 7.45E-9, 142.5905, 0.011725806),
        Planet.NEPTUNE to OrbitalElements(131.7806, 3.0173E-5, 1.7700, -2.55E-7, 273.2782, 8.12E-6, 30.05826, 3.313E-8, 0.008606, 2.15E-9, 260.2471, 0.005995147)
    )

    private fun normalizeDeg(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    private fun rev(deg: Double): Double = normalizeDeg(deg)

    /**
     * Compute heliocentric coordinates (X, Y, Z) in AU for a planet at epoch d = JD - 2451543.5.
     */
    private fun computeHeliocentric(p: Planet, d: Double): Triple<Double, Double, Double> {
        val elem = planetElements[p] ?: return Triple(0.0, 0.0, 0.0)
        val N = rev(elem.N0 + elem.Nd * d)
        val i = Math.toRadians(elem.i0 + elem.id * d)
        val w = rev(elem.w0 + elem.wd * d)
        val a = elem.a0 + elem.ad * d
        val e = elem.e0 + elem.ed * d
        val M = rev(elem.M0 + elem.Md * d)

        // Solve Kepler's equation M = E - e*sin(E)
        var E = Math.toRadians(M)
        for (k in 0..10) {
            E = E - (E - e * sin(E) - Math.toRadians(M)) / (1.0 - e * cos(E))
        }

        val xv = a * (cos(E) - e)
        val yv = a * (sqrt(1.0 - e * e) * sin(E))

        val v = atan2(yv, xv)
        val r = sqrt(xv * xv + yv * yv)

        val nRad = Math.toRadians(N)
        val wRad = Math.toRadians(w)

        val xh = r * (cos(nRad) * cos(v + wRad) - sin(nRad) * sin(v + wRad) * cos(i))
        val yh = r * (sin(nRad) * cos(v + wRad) + cos(nRad) * sin(v + wRad) * cos(i))
        val zh = r * (sin(v + wRad) * sin(i))

        return Triple(xh, yh, zh)
    }

    /**
     * Compute Earth's heliocentric position at epoch d.
     */
    private fun computeEarthHeliocentric(d: Double): Triple<Double, Double, Double> {
        val w = rev(282.9404 + 4.70935E-5 * d)
        val e = 0.016709 - 1.151E-9 * d
        val M = rev(356.0470 + 0.9856002585 * d)
        var E = Math.toRadians(M)
        for (k in 0..10) {
            E = E - (E - e * sin(E) - Math.toRadians(M)) / (1.0 - e * cos(E))
        }
        val xv = cos(E) - e
        val yv = sqrt(1.0 - e * e) * sin(E)
        val r = sqrt(xv * xv + yv * yv)
        val v = atan2(yv, xv)
        val lon = rev(v * 180.0 / Math.PI + w)

        val lonRad = Math.toRadians(lon)
        return Triple(-r * cos(lonRad), -r * sin(lonRad), 0.0)
    }

    /**
     * Calculate Topocentric Horizontal Coordinates (Altitude, Azimuth, Distance, Sun Altitude).
     */
    fun calculatePosition(
        planet: Planet,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): TopocentricResult {
        val jd = getJulianDate(calendar)
        val d = jd - 2451543.5 // Days since 2000 Jan 0.0 UT (for orbital elements)
        val dJ2000 = jd - 2451545.0 // Days since J2000.0 epoch (for GMST)

        val (xe, ye, ze) = computeEarthHeliocentric(d)
        val (xp, yp, zp) = computeHeliocentric(planet, d)

        // Geocentric ecliptic vector
        val xg = xp - xe
        val yg = yp - ye
        val zg = zp - ze

        val dist = sqrt(xg * xg + yg * yg + zg * zg)

        // Obliquity of ecliptic
        val ecl = Math.toRadians(23.4393 - 3.563E-7 * d)

        // Convert ecliptic to equatorial
        val xeq = xg
        val yeq = yg * cos(ecl) - zg * sin(ecl)
        val zeq = yg * sin(ecl) + zg * cos(ecl)

        val ra = rev(Math.toDegrees(atan2(yeq, xeq))) // Right Ascension in degrees
        val dec = Math.toDegrees(atan2(zeq, sqrt(xeq * xeq + yeq * yeq))) // Declination in degrees

        // Local Sidereal Time (LST)
        val gmst0 = rev(280.46061837 + 360.98564736629 * dJ2000)
        val lst = rev(gmst0 + longitude)

        // Hour Angle H = LST - RA
        val H = rev(lst - ra)
        val hRad = Math.toRadians(H)
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(latitude)

        // Convert Hour Angle & Declination to Altitude & Azimuth
        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(hRad)
        val altRad = asin(sinAlt.coerceIn(-1.0, 1.0))
        val altDeg = Math.toDegrees(altRad)

        val yAz = -sin(hRad)
        val xAz = tan(decRad) * cos(latRad) - sin(latRad) * cos(hRad)
        var azDeg = rev(Math.toDegrees(atan2(yAz, xAz)))

        // Calculate Sun Altitude
        val sunAltDeg = calculateSunAltitude(d, calendar, latitude, longitude)

        // Calculate Visual Magnitude
        val mag = calculateMagnitude(planet, dist, d)

        return TopocentricResult(
            altitudeDeg = altDeg,
            azimuthDeg = azDeg,
            distanceAU = dist,
            magnitude = mag,
            sunAltitudeDeg = sunAltDeg,
            rightAscensionDeg = ra,
            declinationDeg = dec
        )
    }

    private fun calculateSunAltitude(d: Double, calendar: Calendar, latitude: Double, longitude: Double): Double {
        val (xe, ye, ze) = computeEarthHeliocentric(d)
        val xg = -xe
        val yg = -ye
        val zg = -ze

        val ecl = Math.toRadians(23.4393 - 3.563E-7 * d)

        val xeq = xg
        val yeq = yg * cos(ecl) - zg * sin(ecl)
        val zeq = yg * sin(ecl) + zg * cos(ecl)

        val ra = rev(Math.toDegrees(atan2(yeq, xeq)))
        val dec = Math.toDegrees(atan2(zeq, sqrt(xeq * xeq + yeq * yeq)))

        val jd = getJulianDate(calendar)
        val dJ2000 = jd - 2451545.0
        val gmst0 = rev(280.46061837 + 360.98564736629 * dJ2000)
        val lst = rev(gmst0 + longitude)

        val H = rev(lst - ra)
        val hRad = Math.toRadians(H)
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(latitude)

        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(hRad)
        return Math.toDegrees(asin(sinAlt.coerceIn(-1.0, 1.0)))
    }

    private fun calculateMagnitude(planet: Planet, distAU: Double, d: Double): Double {
        return when (planet) {
            Planet.MERCURY -> -0.42 + 5 * Math.log10(distAU)
            Planet.VENUS -> -4.40 + 5 * Math.log10(distAU)
            Planet.MARS -> -1.52 + 5 * Math.log10(distAU)
            Planet.JUPITER -> -2.5 + 5 * Math.log10(distAU)
            Planet.SATURN -> 0.4 + 5 * Math.log10(distAU)
            Planet.URANUS -> 5.7 + 5 * Math.log10(distAU)
            Planet.NEPTUNE -> 7.8 + 5 * Math.log10(distAU)
        }
    }

    /**
     * Full Observation Analysis for a Planet at a specific date/time and location.
     */
    fun analyzeObservation(
        planet: Planet,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): PlanetObservation {
        val topo = calculatePosition(planet, calendar, latitude, longitude)

        val isAbove = topo.altitudeDeg > 0.0
        val isDark = topo.sunAltitudeDeg < -18.0

        val dirLabel = convertAzimuthToDirection(topo.azimuthDeg)
        val heightLabel = convertAltitudeToHeightLabel(topo.altitudeDeg)

        // Evaluate observation quality & score
        val eval = evaluateQuality(
            altDeg = topo.altitudeDeg,
            sunAltDeg = topo.sunAltitudeDeg,
            mag = topo.magnitude,
            planet = planet
        )

        // Calculate Rise, Transit, Set times for the target day
        val times = calculateDailyTimes(planet, calendar, latitude, longitude)

        // Calculate Solar Proximity Alert (< 60° when Sun is above horizon)
        val solarProximity = calculateSolarProximity(
            targetRaDeg = topo.rightAscensionDeg,
            targetDecDeg = topo.declinationDeg,
            calendar = calendar,
            latitude = latitude,
            longitude = longitude,
            targetName = planet.portugueseName
        )

        return PlanetObservation(
            planet = planet,
            altitudeDeg = topo.altitudeDeg,
            azimuthDeg = topo.azimuthDeg,
            distanceAU = topo.distanceAU,
            magnitude = topo.magnitude,
            isAboveHorizon = isAbove,
            directionLabel = dirLabel,
            heightLabel = heightLabel,
            quality = eval.quality,
            qualityReason = eval.reason,
            qualityMessage = eval.message,
            sunAltitudeDeg = topo.sunAltitudeDeg,
            isSkyDark = isDark,
            riseTimeStr = times.riseStr,
            transitTimeStr = times.transitStr,
            setTimeStr = times.setStr,
            isNextDayRise = times.isNextDayRise,
            isNextDaySet = times.isNextDaySet,
            score = eval.score,
            rightAscensionDeg = topo.rightAscensionDeg,
            declinationDeg = topo.declinationDeg,
            solarProximityInfo = solarProximity
        )
    }

    private data class PlanetEvalResult(
        val quality: ObservationQuality,
        val reason: String,
        val message: String,
        val score: Double
    )

    private fun evaluateQuality(
        altDeg: Double,
        sunAltDeg: Double,
        mag: Double,
        planet: Planet
    ): PlanetEvalResult {
        if (altDeg <= 0.0) {
            return PlanetEvalResult(
                quality = ObservationQuality.UNAVAILABLE,
                reason = "Abaixo do horizonte",
                message = "${planet.portugueseName} está abaixo do horizonte.",
                score = 0.0
            )
        }

        val isVenusDaytime = (planet == Planet.VENUS && mag < -3.5 && altDeg > 15.0)
        if (sunAltDeg > -6.0 && !isVenusDaytime) {
            return PlanetEvalResult(
                quality = ObservationQuality.UNAVAILABLE,
                reason = "Céu claro",
                message = "Observação não recomendada durante o dia ou crepúsculo claro.",
                score = 0.0
            )
        }

        // 1. Altitude Score (0..35 points)
        val altitudeScore = when {
            altDeg <= 0.0 -> 0.0
            altDeg < 20.0 -> 35.0 * Math.pow(altDeg / 20.0, 0.8)
            altDeg <= 60.0 -> 25.0 + 10.0 * ((altDeg - 20.0) / 40.0)
            else -> 35.0
        }

        // 2. Darkness Score (0..30 points)
        val darknessScore = when {
            sunAltDeg <= -12.0 -> 30.0
            sunAltDeg <= -6.0 -> 20.0 + 10.0 * ((-6.0 - sunAltDeg) / 6.0)
            sunAltDeg <= 0.0 -> 5.0 + 15.0 * ((0.0 - sunAltDeg) / 6.0)
            else -> if (isVenusDaytime) 12.0 else 1.0
        }

        // 3. Magnitude Score (0..20 points)
        val magnitudeScore = when {
            mag <= -4.0 -> 20.0
            mag >= 3.0 -> 1.0
            else -> (20.0 * (3.0 - mag) / 7.0).coerceIn(1.0, 20.0)
        }

        // 4. Position & Elevation Proximity Score (0..15 points)
        val windowScore = (15.0 * (altDeg / 90.0)).coerceIn(1.0, 15.0)

        val totalScore = (altitudeScore + darknessScore + magnitudeScore + windowScore).coerceIn(0.0, 100.0)

        val quality = when {
            sunAltDeg > -6.0 && isVenusDaytime -> ObservationQuality.DIFFICULT
            sunAltDeg > -12.0 -> if (totalScore >= 55.0) ObservationQuality.GOOD else ObservationQuality.DIFFICULT
            totalScore >= 80.0 -> ObservationQuality.IDEAL
            totalScore >= 65.0 -> ObservationQuality.EXCELLENT
            totalScore >= 45.0 -> ObservationQuality.GOOD
            else -> ObservationQuality.DIFFICULT
        }

        val reason = when {
            quality != ObservationQuality.DIFFICULT -> ""
            sunAltDeg > 0.0 -> "Céu claro"
            sunAltDeg > -12.0 -> "Crepúsculo"
            altDeg < 15.0 -> "Altitude baixa"
            else -> "Condições desfavoráveis"
        }

        val msg = when {
            isVenusDaytime -> "Visível de dia perto da maior elongação se o céu estiver limpo."
            quality == ObservationQuality.IDEAL -> "Condições ideais! Ótima altura e céu escuro."
            quality == ObservationQuality.EXCELLENT -> "Excelente momento! Bem posicionado no céu escuro."
            quality == ObservationQuality.GOOD -> "Boas condições para observar no horizonte atual."
            quality == ObservationQuality.DIFFICULT -> "Baixo no horizonte ou próximo do crepúsculo."
            else -> "Indisponível no momento."
        }

        return PlanetEvalResult(quality, reason, msg, totalScore)
    }

    fun convertAzimuthToDirection(azDeg: Double): String {
        val norm = normalizeDeg(azDeg)
        return when {
            norm >= 337.5 || norm < 22.5 -> "Norte"
            norm >= 22.5 && norm < 67.5 -> "Nordeste"
            norm >= 67.5 && norm < 112.5 -> "Leste"
            norm >= 112.5 && norm < 157.5 -> "Sudeste"
            norm >= 157.5 && norm < 202.5 -> "Sul"
            norm >= 202.5 && norm < 247.5 -> "Sudoeste"
            norm >= 247.5 && norm < 292.5 -> "Oeste"
            else -> "Noroeste"
        }
    }

    fun convertAltitudeToHeightLabel(altDeg: Double): String {
        return when {
            altDeg < 0.0 -> "Abaixo do horizonte"
            altDeg < 12.0 -> "Próximo do horizonte"
            altDeg < 28.0 -> "Baixo"
            altDeg < 48.0 -> "Bem posicionado"
            altDeg < 70.0 -> "Alto no céu"
            else -> "Muito alto"
        }
    }

    data class DailyTimesResult(
        val riseStr: String,
        val transitStr: String,
        val setStr: String,
        val isNextDayRise: Boolean,
        val isNextDaySet: Boolean
    )

    fun calculateDailyTimes(
        planet: Planet,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): DailyTimesResult {
        val testCal = calendar.clone() as Calendar
        testCal.set(Calendar.HOUR_OF_DAY, 0)
        testCal.set(Calendar.MINUTE, 0)
        testCal.set(Calendar.SECOND, 0)

        var prevAlt = calculatePosition(planet, testCal, latitude, longitude).altitudeDeg
        var riseMin = -1
        var setMin = -1
        var maxAlt = -90.0
        var transitMin = -1

        // Sample 24 hours at 10-min resolution (144 points)
        for (m in 0 until 1440 step 10) {
            testCal.set(Calendar.HOUR_OF_DAY, m / 60)
            testCal.set(Calendar.MINUTE, m % 60)
            val currAlt = calculatePosition(planet, testCal, latitude, longitude).altitudeDeg

            if (currAlt > maxAlt) {
                maxAlt = currAlt
                transitMin = m
            }

            if (prevAlt <= 0.0 && currAlt > 0.0 && riseMin == -1) {
                riseMin = m
            } else if (prevAlt > 0.0 && currAlt <= 0.0 && setMin == -1) {
                setMin = m
            }
            prevAlt = currAlt
        }

        fun formatMin(min: Int): String {
            if (min < 0) return "--:--"
            val h = min / 60
            val m = min % 60
            return String.format("%02d:%02d", h, m)
        }

        val isNextRise = riseMin > setMin && setMin != -1
        val isNextSet = setMin < riseMin && riseMin != -1

        return DailyTimesResult(
            riseStr = if (riseMin != -1) formatMin(riseMin) else "Sem nascer hoje",
            transitStr = if (transitMin != -1) formatMin(transitMin) else "--:--",
            setStr = if (setMin != -1) formatMin(setMin) else "Sem ocaso hoje",
            isNextDayRise = isNextRise,
            isNextDaySet = isNextSet
        )
    }

    data class SunTimesResult(
        val riseStr: String,
        val setStr: String
    )

    fun calculateSunTimes(calendar: Calendar, latitude: Double, longitude: Double): SunTimesResult {
        val testCal = calendar.clone() as Calendar
        testCal.set(Calendar.HOUR_OF_DAY, 0)
        testCal.set(Calendar.MINUTE, 0)
        testCal.set(Calendar.SECOND, 0)

        var jd = getJulianDate(testCal)
        var d = jd - 2451543.5
        var prevAlt = calculateSunAltitude(d, testCal, latitude, longitude)

        var riseMin = -1
        var setMin = -1

        for (m in 0 until 1440 step 5) {
            testCal.set(Calendar.HOUR_OF_DAY, m / 60)
            testCal.set(Calendar.MINUTE, m % 60)
            jd = getJulianDate(testCal)
            d = jd - 2451543.5
            val currAlt = calculateSunAltitude(d, testCal, latitude, longitude)

            if (prevAlt <= 0.0 && currAlt > 0.0 && riseMin == -1) {
                riseMin = m
            } else if (prevAlt > 0.0 && currAlt <= 0.0 && setMin == -1) {
                setMin = m
            }
            prevAlt = currAlt
        }

        fun formatMin(min: Int): String {
            if (min < 0) return "--:--"
            val h = min / 60
            val m = min % 60
            return String.format("%02d:%02d", h, m)
        }

        return SunTimesResult(
            riseStr = formatMin(riseMin),
            setStr = formatMin(setMin)
        )
    }

    /**
     * Function "Encontrar melhor horário":
     * Scans 24-36h observation window to find optimal observation intervals.
     */
    fun findBestObservationWindows(
        planet: Planet,
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        minDesiredHeightLabel: String, // e.g. "Próximo do horizonte", "Baixo", "Bem posicionado", "Alto no céu", "O mais alto possível"
        darkSkyOnly: Boolean
    ): List<ObservationWindow> {
        val minAltThreshold = when (minDesiredHeightLabel) {
            "Próximo do horizonte" -> 5.0
            "Baixo" -> 15.0
            "Bem posicionado" -> 25.0
            "Alto no céu" -> 40.0
            "O mais alto possível" -> 55.0
            else -> 15.0
        }

        val scanCal = calendar.clone() as Calendar
        scanCal.set(Calendar.HOUR_OF_DAY, 12)
        scanCal.set(Calendar.MINUTE, 0)
        scanCal.set(Calendar.SECOND, 0)

        val samples = mutableListOf<SamplePointForWindow>()
        // 24 hours at 15-min intervals (96 points)
        for (i in 0 until 96) {
            val pointCal = scanCal.clone() as Calendar
            pointCal.add(Calendar.MINUTE, i * 15)

            val pos = calculatePosition(planet, pointCal, latitude, longitude)
            val isValidSky = if (darkSkyOnly) pos.sunAltitudeDeg < -18.0 else pos.sunAltitudeDeg < 0.0
            val isValidAlt = pos.altitudeDeg >= minAltThreshold

            samples.add(
                SamplePointForWindow(
                    cal = pointCal,
                    alt = pos.altitudeDeg,
                    az = pos.azimuthDeg,
                    sunAlt = pos.sunAltitudeDeg,
                    isValid = isValidSky && isValidAlt,
                    dir = convertAzimuthToDirection(pos.azimuthDeg)
                )
            )
        }

        val windows = mutableListOf<ObservationWindow>()
        var currentWindowStart: SamplePointForWindow? = null
        var currentWindowPoints = mutableListOf<SamplePointForWindow>()

        for (sample in samples) {
            if (sample.isValid) {
                if (currentWindowStart == null) {
                    currentWindowStart = sample
                }
                currentWindowPoints.add(sample)
            } else {
                if (currentWindowStart != null && currentWindowPoints.size >= 2) {
                    windows.add(createWindowFromPoints(planet, currentWindowPoints))
                }
                currentWindowStart = null
                currentWindowPoints = mutableListOf()
            }
        }
        if (currentWindowStart != null && currentWindowPoints.size >= 2) {
            windows.add(createWindowFromPoints(planet, currentWindowPoints))
        }

        if (windows.isEmpty()) {
            // Fallback: filter to valid sky samples where planet is above horizon
            val validSkySamples = samples.filter { sample ->
                val validSky = if (darkSkyOnly) sample.sunAlt < -18.0 else sample.sunAlt < 0.0
                validSky && sample.alt > 0.0
            }
            if (validSkySamples.isNotEmpty()) {
                val bestPoint = validSkySamples.maxByOrNull { it.alt }
                if (bestPoint != null) {
                    val bestIndex = validSkySamples.indexOf(bestPoint)
                    var startIndex = bestIndex
                    var endIndex = bestIndex
                    while (startIndex > 0 && Math.abs(validSkySamples[startIndex - 1].cal.timeInMillis - validSkySamples[startIndex].cal.timeInMillis) <= 20 * 60 * 1000) {
                        startIndex--
                    }
                    while (endIndex < validSkySamples.size - 1 && Math.abs(validSkySamples[endIndex + 1].cal.timeInMillis - validSkySamples[endIndex].cal.timeInMillis) <= 20 * 60 * 1000) {
                        endIndex++
                    }
                    val windowPoints = validSkySamples.subList(startIndex, endIndex + 1)
                    if (windowPoints.isNotEmpty()) {
                        windows.add(createWindowFromPoints(planet, windowPoints))
                    }
                }
            }
        }

        return windows
    }

    private fun createWindowFromPoints(
        planet: Planet,
        points: List<SamplePointForWindow>
    ): ObservationWindow {
        val samplePoints = points

        val startPoint = samplePoints.first()
        val endPoint = samplePoints.last()
        val maxPoint = samplePoints.maxByOrNull { it.alt } ?: startPoint

        val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).apply {
            timeZone = startPoint.cal.timeZone
        }

        val quality = when {
            maxPoint.sunAlt >= 0.0 -> ObservationQuality.UNAVAILABLE
            maxPoint.sunAlt >= -6.0 -> ObservationQuality.DIFFICULT
            maxPoint.sunAlt >= -12.0 -> if (maxPoint.alt > 30.0) ObservationQuality.GOOD else ObservationQuality.DIFFICULT
            maxPoint.sunAlt >= -18.0 -> if (maxPoint.alt > 25.0) ObservationQuality.GOOD else ObservationQuality.DIFFICULT
            else -> when {
                maxPoint.alt > 45.0 -> ObservationQuality.EXCELLENT
                maxPoint.alt > 20.0 -> ObservationQuality.GOOD
                else -> ObservationQuality.DIFFICULT
            }
        }

        val heightLabel = convertAltitudeToHeightLabel(maxPoint.alt)

        return ObservationWindow(
            planet = planet,
            startTimeStr = fmt.format(startPoint.cal.time),
            endTimeStr = fmt.format(endPoint.cal.time),
            startCal = startPoint.cal,
            endCal = endPoint.cal,
            quality = quality,
            description = "${planet.portugueseName} estará $heightLabel durante grande parte desse período.",
            startDirection = startPoint.dir,
            peakDirection = maxPoint.dir,
            heightLabel = heightLabel,
            maxAltitudeDeg = maxPoint.alt
        )
    }

    /**
     * Calculate Topocentric Horizontal Coordinates for fixed Equatorial Coordinates (RA, Dec).
     */
    fun calculateEquatorialPosition(
        raDeg: Double,
        decDeg: Double,
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): EquatorialResult {
        val jd = getJulianDate(calendar)
        val d = jd - 2451543.5 // Days since J2000.0
        val dJ2000 = jd - 2451545.0 // Days since J2000.0 epoch (for GMST)

        // Local Sidereal Time (LST)
        val gmst0 = rev(280.46061837 + 360.98564736629 * dJ2000)
        val lst = rev(gmst0 + longitude)

        // Hour Angle H = LST - RA
        val H = rev(lst - raDeg)
        val hRad = Math.toRadians(H)
        val decRad = Math.toRadians(decDeg)
        val latRad = Math.toRadians(latitude)

        // Convert Hour Angle & Declination to Altitude & Azimuth
        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(hRad)
        val altRad = asin(sinAlt.coerceIn(-1.0, 1.0))
        val altDeg = Math.toDegrees(altRad)

        val yAz = -sin(hRad)
        val xAz = tan(decRad) * cos(latRad) - sin(latRad) * cos(hRad)
        val azDeg = rev(Math.toDegrees(atan2(yAz, xAz)))

        // Calculate Sun Altitude
        val sunAltDeg = calculateSunAltitude(d, calendar, latitude, longitude)

        return EquatorialResult(
            altitudeDeg = altDeg,
            azimuthDeg = azDeg,
            sunAltitudeDeg = sunAltDeg
        )
    }

    fun calculateSunPosition(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): TopocentricResult {
        val jd = getJulianDate(calendar)
        val d = jd - 2451543.5
        val sunAlt = calculateSunAltitude(d, calendar, latitude, longitude)
        val (xe, ye, ze) = computeEarthHeliocentric(d)
        val xg = -xe
        val yg = -ye
        val zg = -ze
        val dist = sqrt(xg * xg + yg * yg + zg * zg)
        val ecl = Math.toRadians(23.4393 - 3.563E-7 * d)
        val xeq = xg
        val yeq = yg * cos(ecl) - zg * sin(ecl)
        val zeq = yg * sin(ecl) + zg * cos(ecl)
        val ra = rev(Math.toDegrees(atan2(yeq, xeq)))
        val dec = Math.toDegrees(atan2(zeq, sqrt(xeq * xeq + yeq * yeq)))
        val dJ2000 = jd - 2451545.0
        val gmst0 = rev(280.46061837 + 360.98564736629 * dJ2000)
        val lst = rev(gmst0 + longitude)
        val H = rev(lst - ra)
        val hRad = Math.toRadians(H)
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(latitude)
        val yAz = -sin(hRad)
        val xAz = tan(decRad) * cos(latRad) - sin(latRad) * cos(hRad)
        val azDeg = rev(Math.toDegrees(atan2(yAz, xAz)))

        return TopocentricResult(
            altitudeDeg = sunAlt,
            azimuthDeg = azDeg,
            distanceAU = dist,
            magnitude = -26.7,
            rightAscensionDeg = ra,
            declinationDeg = dec,
            sunAltitudeDeg = sunAlt
        )
    }

    fun calculateMoonPosition(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): MoonPositionResult {
        val jd = getJulianDate(calendar)
        val d = jd - 2451543.5 // Days since 2000 Jan 0.0
        val dJ2000 = jd - 2451545.0 // Days since J2000.0 epoch

        // Moon's orbital elements
        val N = rev(125.1228 - 0.0529538083 * d)
        val i = 5.1454
        val w = rev(318.0634 + 0.1643573223 * d)
        val a = 60.2666
        val e = 0.054900
        val M = rev(115.3654 + 13.0649929509 * d)

        var E = Math.toRadians(M)
        for (k in 0..10) {
            E = E - (E - e * sin(E) - Math.toRadians(M)) / (1.0 - e * cos(E))
        }

        val x = a * (cos(E) - e)
        val y = a * sqrt(1.0 - e * e) * sin(E)

        val r = sqrt(x * x + y * y)
        val v = atan2(y, x)

        val nRad = Math.toRadians(N)
        val wRad = Math.toRadians(w)
        val iRad = Math.toRadians(i)

        val xeclip = r * (cos(nRad) * cos(v + wRad) - sin(nRad) * sin(v + wRad) * cos(iRad))
        val yeclip = r * (sin(nRad) * cos(v + wRad) + cos(nRad) * sin(v + wRad) * cos(iRad))
        val zeclip = r * (sin(v + wRad) * sin(iRad))

        val lonMoon = rev(Math.toDegrees(atan2(yeclip, xeclip)))

        // Obliquity of ecliptic
        val ecl = Math.toRadians(23.4393 - 3.563E-7 * d)

        // Convert ecliptic to equatorial
        val xeq = xeclip
        val yeq = yeclip * cos(ecl) - zeclip * sin(ecl)
        val zeq = yeclip * sin(ecl) + zeclip * cos(ecl)

        val ra = rev(Math.toDegrees(atan2(yeq, xeq)))
        val dec = Math.toDegrees(atan2(zeq, sqrt(xeq * xeq + yeq * yeq)))

        // Local Sidereal Time
        val gmst0 = rev(280.46061837 + 360.98564736629 * dJ2000)
        val lst = rev(gmst0 + longitude)

        // Hour Angle H = LST - RA
        val H = rev(lst - ra)
        val hRad = Math.toRadians(H)
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(latitude)

        // Altitude & Azimuth
        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(hRad)
        val altRad = asin(sinAlt.coerceIn(-1.0, 1.0))
        val altDeg = Math.toDegrees(altRad)

        val yAz = -sin(hRad)
        val xAz = tan(decRad) * cos(latRad) - sin(latRad) * cos(hRad)
        val azDeg = rev(Math.toDegrees(atan2(yAz, xAz)))

        // Sun position for phase calculation
        val (xe, ye, ze) = computeEarthHeliocentric(d)
        val lonSun = rev(Math.toDegrees(atan2(-ye, -xe)))

        val phaseAngle = rev(lonMoon - lonSun)
        val illumFrac = (1.0 - cos(Math.toRadians(phaseAngle))) / 2.0
        val illumPercent = (illumFrac * 100.0).toInt().coerceIn(0, 100)

        val phaseName = when {
            illumPercent < 5 -> "Nova"
            phaseAngle < 85 -> "Crescente"
            phaseAngle < 95 -> "Quarto Crescente"
            phaseAngle < 175 -> "Gibosa Crescente"
            phaseAngle < 185 -> "Cheia"
            phaseAngle < 265 -> "Gibosa Minguante"
            phaseAngle < 275 -> "Quarto Minguante"
            else -> "Minguante"
        }

        val sunAltDeg = calculateSunAltitude(d, calendar, latitude, longitude)

        return MoonPositionResult(
            altitudeDeg = altDeg,
            azimuthDeg = azDeg,
            illuminationPercent = illumPercent,
            phaseName = phaseName,
            sunAltitudeDeg = sunAltDeg,
            rightAscensionDeg = ra,
            declinationDeg = dec
        )
    }

    fun calculateMoonTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): MoonTimesResult {
        val testCal = calendar.clone() as Calendar
        testCal.set(Calendar.HOUR_OF_DAY, 0)
        testCal.set(Calendar.MINUTE, 0)
        testCal.set(Calendar.SECOND, 0)

        var prevAlt = calculateMoonPosition(testCal, latitude, longitude).altitudeDeg
        var riseMin = -1
        var setMin = -1
        var riseAz = 0.0
        var setAz = 0.0
        var maxAlt = -90.0
        var transitMin = -1
        var transitAz = 0.0
        var visibleMinutes = 0

        for (m in 0 until 1440 step 5) {
            testCal.set(Calendar.HOUR_OF_DAY, m / 60)
            testCal.set(Calendar.MINUTE, m % 60)
            val pos = calculateMoonPosition(testCal, latitude, longitude)
            val currAlt = pos.altitudeDeg

            if (currAlt > 0.0) {
                visibleMinutes += 5
            }

            if (currAlt > maxAlt) {
                maxAlt = currAlt
                transitMin = m
                transitAz = pos.azimuthDeg
            }

            if (prevAlt <= 0.0 && currAlt > 0.0 && riseMin == -1) {
                riseMin = m
                riseAz = pos.azimuthDeg
            } else if (prevAlt > 0.0 && currAlt <= 0.0 && setMin == -1) {
                setMin = m
                setAz = pos.azimuthDeg
            }
            prevAlt = currAlt
        }

        fun formatMin(min: Int): String {
            if (min < 0) return "--:--"
            val h = min / 60
            val m = min % 60
            return String.format("%02d:%02d", h, m)
        }

        val visDurationStr = if (visibleMinutes > 0) {
            val vh = visibleMinutes / 60
            val vm = visibleMinutes % 60
            "${String.format("%02d", vh)}h ${String.format("%02d", vm)}m"
        } else {
            "00h 00m"
        }

        return MoonTimesResult(
            riseStr = if (riseMin != -1) formatMin(riseMin) else "Sem nascer",
            setStr = if (setMin != -1) formatMin(setMin) else "Sem ocaso",
            riseAzimuthDeg = riseAz,
            riseDirectionLabel = convertAzimuthToDirection(riseAz),
            transitStr = if (transitMin != -1 && maxAlt > 0.0) formatMin(transitMin) else "--:--",
            transitAltitudeDeg = if (maxAlt > 0.0) maxAlt else 0.0,
            transitDirectionLabel = convertAzimuthToDirection(transitAz),
            setAzimuthDeg = setAz,
            setDirectionLabel = convertAzimuthToDirection(setAz),
            visibilityDurationStr = visDurationStr
        )
    }

    fun analyzeMoonObservation(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): MoonObservation {
        val pos = calculateMoonPosition(calendar, latitude, longitude)
        val times = calculateMoonTimes(calendar, latitude, longitude)
        val isAbove = pos.altitudeDeg > 0.0
        val isDaytime = pos.sunAltitudeDeg > -2.0
        val isSkyDark = pos.sunAltitudeDeg < -18.0

        val angularSeparationDeg = calculateMoonSunAngularSeparationDeg(calendar, latitude, longitude)
        val solarProximity = calculateSolarProximityForMoon(calendar, latitude, longitude)

        // Age of moon in days (approx based on phase angle)
        val jd = getJulianDate(calendar)
        val d = jd - 2451543.5
        val (xe, ye, _) = computeEarthHeliocentric(d)
        val lonSun = rev(Math.toDegrees(atan2(-ye, -xe)))
        // Moon lon
        val N = rev(125.1228 - 0.0529538083 * d)
        val w = rev(318.0634 + 0.1643573223 * d)
        val M = rev(115.3654 + 13.0649929509 * d)
        val a = 60.2666
        val e = 0.054900
        var E = Math.toRadians(M)
        for (k in 0..10) {
            E = E - (E - e * sin(E) - Math.toRadians(M)) / (1.0 - e * cos(E))
        }
        val x = a * (cos(E) - e)
        val y = a * sqrt(1.0 - e * e) * sin(E)
        val r = sqrt(x * x + y * y)
        val v = atan2(y, x)
        val nRad = Math.toRadians(N)
        val wRad = Math.toRadians(w)
        val iRad = Math.toRadians(5.1454)
        val xeclip = r * (cos(nRad) * cos(v + wRad) - sin(nRad) * sin(v + wRad) * cos(iRad))
        val yeclip = r * (sin(nRad) * cos(v + wRad) + cos(nRad) * sin(v + wRad) * cos(iRad))
        val lonMoon = rev(Math.toDegrees(atan2(yeclip, xeclip)))
        val phaseAngle = rev(lonMoon - lonSun)
        val moonAgeDays = (phaseAngle / 360.0 * 29.53059).coerceIn(0.0, 29.53)

        // Quality and display status strictly for the selected moment
        val quality = when {
            !isAbove -> ObservationQuality.UNAVAILABLE
            solarProximity.showAlert -> ObservationQuality.UNAVAILABLE
            isDaytime -> {
                if (pos.altitudeDeg > 20.0 && angularSeparationDeg >= MIN_SAFE_SOLAR_ELONGATION_DEG) {
                    ObservationQuality.GOOD
                } else {
                    ObservationQuality.DIFFICULT
                }
            }
            pos.altitudeDeg >= 30.0 -> {
                if (pos.illuminationPercent in 15..85) ObservationQuality.IDEAL else ObservationQuality.EXCELLENT
            }
            pos.altitudeDeg >= 15.0 -> ObservationQuality.GOOD
            else -> ObservationQuality.DIFFICULT
        }

        val displayStatus = when (quality) {
            ObservationQuality.UNAVAILABLE -> {
                if (!isAbove) "Abaixo do horizonte" else "Próxima ao Sol"
            }
            ObservationQuality.DIFFICULT -> {
                if (isDaytime) "Difícil (Céu diurno)" else "Difícil (Baixa altitude)"
            }
            ObservationQuality.GOOD -> {
                if (isDaytime) "Bom (Visível de dia)" else "Bom"
            }
            ObservationQuality.EXCELLENT -> "Excelente"
            ObservationQuality.IDEAL -> "Ideal"
        }

        val lunarInfluenceMessage = when {
            pos.illuminationPercent < 10 ->
                "Influência mínima no céu: céu escuro ideal para observação de nebulosas e galáxias débeis."
            pos.illuminationPercent in 10..40 ->
                "Excelente contraste nas crateras e relevo lunar ao longo do terminador, com impacto luminoso moderado no céu noturno."
            pos.illuminationPercent in 41..75 ->
                "Destaque espetacular para relevo de crateras e montanhas. O luar começa a clarear o céu noturno."
            else ->
                "Luar intenso ilumina a noite, ofuscando alvos de céu profundo fracos. Ideal para observar os sistemas de raios brilhantes (ex: Tycho) e mares lunares."
        }

        val skyCondition = SkyCondition.fromSunAltitude(pos.sunAltitudeDeg)

        return MoonObservation(
            altitudeDeg = pos.altitudeDeg,
            azimuthDeg = pos.azimuthDeg,
            directionLabel = convertAzimuthToDirection(pos.azimuthDeg),
            heightLabel = convertAltitudeToHeightLabel(pos.altitudeDeg),
            illuminationPercent = pos.illuminationPercent,
            phaseName = pos.phaseName,
            moonAgeDays = moonAgeDays,
            angularSeparationSunDeg = angularSeparationDeg,
            riseTimeStr = times.riseStr,
            riseAzimuthDeg = times.riseAzimuthDeg,
            riseDirectionLabel = times.riseDirectionLabel,
            transitTimeStr = times.transitStr,
            transitAltitudeDeg = times.transitAltitudeDeg,
            transitDirectionLabel = times.transitDirectionLabel,
            setTimeStr = times.setStr,
            setAzimuthDeg = times.setAzimuthDeg,
            setDirectionLabel = times.setDirectionLabel,
            visibilityDurationStr = times.visibilityDurationStr,
            isAboveHorizon = isAbove,
            sunAltitudeDeg = pos.sunAltitudeDeg,
            skyCondition = skyCondition,
            isSkyDark = isSkyDark,
            solarProximityInfo = solarProximity,
            quality = quality,
            displayStatus = displayStatus,
            lunarInfluenceMessage = lunarInfluenceMessage,
            rightAscensionDeg = pos.rightAscensionDeg,
            declinationDeg = pos.declinationDeg
        )
    }

    fun findBestObservationWindowsForMoon(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        minDesiredHeightLabel: String = "Baixo",
        darkSkyOnly: Boolean = false
    ): List<MoonObservationWindow> {
        val minAltThreshold = when (minDesiredHeightLabel) {
            "Próximo do horizonte" -> 5.0
            "Baixo" -> 15.0
            "Bem posicionado" -> 25.0
            "Alto no céu" -> 40.0
            "O mais alto possível" -> 55.0
            else -> 15.0
        }

        val scanCal = calendar.clone() as Calendar
        scanCal.set(Calendar.HOUR_OF_DAY, 12)
        scanCal.set(Calendar.MINUTE, 0)
        scanCal.set(Calendar.SECOND, 0)

        val samples = mutableListOf<SamplePointForWindow>()
        // 24 hours at 15-min intervals (96 points)
        for (i in 0 until 96) {
            val pointCal = scanCal.clone() as Calendar
            pointCal.add(Calendar.MINUTE, i * 15)

            val pos = calculateMoonPosition(pointCal, latitude, longitude)
            val isValidSky = if (darkSkyOnly) pos.sunAltitudeDeg < -18.0 else pos.sunAltitudeDeg < 0.0
            val isValidAlt = pos.altitudeDeg >= minAltThreshold
            val sepDeg = calculateMoonSunAngularSeparationDeg(pointCal, latitude, longitude)
            val isSafeFromSun = pos.sunAltitudeDeg < 0.0 || sepDeg >= MIN_SAFE_SOLAR_ELONGATION_DEG

            samples.add(
                SamplePointForWindow(
                    cal = pointCal,
                    alt = pos.altitudeDeg,
                    az = pos.azimuthDeg,
                    sunAlt = pos.sunAltitudeDeg,
                    isValid = isValidSky && isValidAlt && isSafeFromSun,
                    dir = convertAzimuthToDirection(pos.azimuthDeg)
                )
            )
        }

        val windows = mutableListOf<MoonObservationWindow>()
        var currentWindowStart: SamplePointForWindow? = null
        var currentWindowPoints = mutableListOf<SamplePointForWindow>()

        fun createWindow(points: List<SamplePointForWindow>): MoonObservationWindow {
            val startPoint = points.first()
            val endPoint = points.last()
            val maxPoint = points.maxByOrNull { it.alt } ?: startPoint
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                timeZone = startPoint.cal.timeZone
            }
            val quality = when {
                maxPoint.sunAlt >= 0.0 -> ObservationQuality.GOOD
                maxPoint.sunAlt >= -6.0 -> ObservationQuality.GOOD
                maxPoint.alt > 35.0 -> ObservationQuality.IDEAL
                maxPoint.alt > 20.0 -> ObservationQuality.EXCELLENT
                else -> ObservationQuality.GOOD
            }
            return MoonObservationWindow(
                startTimeStr = fmt.format(startPoint.cal.time),
                endTimeStr = fmt.format(endPoint.cal.time),
                startCal = startPoint.cal,
                endCal = endPoint.cal,
                quality = quality,
                maxAltitudeDeg = maxPoint.alt,
                heightLabel = convertAltitudeToHeightLabel(maxPoint.alt),
                directionLabel = maxPoint.dir,
                isDaytime = maxPoint.sunAlt >= 0.0
            )
        }

        for (sample in samples) {
            if (sample.isValid) {
                if (currentWindowStart == null) {
                    currentWindowStart = sample
                }
                currentWindowPoints.add(sample)
            } else {
                if (currentWindowStart != null && currentWindowPoints.size >= 2) {
                    windows.add(createWindow(currentWindowPoints))
                }
                currentWindowStart = null
                currentWindowPoints = mutableListOf()
            }
        }
        if (currentWindowStart != null && currentWindowPoints.size >= 2) {
            windows.add(createWindow(currentWindowPoints))
        }

        if (windows.isEmpty()) {
            val validSamples = samples.filter { it.alt > 0.0 }
            if (validSamples.isNotEmpty()) {
                val bestPoint = validSamples.maxByOrNull { it.alt }
                if (bestPoint != null) {
                    val bestIndex = validSamples.indexOf(bestPoint)
                    var startIndex = bestIndex
                    var endIndex = bestIndex
                    while (startIndex > 0 && Math.abs(validSamples[startIndex - 1].cal.timeInMillis - validSamples[startIndex].cal.timeInMillis) <= 20 * 60 * 1000) {
                        startIndex--
                    }
                    while (endIndex < validSamples.size - 1 && Math.abs(validSamples[endIndex + 1].cal.timeInMillis - validSamples[endIndex].cal.timeInMillis) <= 20 * 60 * 1000) {
                        endIndex++
                    }
                    val windowPoints = validSamples.subList(startIndex, endIndex + 1)
                    if (windowPoints.isNotEmpty()) {
                        windows.add(createWindow(windowPoints))
                    }
                }
            }
        }

        return windows
    }

    fun calculateAngularSeparationDeg(
        ra1Deg: Double,
        dec1Deg: Double,
        ra2Deg: Double,
        dec2Deg: Double
    ): Double {
        val ra1Rad = Math.toRadians(ra1Deg)
        val dec1Rad = Math.toRadians(dec1Deg)
        val ra2Rad = Math.toRadians(ra2Deg)
        val dec2Rad = Math.toRadians(dec2Deg)

        val cosSep = sin(dec1Rad) * sin(dec2Rad) + cos(dec1Rad) * cos(dec2Rad) * cos(ra1Rad - ra2Rad)
        val sepRad = acos(cosSep.coerceIn(-1.0, 1.0))
        return Math.toDegrees(sepRad)
    }

    private data class SamplePointForWindow(
        val cal: Calendar,
        val alt: Double,
        val az: Double,
        val sunAlt: Double,
        val isValid: Boolean,
        val dir: String
    )

    /**
     * Estimates magnetic declination in degrees for a given latitude and longitude.
     * Negative values indicate West declination (e.g. -19.5° W), positive values East.
     */
    fun calculateMagneticDeclination(lat: Double, lon: Double): Double {
        val poleLat = Math.toRadians(80.8)
        val poleLon = Math.toRadians(-72.7)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)

        val dLambda = poleLon - lambda
        val y = Math.sin(dLambda) * Math.cos(poleLat)
        val x = Math.cos(phi) * Math.sin(poleLat) - Math.sin(phi) * Math.cos(poleLat) * Math.cos(dLambda)
        var decDeg = Math.toDegrees(Math.atan2(y, x))

        // Regional offset for South America & Brazil (South Atlantic Anomaly adjustment)
        if (lat in -55.0..15.0 && lon in -85.0..-30.0) {
            val saOffset = -16.5 + (lat + 20.0) * 0.12 - (lon + 50.0) * 0.15
            decDeg += saOffset
        }

        if (decDeg > 180) decDeg -= 360
        if (decDeg < -180) decDeg += 360
        return decDeg
    }
}

data class MoonPositionResult(
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val illuminationPercent: Int,
    val phaseName: String,
    val sunAltitudeDeg: Double,
    val rightAscensionDeg: Double,
    val declinationDeg: Double
)

data class MoonTimesResult(
    val riseStr: String,
    val setStr: String,
    val riseAzimuthDeg: Double = 0.0,
    val riseDirectionLabel: String = "",
    val transitStr: String = "--:--",
    val transitAltitudeDeg: Double = 0.0,
    val transitDirectionLabel: String = "",
    val setAzimuthDeg: Double = 0.0,
    val setDirectionLabel: String = "",
    val visibilityDurationStr: String = ""
)

data class MoonObservation(
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val directionLabel: String = "",
    val heightLabel: String = "",
    val illuminationPercent: Int,
    val phaseName: String,
    val moonAgeDays: Double = 0.0,
    val angularSeparationSunDeg: Double = 0.0,
    val riseTimeStr: String,
    val riseAzimuthDeg: Double = 0.0,
    val riseDirectionLabel: String = "",
    val transitTimeStr: String = "--:--",
    val transitAltitudeDeg: Double = 0.0,
    val transitDirectionLabel: String = "",
    val setTimeStr: String,
    val setAzimuthDeg: Double = 0.0,
    val setDirectionLabel: String = "",
    val visibilityDurationStr: String = "",
    val isAboveHorizon: Boolean,
    val sunAltitudeDeg: Double,
    val skyCondition: SkyCondition = SkyCondition.ASTRONOMICAL_NIGHT,
    val isSkyDark: Boolean = false,
    val solarProximityInfo: SolarProximityInfo? = null,
    val quality: ObservationQuality = ObservationQuality.GOOD,
    val displayStatus: String = "Bom",
    val lunarInfluenceMessage: String = "",
    val rightAscensionDeg: Double = 0.0,
    val declinationDeg: Double = 0.0
)

data class MoonObservationWindow(
    val startTimeStr: String,
    val endTimeStr: String,
    val startCal: Calendar,
    val endCal: Calendar,
    val quality: ObservationQuality,
    val maxAltitudeDeg: Double,
    val heightLabel: String,
    val directionLabel: String,
    val isDaytime: Boolean = false
)

data class EquatorialResult(
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val sunAltitudeDeg: Double
)

data class TopocentricResult(
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val distanceAU: Double,
    val magnitude: Double,
    val sunAltitudeDeg: Double,
    val rightAscensionDeg: Double,
    val declinationDeg: Double
)
