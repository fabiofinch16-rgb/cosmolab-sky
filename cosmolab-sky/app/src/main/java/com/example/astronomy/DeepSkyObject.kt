package com.example.astronomy

/**
 * Types of Deep Sky Objects.
 */
enum class DeepSkyType(
    val portugueseName: String,
    val symbol: String,
    val tagColorHex: String
) {
    NEBULA("Nebulosa Difusa", "🌫️", "#EC4899"),
    PLANETARY_NEBULA("Nebulosa Planetária", "🔵", "#06B6D4"),
    OPEN_CLUSTER("Aglomerado Aberto", "✨", "#F59E0B"),
    GLOBULAR_CLUSTER("Aglomerado Globular", "🔮", "#8B5CF6"),
    GALAXY("Galáxia", "🌀", "#3B82F6"),
    DOUBLE_STAR("Estrela Dupla", "⭐", "#10B981")
}

/**
 * Astronomical representation of a Deep Sky Object (DSO).
 */
data class DeepSkyObject(
    val id: String,                    // e.g. "M42"
    val messierNgc: String,            // e.g. "M42 / NGC 1976"
    val commonName: String,            // e.g. "Nebulosa de Órion"
    val constellation: String,         // e.g. "Orion"
    val type: DeepSkyType,
    val rightAscensionHours: Double,   // e.g. 5.588
    val declinationDeg: Double,        // e.g. -5.39
    val apparentMagnitude: Double,     // e.g. 4.0
    val angularSizeArcmin: Double,     // e.g. 65.0
    val surfaceBrightness: Double? = null, // e.g. 13.0 mag/arcmin²
    val separationArcsec: Double? = null,  // For double stars
    val description: String,
    val bestFilter: String? = null,    // e.g. "UHC / OIII"
    val imageUrl: String? = null       // Prepared for future image library integration
) {
    val raDeg: Double
        get() = rightAscensionHours * 15.0
}
