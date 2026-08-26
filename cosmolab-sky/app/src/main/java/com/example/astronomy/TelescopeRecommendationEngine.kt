package com.example.astronomy

import com.example.data.City
import kotlin.math.roundToInt

enum class ObservationTargetChoice(val label: String, val icon: String) {
    MOON("Lua", "🌙"),
    PLANETS("Planetas", "🪐"),
    DOUBLE_STARS("Estrelas duplas", "✨"),
    NEBULAE_CLUSTERS("Nebulosas e aglomerados", "🌌"),
    GALAXIES("Galáxias", "🌠"),
    EVERYTHING("Um pouco de tudo", "🔭")
}

enum class LocationTypeChoice(val label: String, val icon: String) {
    USE_MY_LOCATION("Usar minha localização", "📍"),
    SELECT_CITY("Escolher uma cidade", "🔎"),
    MULTIPLE_LOCATIONS("Vários locais", "🚗")
}

enum class DisplacementChoice(val label: String, val icon: String) {
    SINGLE_LOCATION("Principalmente em um único local", "🏠"),
    TRANSPORT_LOCATIONS("Vou transportar para diferentes locais", "🚗"),
    DARK_SKY_TRIPS("Pretendo observar frequentemente em locais de céu escuro", "🌌")
}

enum class BudgetChoice(val label: String, val maxAmountBrl: Double) {
    UP_TO_500("Até R$ 500", 500.0),
    UP_TO_1000("Até R$ 1.000", 1000.0),
    UP_TO_2000("Até R$ 2.000", 2000.0),
    UP_TO_4000("Até R$ 4.000", 4000.0),
    UP_TO_7000("Até R$ 7.000", 7000.0),
    ABOVE_7000("Acima de R$ 7.000", 250000.0)
}

enum class PortabilityChoice(val label: String, val icon: String) {
    EASY("Preciso transportar facilmente", "🧳"),
    MEDIUM("Posso transportar um equipamento médio", "⚖️"),
    NO_PROBLEM("Tamanho e peso não são um problema", "🏠")
}

enum class LocationMethodChoice(val label: String, val icon: String) {
    MANUAL("Quero apontar manualmente", "👁️"),
    GOTO("Quero localização automática / GoTo", "🤖"),
    EITHER("Tanto faz", "🔄")
}

enum class ExperienceChoice(val label: String, val icon: String) {
    NOVICE("Nunca tive telescópio", "🌱"),
    OCCASIONAL("Já observo ocasionalmente", "🔭"),
    EXPERIENCED("Tenho experiência", "🧑🚀")
}

data class TelescopeUserProfile(
    val targets: Set<ObservationTargetChoice> = setOf(ObservationTargetChoice.EVERYTHING),
    val locationType: LocationTypeChoice = LocationTypeChoice.USE_MY_LOCATION,
    val selectedCity: City,
    val displacement: DisplacementChoice = DisplacementChoice.SINGLE_LOCATION,
    val budget: BudgetChoice = BudgetChoice.UP_TO_4000,
    val portability: PortabilityChoice = PortabilityChoice.MEDIUM,
    val locationMethod: LocationMethodChoice = LocationMethodChoice.EITHER,
    val experience: ExperienceChoice = ExperienceChoice.NOVICE
)

data class TelescopeEvaluationResult(
    val model: TelescopeModel,
    val compatibilityPercent: Int,
    val planetaryScore: Int,
    val deepSkyScore: Int,
    val lunarScore: Int,
    val doubleStarScore: Int,
    val portabilityScore: Int,
    val automationScore: Int,
    val beginnerScore: Int,
    val costBenefitScore: Int,
    val explanation: String
)

data class RecommendationEngineOutput(
    val bestChoice: TelescopeEvaluationResult,
    val bestCostBenefit: TelescopeEvaluationResult?,
    val bestCompact: TelescopeEvaluationResult?,
    val outOfBudgetOption: TelescopeEvaluationResult?,
    val eligibleModelsCount: Int
)

object TelescopeRecommendationEngine {

