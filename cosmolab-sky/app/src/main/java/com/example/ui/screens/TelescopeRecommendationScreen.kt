package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.*
import com.example.data.City
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelescopeRecommendationScreen(
    viewModel: CosmoLabViewModel
) {
    val currentCity = viewModel.selectedCity

    var selectedTargets by remember {
        mutableStateOf(setOf(ObservationTargetChoice.EVERYTHING))
    }
    var locationType by remember {
        mutableStateOf(LocationTypeChoice.USE_MY_LOCATION)
    }
    var selectedCity by remember {
        mutableStateOf(currentCity)
    }
    var displacement by remember {
        mutableStateOf(DisplacementChoice.SINGLE_LOCATION)
    }
    var budget by remember {
        mutableStateOf(BudgetChoice.UP_TO_4000)
    }
    var portability by remember {
        mutableStateOf(PortabilityChoice.MEDIUM)
    }
    var locationMethod by remember {
        mutableStateOf(LocationMethodChoice.EITHER)
    }
    var experience by remember {
        mutableStateOf(ExperienceChoice.NOVICE)
    }

    var showCitySearchDialog by remember { mutableStateOf(false) }

    var activeAlternativeDetail by remember {
        mutableStateOf<Pair<String, TelescopeEvaluationResult>?>(null)
    }

    // Keep selectedCity in sync when city changes
    LaunchedEffect(currentCity) {
        selectedCity = currentCity
    }

    val userProfile = TelescopeUserProfile(
        targets = selectedTargets,
        locationType = locationType,
        selectedCity = selectedCity,
        displacement = displacement,
        budget = budget,
        portability = portability,
        locationMethod = locationMethod,
        experience = experience
    )

    val recommendationOutput = remember(userProfile) {
        TelescopeRecommendationEngine.recommend(userProfile)
    }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    }

    if (activeAlternativeDetail != null && recommendationOutput != null) {
        AlternativeDetailComparisonView(
            badgeTitle = activeAlternativeDetail!!.first,
            badgeColor = when {
                activeAlternativeDetail!!.first.contains("CUSTO-BENEFÍCIO") -> Color(0xFF38BDF8)
                activeAlternativeDetail!!.first.contains("COMPACTA") -> Color(0xFF34D399)
                else -> Color(0xFFFACC15)
            },
            bestChoice = recommendationOutput.bestChoice,
            altEval = activeAlternativeDetail!!.second,
            currencyFormat = currencyFormat,
            onBack = { activeAlternativeDetail = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
        // Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🔭", fontSize = 26.sp)
                    Text(
                        text = "ENCONTRE SEU TELESCÓPIO",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Descubra qual telescópio combina com seus objetivos, seu céu, sua experiência e sua rotina de observação.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // --- QUESTIONNAIRE SECTION ---

        // 1. O QUE VOCÊ QUER OBSERVAR?
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. O que você quer observar?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val allTargets = ObservationTargetChoice.entries
                    val chunked = allTargets.chunked(2)

                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { choice ->
                                val isSelected = selectedTargets.contains(choice)
                                SelectChip(
                                    text = "${choice.icon} ${choice.label}",
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (choice == ObservationTargetChoice.EVERYTHING) {
                                            selectedTargets = setOf(ObservationTargetChoice.EVERYTHING)
                                        } else {
                                            val newSet = selectedTargets.minus(ObservationTargetChoice.EVERYTHING).toMutableSet()
                                            if (isSelected) newSet.remove(choice) else newSet.add(choice)
                                            selectedTargets = if (newSet.isEmpty()) setOf(ObservationTargetChoice.EVERYTHING) else newSet
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. ONDE VOCÊ PRETENDE OBSERVAR?
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Onde você pretende observar?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationTypeChoice.entries.forEach { locChoice ->
                        val isSelected = locationType == locChoice
                        SelectChip(
                            text = "${locChoice.icon} ${locChoice.label}",
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                locationType = locChoice
                                if (locChoice == LocationTypeChoice.SELECT_CITY) {
                                    showCitySearchDialog = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location Details Badge
                val bortle = selectedCity.bortleClass ?: BortleScale.BORTLE_4
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1B192A))
                        .border(1.dp, CosmicPurpleContainer, RoundedCornerShape(14.dp))
                        .clickable { showCitySearchDialog = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val locTitle = if (locationType == LocationTypeChoice.MULTIPLE_LOCATIONS) {
                                    "🚗 Vários locais (Base: ${selectedCity.displayName})"
                                } else {
                                    "📍 ${selectedCity.displayName}"
                                }
                                Text(
                                    text = locTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "✏️",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            val locSub = if (locationType == LocationTypeChoice.MULTIPLE_LOCATIONS) {
                                "🌌 Versatilidade para múltiplos céus (Subúrbio & Céu Escuro)"
                            } else {
                                "🌌 Bortle ${bortle.level} — ${bortle.friendlyTitle}"
                            }
                            Text(
                                text = locSub,
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CosmicPurpleContainer
                        ) {
                            Text(
                                text = "Mudar Cidade",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. PERFIL DE DESLOCAMENTO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Como pretende utilizar o equipamento?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplacementChoice.entries.forEach { dispChoice ->
                        val isSelected = displacement == dispChoice
                        SelectOptionCard(
                            title = "${dispChoice.icon} ${dispChoice.label}",
                            isSelected = isSelected,
                            onClick = { displacement = dispChoice }
                        )
                    }
                }
            }
        }

        // 4. ORÇAMENTO — LIMITE MÁXIMO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Quanto pretende investir? (Limite Máximo)",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "O valor representa um LIMITE MÁXIMO. O algoritmo analisará todos os modelos abaixo deste valor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val budgetOptions = BudgetChoice.entries
                val chunkedBudget = budgetOptions.chunked(2)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunkedBudget.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { bChoice ->
                                val isSelected = budget == bChoice
                                SelectChip(
                                    text = bChoice.label,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { budget = bChoice }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. PORTABILIDADE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5. Quanto a portabilidade importa?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PortabilityChoice.entries.forEach { portChoice ->
                        val isSelected = portability == portChoice
                        SelectOptionCard(
                            title = "${portChoice.icon} ${portChoice.label}",
                            isSelected = isSelected,
                            onClick = { portability = portChoice }
                        )
                    }
                }
            }
        }

        // 6. LOCALIZAÇÃO DOS OBJETOS (Manual vs GoTo)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "6. Como prefere localizar os objetos?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationMethodChoice.entries.forEach { methodChoice ->
                        val isSelected = locationMethod == methodChoice
                        SelectChip(
                            text = "${methodChoice.icon} ${methodChoice.label}",
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { locationMethod = methodChoice }
                        )
                    }
                }
            }
        }

        // 7. EXPERIÊNCIA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "7. Qual sua experiência com telescópios?",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExperienceChoice.entries.forEach { expChoice ->
                        val isSelected = experience == expChoice
                        SelectChip(
                            text = "${expChoice.icon} ${expChoice.label}",
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { experience = expChoice }
                        )
                    }
                }
            }
        }

        // --- RESULTS DISPLAY SECTION ---
        if (recommendationOutput != null) {
            val best = recommendationOutput.bestChoice

            Text(
                text = "🎯 RECOMENDAÇÃO PERSONALIZADA",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // 🥇 MELHOR PARA VOCÊ (PRIMARY RECOMMENDATION)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, CosmicPurplePrimary, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E162B))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = "🥇 MELHOR PARA VOCÊ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CosmicPurpleContainer
                        ) {
                            Text(
                                text = "${best.compatibilityPercent}% COMPATÍVEL",
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = best.model.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (best.model.precoBrl != null) currencyFormat.format(best.model.precoBrl) else "Preço sob consulta",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Explanation Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF120E1A))
                            .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "💡 Por que recomendamos:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = best.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Performance Indicators Matrix
                    Text(
                        text = "INDICADORES DE DESEMPENHO:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val indicators = listOf(
                        Triple("🪐 Planetas", best.planetaryScore, CosmicPurplePrimary),
                        Triple("🌌 Céu profundo", best.deepSkyScore, Color(0xFF38BDF8)),
                        Triple("🌙 Lua", best.lunarScore, Color(0xFFFBBF24)),
                        Triple("✨ Estrelas duplas", best.doubleStarScore, Color(0xFFA78BFA)),
                        Triple("🎒 Portabilidade", best.portabilityScore, Color(0xFF34D399)),
                        Triple("🤖 Automação", best.automationScore, Color(0xFFF43F5E)),
                        Triple("🌱 Facilidade", best.beginnerScore, Color(0xFF4ADE80)),
                        Triple("💰 Custo-benefício", best.costBenefitScore, Color(0xFFFACC15))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        indicators.forEach { (label, value, color) ->
                            ScoreIndicatorRow(label = label, score = value, color = color)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Specs summary chips
                    Text(
                        text = "ESPECIFICAÇÕES TÉCNICAS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpecTag(text = "🔍 ${best.model.aberturaMm}mm")
                        if (best.model.distanciaFocalMm != null) {
                            SpecTag(text = "📏 ${best.model.distanciaFocalMm}mm (f/${String.format(Locale.US, "%.1f", best.model.computedRazaoFocal ?: 0.0)})")
                        }
                        SpecTag(text = "🔭 ${best.model.tipoOptico}")
                    }
                }
            }

            // ALTERNATIVES
            if (recommendationOutput.bestCostBenefit != null || recommendationOutput.bestCompact != null || recommendationOutput.outOfBudgetOption != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📊 OUTRAS EXCELENTES OPÇÕES",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                // 🥈 MELHOR CUSTO-BENEFÍCIO
                recommendationOutput.bestCostBenefit?.let { cb ->
                    AlternativeTelescopeCard(
                        badgeTitle = "🥈 MELHOR CUSTO-BENEFÍCIO",
                        badgeColor = Color(0xFF38BDF8),
                        eval = cb,
                        currencyFormat = currencyFormat,
                        onClick = {
                            activeAlternativeDetail = Pair("🥈 MELHOR CUSTO-BENEFÍCIO", cb)
                        }
                    )
                }

                // 🥉 MELHOR OPÇÃO COMPACTA
                recommendationOutput.bestCompact?.let { compact ->
                    AlternativeTelescopeCard(
                        badgeTitle = "🥉 MELHOR OPÇÃO COMPACTA",
                        badgeColor = Color(0xFF34D399),
                        eval = compact,
                        currencyFormat = currencyFormat,
                        onClick = {
                            activeAlternativeDetail = Pair("🥉 MELHOR OPÇÃO COMPACTA", compact)
                        }
                    )
                }

                // 💡 ALTERNATIVA ACIMA DO ORÇAMENTO
                recommendationOutput.outOfBudgetOption?.let { out ->
                    AlternativeTelescopeCard(
                        badgeTitle = "💡 ALTERNATIVA ACIMA DO ORÇAMENTO",
                        badgeColor = Color(0xFFFACC15),
                        eval = out,
                        currencyFormat = currencyFormat,
                        onClick = {
                            activeAlternativeDetail = Pair("💡 ALTERNATIVA ACIMA DO ORÇAMENTO", out)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCitySearchDialog) {
        CitySearchModal(
            viewModel = viewModel,
            onDismiss = { showCitySearchDialog = false }
        )
    }
}
}

@Composable
private fun SelectChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CosmicPurplePrimary else Color(0xFF1F1D2C))
            .border(
                width = 1.dp,
                color = if (isSelected) CosmicPurplePrimary else SpaceBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) CosmicPurpleOnPrimary else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun SelectOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF2E1C48) else Color(0xFF1F1D2C))
            .border(
                width = 1.dp,
                color = if (isSelected) CosmicPurplePrimary else SpaceBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CosmicPurplePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreIndicatorRow(
    label: String,
    score: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(130.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF151421))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (score / 100.0f).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun SpecTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF171524),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AlternativeTelescopeCard(
    badgeTitle: String,
    badgeColor: Color,
    eval: TelescopeEvaluationResult,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${eval.compatibilityPercent}% compatível",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = eval.model.fullName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (eval.model.precoBrl != null) currencyFormat.format(eval.model.precoBrl) else "Preço sob consulta",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF34D399),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = eval.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔎 Toque para ver análise completa e comparar →",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun AlternativeDetailComparisonView(
    badgeTitle: String,
    badgeColor: Color,
    bestChoice: TelescopeEvaluationResult,
    altEval: TelescopeEvaluationResult,
    currencyFormat: NumberFormat,
    onBack: () -> Unit
) {
    var showDirectComparison by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpaceCardSurface,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
            ) {
                Text("← Voltar às recomendações", fontWeight = FontWeight.Bold, color = CosmicPurplePrimary)
            }
        }

        // Comparison Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CosmicPurpleContainer, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1728))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🔎 COMPARANDO COM SUA ESCOLHA PRINCIPAL",
                    style = MaterialTheme.typography.titleSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🥇 Escolha Principal:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bestChoice.model.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${bestChoice.compatibilityPercent}% compatível",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📊 Esta Alternativa:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = altEval.model.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${altEval.compatibilityPercent}% compatível",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Detailed Card for Alternative Model (Exact same layout & structure as primary choice)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, badgeColor, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E162B))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicPurpleContainer
                    ) {
                        Text(
                            text = "${altEval.compatibilityPercent}% COMPATÍVEL",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = altEval.model.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (altEval.model.precoBrl != null) currencyFormat.format(altEval.model.precoBrl) else "Preço sob consulta",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Explanation Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF120E1A))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Por que recomendamos:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = altEval.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Performance Indicators Matrix
                Text(
                    text = "INDICADORES DE DESEMPENHO:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val indicators = listOf(
                    Triple("🪐 Planetas", altEval.planetaryScore, CosmicPurplePrimary),
                    Triple("🌌 Céu profundo", altEval.deepSkyScore, Color(0xFF38BDF8)),
                    Triple("🌙 Lua", altEval.lunarScore, Color(0xFFFBBF24)),
                    Triple("✨ Estrelas duplas", altEval.doubleStarScore, Color(0xFFA78BFA)),
                    Triple("🎒 Portabilidade", altEval.portabilityScore, Color(0xFF34D399)),
                    Triple("🤖 Automação", altEval.automationScore, Color(0xFFF43F5E)),
                    Triple("🌱 Facilidade", altEval.beginnerScore, Color(0xFF4ADE80)),
                    Triple("💰 Custo-benefício", altEval.costBenefitScore, Color(0xFFFACC15))
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    indicators.forEach { (label, value, color) ->
                        ScoreIndicatorRow(label = label, score = value, color = color)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specs summary chips
                Text(
                    text = "ESPECIFICAÇÕES TÉCNICAS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SpecTag(text = "🔍 ${altEval.model.aberturaMm}mm")
                    if (altEval.model.distanciaFocalMm != null) {
                        SpecTag(text = "📏 ${altEval.model.distanciaFocalMm}mm (f/${String.format(Locale.US, "%.1f", altEval.model.computedRazaoFocal ?: 0.0)})")
                    }
                    SpecTag(text = "🔭 ${altEval.model.tipoOptico}")
                }
            }
        }

        // Comparison Section with Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { showDirectComparison = !showDirectComparison },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B203E),
                        contentColor = CosmicPurplePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (showDirectComparison) "⚖️ Ocultar comparação com o melhor para você" else "⚖️ Comparar com o melhor para você",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (showDirectComparison) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "COMPARAÇÃO DIRETA DE INDICADORES",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Indicador",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "Principal",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.9f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Alternativa",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.9f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SpaceBorder)

                    val comparisonRows = listOf(
                        Triple("🪐 Planetas", bestChoice.planetaryScore, altEval.planetaryScore),
                        Triple("🌌 Céu profundo", bestChoice.deepSkyScore, altEval.deepSkyScore),
                        Triple("🌙 Lua", bestChoice.lunarScore, altEval.lunarScore),
                        Triple("✨ Estrelas duplas", bestChoice.doubleStarScore, altEval.doubleStarScore),
                        Triple("🎒 Portabilidade", bestChoice.portabilityScore, altEval.portabilityScore),
                        Triple("🤖 Automação", bestChoice.automationScore, altEval.automationScore),
                        Triple("🌱 Facilidade", bestChoice.beginnerScore, altEval.beginnerScore),
                        Triple("💰 Custo-benefício", bestChoice.costBenefitScore, altEval.costBenefitScore)
                    )

                    comparisonRows.forEach { (label, scoreP, scoreA) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.weight(1.3f),
                                fontSize = 12.sp
                            )

                            val isWinnerP = scoreP > scoreA
                            Text(
                                text = "$scoreP%" + if (isWinnerP) " ★" else "",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWinnerP) Color(0xFF4ADE80) else TextPrimary,
                                fontWeight = if (isWinnerP) FontWeight.ExtraBold else FontWeight.Normal,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp
                            )

                            val isWinnerA = scoreA > scoreP
                            Text(
                                text = "$scoreA%" + if (isWinnerA) " ★" else "",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWinnerA) Color(0xFF4ADE80) else TextPrimary,
                                fontWeight = if (isWinnerA) FontWeight.ExtraBold else FontWeight.Normal,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CosmicPurplePrimary,
                contentColor = CosmicPurpleOnPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("← Voltar às recomendações", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
