package com.example.astronomy

data class StarObject(
    val name: String,
    val constellation: String,
    val raDeg: Double,
    val decDeg: Double,
    val magnitude: Double,
    val colorHex: String = "#FFFFFF"
)

data class ConstellationLine(
    val constellationName: String,
    val star1: StarObject,
    val star2: StarObject
)

object CelestialCatalog {

    // Major Bright Stars
    val SIRIUS = StarObject("Sirius", "Cão Maior", 101.287, -16.716, -1.46, "#A5F3FC")
    val CANOPUS = StarObject("Canopus", "Carina", 95.988, -52.696, -0.74, "#FEF08A")
    val RIGIL_KENTAURUS = StarObject("Alpha Centauri", "Centauro", 219.901, -60.833, -0.27, "#FEF08A")
    val ARCTURUS = StarObject("Arcturus", "Boieiro", 213.915, 19.182, -0.05, "#FFEDD5")
    val VEGA = StarObject("Vega", "Lira", 279.235, 38.784, 0.03, "#CFFAFE")
    val CAPELLA = StarObject("Capella", "Cocheiro", 79.172, 45.998, 0.08, "#FEF08A")
    val RIGEL = StarObject("Rigel", "Órion", 78.634, -8.202, 0.13, "#BAE6FD")
    val PROCYON = StarObject("Procyon", "Cão Menor", 114.825, 5.225, 0.34, "#FEF08A")
    val BETELGEUSE = StarObject("Betelgeuse", "Órion", 88.793, 7.407, 0.42, "#FFD6A5")
    val ACHERNAR = StarObject("Achernar", "Eridano", 24.429, -57.237, 0.45, "#BAE6FD")
    val HADAR = StarObject("Hadar", "Centauro", 210.956, -60.373, 0.61, "#CFFAFE")
    val ALTAIR = StarObject("Altair", "Águia", 297.696, 8.868, 0.76, "#E0F2FE")
    val ALDEBARAN = StarObject("Aldebaran", "Touro", 68.980, 16.509, 0.85, "#FFD6A5")
    val ANTARES = StarObject("Antares", "Escorpião", 247.352, -26.432, 0.96, "#FFB3BA")
    val SPICA = StarObject("Spica", "Virgem", 201.298, -11.161, 0.98, "#BAE6FD")
    val POLLUX = StarObject("Pollux", "Gêmeos", 116.329, 28.026, 1.14, "#FEF08A")
    val FOMALHAUT = StarObject("Fomalhaut", "Peixe Austral", 344.413, -29.622, 1.17, "#E0F2FE")
    val DENEB = StarObject("Deneb", "Cisne", 310.358, 45.280, 1.25, "#CFFAFE")
    val REGULUS = StarObject("Regulus", "Leão", 152.093, 11.967, 1.36, "#BAE6FD")
    val CASTOR = StarObject("Castor", "Gêmeos", 113.650, 31.888, 1.58, "#E0F2FE")
    val BELLATRIX = StarObject("Bellatrix", "Órion", 81.283, 6.350, 1.64, "#CFFAFE")
    val ALNILAM = StarObject("Alnilam", "Órion", 84.053, -1.202, 1.69, "#BAE6FD")
    val ALNITAK = StarObject("Alnitak", "Órion", 85.190, -1.943, 1.74, "#BAE6FD")
    val MINTAKA = StarObject("Mintaka", "Órion", 83.002, -0.299, 2.25, "#BAE6FD")
    val SAIPH = StarObject("Saiph", "Órion", 86.939, -9.669, 2.07, "#BAE6FD")
    val ACRUX = StarObject("Acrux", "Cruzeiro do Sul", 186.650, -63.099, 0.77, "#BAE6FD")
    val MIMOSA = StarObject("Mimosa", "Cruzeiro do Sul", 191.930, -59.689, 1.25, "#CFFAFE")
    val GACRUX = StarObject("Gacrux", "Cruzeiro do Sul", 187.792, -57.113, 1.63, "#FFD6A5")
    val DELTA_CRUCIS = StarObject("Imai", "Cruzeiro do Sul", 183.842, -58.749, 2.75, "#BAE6FD")
    val SHAULA = StarObject("Shaula", "Escorpião", 263.402, -37.104, 1.62, "#CFFAFE")
    val GRAFFIAS = StarObject("Acrab", "Escorpião", 241.358, -19.805, 2.56, "#BAE6FD")
    val DSCHUBBA = StarObject("Dschubba", "Escorpião", 240.083, -22.622, 2.29, "#BAE6FD")
    val SARGAS = StarObject("Sargas", "Escorpião", 263.858, -42.998, 1.86, "#FFEDD5")
    val DUBHE = StarObject("Dubhe", "Ursa Maior", 165.932, 61.751, 1.79, "#FEF08A")
    val MERAK = StarObject("Merak", "Ursa Maior", 165.460, 56.382, 2.37, "#CFFAFE")
    val PHECDA = StarObject("Phecda", "Ursa Maior", 178.458, 53.695, 2.44, "#CFFAFE")
    val MEGREZ = StarObject("Megrez", "Ursa Maior", 183.857, 57.032, 3.32, "#CFFAFE")
    val ALIOTH = StarObject("Alioth", "Ursa Maior", 193.507, 55.959, 1.76, "#CFFAFE")
    val MIZAR = StarObject("Mizar", "Ursa Maior", 200.981, 54.925, 2.23, "#CFFAFE")
    val ALKAID = StarObject("Alkaid", "Ursa Maior", 206.885, 49.313, 1.85, "#BAE6FD")
    val POLARIS = StarObject("Polaris", "Ursa Menor", 37.954, 89.264, 1.98, "#FEF08A")