    fun recommend(profile: TelescopeUserProfile): RecommendationEngineOutput? {
        val allModels = TelescopeCatalog.models
        if (allModels.isEmpty()) return null

        val maxBudget = profile.budget.maxAmountBrl
        val currentBortle = profile.selectedCity.bortleClass ?: BortleScale.BORTLE_4

        // Evaluate all models
        val evaluatedModels = allModels.map { model ->
            evaluateModel(model, profile, currentBortle)
        }

        // Budget filtering logic:
        // Eligible models are those with price <= maxBudget (or null price, treated as eligible fallback)
        val eligibleModels = evaluatedModels.filter { eval ->
            val price = eval.model.precoBrl
            price == null || price <= maxBudget
        }.sortedByDescending { it.compatibilityPercent }

        if (eligibleModels.isEmpty()) {
            // Fallback if no models are strictly below budget: take lowest priced models
            val fallback = evaluatedModels.sortedBy { it.model.precoBrl ?: 999999.0 }.first()
            return RecommendationEngineOutput(
                bestChoice = fallback,
                bestCostBenefit = null,
                bestCompact = null,
                outOfBudgetOption = null,
                eligibleModelsCount = 1
            )
        }

        val bestChoice = eligibleModels.first()

        // Find Best Cost-Benefit among eligible (distinct from bestChoice)
        val bestCostBenefit = eligibleModels
            .filter { it.model.id != bestChoice.model.id }
            .maxByOrNull { it.costBenefitScore * 0.7 + it.compatibilityPercent * 0.3 }

        // Find Best Compact option among eligible (distinct from bestChoice & costBenefit)
        val bestCompact = eligibleModels
            .filter { it.model.id != bestChoice.model.id && it.model.id != bestCostBenefit?.model?.id }
            .filter { it.portabilityScore >= 70 }
            .maxByOrNull { it.portabilityScore * 0.6 + it.compatibilityPercent * 0.4 }

        // Find Out-of-Budget Alternative (if profile budget is not MAX, check models slightly above maxBudget up to 35% higher)
        val outOfBudgetThreshold = maxBudget * 1.35
        val outOfBudgetOption = if (profile.budget != BudgetChoice.ABOVE_7000) {
            evaluatedModels
                .filter { eval ->
                    val price = eval.model.precoBrl
                    price != null && price > maxBudget && price <= outOfBudgetThreshold
                }
                .filter { it.compatibilityPercent >= bestChoice.compatibilityPercent - 5 }
                .maxByOrNull { it.compatibilityPercent }
        } else null

        return RecommendationEngineOutput(
            bestChoice = bestChoice,
            bestCostBenefit = bestCostBenefit,
            bestCompact = bestCompact,
            outOfBudgetOption = outOfBudgetOption,
            eligibleModelsCount = eligibleModels.size
        )
    }

