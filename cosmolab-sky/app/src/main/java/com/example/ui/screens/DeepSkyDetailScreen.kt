package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.DeepSkyEngine
import com.example.astronomy.DeepSkyObject
import com.example.astronomy.ObservationQuality
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleContainer
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DeepSkyDetailScreen(
    dso: DeepSkyObject,
    viewModel: CosmoLabViewModel,
    onBack: () -> Unit
) {
    val evaluation = DeepSkyEngine.evaluateDso(
        dso = dso,
        calendar = viewModel.selectedCalendar,
        latitude = viewModel.selectedCity.latitude,
        longitude = viewModel.selectedCity.longitude,
        equipment = viewModel.telescopeEquipment,
        bortle = viewModel.bortleScale,
        mode = viewModel.observationMode,
        binocularApertureMm = viewModel.binocularApertureMm,
        binocularMagnification = viewModel.binocularMagnification
    )

    val equipment = viewModel.telescopeEquipment
    val bortle = viewModel.bortleScale
    val calendar = viewModel.selectedCalendar
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SpaceCardSurface)
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dso.commonName.ifEmpty { dso.messierNgc },
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
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
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicPurpleContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${dso.type.symbol} ${dso.type.portugueseName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
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
            // Hero Card (Icon / Type representation - NO real deep sky photos per instructions)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(dso.type.tagColorHex)).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = dso.type.symbol, fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dso.commonName.ifEmpty { dso.messierNgc },
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dso.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quality Badge
                    val qColor = try { Color(android.graphics.Color.parseColor(evaluation.quality.colorHex)) } catch (e: Exception) { CosmicPurplePrimary }
                    val qLabel = "${evaluation.quality.emoji} ${evaluation.displayStatus}"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(qColor.copy(alpha = 0.15f))
                            .border(1.dp, qColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        val qTarget = when (viewModel.observationMode) {
                            com.example.astronomy.ObservationMode.NAKED_EYE -> "a olho nu"
                            com.example.astronomy.ObservationMode.BINOCULAR -> "no binóculo"
                            com.example.astronomy.ObservationMode.TELESCOPE -> "no telescópio"
                        }
                        Text(
                            text = "$qLabel $qTarget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = qColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (evaluation.detectionCapability.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = evaluation.detectionCapability,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (evaluation.qualityMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = evaluation.qualityMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    val breakdown = evaluation.qualityBreakdown
                    if (breakdown != null && breakdown.limitations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nota: A observação astronômica visual depende também da adaptação dos olhos à escuridão, qualidade das lentes e transparência atmosférica do local.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Equipment & Context Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val cardHeader = when (viewModel.observationMode) {
                        com.example.astronomy.ObservationMode.NAKED_EYE -> "👁️ MODALIDADE & CONTEXTO DO CÉU"
                        com.example.astronomy.ObservationMode.BINOCULAR -> "🔭 SEU BINÓCULO & CONTEXTO"
                        com.example.astronomy.ObservationMode.TELESCOPE -> "🔭 SEU TELESCÓPIO & CONTEXTO"
                    }
                    Text(
                        text = cardHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            when (viewModel.observationMode) {
                                com.example.astronomy.ObservationMode.NAKED_EYE -> {
                                    Text(
                                        text = "Visão Direta (A Olho Nu)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sem instrumentos ópticos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                com.example.astronomy.ObservationMode.BINOCULAR -> {
                                    Text(
                                        text = "Binóculo ${viewModel.binocularApertureMm.toInt()} mm × ${viewModel.binocularMagnification.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Objetiva ${viewModel.binocularApertureMm.toInt()} mm • Aumento ${viewModel.binocularMagnification.toInt()}×",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                com.example.astronomy.ObservationMode.TELESCOPE -> {
                                    Text(
                                        text = "${equipment.apertureMm.toInt()} mm / ${equipment.focalLengthMm.toInt()} mm",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ocular ${equipment.eyepieceFocalLengthMm.toInt()} mm (${String.format("%.0f", equipment.magnification)}×)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "🌌 ${bortle.displayLabel}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bortle.sqmRange + " mag/arcsec²",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = evaluation.bortleImpactMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicPurplePrimary
                    )
                }
            }

            // ⭐ Automatic Best Time Card
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
                        text = evaluation.bestWindowStr.replace(" – ", " → "),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Melhor período para observar com o céu escuro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 🌐 Posição na Esfera Celeste 3D
            com.example.ui.components.CelestialSphere3DComposable(
                viewModel = viewModel,
                highlightTargetId = dso.id,
                modifier = Modifier.fillMaxWidth()
            )

            // ⚠️ Solar Proximity Alert Card
            val solarInfo = evaluation.solarProximityInfo
            if (solarInfo != null && solarInfo.showAlert) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
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
                                    text = "Separação angular do Sol: ${String.format(java.util.Locale.US, "%.1f", solarInfo.angularSeparationDeg)}° (segurança: ≥ 60°)",
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

            // 🌙 Lunar Interference Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🌙 INFLUÊNCIA DA LUA",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val lunar = evaluation.lunarInfo
                    if (lunar == null || !lunar.isMoonAboveHorizon) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Lua abaixo do horizonte",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Fase: ${lunar?.moonPhaseName ?: viewModel.moonObservation?.phaseName ?: "Sem interferência"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF4ADE80).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Interferência: Nenhuma",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lua: ${lunar.moonIlluminationPercent}% iluminada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Altitude: ${String.format("%.0f", lunar.moonAltitudeDeg)}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Direção: ${lunar.moonDirectionLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Distância do objeto: ${String.format("%.0f", lunar.angularSeparationDeg)}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicPurplePrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val (badgeColor, levelText) = when {
                                lunar.penaltyScore < 6.0 -> Pair(Color(0xFF4ADE80), "Interferência: Baixa")
                                lunar.penaltyScore < 12.0 -> Pair(Color(0xFF38BDF8), "Interferência: Moderada")
                                lunar.penaltyScore < 18.0 -> Pair(Color(0xFFFACC15), "Interferência: Notável")
                                else -> Pair(Color(0xFFFB923C), "Interferência: Forte")
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = levelText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val summaryMsg = when {
                            lunar.angularSeparationDeg >= 70.0 -> "Embora a Lua esteja ${lunar.moonIlluminationPercent}% iluminada, ela está a ${String.format("%.0f", lunar.angularSeparationDeg)}° deste objeto e em uma região diferente do céu. Portanto, a interferência lunar é baixa."
                            lunar.penaltyScore < 6.0 -> "Influência lunar baixa. O objeto pode ser observado com bom contraste."
                            lunar.penaltyScore < 12.0 -> "Interferência lunar moderada. Detalhes mais tênues do objeto podem ser levemente atenuados."
                            else -> "Lua ${lunar.moonIlluminationPercent}% iluminada e a apenas ${String.format("%.0f", lunar.angularSeparationDeg)}° do objeto. Forte interferência lunar. Recomendado procurar outro horário ou outro alvo."
                        }

                        Text(
                            text = summaryMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Position & Window Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🧭 POSIÇÃO NO CÉU E HORÁRIO",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Direção",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${evaluation.directionLabel} (${String.format("%.0f", evaluation.azimuthDeg)}°)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "Altura Atual",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${String.format("%.1f", evaluation.altitudeDeg)}° (${evaluation.heightLabel})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Melhor Janela Hoje",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = evaluation.bestWindowStr,
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Máx: ${String.format("%.0f", evaluation.maxAltitudeWindowDeg)}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ✨ VOCÊ SABIA? (Curiosity Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "✨ VOCÊ SABIA?",
                        style = MaterialTheme.typography.labelLarge,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = com.example.astronomy.CuriosityCatalog.getCuriosityForDso(dso),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            // Calculate naked eye visibility for DSO
            val nakedEyeLimitingMag = when (viewModel.bortleScale.level) {
                1, 2, 3 -> 6.0
                4, 5 -> 5.2
                6, 7 -> 4.2
                else -> 3.5
            }
            val isNakedEyeDso = evaluation.isAboveHorizon && evaluation.altitudeDeg >= 8.0 &&
                    evaluation.sunAltitudeDeg <= -6.0 && (dso.apparentMagnitude <= nakedEyeLimitingMag)

            // 🔭 HOW TO POINT SECTION
            com.example.ui.components.HowToPointSection(
                targetName = dso.commonName.ifEmpty { dso.messierNgc },
                altitudeDeg = evaluation.altitudeDeg,
                azimuthDeg = evaluation.azimuthDeg,
                directionLabel = evaluation.directionLabel,
                heightLabel = evaluation.heightLabel,
                isAboveHorizon = evaluation.isAboveHorizon,
                rightAscensionDeg = evaluation.rightAscensionDeg,
                declinationDeg = evaluation.declinationDeg,
                magnitude = dso.apparentMagnitude,
                constellation = dso.constellation,
                isNakedEyeVisible = isNakedEyeDso,
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

            // Technical Details Grid Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📊 DADOS TÉCNICOS DO CATÁLOGO",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItemRow("Designação", dso.messierNgc)
                    DetailItemRow("Tipo", dso.type.portugueseName)
                    DetailItemRow("Constelação", dso.constellation)
                    DetailItemRow("Magnitude Aparente", "${dso.apparentMagnitude} mag")
                    DetailItemRow("Tamanho Angular", "${dso.angularSizeArcmin}' min de arco")

                    dso.surfaceBrightness?.let {
                        DetailItemRow("Brilho Superficial", "$it mag/arcmin²")
                    }

                    dso.separationArcsec?.let {
                        DetailItemRow("Separação (Estrela Dupla)", "$it\" seg de arco")
                    }

                    dso.bestFilter?.let {
                        DetailItemRow("Filtro Recomendado", it)
                    }

                    DetailItemRow("Ascensão Reta (AR)", "${String.format("%.3f", dso.rightAscensionHours)}h")
                    DetailItemRow("Declinação (Dec)", "${String.format("%.2f", dso.declinationDeg)}°")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}
