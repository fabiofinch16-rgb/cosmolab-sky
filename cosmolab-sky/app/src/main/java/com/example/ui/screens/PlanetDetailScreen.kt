package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.Planet
import com.example.ui.components.SkyVaultComposable
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.imageResId
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PlanetDetailScreen(
    planet: Planet,
    viewModel: CosmoLabViewModel,
    onBack: () -> Unit,
    onOpenBestTimeFinder: () -> Unit
) {
    val obs = viewModel.observationsMap[planet] ?: return
    val city = viewModel.selectedCity
    val calendar = viewModel.selectedCalendar
    val context = LocalContext.current
    var showCompassModal by remember { mutableStateOf(false) }

    val dateFormat = remember(calendar.timeZone, calendar.timeInMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
            timeZone = calendar.timeZone
        }
    }
    val timeFormat = remember(calendar.timeZone, calendar.timeInMillis) {
        SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply {
            timeZone = calendar.timeZone
        }
    }

    val showDatePicker = {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, y, m, d ->
            viewModel.setDate(y, m, d)
        }, year, month, day).show()
    }

    val showTimePicker = {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, h, m ->
            viewModel.setTime(h, m)
        }, hour, minute, true).show()
    }

    var showTechDetails by remember { mutableStateOf(false) }

    val teleEval = viewModel.telescopeEvaluations[planet]
    val currentQuality = teleEval?.quality ?: obs.quality
    val currentDisplayStatus = teleEval?.displayStatus ?: obs.displayStatus

    val qualityColor = try {
        Color(android.graphics.Color.parseColor(currentQuality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    val nowMillis = System.currentTimeMillis()
    val deltaMillis = calendar.timeInMillis - nowMillis
    val diffMinutes = if (kotlin.math.abs(deltaMillis) < 3000) 0.0 else deltaMillis / (60.0 * 1000.0)
    val sliderPosition = (0.5f + (diffMinutes.toFloat() / 720.0f) * 0.5f).coerceIn(0.0f, 1.0f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${planet.symbol} ${planet.portugueseName.uppercase()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 ${dateFormat.format(calendar.time)} ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        modifier = Modifier.clickable { showDatePicker() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🕐 ${timeFormat.format(calendar.time)} ✏️",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        modifier = Modifier.clickable { showTimePicker() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${city.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(qualityColor.copy(alpha = 0.2f))
                    .border(1.dp, qualityColor.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentDisplayStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = qualityColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Planet Hero Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = planet.imageResId),
                        contentDescription = planet.portugueseName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        alpha = 0.5f
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = planet.portugueseName.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = planet.symbol,
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = planet.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Visual Sky Representation (3D Celestial Sphere)
            com.example.ui.components.CelestialSphere3DComposable(
                viewModel = viewModel,
                highlightTargetId = planet.id,
                modifier = Modifier.fillMaxWidth()
            )

            // ⚠️ Solar Proximity Alert Card (if Sun is above horizon and separation < 60°)
            val solarInfo = obs.solarProximityInfo
            if (solarInfo != null && solarInfo.showAlert) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261908))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "☀️", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = solarInfo.warningTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Separação angular do Sol: ${String.format(Locale.US, "%.1f", solarInfo.angularSeparationDeg)}° (segurança: ≥ 60°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }

                        Text(
                            text = solarInfo.warningMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFEF3C7),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Friendly Direction & Height Info Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🧭 DIREÇÃO", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = obs.directionLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Azimute: ${String.format(Locale.US, "%.1f", obs.azimuthDeg)}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFEF08A),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⬆️ ALTURA NO CÉU", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = obs.heightLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Altitude: ${String.format(Locale.US, "%.1f", obs.altitudeDeg)}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCompassModal = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A1F45),
                            contentColor = Color(0xFFFDE047)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF7E22CE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🧭 LOCALIZAR DIREÇÃO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // ⭐ Automatic Best Time Recommendation Card
            val autoWindows = remember(planet, calendar.timeInMillis, city.latitude, city.longitude, viewModel.darkSkyOnlyChoice) {
                com.example.astronomy.AstronomyEngine.findBestObservationWindows(
                    planet = planet,
                    calendar = calendar,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    minDesiredHeightLabel = "Baixo",
                    darkSkyOnly = viewModel.darkSkyOnlyChoice
                )
            }
            val autoBestTimeStr = if (autoWindows.isNotEmpty()) {
                "${autoWindows.first().startTimeStr} → ${autoWindows.first().endTimeStr}"
            } else {
                "Sem janela favorável hoje"
            }
            val autoBestTimeExp = if (autoWindows.isNotEmpty()) {
                if (viewModel.darkSkyOnlyChoice) "Melhor período para observar com o céu escuro." else "Melhor período de visualização hoje."
            } else if (viewModel.darkSkyOnlyChoice) {
                "Objeto não atinge altura ideal com o céu escuro nesta data."
            } else {
                "Objeto permanece muito próximo ou abaixo do horizonte."
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⭐ MELHOR HORÁRIO PARA OBSERVAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = autoBestTimeStr,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = autoBestTimeExp,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Interactive Time Scrubber Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🕐 CONTROLE DE HORÁRIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFormat.format(calendar.time),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = timeFormat.format(calendar.time),
                            style = MaterialTheme.typography.titleLarge,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-12h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("← PASSADO | AGORA | FUTURO →", style = MaterialTheme.typography.labelSmall, color = CosmicPurplePrimary.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text("+12h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Slider(
                        value = sliderPosition,
                        onValueChange = { newPos ->
                            val targetDiffMinutes = (newPos - 0.5f) * 1440.0f
                            viewModel.setTimeOffsetFromNowMinutes(targetDiffMinutes.toDouble())
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CosmicPurplePrimary,
                            activeTrackColor = CosmicPurplePrimary,
                            inactiveTrackColor = Color(0xFF2D2A36)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-12h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("-6h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("Agora", style = MaterialTheme.typography.labelSmall, color = CosmicPurplePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("+6h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("+12h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val canGoBack = diffMinutes > -719.0
                        val canGoForward = diffMinutes < 719.0

                        TimeAdjustButton(text = "-1h", enabled = canGoBack) { viewModel.updateTimeOffset(-60) }
                        TimeAdjustButton(text = "-15m", enabled = canGoBack) { viewModel.updateTimeOffset(-15) }
                        TimeAdjustButton(text = "Agora", enabled = true) { viewModel.resetToCurrentTime() }
                        TimeAdjustButton(text = "+15m", enabled = canGoForward) { viewModel.updateTimeOffset(15) }
                        TimeAdjustButton(text = "+1h", enabled = canGoForward) { viewModel.updateTimeOffset(60) }
                    }
                }
            }

            // Telescopic Observation & Optical Suitability Card
            val teleEval = viewModel.telescopeEvaluations[planet]
            if (teleEval != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val obsModeTitle = when (viewModel.observationMode) {
                                com.example.astronomy.ObservationMode.NAKED_EYE -> "👁️ OBSERVAÇÃO A OLHO NU"
                                com.example.astronomy.ObservationMode.BINOCULAR -> "🔭 OBSERVAÇÃO COM BINÓCULO"
                                com.example.astronomy.ObservationMode.TELESCOPE -> "🔭 OBSERVAÇÃO COM TELESCÓPIO"
                            }
                            Text(
                                text = obsModeTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = teleEval.displayStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = try { Color(android.graphics.Color.parseColor(teleEval.quality.colorHex)) } catch (e: Exception) { CosmicPurplePrimary },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = teleEval.detectionCapability,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = teleEval.qualityMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        val breakdown = teleEval.qualityBreakdown
                        if (breakdown != null && breakdown.limitations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2A1B1B))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "⚠️ FATORES LIMITANTES DA OBSERVAÇÃO:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                breakdown.limitations.forEach { lim ->
                                    Text(
                                        text = "• $lim",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            when (viewModel.observationMode) {
                                com.example.astronomy.ObservationMode.NAKED_EYE -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Visão", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "A Olho Nu",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Brilho Aparente", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${String.format("%.1f", obs.magnitude)} mag",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Condição do Céu", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "Classe ${viewModel.bortleScale.level}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                com.example.astronomy.ObservationMode.BINOCULAR -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Aumento", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${viewModel.binocularMagnification.toInt()}×",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Objetiva", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${viewModel.binocularApertureMm.toInt()} mm",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Pupila de Saída", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${String.format("%.1f", teleEval.exitPupilMm)} mm",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                com.example.astronomy.ObservationMode.TELESCOPE -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Aumento", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${String.format("%.0f", teleEval.magnification)}×",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Pupila de Saída", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${String.format("%.2f", teleEval.exitPupilMm)} mm",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Diâmetro Aparente", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "${String.format("%.1f", teleEval.planetAngularDiameterArcsec)}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ✨ VOCÊ SABIA? (Curiosity Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ VOCÊ SABIA?",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = com.example.astronomy.CuriosityCatalog.getCuriosityForPlanet(planet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SpaceCardSurface)
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌅 NASCE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = obs.riseTimeStr,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔭 CULMINA", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = obs.transitTimeStr,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌄 SE PÕE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = obs.setTimeStr,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Primary Actions: Encontrar melhor horário & Detalhes astronômicos
            Button(
                onClick = onOpenBestTimeFinder,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicPurplePrimary,
                    contentColor = CosmicPurpleOnPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔭 Encontrar melhor horário",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 🔭 HOW TO POINT SECTION
            com.example.ui.components.HowToPointSection(
                targetName = planet.portugueseName,
                altitudeDeg = obs.altitudeDeg,
                azimuthDeg = obs.azimuthDeg,
                directionLabel = obs.directionLabel,
                heightLabel = obs.heightLabel,
                isAboveHorizon = obs.isAboveHorizon,
                rightAscensionDeg = obs.rightAscensionDeg,
                declinationDeg = obs.declinationDeg,
                isNakedEyeVisible = planet in com.example.astronomy.Planet.nakedEyePlanets,
                currentMountType = viewModel.telescopeEquipment.mountType,
                onMountTypeSelected = { mount -> viewModel.updateMountType(mount) },
                isCelestialPoleVisible = viewModel.showCelestialPoleMarker,
                onToggleCelestialPoleMarker = { viewModel.toggleCelestialPoleMarker() },
                latitude = viewModel.selectedCity.latitude,
                longitude = viewModel.selectedCity.longitude,
                cityName = viewModel.selectedCity.displayName,
                calendar = viewModel.selectedCalendar,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showTechDetails = !showTechDetails },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📐 Detalhes astronômicos",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (showTechDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            // Collapsible Technical Details Section
            AnimatedVisibility(
                visible = showTechDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DADOS TÉCNICOS DE ${planet.portugueseName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        TechDetailRow("Altitude", String.format("%.1f°", obs.altitudeDeg))
                        TechDetailRow("Azimute", String.format("%.1f°", obs.azimuthDeg))
                        TechDetailRow("Magnitude aparente", String.format("%.2f", obs.magnitude))
                        TechDetailRow("Distância da Terra", String.format("%.2f UA", obs.distanceAU))
                        TechDetailRow("Condição do Céu", "${obs.skyCondition.icon} ${obs.skyCondition.label}")
                        TechDetailRow("Altura do Sol", String.format(Locale.US, "%.1f°", obs.sunAltitudeDeg))
                        TechDetailRow("Céu Escuro", if (obs.isSkyDark) "Sim (Noite)" else "Não (${obs.skyCondition.label})")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCompassModal) {
        com.example.ui.components.CompassDirectionModal(
            targetName = planet.portugueseName,
            targetAzimuthGeo = obs.azimuthDeg,
            targetAltitudeDeg = obs.altitudeDeg,
            directionLabel = obs.directionLabel,
            latitude = city.latitude,
            longitude = city.longitude,
            calendar = viewModel.selectedCalendar,
            onDismiss = { showCompassModal = false }
        )
    }
}

@Composable
fun TimeAdjustButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Color(0xFF25232A) else Color(0xFF18171E))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) TextPrimary else TextMuted,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TechDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}