    private fun evaluateModel(
        model: TelescopeModel,
        profile: TelescopeUserProfile,
        bortle: BortleScale
    ): TelescopeEvaluationResult {
        val aperture = model.aberturaMm.toDouble()
        val fRatio = model.computedRazaoFocal ?: 8.0

        // 1. Planetary Score
        var planScore = ((aperture / 180.0).coerceAtMost(1.0) * 60) + 15
        if (fRatio >= 9.0) planScore += 15
        if (model.tipoOptico.contains("Maksutov", ignoreCase = true)) planScore += 15
        if (model.tipoOptico.contains("Schmidt-Cassegrain", ignoreCase = true)) planScore += 12
        if (model.tipoOptico.contains("Refrator", ignoreCase = true)) planScore += 10
        val planetaryScore = planScore.roundToInt().coerceIn(10, 100)

        // 2. Deep Sky Score
        var dsScore = ((aperture / 250.0).coerceAtMost(1.0) * 75)
        if (fRatio in 4.0..6.0) dsScore += 15
        if (model.isDobsonian) dsScore += 10
        if (bortle.level <= 3) dsScore += 10 // Extra boost under dark skies
        val deepSkyScore = dsScore.roundToInt().coerceIn(10, 100)

        // 3. Lunar Score
        val lunarScore = ((aperture / 150.0).coerceAtMost(1.0) * 70 + 25).roundToInt().coerceIn(10, 100)

        // 4. Double Star Score
        var doubleScore = ((aperture / 150.0).coerceAtMost(1.0) * 65) + 20
        if (model.tipoOptico.contains("Refrator", ignoreCase = true) || model.tipoOptico.contains("Maksutov", ignoreCase = true)) {
            doubleScore += 15
        }
        val doubleStarScore = doubleScore.roundToInt().coerceIn(10, 100)

        // 5. Portability Score
        var portScore = 50
        if (model.isPortable || aperture <= 80) portScore += 45
        else if (aperture <= 130 && (model.isDobsonian || model.isAltAzimuth)) portScore += 30
        else if (aperture <= 150 && model.isDobsonian) portScore += 20
        else if (aperture >= 250) portScore -= 30

        if (model.isEquatorial && !model.isPortable) portScore -= 20
        val portabilityScore = portScore.coerceIn(10, 100)

        // 6. Automation Score
        val automationScore = if (model.isGoTo) 100 else if (model.montagem.contains("motorizad", ignoreCase = true)) 65 else 30

        // 7. Beginner Score
        var begScore = 50
        if (model.isAltAzimuth || model.isDobsonian || model.modelo.contains("StarSense", ignoreCase = true)) begScore += 35
        if (model.isGoTo) begScore += 10
        if (model.isEquatorial) begScore -= 25
        val beginnerScore = begScore.coerceIn(10, 100)

        // 8. Cost-Benefit Score
        val price = model.precoBrl ?: 5000.0
        val costBenefitScore = ((aperture / (price.coerceAtLeast(400.0) / 1000.0)) * 2.8).roundToInt().coerceIn(10, 100)

        // 9. Profile Compatibility Weights Calculation
        var weightedScore = 0.0
        var totalWeight = 0.0

        // Targets weighting
        val hasEverything = profile.targets.contains(ObservationTargetChoice.EVERYTHING)
        val wantsPlanets = hasEverything || profile.targets.contains(ObservationTargetChoice.PLANETS) || profile.targets.contains(ObservationTargetChoice.MOON)
        val wantsDeepSky = hasEverything || profile.targets.contains(ObservationTargetChoice.NEBULAE_CLUSTERS) || profile.targets.contains(ObservationTargetChoice.GALAXIES)
        val wantsDoubleStars = profile.targets.contains(ObservationTargetChoice.DOUBLE_STARS)

        if (wantsPlanets) {
            weightedScore += planetaryScore * 0.25
            totalWeight += 0.25
        }
        if (wantsDeepSky) {
            // Adjust deep sky importance based on Bortle or Multiple Locations
            val bortleMultiplier = if (profile.locationType == LocationTypeChoice.MULTIPLE_LOCATIONS) {
                0.25
            } else if (bortle.level >= 8) {
                0.15
            } else {
                0.30
            }
            weightedScore += deepSkyScore * bortleMultiplier
            totalWeight += bortleMultiplier
        }
        if (wantsDoubleStars) {
            weightedScore += doubleStarScore * 0.15
            totalWeight += 0.15
        }

        // 1) Physical Portability Tolerance (Question 5: tolerance for size/weight/volume)
        val physicalPortabilityWeight = when (profile.portability) {
            PortabilityChoice.EASY -> 0.25
            PortabilityChoice.MEDIUM -> 0.12
            PortabilityChoice.NO_PROBLEM -> 0.02
        }
        weightedScore += portabilityScore * physicalPortabilityWeight
        totalWeight += physicalPortabilityWeight

        // 2) Context of Use & Displacement Frequency (Question 3: usage routine)
        val displacementScore = when (profile.displacement) {
            DisplacementChoice.DARK_SKY_TRIPS -> {
                // Trips to dark sky spots: values deep sky performance + manageable transport
                (deepSkyScore * 0.6 + portabilityScore * 0.4)
            }
            DisplacementChoice.TRANSPORT_LOCATIONS -> {
                // Moving between different spots: favors good transportability and quick setup
                (portabilityScore * 0.7 + beginnerScore * 0.3)
            }
            DisplacementChoice.SINGLE_LOCATION -> {
                // Fixed location: stable telescopes with large aperture (Dobsonians/EQ) are great, weight is no issue!
                if (model.isDobsonian || aperture >= 150) 95.0 else 75.0
            }
        }
        val displacementWeight = 0.15
        weightedScore += displacementScore * displacementWeight
        totalWeight += displacementWeight

        // 3) Sky Versatility Boost for MULTIPLE_LOCATIONS
        if (profile.locationType == LocationTypeChoice.MULTIPLE_LOCATIONS) {
            val versatilityScore = ((planetaryScore + deepSkyScore) / 2.0)
            weightedScore += versatilityScore * 0.10
            totalWeight += 0.10
        }

        // Location Method / GoTo preference
        if (profile.locationMethod == LocationMethodChoice.GOTO) {
            weightedScore += automationScore * 0.25
            totalWeight += 0.25
        } else if (profile.locationMethod == LocationMethodChoice.MANUAL) {
            val manualScore = if (!model.isGoTo) 90 else 50
            weightedScore += manualScore * 0.15
            totalWeight += 0.15
        }

        // Experience weighting
        if (profile.experience == ExperienceChoice.NOVICE) {
            weightedScore += beginnerScore * 0.20
            totalWeight += 0.20
        }

        // Price appropriateness (don't waste budget if cheap option is sufficient, but favor good match)
        weightedScore += costBenefitScore * 0.10
        totalWeight += 0.10

        val rawCompatibility = if (totalWeight > 0) (weightedScore / totalWeight) else 70.0
        val finalCompatibilityPercent = rawCompatibility.roundToInt().coerceIn(45, 99)

        // Generate custom personalized explanation
        val explanation = buildExplanation(model, profile, bortle, finalCompatibilityPercent)

        return TelescopeEvaluationResult(
            model = model,
            compatibilityPercent = finalCompatibilityPercent,
            planetaryScore = planetaryScore,
            deepSkyScore = deepSkyScore,
            lunarScore = lunarScore,
            doubleStarScore = doubleStarScore,
            portabilityScore = portabilityScore,
            automationScore = automationScore,
            beginnerScore = beginnerScore,
            costBenefitScore = costBenefitScore,
            explanation = explanation
        )
    }

