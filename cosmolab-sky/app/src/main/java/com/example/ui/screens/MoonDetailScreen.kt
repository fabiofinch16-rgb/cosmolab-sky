package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.astronomy.AstronomyEngine
import com.example.astronomy.CelestialTarget
import com.example.astronomy.MoonObservationWindow
import com.example.astronomy.ObservationQuality
import com.example.ui.components.CelestialSphere3DComposable
import com.example.ui.components.CompassDirectionModal
import com.example.ui.components.HowToPointSection
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleOnPrimary
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
fun MoonDetailScreen(
    viewModel: CosmoLabViewModel,
    onBack: () -> Unit
) {
    val calendar = viewModel.selectedCalendar
    val city = viewModel.selectedCity
    val context = LocalContext.current
    var showCompassModal by remember { mutableStateOf(false) }
    var showBestTimeDialog by remember { mutableStateOf(false) }
    var showTechDetails by remember { mutableStateOf(false) }

    // Real-time Moon observation calculated strictly using AstronomyEngine
    val obs = remember(calendar.timeInMillis, city.latitude, city.longitude) {
        AstronomyEngine.analyzeMoonObservation(
            calendar = calendar,
            latitude = city.latitude,
            longitude = city.longitude
        )
    }

    val nowCal = Calendar.getInstance(calendar.timeZone)
    val isToday = calendar.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

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

    val qualityColor = try {
        Color(android.graphics.Color.parseColor(obs.quality.colorHex))
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
        // 1. CABEÇALHO
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
                        text = "🌙 LUA",
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

            // Indicador de status no canto superior direito calculado para o momento atual
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(qualityColor.copy(alpha = 0.2f))
                    .border(1.dp, qualityColor.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = obs.displayStatus,
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
            // 2. HERO DA LUA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_moon_hero_1787076540797),
                        contentDescription = "Lua",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        alpha = 0.45f
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
                            Column {
                                Text(
                                    text = "LUA",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Satélite Natural da Terra",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CosmicPurplePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "🌙",
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "O único satélite natural da Terra. Revela impressionantes crateras, mares basálticos e relevo sob iluminação rasante ao longo do terminador.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1B4B))
                                    .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Fase: ${obs.phaseName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E1065))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${obs.illuminationPercent}% iluminada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE9D5FF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. MAQUETE 3D DO HORIZONTE
            CelestialSphere3DComposable(
                viewModel = viewModel,
                highlightTargetId = "moon",
                modifier = Modifier.fillMaxWidth()
            )

            // 8. ALERTA DE PROXIMIDADE DO SOL
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
                                    text = "⚠️ ATENÇÃO: PRÓXIMA AO SOL",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Separação angular do Sol: ${String.format(Locale.US, "%.1f", obs.angularSeparationSunDeg)}° (segurança: ≥ 60°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }

                        Text(
                            text = "A proximidade do Sol pode dificultar ou impedir a observação da Lua durante o dia. Nunca aponte binóculos ou telescópios para regiões próximas ao Sol sem proteção adequada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFEF3C7),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 7. POSIÇÃO NO CÉU (Direção & Altura)
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

            // 6. FASE DA LUA (Seção Específica)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
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
                        Text(
                            text = "🌙 FASE DA LUA",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = obs.phaseName,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Iluminação", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${obs.illuminationPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Idade Lunar", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", obs.moonAgeDays)} dias",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF93C5FD),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Separação do Sol", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", obs.angularSeparationSunDeg)}°",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = obs.lunarInfluenceMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // 9. MELHOR HORÁRIO PARA OBSERVAR
            val autoWindows = remember(calendar.timeInMillis, city.latitude, city.longitude, viewModel.darkSkyOnlyChoice) {
                AstronomyEngine.findBestObservationWindowsForMoon(
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
                if (isToday) "Sem janela favorável hoje" else "Sem janela favorável nesta data"
            }
            val autoBestTimeExp = if (autoWindows.isNotEmpty()) {
                if (isToday) {
                    if (viewModel.darkSkyOnlyChoice) "Melhor período para observar com o céu escuro hoje." else "Melhor período de observação hoje."
                } else {
                    if (viewModel.darkSkyOnlyChoice) "Melhor período para observar com o céu escuro na data." else "Melhores condições para observar na data selecionada."
                }
            } else if (viewModel.darkSkyOnlyChoice) {
                "Lua não atinge altura ideal com o céu escuro nesta data."
            } else {
                "Lua permanece muito baixa ou abaixo do horizonte."
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

            // 4. CONTROLE DE HORÁRIO
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

            // 5. NASCE / CULMINA / PÕE / VISIBILIDADE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            if (obs.riseDirectionLabel.isNotEmpty()) {
                                Text(
                                    text = "${obs.riseDirectionLabel} (${String.format(Locale.US, "%.0f", obs.riseAzimuthDeg)}°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
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
                            if (obs.transitAltitudeDeg > 0.0) {
                                Text(
                                    text = "Alt: ${String.format(Locale.US, "%.1f", obs.transitAltitudeDeg)}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp
                                )
                            }
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
                            if (obs.setDirectionLabel.isNotEmpty()) {
                                Text(
                                    text = "${obs.setDirectionLabel} (${String.format(Locale.US, "%.0f", obs.setAzimuthDeg)}°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (obs.visibilityDurationStr.isNotEmpty() && obs.visibilityDurationStr != "00h 00m") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1B2E))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◷ Duração visível acima do horizonte: ${obs.visibilityDurationStr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC4B5FD),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 10. COMO OBSERVAR / MÉTODOS ÓPTICOS
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
                    Text(
                        text = "🔭 INSTRUMENTOS & RECOMENDAÇÕES",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OpticalMethodCard(
                        icon = "👁️",
                        title = "Olho Nu",
                        description = "Mares lunares basálticos, contorno e progressão das fases, silhueta do 'Homem na Lua' e brilho da luz cinérea na Lua Nova/Crescente inicial."
                    )

                    OpticalMethodCard(
                        icon = "🔭",
                        title = "Binóculos (7×50 / 10×50)",
                        description = "Relevo tridimensional no terminador: grandes crateras (Tycho, Copérnico, Plato, Clavius), cadeias montanhosas (Montes Apenninus) e raios brilhantes."
                    )

                    OpticalMethodCard(
                        icon = "🔭",
                        title = "Telescópio (50× a 200×)",
                        description = "Riquíssima em detalhes geológicos: picos centrais, terraços de crateras, domos vulcânicos e rimas sinuosas. Filtros lunares amenizam o ofuscamento."
                    )
                }
            }

            // Botão Encontrar Melhor Horário
            Button(
                onClick = { showBestTimeDialog = true },
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

            // 10. COMO APONTAR SECTION (Altazimutal / Equatorial / Alinhamento Polar)
            HowToPointSection(
                targetName = "Lua",
                altitudeDeg = obs.altitudeDeg,
                azimuthDeg = obs.azimuthDeg,
                directionLabel = obs.directionLabel,
                heightLabel = obs.heightLabel,
                isAboveHorizon = obs.isAboveHorizon,
                rightAscensionDeg = obs.rightAscensionDeg,
                declinationDeg = obs.declinationDeg,
                isNakedEyeVisible = true,
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

            // Detalhes astronômicos expansíveis
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
                            text = "DADOS TÉCNICOS DA LUA",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        TechDetailRow("Altitude", String.format(Locale.US, "%.1f°", obs.altitudeDeg))
                        TechDetailRow("Azimute", String.format(Locale.US, "%.1f°", obs.azimuthDeg))
                        TechDetailRow("Ascensão Reta (RA)", String.format(Locale.US, "%.2f h (%.1f°)", obs.rightAscensionDeg / 15.0, obs.rightAscensionDeg))
                        TechDetailRow("Declinação (Dec)", String.format(Locale.US, "%.2f°", obs.declinationDeg))
                        TechDetailRow("Fase Lunar", "${obs.phaseName} (${obs.illuminationPercent}%)")
                        TechDetailRow("Idade Lunar", String.format(Locale.US, "%.1f dias", obs.moonAgeDays))
                        TechDetailRow("Separação do Sol", String.format(Locale.US, "%.1f°", obs.angularSeparationSunDeg))
                        TechDetailRow("Condição do Céu", "${obs.skyCondition.icon} ${obs.skyCondition.label}")
                        TechDetailRow("Altura do Sol", String.format(Locale.US, "%.1f°", obs.sunAltitudeDeg))
                        TechDetailRow("Céu Escuro", if (obs.isSkyDark) "Sim (Noite escura)" else "Não (${obs.skyCondition.label})")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCompassModal) {
        CompassDirectionModal(
            targetName = "Lua",
            targetAzimuthGeo = obs.azimuthDeg,
            targetAltitudeDeg = obs.altitudeDeg,
            directionLabel = obs.directionLabel,
            latitude = city.latitude,
            longitude = city.longitude,
            calendar = viewModel.selectedCalendar,
            onDismiss = { showCompassModal = false }
        )
    }

    if (showBestTimeDialog) {
        MoonBestTimeFinderDialog(
            viewModel = viewModel,
            onDismiss = { showBestTimeDialog = false },
            onApplyWindowTime = { targetCal ->
                viewModel.setTime(targetCal.get(Calendar.HOUR_OF_DAY), targetCal.get(Calendar.MINUTE))
                showBestTimeDialog = false
            }
        )
    }
}

@Composable
private fun OpticalMethodCard(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161520))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun MoonBestTimeFinderDialog(
    viewModel: CosmoLabViewModel,
    onDismiss: () -> Unit,
    onApplyWindowTime: (Calendar) -> Unit
) {
    val windows = viewModel.findBestWindowsForMoon()
    val heightChoices = listOf(
        "Próximo do horizonte",
        "Baixo",
        "Bem posicionado",
        "Alto no céu",
        "O mais alto possível"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFF3D3846), RoundedCornerShape(28.dp)),
            color = SpaceCardSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔭 ENCONTRAR MELHOR HORÁRIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🌙 Lua",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextMuted
                        )
                    }
                }

                Text(
                    text = "Selecione os critérios desejados para a observação da Lua nesta data:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Filtro Céu Escuro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1C24))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            viewModel.darkSkyOnlyChoice = !viewModel.darkSkyOnlyChoice
                        }
                ) {
                    Checkbox(
                        checked = viewModel.darkSkyOnlyChoice,
                        onCheckedChange = { viewModel.darkSkyOnlyChoice = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CosmicPurplePrimary,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Apenas com céu escuro (sem luz solar)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Filtra apenas momentos com Sol bem abaixo do horizonte.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Altura mínima desejada
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Altura mínima desejada:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        heightChoices.take(3).forEach { choice ->
                            val isSelected = viewModel.minHeightChoice == choice
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CosmicPurplePrimary else Color(0xFF1E1C24))
                                    .clickable { viewModel.minHeightChoice = choice }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = choice.replace(" no céu", "").replace(" do horizonte", ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Resultados das Janelas
                Text(
                    text = "JANELAS ENCONTRADAS (${windows.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                if (windows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma janela atende aos filtros atuais para a Lua nesta data.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        windows.forEach { window ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1C24))
                                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                                    .clickable { onApplyWindowTime(window.startCal) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${window.startTimeStr} → ${window.endTimeStr}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${window.directionLabel} • Max Alt: ${String.format(Locale.US, "%.0f", window.maxAltitudeDeg)}° (${window.heightLabel})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = { onApplyWindowTime(window.startCal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurplePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Aplicar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
