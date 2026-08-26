package com.example.astronomy

import java.util.Calendar

/**
 * Represents a Meteor Shower phenomenon.
 * Meteor showers are wide-field celestial events produced when Earth intersects
 * streams of debris (meteoroids) left behind by comets or asteroids.
 */
data class MeteorShower(
    val id: String,
    val portugueseName: String,
    val internationalName: String,
    val startMonth: Int,       // 1..12
    val startDay: Int,         // 1..31
    val endMonth: Int,         // 1..12
    val endDay: Int,           // 1..31
    val peakMonth: Int,        // 1..12
    val peakDay: Int,          // 1..31
    val radiantConstellation: String,
    val radiantRaDeg: Double,  // Right ascension in degrees (0..360)
    val radiantDecDeg: Double, // Declination in degrees (-90..+90)
    val peakZhr: Int,          // Zenithal Hourly Rate at peak
    val velocityKmS: Int,      // Typical velocity in km/s
    val velocityDescription: String,
    val parentBody: String,
    val description: String,
    val bestObservingTimeDescription: String,
    val opticalRecommendation: String = "Observação exclusivamente a olho nu com campo visual de 180°. Telescópios e binóculos restringem o campo e não são recomendados."
) {
    val raHoursStr: String
        get() {
            val totalHours = radiantRaDeg / 15.0
            val h = totalHours.toInt()
            val m = ((totalHours - h) * 60).toInt()
            return String.format("%02dh %02dm", h, m)
        }

    val decDegStr: String
        get() = String.format("%+.1f°", radiantDecDeg)

    val activityPeriodStr: String
        get() = "${formatDayMonth(startDay, startMonth)} a ${formatDayMonth(endDay, endMonth)}"

    val peakDateStr: String
        get() = formatDayMonth(peakDay, peakMonth)

    /**
     * Checks whether a given calendar date falls inside the activity period of this shower.
     */
    fun isActive(calendar: Calendar): Boolean {
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return if (startMonth <= endMonth) {
            // Normal range within same calendar year (e.g., April 14 to April 30)
            if (month in (startMonth + 1) until endMonth) {
                true
            } else if (month == startMonth && month == endMonth) {
                day in startDay..endDay
            } else if (month == startMonth) {
                day >= startDay
            } else if (month == endMonth) {
                day <= endDay
            } else {
                false
            }
        } else {
            // Year-wrapping range (e.g. Quadrantids Dec 28 to Jan 12)
            if (month > startMonth || month < endMonth) {
                true
            } else if (month == startMonth) {
                day >= startDay
            } else if (month == endMonth) {
                day <= endDay
            } else {
                false
            }
        }
    }

    /**
     * Calculates the absolute distance in days between the calendar date and the shower's peak.
     */
    fun daysFromPeak(calendar: Calendar): Double {
        val calYear = calendar.get(Calendar.YEAR)
        val targetCal = Calendar.getInstance(calendar.timeZone).apply {
            set(Calendar.YEAR, calYear)
            set(Calendar.MONTH, peakMonth - 1)
            set(Calendar.DAY_OF_MONTH, peakDay)
            set(Calendar.HOUR_OF_DAY, 3) // Approx midnight/pre-dawn peak
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var diffMillis = (calendar.timeInMillis - targetCal.timeInMillis).toDouble()
        val dayMillis = 24.0 * 3600.0 * 1000.0

        // Handle year boundary if peak is in Jan and current cal in Dec, or vice-versa
        if (diffMillis > 180 * dayMillis) {
            targetCal.add(Calendar.YEAR, 1)
            diffMillis = (calendar.timeInMillis - targetCal.timeInMillis).toDouble()
        } else if (diffMillis < -180 * dayMillis) {
            targetCal.add(Calendar.YEAR, -1)
            diffMillis = (calendar.timeInMillis - targetCal.timeInMillis).toDouble()
        }

        return diffMillis / dayMillis
    }

    /**
     * True if the date is within 2 days of peak.
     */
    fun isNearPeak(calendar: Calendar, toleranceDays: Double = 2.0): Boolean {
        return kotlin.math.abs(daysFromPeak(calendar)) <= toleranceDays
    }

    private fun formatDayMonth(day: Int, month: Int): String {
        val monthNames = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val mName = if (month in 1..12) monthNames[month - 1] else "$month"
        return String.format("%02d de %s", day, mName)
    }
}
