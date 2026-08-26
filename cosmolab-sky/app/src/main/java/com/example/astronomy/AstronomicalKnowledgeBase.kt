package com.example.astronomy

import kotlin.math.max

/**
 * Difficulty level for resolving specific astronomical features.
 */
enum class FeatureDifficulty(val label: String) {
    EASY("Fácil"),
    MODERATE("Moderado"),
    DEMANDING("Exigente"),
    CHALLENGING("Desafiador"),
    EXCEPTIONAL("Excepcional")
}

/**
 * A specific observable feature belonging to a celestial target.
 */
data class TargetObservableFeature(
    val id: String,
    val name: String,
    val minApertureMm: Double,
    val minMagnification: Double,
    val maxRecommendedMagnification: Double? = null,
    val requiredResolutionArcsec: Double = 2.0,
    val minPlanetAngularDiameterArcsec: Double = 0.0,
    val minAltitudeDeg: Double = 10.0,
    val sensitiveToBortle: Boolean = false,
    val sensitiveToMoon: Boolean = false,
    val visibleNakedEye: Boolean = false,
    val visibleBinocular: Boolean = false,
    val visibleTelescope: Boolean = true,
    val difficulty: FeatureDifficulty = FeatureDifficulty.MODERATE,
    val descriptionWhenResolved: String,
    val descriptionWhenUnresolved: String? = null
)

/**
 * Observational profile for a planet or deep-sky object.
 */
data class ObjectObservationalProfile(
    val targetId: String,
    val isPlanet: Boolean,
    val isWideObject: Boolean = false,
    val minUsefulMagnification: Double = 20.0,
    val maxOptimalMagnification: Double = 200.0,
    val idealExitPupilMinMm: Double = 0.8,
    val idealExitPupilMaxMm: Double = 3.0,
    val features: List<TargetObservableFeature>
)

/**
 * Knowledge base containing structured features and optical requirements
 * for planets and deep-sky objects in CosmoLab Sky.
 */
object AstronomicalKnowledgeBase {