    val brightStars = listOf(
        SIRIUS, CANOPUS, RIGIL_KENTAURUS, ARCTURUS, VEGA, CAPELLA, RIGEL, PROCYON,
        BETELGEUSE, ACHERNAR, HADAR, ALTAIR, ALDEBARAN, ANTARES, SPICA, POLLUX,
        FOMALHAUT, DENEB, REGULUS, CASTOR, BELLATRIX, ALNILAM, ALNITAK, MINTAKA,
        SAIPH, ACRUX, MIMOSA, GACRUX, DELTA_CRUCIS, SHAULA, GRAFFIAS, DSCHUBBA,
        SARGAS, DUBHE, MERAK, PHECDA, MEGREZ, ALIOTH, MIZAR, ALKAID, POLARIS
    )

    val constellationLines = listOf(
        // Cruzeiro do Sul
        ConstellationLine("Cruzeiro do Sul", ACRUX, GACRUX),
        ConstellationLine("Cruzeiro do Sul", MIMOSA, DELTA_CRUCIS),

        // Órion
        ConstellationLine("Órion", BETELGEUSE, BELLATRIX),
        ConstellationLine("Órion", BELLATRIX, RIGEL),
        ConstellationLine("Órion", RIGEL, SAIPH),
        ConstellationLine("Órion", SAIPH, BETELGEUSE),
        ConstellationLine("Órion", MINTAKA, ALNILAM),
        ConstellationLine("Órion", ALNILAM, ALNITAK),
        ConstellationLine("Órion", BETELGEUSE, ALNITAK),
        ConstellationLine("Órion", RIGEL, MINTAKA),

        // Escorpião
        ConstellationLine("Escorpião", ANTARES, GRAFFIAS),
        ConstellationLine("Escorpião", GRAFFIAS, DSCHUBBA),
        ConstellationLine("Escorpião", ANTARES, SARGAS),
        ConstellationLine("Escorpião", SARGAS, SHAULA),

        // Centauro
        ConstellationLine("Centauro", RIGIL_KENTAURUS, HADAR),

        // Gêmeos
        ConstellationLine("Gêmeos", CASTOR, POLLUX),

        // Ursa Maior
        ConstellationLine("Ursa Maior", DUBHE, MERAK),
        ConstellationLine("Ursa Maior", MERAK, PHECDA),
        ConstellationLine("Ursa Maior", PHECDA, MEGREZ),
        ConstellationLine("Ursa Maior", MEGREZ, DUBHE),
        ConstellationLine("Ursa Maior", MEGREZ, ALIOTH),
        ConstellationLine("Ursa Maior", ALIOTH, MIZAR),
        ConstellationLine("Ursa Maior", MIZAR, ALKAID)
    )
}
