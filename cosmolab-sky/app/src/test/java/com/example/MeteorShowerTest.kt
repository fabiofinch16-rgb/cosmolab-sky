package com.example

import com.example.astronomy.BortleScale
import com.example.astronomy.MeteorShowerCatalog
import com.example.astronomy.MeteorShowerEngine
import com.example.astronomy.ObservationQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class MeteorShowerTest {

    @Test
    fun testCatalogHasAllMajorShowers() {
        val allShowers = MeteorShowerCatalog.allShowers
        assertTrue(allShowers.size >= 9)

        val perseids = MeteorShowerCatalog.findShowerById("perseids")
        assertNotNull(perseids)
        assertEquals("Perseidas", perseids?.portugueseName)
        assertEquals(100, perseids?.peakZhr)

        val geminids = MeteorShowerCatalog.findShowerById("geminids")
        assertNotNull(geminids)
        assertEquals("Geminídeas", geminids?.portugueseName)
        assertEquals(150, geminids?.peakZhr)
    }

    @Test
    fun testPerseidsActiveInAugust() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 12, 23, 0, 0)
        }

        val activeShowers = MeteorShowerCatalog.getActiveShowers(cal)
        val perseids = activeShowers.find { it.id == "perseids" }
        assertNotNull("Perseids should be active around August 12", perseids)

        val eval = MeteorShowerEngine.evaluateShower(
            shower = perseids!!,
            calendar = cal,
            latitude = -26.7389,
            longitude = -49.1764,
            bortle = BortleScale.BORTLE_4
        )
        assertNotNull(eval)
        assertTrue((eval?.effectiveExpectedZhr ?: 0) > 0)
    }

    @Test
    fun testGeminidsActiveInDecember() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.DECEMBER, 14, 2, 0, 0)
        }

        val activeShowers = MeteorShowerCatalog.getActiveShowers(cal)
        val geminids = activeShowers.find { it.id == "geminids" }
        assertNotNull("Geminids should be active around December 14", geminids)

        val eval = MeteorShowerEngine.evaluateShower(
            shower = geminids!!,
            calendar = cal,
            latitude = -26.7389,
            longitude = -49.1764,
            bortle = BortleScale.BORTLE_4
        )
        assertNotNull(eval)
        assertTrue(eval?.altitudeDeg != null)
    }

    @Test
    fun testClearSkyRuleDuringDaytime() {
        // Geminids on Dec 14 at 15:00 UTC (12:00 local midday at longitude -45)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-3")).apply {
            set(2026, Calendar.DECEMBER, 14, 12, 0, 0)
        }

        val geminids = MeteorShowerCatalog.findShowerById("geminids")!!
        val eval = MeteorShowerEngine.evaluateShower(
            shower = geminids,
            calendar = cal,
            latitude = -23.55,
            longitude = -46.63,
            bortle = BortleScale.BORTLE_4
        )
        assertNotNull(eval)
        // Sun is high in the sky: MUST be unavailable due to clear sky
        assertEquals(ObservationQuality.UNAVAILABLE, eval?.quality)
        assertEquals("Céu claro", eval?.qualityReason)
        assertEquals("Indisponível — Céu claro", eval?.displayStatus)
        assertTrue(eval?.sunAltitudeDeg ?: 0.0 > 0.0)

        // Time window must NOT contain generic strings
        assertFalse(eval?.bestWindowStr?.contains("madrugada") ?: true)
        assertFalse(eval?.bestWindowStr?.contains("alvorada") ?: true)
        assertFalse(eval?.bestWindowStr?.contains("pôr do Sol") ?: true)
        // Must contain standard time interval formatted like HH:mm – HH:mm
        assertTrue(eval?.bestWindowStr?.contains("–") ?: false)
    }

    @Test
    fun testWindowFormatPattern() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-3")).apply {
            set(2026, Calendar.MAY, 6, 22, 0, 0)
        }

        val etaAquariids = MeteorShowerCatalog.findShowerById("eta_aquariids")!!
        val eval = MeteorShowerEngine.evaluateShower(
            shower = etaAquariids,
            calendar = cal,
            latitude = -26.7389,
            longitude = -49.1764,
            bortle = BortleScale.BORTLE_3
        )
        assertNotNull(eval)
        val window = eval?.bestWindowStr ?: ""
        assertTrue(window.matches(Regex("""\d{2}:\d{2} – \d{2}:\d{2}""")) || window == "Não observável hoje")
    }
}

