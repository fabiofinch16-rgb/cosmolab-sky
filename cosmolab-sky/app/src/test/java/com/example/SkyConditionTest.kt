package com.example

import com.example.astronomy.AstronomyEngine
import com.example.astronomy.Planet
import com.example.astronomy.SkyCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SkyConditionTest {

    private val latPomerode = -26.74
    private val lonPomerode = -49.17
    private val tzPomerode = TimeZone.getTimeZone("America/Sao_Paulo")

    private fun createCalendar(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance(tzPomerode).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun testSkyConditionClassificationThresholds() {
        assertEquals(SkyCondition.DAYTIME, SkyCondition.fromSunAltitude(10.0))
        assertEquals(SkyCondition.DAYTIME, SkyCondition.fromSunAltitude(0.1))

        assertEquals(SkyCondition.CIVIL_TWILIGHT, SkyCondition.fromSunAltitude(0.0))
        assertEquals(SkyCondition.CIVIL_TWILIGHT, SkyCondition.fromSunAltitude(-3.0))
        assertEquals(SkyCondition.CIVIL_TWILIGHT, SkyCondition.fromSunAltitude(-5.9))

        assertEquals(SkyCondition.NAUTICAL_TWILIGHT, SkyCondition.fromSunAltitude(-6.0))
        assertEquals(SkyCondition.NAUTICAL_TWILIGHT, SkyCondition.fromSunAltitude(-9.0))
        assertEquals(SkyCondition.NAUTICAL_TWILIGHT, SkyCondition.fromSunAltitude(-11.9))

        assertEquals(SkyCondition.ASTRONOMICAL_TWILIGHT, SkyCondition.fromSunAltitude(-12.0))
        assertEquals(SkyCondition.ASTRONOMICAL_TWILIGHT, SkyCondition.fromSunAltitude(-15.0))
        assertEquals(SkyCondition.ASTRONOMICAL_TWILIGHT, SkyCondition.fromSunAltitude(-17.9))

        assertEquals(SkyCondition.ASTRONOMICAL_NIGHT, SkyCondition.fromSunAltitude(-18.0))
        assertEquals(SkyCondition.ASTRONOMICAL_NIGHT, SkyCondition.fromSunAltitude(-45.0))
        assertEquals(SkyCondition.ASTRONOMICAL_NIGHT, SkyCondition.fromSunAltitude(-57.0))
    }

    @Test
    fun testAstronomicalNightAt0230AM() {
        // Pomerode, SC, 09/08/2026 às 02:30 AM (Madrugada)
        val cal = createCalendar(2026, 8, 9, 2, 30)
        val obs = AstronomyEngine.analyzeObservation(Planet.JUPITER, cal, latPomerode, lonPomerode)

        assertTrue("Sun altitude should be < -18° at 02:30 AM, but was ${obs.sunAltitudeDeg}", obs.sunAltitudeDeg < -18.0)
        assertEquals("Sky condition should be ASTRONOMICAL_NIGHT", SkyCondition.ASTRONOMICAL_NIGHT, obs.skyCondition)
        assertEquals("Noite astronômica", obs.skyCondition.label)
        assertEquals("🌙", obs.skyCondition.icon)
        assertTrue("isSkyDark should be true for astronomical night", obs.isSkyDark)
    }

    @Test
    fun testDaytimeAt1200PM() {
        // Pomerode, SC, 09/08/2026 às 12:00 PM (Meio-dia)
        val cal = createCalendar(2026, 8, 9, 12, 0)
        val obs = AstronomyEngine.analyzeObservation(Planet.JUPITER, cal, latPomerode, lonPomerode)

        assertTrue("Sun altitude should be > 0° at 12:00 PM, but was ${obs.sunAltitudeDeg}", obs.sunAltitudeDeg > 0.0)
        assertEquals("Sky condition should be DAYTIME", SkyCondition.DAYTIME, obs.skyCondition)
        assertEquals("Dia", obs.skyCondition.label)
        assertEquals("☀️", obs.skyCondition.icon)
        assertFalse("isSkyDark should be false during daytime", obs.isSkyDark)
    }

    @Test
    fun testTwilightClassifications() {
        // Test civil twilight (-3°)
        val civilCond = SkyCondition.fromSunAltitude(-3.0)
        assertEquals(SkyCondition.CIVIL_TWILIGHT, civilCond)
        assertEquals("Crepúsculo civil", civilCond.label)

        // Test nautical twilight (-9°)
        val nauticalCond = SkyCondition.fromSunAltitude(-9.0)
        assertEquals(SkyCondition.NAUTICAL_TWILIGHT, nauticalCond)
        assertEquals("Crepúsculo náutico", nauticalCond.label)

        // Test astronomical twilight (-15°)
        val astroTwilightCond = SkyCondition.fromSunAltitude(-15.0)
        assertEquals(SkyCondition.ASTRONOMICAL_TWILIGHT, astroTwilightCond)
        assertEquals("Crepúsculo astronômico", astroTwilightCond.label)
    }

    @Test
    fun testBestTimeFinderJupiterDarkSkyOnlyFilter_NoDaytimeRecommendations() {
        val cal = createCalendar(2026, 8, 9, 12, 0)

        // Test with darkSkyOnly = true
        val windowsDark = AstronomyEngine.findBestObservationWindows(
            planet = Planet.JUPITER,
            calendar = cal,
            latitude = latPomerode,
            longitude = lonPomerode,
            minDesiredHeightLabel = "Bem posicionado",
            darkSkyOnly = true
        )

        // On this date in Pomerode, Jupiter is only in the sky during daylight/twilight.
        // Therefore, darkSkyOnly=true MUST return 0 windows (no daytime or twilight recommendation!).
        for (w in windowsDark) {
            val startCal = w.startCal
            val obs = AstronomyEngine.analyzeObservation(Planet.JUPITER, startCal, latPomerode, lonPomerode)
            assertTrue("No window when darkSkyOnly is true can have sun altitude >= -18°", obs.sunAltitudeDeg < -18.0)
        }

        // Verify Saturn (which is visible at night in August 2026) produces valid dark sky windows
        val saturnWindowsDark = AstronomyEngine.findBestObservationWindows(
            planet = Planet.SATURN,
            calendar = cal,
            latitude = latPomerode,
            longitude = lonPomerode,
            minDesiredHeightLabel = "Bem posicionado",
            darkSkyOnly = true
        )

        assertTrue("Saturn should have dark sky windows at night", saturnWindowsDark.isNotEmpty())
        for (w in saturnWindowsDark) {
            val startCal = w.startCal
            val obs = AstronomyEngine.analyzeObservation(Planet.SATURN, startCal, latPomerode, lonPomerode)
            assertTrue("Saturn dark sky window sun altitude must be < -18°", obs.sunAltitudeDeg < -18.0)
        }
    }

    @Test
    fun testSunTimesPomerode() {
        val baseCal = createCalendar(2026, 8, 9, 12, 0)
        val sunTimes = AstronomyEngine.calculateSunTimes(baseCal, latPomerode, lonPomerode)
        println("=== SUN TIMES POMERODE (UTC-3) ===")
        println("Sunrise: ${sunTimes.riseStr}, Sunset: ${sunTimes.setStr}")

        val venusDaily = AstronomyEngine.calculateDailyTimes(Planet.VENUS, baseCal, latPomerode, lonPomerode)
        println("=== VENUS DAILY POMERODE ===")
        println("Venus Rise: ${venusDaily.riseStr}, Transit: ${venusDaily.transitStr}, Set: ${venusDaily.setStr}")

        val jupiterDaily = AstronomyEngine.calculateDailyTimes(Planet.JUPITER, baseCal, latPomerode, lonPomerode)
        println("=== JUPITER DAILY POMERODE ===")
        println("Jupiter Rise: ${jupiterDaily.riseStr}, Transit: ${jupiterDaily.transitStr}, Set: ${jupiterDaily.setStr}")

        val marsDaily = AstronomyEngine.calculateDailyTimes(Planet.MARS, baseCal, latPomerode, lonPomerode)
        println("=== MARS DAILY POMERODE ===")
        println("Mars Rise: ${marsDaily.riseStr}, Transit: ${marsDaily.transitStr}, Set: ${marsDaily.setStr}")

        val saturnDaily = AstronomyEngine.calculateDailyTimes(Planet.SATURN, baseCal, latPomerode, lonPomerode)
        println("=== SATURN DAILY POMERODE ===")
        println("Saturn Rise: ${saturnDaily.riseStr}, Transit: ${saturnDaily.transitStr}, Set: ${saturnDaily.setStr}")
    }

    @Test
    fun testOtherTimezones() {
        // Tokyo (Asia/Tokyo, UTC+9)
        val tzTokyo = TimeZone.getTimeZone("Asia/Tokyo")
        val calTokyo = Calendar.getInstance(tzTokyo).apply {
            set(2026, Calendar.AUGUST, 9, 12, 0, 0)
        }
        val sunTokyo = AstronomyEngine.calculateSunTimes(calTokyo, 35.6762, 139.6503)
        println("=== TOKYO (UTC+9) SUN TIMES ===")
        println("Sunrise: ${sunTokyo.riseStr}, Sunset: ${sunTokyo.setStr}")

        // New York (America/New_York, UTC-4 EDT)
        val tzNY = TimeZone.getTimeZone("America/New_York")
        val calNY = Calendar.getInstance(tzNY).apply {
            set(2026, Calendar.AUGUST, 9, 12, 0, 0)
        }
        val sunNY = AstronomyEngine.calculateSunTimes(calNY, 40.7128, -74.0060)
        println("=== NEW YORK (UTC-4) SUN TIMES ===")
        println("Sunrise: ${sunNY.riseStr}, Sunset: ${sunNY.setStr}")
    }
}
