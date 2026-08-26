package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.astronomy.ObservationQuality
import com.example.astronomy.Planet
import com.example.astronomy.PlanetObservation
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleContainer
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.SpaceHeaderGradientTop
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import com.example.astronomy.TelescopeEquipment
import com.example.ui.imageResId
import java.text.SimpleDateFormat
import java.util.Locale

import com.example.ui.components.CosmoLabHeader

@Composable
fun SkyScreen(
    viewModel: CosmoLabViewModel,
    onNavigateToPlanet: (Planet) -> Unit,
    onNavigateToDso: (com.example.astronomy.DeepSkyObject) -> Unit = {},
    onNavigateToMeteorShower: (com.example.astronomy.MeteorShower) -> Unit = {},
    onNavigateToMoon: () -> Unit = {},
    onOpenChangeContextModal: () -> Unit
) {
    val top20Targets = viewModel.top20TelescopeTargets
    val equipment = viewModel.telescopeEquipment
    val bortle = viewModel.bortleScale
    val currentCity = viewModel.selectedCity
    val calendar = viewModel.selectedCalendar
    val context = LocalContext.current

    val nowCal = java.util.Calendar.getInstance()
    val isToday = calendar.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
    val isCurrentMoment = isToday && Math.abs(calendar.timeInMillis - nowCal.timeInMillis) < 45 * 60 * 1000L

    val dynamicSectionTitle = when {
        isToday && isCurrentMoment -> "🔭 O QUE OBSERVAR AGORA"
        isToday -> "🌌 O QUE OBSERVAR HOJE"
        else -> "🌌 O QUE OBSERVAR NESSA DATA"
    }
    val dynamicSectionSubtitle = "Os melhores alvos disponíveis para observação"

    var showTelescopeModal by remember { mutableStateOf(false) }
    var showBinocularModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // Top Header
        CosmoLabHeader(
            viewModel = viewModel,
            onOpenChangeContextModal = onOpenChangeContextModal
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Observation Mode Selection & Target Recommendations Section
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌌 COMO ESTOU OLHANDO PARA O CÉU",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    if (viewModel.observationMode == com.example.astronomy.ObservationMode.TELESCOPE) {
                        Text(
                            text = "Configurar ⚙️",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showTelescopeModal = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else if (viewModel.observationMode == com.example.astronomy.ObservationMode.BINOCULAR) {
                        Text(
                            text = "Configurar ⚙️",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showBinocularModal = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Three small buttons side-by-side: 👁️ Olho nu | 🔭 Binóculo | 🔭 Telescópio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    com.example.astronomy.ObservationMode.entries.forEach { mode ->
                        val isSelected = viewModel.observationMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CosmicPurplePrimary else Color.Transparent)
                                .clickable { viewModel.selectObservationMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.buttonLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) CosmicPurpleOnPrimary else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equipment Card depending on mode
                when (viewModel.observationMode) {
                    com.example.astronomy.ObservationMode.NAKED_EYE -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "👁️", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Visão a Olho Nu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Exibindo objetos visíveis e identificáveis a olho nu sob o céu atual.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    com.example.astronomy.ObservationMode.BINOCULAR -> {
                        BinocularEquipmentCard(
                            apertureMm = viewModel.binocularApertureMm,
                            magnification = viewModel.binocularMagnification,
                            onEdit = { showBinocularModal = true }
                        )
                    }
                    com.example.astronomy.ObservationMode.TELESCOPE -> {
                        TelescopeEquipmentCard(
                            equipment = equipment,
                            bortle = bortle,
                            onEdit = { showTelescopeModal = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val modeSubtitle = when (viewModel.observationMode) {
                    com.example.astronomy.ObservationMode.NAKED_EYE -> "Objetos destacados para observação direta sem instrumentos"
                    com.example.astronomy.ObservationMode.BINOCULAR -> "Melhores objetos selecionados para amplificação e campo amplo de binóculos"
                    com.example.astronomy.ObservationMode.TELESCOPE -> "Alvos ordenados pela qualidade de observação no ocular do seu telescópio"
                }

                Column {
                    Text(
                        text = dynamicSectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = modeSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            itemsIndexed(top20Targets) { index, targetEval ->
                TelescopeTargetRow(
                    rankNumber = index + 1,
                    evaluation = targetEval,
                    bortle = bortle,
                    onPlanetClick = { planet -> onNavigateToPlanet(planet) },
                    onDsoClick = { dso -> onNavigateToDso(dso) },
                    onMeteorShowerClick = { shower -> onNavigateToMeteorShower(shower) },
                    onMoonClick = { onNavigateToMoon() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showTelescopeModal) {
        TelescopeEquipmentModal(
            currentEquipment = equipment,
            onSave = { ap, fl, ep, fov ->
                viewModel.updateTelescopeEquipment(ap, fl, ep, fov)
                showTelescopeModal = false
            },
            onDismiss = { showTelescopeModal = false }
        )
    }

    if (showBinocularModal) {
        BinocularEquipmentModal(
            currentApertureMm = viewModel.binocularApertureMm,
            currentMagnification = viewModel.binocularMagnification,
            onSave = { ap, mag ->
                viewModel.updateBinocularEquipment(ap, mag)
                showBinocularModal = false
            },
            onDismiss = { showBinocularModal = false }
        )
    }
}

@Composable
fun TelescopeEquipmentCard(
    equipment: com.example.astronomy.TelescopeEquipment,
    bortle: com.example.astronomy.BortleScale,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp))
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔭 EQUIPAMENTO & CÉU",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${String.format("%.0f", equipment.magnification)}× de Aumento",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Abertura: ${equipment.apertureMm.toInt()} mm • Focal: ${equipment.focalLengthMm.toInt()} mm • Ocular: ${equipment.eyepieceFocalLengthMm.toInt()} mm",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pupila: ${String.format("%.1f", equipment.exitPupilMm)} mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicPurpleContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🌌 ${bortle.displayLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TelescopeTargetRow(
    rankNumber: Int,
    evaluation: com.example.astronomy.TelescopeTargetEvaluation,
    bortle: com.example.astronomy.BortleScale = com.example.astronomy.BortleScale.BORTLE_5,
    onPlanetClick: (Planet) -> Unit,
    onDsoClick: (com.example.astronomy.DeepSkyObject) -> Unit,
    onMeteorShowerClick: (com.example.astronomy.MeteorShower) -> Unit = {},
    onMoonClick: () -> Unit = {}
) {
    val target = evaluation.target
    val observationMethods = computeObservationMethods(evaluation, bortle)

    val qualityColor = try {
        Color(android.graphics.Color.parseColor(evaluation.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    val rankColor = when (rankNumber) {
        1 -> Color(0xFFEAB308) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFD97706) // Bronze
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .clickable {
                when (target) {
                    is com.example.astronomy.CelestialTarget.PlanetTarget -> onPlanetClick(target.planet)
                    is com.example.astronomy.CelestialTarget.DeepSkyTarget -> onDsoClick(target.dso)
                    is com.example.astronomy.CelestialTarget.MoonTarget -> onMoonClick()
                    is com.example.astronomy.CelestialTarget.MeteorShowerTarget -> onMeteorShowerClick(target.shower)
                }
            },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rankNumber",
                style = MaterialTheme.typography.titleMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(22.dp)
            )

            when (target) {
                is com.example.astronomy.CelestialTarget.PlanetTarget -> {
                    Image(
                        painter = painterResource(id = target.planet.imageResId),
                        contentDescription = target.planet.portugueseName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                    )
                }
                is com.example.astronomy.CelestialTarget.DeepSkyTarget -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(android.graphics.Color.parseColor(target.dso.type.tagColorHex)).copy(alpha = 0.2f))
                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = target.dso.type.symbol, fontSize = 22.sp)
                    }
                }
                is com.example.astronomy.CelestialTarget.MoonTarget -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌙", fontSize = 22.sp)
                    }
                }
                is com.example.astronomy.CelestialTarget.MeteorShowerTarget -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌠", fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = target.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Observation method visual badge before opening details
                Text(
                    text = observationMethods,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFEF08A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🕐 ${evaluation.bestWindowStr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(qualityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = evaluation.displayStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = qualityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (evaluation.solarProximityInfo?.showAlert == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "☀️ ${evaluation.solarProximityInfo.angularSeparationDeg.toInt()}° Sol",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${evaluation.directionLabel} • ${evaluation.heightLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TelescopePlanetRow(
    evaluation: com.example.astronomy.TelescopeObservationEvaluation,
    onClick: () -> Unit
) {
    val planet = evaluation.planet
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(evaluation.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = planet.imageResId),
                contentDescription = planet.portugueseName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = planet.portugueseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = evaluation.opticalSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(qualityColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = evaluation.displayStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = qualityColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeaturedPlanetCard(
    observation: PlanetObservation,
    onViewPlanet: () -> Unit
) {
    val planet = observation.planet
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(observation.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = planet.imageResId),
                contentDescription = planet.portugueseName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                alpha = 0.35f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = planet.portugueseName.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "★".repeat(observation.quality.stars),
                                color = Color(0xFFFACC15),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(qualityColor.copy(alpha = 0.2f))
                                    .border(1.dp, qualityColor.copy(alpha = 0.4f), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = observation.displayStatus,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = qualityColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (observation.solarProximityInfo?.showAlert == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "☀️ ${observation.solarProximityInfo.angularSeparationDeg.toInt()}° Sol",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = planet.symbol,
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧭", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = observation.directionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⬆️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = observation.heightLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onViewPlanet,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPurplePrimary,
                        contentColor = CosmicPurpleOnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Ver ${planet.portugueseName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RankingPlanetRow(
    rankNumber: Int,
    observation: PlanetObservation,
    onClick: () -> Unit
) {
    val planet = observation.planet
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(observation.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    val rankColor = when (rankNumber) {
        1 -> Color(0xFFEAB308) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFD97706) // Bronze
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rankNumber",
                style = MaterialTheme.typography.titleMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(22.dp)
            )

            Image(
                painter = painterResource(id = planet.imageResId),
                contentDescription = planet.portugueseName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planet.portugueseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = observation.displayStatus.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = qualityColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                    if (observation.solarProximityInfo?.showAlert == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "☀️ ${observation.solarProximityInfo.angularSeparationDeg.toInt()}° Sol",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "🌅 ${observation.riseTimeStr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = observation.directionLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BinocularEquipmentCard(
    apertureMm: Double,
    magnification: Double,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔭",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Binóculo ${apertureMm.toInt()} × ${magnification.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Objetiva: ${apertureMm.toInt()} mm • Aumento: ${magnification.toInt()}×",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CosmicPurplePrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Alterar ⚙️",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BinocularEquipmentModal(
    currentApertureMm: Double,
    currentMagnification: Double,
    onSave: (apertureMm: Double, magnification: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var apertureText by remember { mutableStateOf(currentApertureMm.toInt().toString()) }
    var magText by remember { mutableStateOf(currentMagnification.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SpaceCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⚙️ Configurar Binóculo",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = apertureText,
                    onValueChange = { apertureText = it },
                    label = { Text("Abertura da Objetiva (mm)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = magText,
                    onValueChange = { magText = it },
                    label = { Text("Aumento / Amplificação (x)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancelar", color = TextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val ap = apertureText.toDoubleOrNull() ?: currentApertureMm
                            val mag = magText.toDoubleOrNull() ?: currentMagnification
                            onSave(ap, mag)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurplePrimary)
                    ) {
                        Text("Salvar", color = CosmicPurpleOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TelescopeEquipmentModal(
    currentEquipment: TelescopeEquipment,
    onSave: (apertureMm: Double, focalLengthMm: Double, eyepieceMm: Double, fovDeg: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var apertureText by remember { mutableStateOf(currentEquipment.apertureMm.toInt().toString()) }
    var focalLengthText by remember { mutableStateOf(currentEquipment.focalLengthMm.toInt().toString()) }
    var eyepieceText by remember { mutableStateOf(currentEquipment.eyepieceFocalLengthMm.toInt().toString()) }
    var fovText by remember { mutableStateOf(currentEquipment.eyepieceApparentFovDeg?.toInt()?.toString() ?: "52") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SpaceCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⚙️ Configurar Telescópio",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = apertureText,
                    onValueChange = { apertureText = it },
                    label = { Text("Abertura (mm)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = focalLengthText,
                    onValueChange = { focalLengthText = it },
                    label = { Text("Distância Focal do Telescópio (mm)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = eyepieceText,
                    onValueChange = { eyepieceText = it },
                    label = { Text("Focal da Ocular (mm)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fovText,
                    onValueChange = { fovText = it },
                    label = { Text("Campo Aparente da Ocular (°)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = SpaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancelar", color = TextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val ap = apertureText.toDoubleOrNull() ?: currentEquipment.apertureMm
                            val fl = focalLengthText.toDoubleOrNull() ?: currentEquipment.focalLengthMm
                            val ep = eyepieceText.toDoubleOrNull() ?: currentEquipment.eyepieceFocalLengthMm
                            val fov = fovText.toDoubleOrNull() ?: currentEquipment.eyepieceApparentFovDeg
                            onSave(ap, fl, ep, fov)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurplePrimary)
                    ) {
                        Text("Salvar", color = CosmicPurpleOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun computeObservationMethods(
    evaluation: com.example.astronomy.TelescopeTargetEvaluation,
    bortle: com.example.astronomy.BortleScale
): String {
    val target = evaluation.target
    val isNakedEye: Boolean
    val isBinocular: Boolean
    val isTelescope: Boolean

    val altitudeDeg = evaluation.altitudeDeg
    val sunAltitudeDeg = evaluation.sunAltitudeDeg
    val isAbove = evaluation.isAboveHorizon

    when (target) {
        is com.example.astronomy.CelestialTarget.MeteorShowerTarget -> {
            return "👁️ A olho nu"
        }
        is com.example.astronomy.CelestialTarget.MoonTarget -> {
            isNakedEye = isAbove && altitudeDeg >= 3.0
            isBinocular = isAbove && altitudeDeg >= 3.0
            isTelescope = isAbove && altitudeDeg >= 3.0
        }
        is com.example.astronomy.CelestialTarget.PlanetTarget -> {
            val planet = target.planet
            val isNakedEyePlanet = com.example.astronomy.Planet.nakedEyePlanets.contains(planet)

            // Naked eye check for planets
            isNakedEye = isNakedEyePlanet && isAbove && altitudeDeg >= 5.0 && sunAltitudeDeg <= -2.0

            // Binocular check for planets
            isBinocular = isAbove && altitudeDeg >= 3.0 && sunAltitudeDeg <= -2.0

            // Telescope check for planets
            isTelescope = isAbove && altitudeDeg >= 0.0 && sunAltitudeDeg <= -2.0
        }
        is com.example.astronomy.CelestialTarget.DeepSkyTarget -> {
            val dso = target.dso
            val mag = dso.apparentMagnitude
            val penalty = evaluation.lunarInfo?.penaltyScore ?: 0.0

            // Naked eye limiting magnitude per Bortle scale
            val nakedEyeLimitingMag = when (bortle.level) {
                1, 2, 3 -> 6.0
                4, 5 -> 5.2
                6, 7 -> 4.2
                else -> 3.5 // Bortle 8-9
            }

            val isBright = mag <= 4.5
            val isVisType = dso.type == com.example.astronomy.DeepSkyType.OPEN_CLUSTER ||
                            dso.type == com.example.astronomy.DeepSkyType.GLOBULAR_CLUSTER ||
                            dso.type == com.example.astronomy.DeepSkyType.NEBULA ||
                            dso.type == com.example.astronomy.DeepSkyType.GALAXY ||
                            dso.type == com.example.astronomy.DeepSkyType.DOUBLE_STAR
            isNakedEye = isAbove && altitudeDeg >= 8.0 && sunAltitudeDeg <= -6.0 &&
                        (mag <= nakedEyeLimitingMag) && (penalty < 18.0) && (isBright || isVisType)

            val binocularLimitingMag = when (bortle.level) {
                1, 2, 3 -> 9.5
                4, 5 -> 8.8
                6, 7 -> 8.0
                else -> 7.2
            }

            isBinocular = isAbove && altitudeDeg >= 5.0 && sunAltitudeDeg <= -6.0 && (mag <= binocularLimitingMag || mag <= 8.5)

            isTelescope = isAbove && altitudeDeg >= 0.0 && sunAltitudeDeg <= -2.0
        }
    }

    val methods = mutableListOf<String>()
    if (isNakedEye) methods.add("👁️ A olho nu")
    if (isBinocular) methods.add("🔭 Binóculo")
    if (isTelescope) methods.add("🔭 Telescópio")

    return if (methods.isNotEmpty()) {
        methods.joinToString(" • ")
    } else if (isAbove) {
        "🔭 Telescópio"
    } else {
        "Abaixo do horizonte"
    }
}
