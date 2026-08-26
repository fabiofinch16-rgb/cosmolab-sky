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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.MeteorShower
import com.example.astronomy.MeteorShowerEngine
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
fun MeteorShowerDetailScreen(
    shower: MeteorShower,
    viewModel: CosmoLabViewModel,
    onBack: () -> Unit
) {
    val calendar = viewModel.selectedCalendar
    val city = viewModel.selectedCity
    val bortle = viewModel.bortleScale
    val context = LocalContext.current

    val evaluation = remember(shower, calendar.timeInMillis, city, bortle) {
        MeteorShowerEngine.evaluateShower(
            shower = shower,
            calendar = calendar,
            latitude = city.latitude,
            longitude = city.longitude,
            bortle = bortle
        )
    }

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

    val quality = evaluation?.quality ?: ObservationQuality.UNAVAILABLE
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SpaceCardSurface)
                    .border(1.dp, SpaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "COSMOLAB SKY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "CHUVA DE METEOROS",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2E1065).copy(alpha = 0.5f),
                                    SpaceCardSurface
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🌠 FENÔMENO DE CAMPO AMPLO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC4B5FD),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(qualityColor.copy(alpha = 0.2f))
                                    .border(1.dp, qualityColor.copy(alpha = 0.5f), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = evaluation?.displayStatus ?: quality.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = qualityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = shower.portugueseName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "${shower.internationalName} • Radiante em ${shower.radiantConstellation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Naked Eye Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF08A).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "👁️ A olho nu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFEF08A),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Peak Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "⭐ Pico: ${shower.peakDateStr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Observation Date & Location Context Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CosmicPurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = city.displayName.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "🌌 Bortle ${bortle.level}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SpaceBackground)
                                    .border(1.dp, SpaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { showDatePicker() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dateFormat.format(calendar.time),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SpaceBackground)
                                    .border(1.dp, SpaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { showTimePicker() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeFormat.format(calendar.time),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetToCurrentTime() },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicPurplePrimary)
                        ) {
                            Text("Agora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Observability & Rate Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = CosmicPurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OPORTUNIDADE DE OBSERVAÇÃO",
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = evaluation?.skyConditionLabel ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = evaluation?.qualityMessage ?: "Informações astronômicas calculadas dinamicamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Best Window Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpaceBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "JANELA CALCULADA",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🕒 ${evaluation?.bestWindowStr ?: "--:--"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Expected Rate Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpaceBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "TAXA ESTIMADA NO LOCAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "~${evaluation?.effectiveExpectedZhr ?: shower.peakZhr} meteoros/h",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Window Detailed Times Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SpaceBackground)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Início da Janela: ${evaluation?.windowStartTimeStr ?: "--:--"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Término da Janela: ${evaluation?.windowEndTimeStr ?: "--:--"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = evaluation?.bestPeriodDescription ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Radiant Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpaceBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "RADIANTE AGORA",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${evaluation?.directionLabel} (${String.format(Locale.US, "%.0f°", evaluation?.azimuthDeg ?: 0.0)}) • ${String.format(Locale.US, "%.1f°", evaluation?.altitudeDeg ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpaceBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "ALTURA MÁXIMA NOTURNA",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f°", evaluation?.maxRadiantAltitudeDeg ?: 0.0)} no céu",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Moon & Sky Light Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LUA & POLUIÇÃO LUMINOSA",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌙 Fase da Lua:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${evaluation?.moonPhaseName} (${evaluation?.moonIlluminationPercent}% iluminada)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = evaluation?.moonInterferenceDescription ?: "Avaliação da interferência lunar no campo de visão.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌌 Escala de Bortle:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Classe ${bortle.level} (${bortle.displayLabel})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = evaluation?.bortleImpactDescription ?: "Impacto da poluição luminosa local.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Practical Observation Tips Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFEF08A).copy(alpha = 0.05f))
                    .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E).copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DICAS PARA UMA BOA OBSERVAÇÃO",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFEF08A),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💡 Procure uma área ampla e aberta do céu e evite olhar diretamente para o radiante (${shower.radiantConstellation}). Os meteoros mais longos e espetaculares surgem a cerca de 30° a 60° de distância do radiante!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "👁️ Permita que seus olhos se adaptem à escuridão por pelo menos 15 a 20 minutos. Evite telas de celular e lanternas brilhantes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🚫 Não use binóculos nem telescópios: chuvas de meteoros exigem campo visual de 180° a olho nu para capturar os traços que cruzam o céu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Astronomical Specifications Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CosmicPurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DADOS ASTRONÔMICOS DA CHUVA",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ShowerSpecRow("Período de Atividade", shower.activityPeriodStr)
                    ShowerSpecRow("Pico Anual", shower.peakDateStr)
                    ShowerSpecRow("ZHR Máximo Teórico", "${shower.peakZhr} meteoros/hora")
                    ShowerSpecRow("Velocidade de Entrada", "${shower.velocityKmS} km/s (${shower.velocityDescription})")
                    ShowerSpecRow("Corpo Progenitor", shower.parentBody)
                    ShowerSpecRow("Constelação do Radiante", shower.radiantConstellation)
                    ShowerSpecRow("Ascensão Reta (AR)", shower.raHoursStr)
                    ShowerSpecRow("Declinação (Dec)", shower.decDegStr)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scientific Description Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTÓRIA & CARACTERÍSTICAS",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = shower.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ShowerSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
