package com.example.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.astronomy.AstronomyEngine
import com.example.astronomy.BortleScale
import com.example.astronomy.CelestialTarget
import com.example.astronomy.DeepSkyCatalog
import com.example.astronomy.DeepSkyEngine
import com.example.astronomy.DeepSkyObject
import com.example.astronomy.MoonObservation
import com.example.astronomy.ObservationMode
import com.example.astronomy.ObservationQuality
import com.example.astronomy.ObservationWindow
import com.example.astronomy.OpticsEngine
import com.example.astronomy.Planet
import com.example.astronomy.PlanetObservation
import com.example.astronomy.TelescopeEquipment
import com.example.astronomy.TelescopeObservationEvaluation
import com.example.astronomy.TelescopeTargetEvaluation
import com.example.data.City
import com.example.data.CityRepository
import java.util.Calendar
import java.util.TimeZone

class CosmoLabViewModel : ViewModel() {

    var activeTab by mutableIntStateOf(0) // 0: Céu, 1: Planetas, 2: Telescópios

    var selectedCity by mutableStateOf(CityRepository.DEFAULT_CITY)
        private set

    var recentCities by mutableStateOf<List<City>>(
        listOf(
            CityRepository.DEFAULT_CITY,
            CityRepository.DELFIM_MOREIRA,
            CityRepository.ATINS
        )
    )
        private set

    var selectedCalendar: Calendar by mutableStateOf(
        Calendar.getInstance(TimeZone.getTimeZone(CityRepository.DEFAULT_CITY.timezoneId))
    )
        private set

    var selectedPlanet by mutableStateOf(Planet.SATURN)
        private set

    var selectedDso by mutableStateOf<DeepSkyObject?>(null)
        private set

    var isBortleAuto by mutableStateOf(true)
        private set

    var bortleScale by mutableStateOf(CityRepository.DEFAULT_CITY.bortleClass ?: BortleScale.BORTLE_4)
        private set

    var planetSortMode by mutableStateOf(PlanetSortMode.RECOMMENDED) // RECOMMENDED or SOLAR_SYSTEM

    // User Observation Mode (Naked Eye, Binocular, Telescope)
    var observationMode by mutableStateOf(ObservationMode.NAKED_EYE)
        private set

    // Binocular Equipment
    var binocularApertureMm by mutableStateOf(50.0)
        private set

    var binocularMagnification by mutableStateOf(10.0)
        private set

    // User Telescope Equipment
    var telescopeEquipment by mutableStateOf(TelescopeEquipment(114.0, 900.0, 10.0, 52.0))
        private set

    var showCelestialPoleMarker by mutableStateOf(false)
        private set

    fun toggleCelestialPoleMarker() {
        showCelestialPoleMarker = !showCelestialPoleMarker
    }

    // State for Best Time Finder options
    var minHeightChoice by mutableStateOf("Bem posicionado")
    var darkSkyOnlyChoice by mutableStateOf(true)

    // Calculated observations - Sun times
    var sunRiseStr by mutableStateOf("--:--")
        private set

    var sunSetStr by mutableStateOf("--:--")
        private set

    // Calculated observations - Moon data
    var moonRiseStr by mutableStateOf("--:--")
        private set

    var moonSetStr by mutableStateOf("--:--")
        private set

    var moonIlluminationPercent by mutableStateOf(0)
        private set

    var moonObservation by mutableStateOf<MoonObservation?>(null)
        private set

    // Calculated observations - Naked Eye
    var observationsMap by mutableStateOf<Map<Planet, PlanetObservation>>(emptyMap())
        private set

    var nakedEyeRanking by mutableStateOf<List<PlanetObservation>>(emptyList())
        private set

    var nakedEyeHero by mutableStateOf<PlanetObservation?>(null)
        private set

    // Calculated observations - Telescope (Legacy Planet Evaluations)
    var telescopeEvaluations by mutableStateOf<Map<Planet, TelescopeObservationEvaluation>>(emptyMap())
        private set

    var telescopeRanking by mutableStateOf<List<TelescopeObservationEvaluation>>(emptyList())
        private set

    // Top 20 Telescope Recommendations (Planets + Deep Sky Objects)
    var top20TelescopeTargets by mutableStateOf<List<TelescopeTargetEvaluation>>(emptyList())
        private set

    init {
        recalculate()
    }