    private fun buildExplanation(
        model: TelescopeModel,
        profile: TelescopeUserProfile,
        bortle: BortleScale,
        compatibility: Int
    ): String {
        val cityName = profile.selectedCity.name
        val apertureStr = "${model.aberturaMm}mm"

        val targetPart = when {
            profile.targets.contains(ObservationTargetChoice.PLANETS) || profile.targets.contains(ObservationTargetChoice.MOON) ->
                "excelente desempenho na observação da Lua e planetas"
            profile.targets.contains(ObservationTargetChoice.NEBULAE_CLUSTERS) || profile.targets.contains(ObservationTargetChoice.GALAXIES) ->
                "ótima capacidade de captação de luz para nebulosas e galáxias"
            else -> "um equilíbrio versátil para observar tanto alvos do Sistema Solar quanto de céu profundo"
        }

        val bortlePart = if (profile.locationType == LocationTypeChoice.MULTIPLE_LOCATIONS) {
            "oferecendo versatilidade para uso tanto em céus urbanos quanto em viagens para céus escuros"
        } else if (bortle.level >= 7) {
            "adaptado às condições de céu urbano (${bortle.friendlyTitle}) em $cityName"
        } else {
            "aproveitando ao máximo o céu escuro de $cityName (${bortle.friendlyTitle})"
        }

        val transportPart = when (profile.displacement) {
            DisplacementChoice.TRANSPORT_LOCATIONS -> "oferecendo a mobilidade necessária para transporte frequente"
            DisplacementChoice.DARK_SKY_TRIPS -> "ideal para levar em viagens para locais com céus livres de poluição"
            else -> "com estrutura estável para observação regular no seu local de preferência"
        }

        val setupPart = when (profile.experience) {
            ExperienceChoice.NOVICE -> "com manuseio simples e intuitivo para quem está começando"
            else -> "atendendo às expectativas de quem busca precisão e detalhes ópticos"
        }

        return "Este ${model.fabricante} de $apertureStr combina $targetPart, $bortlePart, $transportPart e $setupPart."
    }
}