    fun getProfileForPlanet(planet: Planet): ObjectObservationalProfile {
        return when (planet) {
            Planet.SATURN -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 40.0,
                maxOptimalMagnification = 220.0,
                idealExitPupilMinMm = 0.6,
                idealExitPupilMaxMm = 2.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "saturn_disk",
                        name = "Disco Planetário",
                        minApertureMm = 10.0,
                        minMagnification = 1.0,
                        requiredResolutionArcsec = 15.0,
                        visibleNakedEye = true,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Disco amarelado distinto do fundo estelar.",
                        descriptionWhenUnresolved = "Ponto estelar amarelado no céu."
                    ),
                    TargetObservableFeature(
                        id = "saturn_ring_shape",
                        name = "Formato Oval dos Anéis",
                        minApertureMm = 40.0,
                        minMagnification = 8.0,
                        requiredResolutionArcsec = 10.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Formato oval/alongado inconfundível indicando a presença dos anéis.",
                        descriptionWhenUnresolved = "Ponto brilhante de formato estelar."
                    ),
                    TargetObservableFeature(
                        id = "saturn_ring_separation",
                        name = "Separação Disco / Anéis",
                        minApertureMm = 60.0,
                        minMagnification = 35.0,
                        requiredResolutionArcsec = 3.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Anéis visivelmente descolados do disco planetário com espaço escuro entre eles.",
                        descriptionWhenUnresolved = "Anéis fundidos ao disco parecendo 'asas' laterais coladas."
                    ),
                    TargetObservableFeature(
                        id = "saturn_titan",
                        name = "Lua Titã",
                        minApertureMm = 50.0,
                        minMagnification = 20.0,
                        requiredResolutionArcsec = 5.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Lua Titã brilhando claramente como um ponto estelar próximo a Saturno.",
                        descriptionWhenUnresolved = "Lua Titã atenuada pelo brilho do céu ou do planeta."
                    ),
                    TargetObservableFeature(
                        id = "saturn_cassini",
                        name = "Divisão de Cassini",
                        minApertureMm = 90.0,
                        minMagnification = 110.0,
                        requiredResolutionArcsec = 0.9,
                        minAltitudeDeg = 20.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "A fina linha escura da Divisão de Cassini cortando a estrutura dos anéis.",
                        descriptionWhenUnresolved = "Os anéis parecem uma folha contínua sem a separação da Divisão de Cassini."
                    ),
                    TargetObservableFeature(
                        id = "saturn_cloud_bands",
                        name = "Bandas Atmosféricas",
                        minApertureMm = 100.0,
                        minMagnification = 100.0,
                        requiredResolutionArcsec = 1.1,
                        minAltitudeDeg = 25.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Sutis variações de tom e faixas de nuvens paralelas no disco do planeta.",
                        descriptionWhenUnresolved = "Disco amarelado uniforme sem estruturas atmosféricas definidas."
                    ),
                    TargetObservableFeature(
                        id = "saturn_other_moons",
                        name = "Luas Secundárias (Réia, Dione, Tétis)",
                        minApertureMm = 114.0,
                        minMagnification = 70.0,
                        requiredResolutionArcsec = 2.0,
                        minAltitudeDeg = 15.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Pontos estelares fracos das luas Réia, Dione e Tétis orbitando o planeta.",
                        descriptionWhenUnresolved = "Luas secundárias fracas demais para a abertura ou condições atuais."
                    )
                )
            )

            Planet.JUPITER -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 30.0,
                maxOptimalMagnification = 220.0,
                idealExitPupilMinMm = 0.6,
                idealExitPupilMaxMm = 2.2,
                features = listOf(
                    TargetObservableFeature(
                        id = "jup_galilean_moons",
                        name = "Luas Galileanas (Io, Europa, Ganimedes, Calisto)",
                        minApertureMm = 30.0,
                        minMagnification = 7.0,
                        requiredResolutionArcsec = 8.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "As 4 grandes luas galileanas alinhadas ao redor de Júpiter como pontos brilhantes.",
                        descriptionWhenUnresolved = "Luas ocultadas ou ofuscadas pelo brilho excessivo."
                    ),
                    TargetObservableFeature(
                        id = "jup_main_belts",
                        name = "Faixas de Nuvens Equatoriais (NEB e SEB)",
                        minApertureMm = 60.0,
                        minMagnification = 40.0,
                        requiredResolutionArcsec = 2.5,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Duas faixas marrons/avermelhadas proeminentes cruzando o disco planetário.",
                        descriptionWhenUnresolved = "Disco planetário sem faixas visíveis de nuvens."
                    ),
                    TargetObservableFeature(
                        id = "jup_grs",
                        name = "Grande Mancha Vermelha",
                        minApertureMm = 90.0,
                        minMagnification = 100.0,
                        requiredResolutionArcsec = 1.2,
                        minAltitudeDeg = 20.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "O oval característico da Grande Mancha Vermelha incrustado na faixa equatorial.",
                        descriptionWhenUnresolved = "Mancha Vermelha indisponível ou oculta no lado oposto do planeta."
                    ),
                    TargetObservableFeature(
                        id = "jup_transits",
                        name = "Sombra de Trânsito das Luas",
                        minApertureMm = 114.0,
                        minMagnification = 120.0,
                        requiredResolutionArcsec = 1.0,
                        minAltitudeDeg = 25.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.CHALLENGING,
                        descriptionWhenResolved = "O ponto negro minúsculo da sombra de uma lua projetado sobre o topo das nuvens de Júpiter.",
                        descriptionWhenUnresolved = "Resolução óptica ou contraste insuficiente para destacar a sombra."
                    )
                )
            )

            Planet.MARS -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 60.0,
                maxOptimalMagnification = 250.0,
                idealExitPupilMinMm = 0.5,
                idealExitPupilMaxMm = 1.5,
                features = listOf(
                    TargetObservableFeature(
                        id = "mars_red_disk",
                        name = "Disco Avermelhado",
                        minApertureMm = 50.0,
                        minMagnification = 30.0,
                        requiredResolutionArcsec = 4.0,
                        visibleNakedEye = true,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Pequeno disco alaranjado/avermelhado muito denso.",
                        descriptionWhenUnresolved = "Ponto estelar vermelho cintilante."
                    ),
                    TargetObservableFeature(
                        id = "mars_polar_cap",
                        name = "Calota Polar de Gelo",
                        minApertureMm = 90.0,
                        minMagnification = 120.0,
                        requiredResolutionArcsec = 1.2,
                        minAltitudeDeg = 20.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Brilho esbranquiçado intenso da calota polar destacado no bordo do planeta.",
                        descriptionWhenUnresolved = "Calota polar pequena demais no momento ou borrada pela atmosfera."
                    ),
                    TargetObservableFeature(
                        id = "mars_syrtis_major",
                        name = "Marcas Escuras (Syrtis Major)",
                        minApertureMm = 114.0,
                        minMagnification = 140.0,
                        requiredResolutionArcsec = 1.0,
                        minAltitudeDeg = 25.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.CHALLENGING,
                        descriptionWhenResolved = "Sombreamentos sutis de rocha e poeira como Syrtis Major contrastando no disco.",
                        descriptionWhenUnresolved = "Disco alaranhado uniforme sem marcas de superfície distinguíveis."
                    )
                )
            )

            Planet.VENUS -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 30.0,
                maxOptimalMagnification = 180.0,
                idealExitPupilMinMm = 0.8,
                idealExitPupilMaxMm = 3.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "venus_phase",
                        name = "Fase Planetária (Crescente/Gibosa)",
                        minApertureMm = 50.0,
                        minMagnification = 15.0,
                        requiredResolutionArcsec = 6.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "O formato em crescente ou giboso iluminado pelo Sol, semelhante à Lua.",
                        descriptionWhenUnresolved = "Ofuscado por brilho intendo parecido com uma estrela sem formato."
                    )
                )
            )

            Planet.MERCURY -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 50.0,
                maxOptimalMagnification = 180.0,
                idealExitPupilMinMm = 0.8,
                idealExitPupilMaxMm = 2.5,
                features = listOf(
                    TargetObservableFeature(
                        id = "mercury_phase",
                        name = "Fase em Crescente",
                        minApertureMm = 70.0,
                        minMagnification = 60.0,
                        requiredResolutionArcsec = 3.5,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Pequena fase em crescente visível baixo no horizonte no crepúsculo.",
                        descriptionWhenUnresolved = "Ponto cintilante muito turbulento devido à baixa altitude."
                    )
                )
            )

            Planet.URANUS -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 80.0,
                maxOptimalMagnification = 220.0,
                idealExitPupilMinMm = 0.6,
                idealExitPupilMaxMm = 2.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "uranus_disk",
                        name = "Disco Azul-Esverdeado",
                        minApertureMm = 80.0,
                        minMagnification = 90.0,
                        requiredResolutionArcsec = 3.6,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Pequeno disco calmo de tonalidade esverdeada/azulada distinto de uma estrela.",
                        descriptionWhenUnresolved = "Aparece como um ponto estelar pontual sem diâmetro aparente."
                    )
                )
            )

            Planet.NEPTUNE -> ObjectObservationalProfile(
                targetId = planet.id,
                isPlanet = true,
                isWideObject = false,
                minUsefulMagnification = 120.0,
                maxOptimalMagnification = 250.0,
                idealExitPupilMinMm = 0.5,
                idealExitPupilMaxMm = 1.8,
                features = listOf(
                    TargetObservableFeature(
                        id = "neptune_disk",
                        name = "Disco Azul Intenso",
                        minApertureMm = 100.0,
                        minMagnification = 140.0,
                        requiredResolutionArcsec = 2.3,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Minúsculo disco azul de tom azul-cobalto distinguível de estrelas vizinhas.",
                        descriptionWhenUnresolved = "Aparece indistinguível de um ponto estelar fraco."
                    )
                )
            )
        }
    }

    fun getProfileForDso(dso: DeepSkyObject): ObjectObservationalProfile {
        return when (dso.id.uppercase()) {
            "M42" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = true,
                minUsefulMagnification = 15.0,
                maxOptimalMagnification = 120.0,
                idealExitPupilMinMm = 1.5,
                idealExitPupilMaxMm = 5.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "m42_glow",
                        name = "Nuvem de Gás Luminescente",
                        minApertureMm = 30.0,
                        minMagnification = 7.0,
                        sensitiveToBortle = true,
                        visibleNakedEye = true,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Vasta estrutura de gás luminescente em asas desdobrando-se no espaço.",
                        descriptionWhenUnresolved = "Mancha nebulosa suave e tênue."
                    ),
                    TargetObservableFeature(
                        id = "m42_trapezium",
                        name = "Aglomerado do Trapézio",
                        minApertureMm = 60.0,
                        minMagnification = 30.0,
                        requiredResolutionArcsec = 2.5,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "As 4 estrelas jovens do Trapézio (A, B, C, D) reluzindo nitidamente no coração da nebulosa.",
                        descriptionWhenUnresolved = "Estrelas centrais fundidas em uma mancha luminosa única."
                    ),
                    TargetObservableFeature(
                        id = "m42_dust_lanes",
                        name = "Faixas Escuras de Poeira",
                        minApertureMm = 90.0,
                        minMagnification = 50.0,
                        sensitiveToBortle = true,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Recortes e cavidades de poeira escura silhuetados contra a névoa iluminada.",
                        descriptionWhenUnresolved = "Gás luminescente contínuo sem alto contraste de silhueta."
                    )
                )
            )

            "M45" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = true,
                minUsefulMagnification = 10.0,
                maxOptimalMagnification = 50.0,
                idealExitPupilMinMm = 3.0,
                idealExitPupilMaxMm = 7.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "m45_stars",
                        name = "Estrelas Principais (Sete Irmãs)",
                        minApertureMm = 10.0,
                        minMagnification = 1.0,
                        visibleNakedEye = true,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Dezenas de estrelas azuis reluzentes agrupadas em formato de mini-colher.",
                        descriptionWhenUnresolved = "Mancha luminosa nebulosa no céu."
                    ),
                    TargetObservableFeature(
                        id = "m45_nebula",
                        name = "Nebulosa de Reflexão Azulada",
                        minApertureMm = 100.0,
                        minMagnification = 20.0,
                        sensitiveToBortle = true,
                        sensitiveToMoon = true,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "Tênue véu de névoa de reflexão azuis ao redor da estrela Merope.",
                        descriptionWhenUnresolved = "Névoa apagada pelo brilho de poluição luminosa ou luar."
                    )
                )
            )

            "M31" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = true,
                minUsefulMagnification = 15.0,
                maxOptimalMagnification = 60.0,
                idealExitPupilMinMm = 2.5,
                idealExitPupilMaxMm = 6.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "m31_core",
                        name = "Núcleo Galáctico Brilhante",
                        minApertureMm = 30.0,
                        minMagnification = 7.0,
                        visibleNakedEye = true,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Núcleo denso e ovalado brilhando intensamente no centro da galáxia.",
                        descriptionWhenUnresolved = "Mancha difusa apagada em céus urbanos."
                    ),
                    TargetObservableFeature(
                        id = "m31_companions",
                        name = "Galáxias Companheiras (M32 e M110)",
                        minApertureMm = 70.0,
                        minMagnification = 25.0,
                        sensitiveToBortle = true,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "As galáxias satélites M32 e M110 visíveis como pequenas manchas ovais próximas.",
                        descriptionWhenUnresolved = "Galáxias companheiras apagadas pela poluição luminosa."
                    ),
                    TargetObservableFeature(
                        id = "m31_dust_lanes",
                        name = "Faixas de Poeira em Espiral",
                        minApertureMm = 114.0,
                        minMagnification = 35.0,
                        sensitiveToBortle = true,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.DEMANDING,
                        descriptionWhenResolved = "A faixa escura de poeira delimitando a borda do disco espiral de Andrômeda.",
                        descriptionWhenUnresolved = "Disco da galáxia sem contraste de bordas e sem detalhes de poeira."
                    )
                )
            )

            "M13" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = false,
                minUsefulMagnification = 40.0,
                maxOptimalMagnification = 180.0,
                idealExitPupilMinMm = 1.0,
                idealExitPupilMaxMm = 3.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "m13_halo",
                        name = "Halo de Algodão",
                        minApertureMm = 40.0,
                        minMagnification = 10.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Esfera nebulosa intensa e brilhante no campo de visão.",
                        descriptionWhenUnresolved = "Objeto muito tênue quase imperceptível."
                    ),
                    TargetObservableFeature(
                        id = "m13_resolved_stars",
                        name = "Resolução de Estrelas Individuais",
                        minApertureMm = 90.0,
                        minMagnification = 70.0,
                        requiredResolutionArcsec = 1.5,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Bordas do aglomerado 'pipocando' em dezenas de faíscas estelares pontuais resolvidas.",
                        descriptionWhenUnresolved = "Aglomerado aparece como bola de névoa sem estrelas pontuais separadas."
                    )
                )
            )

            "M57" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = false,
                minUsefulMagnification = 60.0,
                maxOptimalMagnification = 200.0,
                idealExitPupilMinMm = 0.8,
                idealExitPupilMaxMm = 2.2,
                features = listOf(
                    TargetObservableFeature(
                        id = "m57_ring",
                        name = "Formato de Anel Smoke-Ring",
                        minApertureMm = 70.0,
                        minMagnification = 50.0,
                        requiredResolutionArcsec = 2.0,
                        visibleNakedEye = false,
                        visibleBinocular = false,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Anel nebuloso com o centro visivelmente mais escuro e vazado.",
                        descriptionWhenUnresolved = "Pequeno disco nebuloso parecido com uma estrela desfocada."
                    )
                )
            )

            "ALBIREO" -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = false,
                minUsefulMagnification = 20.0,
                maxOptimalMagnification = 120.0,
                idealExitPupilMinMm = 1.0,
                idealExitPupilMaxMm = 4.0,
                features = listOf(
                    TargetObservableFeature(
                        id = "albireo_split",
                        name = "Separação das Estrelas Duplas",
                        minApertureMm = 40.0,
                        minMagnification = 15.0,
                        requiredResolutionArcsec = 34.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "O par estelar separado nitidamente em duas componentes distintas.",
                        descriptionWhenUnresolved = "Estrela única sem separação visual."
                    ),
                    TargetObservableFeature(
                        id = "albireo_colors",
                        name = "Contraste de Cores (Dourado & Azul Safira)",
                        minApertureMm = 60.0,
                        minMagnification = 25.0,
                        visibleNakedEye = false,
                        visibleBinocular = true,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.EASY,
                        descriptionWhenResolved = "Incrível contraste de cores entre a primária dourada/alaranjada e a secundária azul-safira.",
                        descriptionWhenUnresolved = "Cores lavadas pela pouca abertura ou aumento inadequado."
                    )
                )
            )

            else -> ObjectObservationalProfile(
                targetId = dso.id,
                isPlanet = false,
                isWideObject = dso.angularSizeArcmin > 20.0,
                minUsefulMagnification = if (dso.angularSizeArcmin > 30.0) 15.0 else 30.0,
                maxOptimalMagnification = if (dso.angularSizeArcmin > 30.0) 80.0 else 160.0,
                idealExitPupilMinMm = 1.0,
                idealExitPupilMaxMm = 4.5,
                features = listOf(
                    TargetObservableFeature(
                        id = "${dso.id}_main",
                        name = dso.commonName.ifEmpty { dso.messierNgc },
                        minApertureMm = 50.0,
                        minMagnification = 15.0,
                        sensitiveToBortle = true,
                        visibleNakedEye = dso.apparentMagnitude <= 5.0,
                        visibleBinocular = dso.apparentMagnitude <= 8.5,
                        visibleTelescope = true,
                        difficulty = FeatureDifficulty.MODERATE,
                        descriptionWhenResolved = "Forma característica e brilho concentrado detectados no campo de visão.",
                        descriptionWhenUnresolved = "Objeto difuso e tênue no limite de detecção."
                    )
                )
            )
        }
    }
}