    fun setCity(city: City) {
        selectedCity = city
        recentCities = (listOf(city) + recentCities.filter {
            it.displayName != city.displayName && (it.latitude != city.latitude || it.longitude != city.longitude)
        }).take(5)

        val tz = TimeZone.getTimeZone(city.timezoneId)
        val y = selectedCalendar.get(Calendar.YEAR)
        val m = selectedCalendar.get(Calendar.MONTH)
        val d = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val h = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val min = selectedCalendar.get(Calendar.MINUTE)
        val newCal = Calendar.getInstance(tz)
        newCal.set(y, m, d, h, min, 0)
        newCal.set(Calendar.MILLISECOND, 0)
        selectedCalendar = newCal
        if (isBortleAuto && city.bortleClass != null) {
            bortleScale = city.bortleClass
        }
        recalculate()
    }

    fun setCalendar(calendar: Calendar) {
        val updated = calendar.clone() as Calendar
        updated.timeZone = TimeZone.getTimeZone(selectedCity.timezoneId)
        selectedCalendar = updated
        recalculate()
    }

    fun updateTimeOffset(minutesDelta: Int) {
        val nowMillis = System.currentTimeMillis()
        val currentDiffMinutes = (selectedCalendar.timeInMillis - nowMillis) / (60.0 * 1000.0)
        val newDiffMinutes = (currentDiffMinutes + minutesDelta).coerceIn(-720.0, 720.0)
        setTimeOffsetFromNowMinutes(newDiffMinutes)
    }

    fun advanceTimeMinutes(minutesDelta: Double) {
        val updated = selectedCalendar.clone() as Calendar
        val addedMillis = (minutesDelta * 60.0 * 1000.0).toLong()
        updated.timeInMillis += addedMillis
        selectedCalendar = updated
        recalculate()
    }

    fun updateDateOffset(daysDelta: Int) {
        val updated = selectedCalendar.clone() as Calendar
        updated.add(Calendar.DAY_OF_MONTH, daysDelta)
        selectedCalendar = updated
        recalculate()
    }

    fun setTime(hour: Int, minute: Int) {
        val updated = selectedCalendar.clone() as Calendar
        updated.set(Calendar.HOUR_OF_DAY, hour)
        updated.set(Calendar.MINUTE, minute)
        updated.set(Calendar.SECOND, 0)
        updated.set(Calendar.MILLISECOND, 0)
        selectedCalendar = updated
        recalculate()
    }

    fun setDate(year: Int, monthZeroBased: Int, day: Int) {
        val updated = selectedCalendar.clone() as Calendar
        updated.set(Calendar.YEAR, year)
        updated.set(Calendar.MONTH, monthZeroBased)
        updated.set(Calendar.DAY_OF_MONTH, day)
        selectedCalendar = updated
        recalculate()
    }

    fun setDateAndTime(year: Int, monthZeroBased: Int, day: Int, hour: Int, minute: Int) {
        val updated = selectedCalendar.clone() as Calendar
        updated.set(Calendar.YEAR, year)
        updated.set(Calendar.MONTH, monthZeroBased)
        updated.set(Calendar.DAY_OF_MONTH, day)
        updated.set(Calendar.HOUR_OF_DAY, hour)
        updated.set(Calendar.MINUTE, minute)
        updated.set(Calendar.SECOND, 0)
        updated.set(Calendar.MILLISECOND, 0)
        selectedCalendar = updated
        recalculate()
    }

    fun selectPlanet(planet: Planet) {
        selectedPlanet = planet
    }

    fun selectDso(dso: DeepSkyObject?) {
        selectedDso = dso
    }

    fun setBortleAutoMode() {
        isBortleAuto = true
        if (selectedCity.bortleClass != null) {
            bortleScale = selectedCity.bortleClass!!
        }
        recalculate()
    }

    fun updateBortleScale(bortle: BortleScale, isManual: Boolean = true) {
        if (isManual) {
            isBortleAuto = false
        }
        bortleScale = bortle
        recalculate()
    }

    fun selectObservationMode(mode: ObservationMode) {
        observationMode = mode
        recalculate()
    }

    fun updateBinocularEquipment(apertureMm: Double, magnification: Double) {
        binocularApertureMm = apertureMm
        binocularMagnification = magnification
        recalculate()
    }

    fun updateTelescopeEquipment(
        apertureMm: Double,
        focalLengthMm: Double,
        eyepieceFocalLengthMm: Double,
        apparentFovDeg: Double?
    ) {
        telescopeEquipment = TelescopeEquipment(
            apertureMm = apertureMm,
            focalLengthMm = focalLengthMm,
            eyepieceFocalLengthMm = eyepieceFocalLengthMm,
            eyepieceApparentFovDeg = apparentFovDeg
        )
        recalculate()
    }

