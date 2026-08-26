package com.example.astronomy

/**
 * The Bortle Dark-Sky Scale measures night sky darkness from 1 (Class 1: Excellent dark sky)
 * to 9 (Class 9: Inner-city sky).
 */
enum class BortleScale(
    val level: Int,
    val friendlyTitle: String,
    val description: String,
    val sqmRange: String,
    val maxNakedEyeMag: Double
) {
    BORTLE_1(1, "Céu Excepcional", "Sem poluição luminosa. A Via Láctea projeta sombras no chão.", "21.9 - 22.0", 7.8),
    BORTLE_2(2, "Céu Extraordinário", "Poluição negligenciável. Poeira e luz zodiacal bem visíveis.", "21.5 - 21.9", 7.3),
    BORTLE_3(3, "Céu Muito Escuro", "Leve domo de luz no horizonte distante. Via Láctea rica em detalhes.", "21.3 - 21.5", 6.8),
    BORTLE_4(4, "Céu Escuro", "Brilho de poluição visível sobre cidades distantes. Via Láctea bem definida.", "20.4 - 21.3", 6.3),
    BORTLE_5(5, "Céu Moderado", "Via Láctea fraca perto do zênite. Fontes luminosas visíveis ao redor.", "19.1 - 20.4", 5.8),
    BORTLE_6(6, "Céu Iluminado", "Via Láctea visível apenas no zênite. Céu com tom cinza-azulado.", "18.0 - 19.1", 5.3),
    BORTLE_7(7, "Céu Muito Iluminado", "Brilho difuso em todo o céu. Via Láctea totalmente invisível.", "18.0 - 18.5", 4.8),
    BORTLE_8(8, "Céu Urbano", "Céu acinzentado ou alaranjado. Apenas constelações principais visíveis.", "< 18.0", 4.2),
    BORTLE_9(9, "Céu Extremamente Iluminado", "Céu muito brilhante. Apenas Lua, planetas e poucas estrelas brilhantes.", "< 17.5", 3.5);

    val title: String
        get() = "Bortle $level — $friendlyTitle"

    val shortName: String
        get() = friendlyTitle

    val displayLabel: String
        get() = "$friendlyTitle · Bortle $level"

    companion object {
        fun fromLevel(level: Int): BortleScale = entries.firstOrNull { it.level == level } ?: BORTLE_4
    }
}