    fun resetToCurrentTime() {
        val tz = TimeZone.getTimeZone(selectedCity.timezoneId)
        selectedCalendar = Calendar.getInstance(tz)
        recalculate()
    }

    fun setTimeOffsetFromNowMinutes(minutesOffset: Double) {
        val clampedMinutes = minutesOffset.coerceIn(-720.0, 720.0)
        val tz = TimeZone.getTimeZone(selectedCity.timezoneId)
        val nowMillis = System.currentTimeMillis()
        val newCal = Calendar.getInstance(tz)
        newCal.timeInMillis = nowMillis + (clampedMinutes * 60.0 * 1000.0).toLong()
        selectedCalendar = newCal
        recalculate()
    }

    fun recalculate() {
        val sunTimes = AstronomyEngine.calculateSunTimes(
            calendar = selectedCalendar,
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude
        )
        sunRiseStr = sunTimes.riseStr
        sunSetStr = sunTimes.setStr

        val moonObs = AstronomyEngine.analyzeMoonObservation(
            calendar = selectedCalendar,
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude
        )
        moonRiseStr = moonObs.riseTimeStr
        moonSetStr = moonObs.setTimeStr
        moonIlluminationPercent = moonObs.illuminationPercent
        moonObservation = moonObs

        val allMap = mutableMapOf<Planet, PlanetObservation>()
        val nakedEyeList = mutableListOf<PlanetObservation>()
        val teleMap = mutableMapOf<Planet, TelescopeObservationEvaluation>()
        val teleList = mutableListOf<TelescopeObservationEvaluation>()

        for (planet in Planet.entries) {
            val obs = AstronomyEngine.analyzeObservation(
                planet = planet,
                calendar = selectedCalendar,
                latitude = selectedCity.latitude,
                longitude = selectedCity.longitude
            )
            allMap[planet] = obs

            // Naked eye candidate list strictly contains the 5 naked-eye planets
            if (Planet.nakedEyePlanets.contains(planet)) {
                nakedEyeList.add(obs)
            }

            // Mode-aware evaluation for ALL 7 planets
            val teleEval = OpticsEngine.evaluateObservationForMode(
                planet = planet,
                altitudeDeg = obs.altitudeDeg,
                sunAltitudeDeg = obs.sunAltitudeDeg,
                magnitude = obs.magnitude,
                distanceAU = obs.distanceAU,
                mode = observationMode,
                telescopeEquipment = telescopeEquipment,
                binocularApertureMm = binocularApertureMm,
                binocularMagnification = binocularMagnification,
                bortle = bortleScale
            )
            teleMap[planet] = teleEval
            teleList.add(teleEval)
        }

        observationsMap = allMap
        telescopeEvaluations = teleMap

        // Sort naked eye ranking
        val sortedNakedEye = nakedEyeList.sortedByDescending { it.score }
        nakedEyeRanking = sortedNakedEye
        nakedEyeHero = sortedNakedEye.firstOrNull()

        // Sort telescope ranking
        val sortedTelescope = teleList.sortedByDescending { it.score }
        telescopeRanking = sortedTelescope

        // Calculate Top 20 Recommendations for current Observation Mode (Planets + Deep Sky Objects)
        top20TelescopeTargets = DeepSkyEngine.getTop20RecommendationsForMode(
            calendar = selectedCalendar,
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude,
            mode = observationMode,
            telescopeEquipment = telescopeEquipment,
            binocularApertureMm = binocularApertureMm,
            binocularMagnification = binocularMagnification,
            bortle = bortleScale
        )
    }

    fun updateMountType(mountType: com.example.astronomy.TelescopeMountType) {
        telescopeEquipment = telescopeEquipment.copy(mountType = mountType)
        recalculate()
    }

    fun findBestWindowsForPlanet(planet: Planet): List<ObservationWindow> {
        return AstronomyEngine.findBestObservationWindows(
            planet = planet,
            calendar = selectedCalendar,
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude,
            minDesiredHeightLabel = minHeightChoice,
            darkSkyOnly = darkSkyOnlyChoice
        )
    }

    fun findBestWindowsForMoon(): List<com.example.astronomy.MoonObservationWindow> {
        return AstronomyEngine.findBestObservationWindowsForMoon(
            calendar = selectedCalendar,
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude,
            minDesiredHeightLabel = minHeightChoice,
            darkSkyOnly = darkSkyOnlyChoice
        )
    }
}

enum class PlanetSortMode {
    RECOMMENDED,
    SOLAR_SYSTEM
}
